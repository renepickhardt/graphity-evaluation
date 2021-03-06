package de.metalcon.neo.evaluation.dumps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import de.metalcon.neo.evaluation.utils.H;

/**
 * SnapshotGenerator takes a "social network dump" extracted from wikipedia and
 * generates two files: One with all relations existing until a specified date
 * and one with all updates until that date
 * 
 * the result is written to filename+".sn"+dateSeconds
 * 
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */
public class SnapshotGenerator {
	private final String fileName;
	private final long[] timestamps;
	private final String CleanWikiSnapshotUpdatePrefix;
	private final String CleanWikiSnapshotFriendPrefix;


	
	public SnapshotGenerator(String cleanWikiTransactionsFile,
			long[] timestamps, String CleanWikiSnapshotUpdatePrefix,
			String CleanWikiSnapshotFriendPrefix) {
		this.fileName = cleanWikiTransactionsFile;
		this.timestamps = timestamps;
		this.CleanWikiSnapshotUpdatePrefix = CleanWikiSnapshotUpdatePrefix;
		this.CleanWikiSnapshotFriendPrefix = CleanWikiSnapshotFriendPrefix;

	}

	public void run() {
		// generateUpdatesTill(snapshots);

		generateStar();

		// generateTransactionsFrom(snapshots.get(0), 2000000);
	}

	private void generateTransactionsFrom(Long long1, int i) {
		// TODO Auto-generated method stub

	}

	class StarGenerator extends Thread {
		public long lineCount = 0;

		public long timestamp;

		public BufferedWriter updates;

		public StarGenerator(long timestamp) {
			this.timestamp = timestamp;
			updates = H
					.openWriteFile(CleanWikiSnapshotUpdatePrefix + timestamp);
		}

		public void run() {
			BufferedWriter bw = H.openWriteFile(CleanWikiSnapshotFriendPrefix
					+ timestamp);

			HashSet<String> edges = new HashSet<String>();

			BufferedReader in = H.openReadFile(fileName);
			String strLine;
			int cnt = 0;
			String[] values;

			try {
				while ((strLine = in.readLine()) != null) {
					values = strLine.split("\t");
					lineCount++;
					if (values[1].equals("U")) {
						updates.write(values[0] + "\t" + values[2] + "\n");
						continue;
					}
					if (Long.parseLong(values[0]) > timestamp)
						break;

					String edge = values[2] + "\t" + values[3];
					if (values[1].equals("A")) {
						if (edges.contains(edge))
							continue;
						edges.add(edge);
					} else if (values[1].equals("R")) {
						if (edges.contains(edge)) {
							edges.remove(edge);
						} else
							continue;
					}
				}

				for (String edge : edges) {
					bw.write(edge + "\n");
				}

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			H.log("lines processed: " + lineCount + " lines corrupt: "
					+ (lineCount - cnt));
		}
	}

	private void generateStar() {
		ArrayList<StarGenerator> threads = new ArrayList<StarGenerator>();
		for (long ts : timestamps) {
			StarGenerator generator = new StarGenerator(ts);
			generator.start();
			threads.add(generator);
		}

		try {
			boolean isRunning = true;
			while (isRunning) {
				isRunning = false;
				Thread.sleep(2000);
				H.log("next round:");
				for (StarGenerator generator : threads) {
					if (generator.isAlive()) {
						H.log(generator.timestamp + " is alive and processed "
								+ generator.lineCount + " lines");
						generator.updates.flush();
						isRunning = true;
					} else {
						H.log(generator.timestamp + " is done!");
					}
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
