/**
 * Reads the given snapshots files containing relation adds and updates and
 * fills a Neo database with the corresponding nodes.
 * 
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */

package de.metalcon.neo.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.TreeSet;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;

import de.metalcon.neo.evaluation.neo.NeoUtils;
import de.metalcon.neo.evaluation.neo.Relations;
import de.metalcon.neo.evaluation.utils.Configs;
import de.metalcon.neo.evaluation.utils.H;
import de.metalcon.neo.evaluation.utils.StopWatch;

public class BSUpdateEvaluator {

	public BSUpdateEvaluator() {
	}

	public void run() throws InterruptedException {
		for (long ts : Configs.get().StarSnapshotTimestamps) {
			if (Configs.get().BaseLineUpdateEvaluatorInsertUpdates) {
				AddSnapshotRunner addRunner = new AddSnapshotRunner(ts);
				addRunner.start();

				StopWatch watch = new StopWatch();
				while (addRunner.isAlive()) {
					Thread.sleep(2000);
					watch.updateRate(addRunner.addedRelations());
					H.log("Adding Content Items " + ts + " Added "
							+ addRunner.addedRelations()
							+ " relations so far: " + watch.getRate(10)
							+ "ps : " + watch.getTotalRate() + "ps total");

				}
				watch.updateRate(addRunner.addedRelations());
				H.strongLog("Adding Content Items " + ts + " finished adding "
						+ addRunner.addedRelations() + "relatoins within "
						+ watch.getSecondsPassed() + " seconds "
						+ watch.getTotalRate() + "ps");
			}
		}

		for (long ts : Configs.get().StarSnapshotTimestamps) {
			if (Configs.get().BaseLineUpdateEvaluatorEvaluate) {
				AbstractGraphDatabase db = NeoUtils
						.getAbstractGraphDatabase(Configs.get().StarDBDirPrefix
								+ ts);

				for (int i = 0; i < Configs.get().runs; i++) {
					GenerateNewsStream streamRunner = new GenerateNewsStream(
							ts, db);
					streamRunner.start();

					StopWatch watch = new StopWatch();
					StopWatch watchHops = new StopWatch();

					while (streamRunner.isAlive()) {
						Thread.sleep(2000);
						watch.updateRate(streamRunner.streamCnt);
						watchHops.updateRate(streamRunner.hops);
						H.log("BS-Evaluating (run " + i + ") " + ts
								+ " Created " + streamRunner.streamCnt
								+ " steams: " + watch.getRate(10) + "ps : "
								+ watch.getTotalRate() + "ps total; "
								+ watchHops.getRate(10) + "hops ps, "
								+ watchHops.getTotalRate() + "hops ps total");

					}
					watch.updateRate(streamRunner.streamCnt);
					watchHops.updateRate(streamRunner.hops);
					H.strongLog("BS-Evaluating (run " + i + ") " + ts
							+ " Finished " + streamRunner.streamCnt
							+ " steams: " + watch.getTotalRate() + "ps total; "
							+ watchHops.getTotalRate() + "hops ps total");
				}
				NeoUtils.ShutdownDB(db, Configs.get().StarDBDirPrefix + ts);
			}
		}
	}


/**
 * inserts wikipedia snapshots of various size to a graph db with standard star topology.
 * 
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */
	private class AddSnapshotRunner extends Thread {
		private int addedRelations = 0;
		private final long starSnapshotTimestamp;
		private final String dbPath;

		public AddSnapshotRunner(long starSnapshotTimestamp) {
			dbPath = Configs.get().StarDBDirPrefix + starSnapshotTimestamp;
			this.starSnapshotTimestamp = starSnapshotTimestamp;
		}

		public int addedRelations() {
			return addedRelations;
		}

		@Override
		public void run() {
			AddUpdates();
		}

		private void AddUpdates() {
			AbstractGraphDatabase db = NeoUtils
					.getAbstractGraphDatabase(dbPath);
			BufferedReader in = H
					.openReadFile(Configs.get().CleanWikiSnapshotUpdatePrefix
							+ starSnapshotTimestamp);
			String strLine;
			String[] values;

			try {
				Transaction tx = db.beginTx();
				try {
					while ((strLine = in.readLine()) != null) {
						if (addedRelations % 10000 == 0) {
							tx.success();
							tx.finish();
							tx = db.beginTx();
						}
						values = strLine.split("\t");
						if (values.length != 2) {
							continue;
						}

						long timestamp = Long.parseLong(values[0]);
						int nodeKey = Integer.valueOf(values[1]);

						try {
							Node ci = db.createNode();
							Node entity = db.getNodeById(nodeKey);

							entity.createRelationshipTo(ci, Relations.UPDATE);
							ci.setProperty("timestamp", timestamp);

						} catch (NotFoundException e) {
							continue;
						}

						addedRelations++;
					}
					tx.success();
				} finally {
					tx.finish();
				}
				NeoUtils.ShutdownDB(db, dbPath);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

/**
 * generates newsstreams for nodes using the standard star topology in a graph db for a social network
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */
	private class GenerateNewsStream extends Thread {
		private final AbstractGraphDatabase db;
		private final int k;
		private long streamCnt;
		private long hops;
		private final long timestamp;

		final String[] zeros = new String[] { "", "0", "00", "000", "0000",
				"00000", "000000" };

		public GenerateNewsStream(long timestamp, AbstractGraphDatabase db) {
			this.db = db;
			this.k = Configs.get().k;
			this.timestamp = timestamp;
		}

		@Override
		public void run() {
			LinkedList<Long> allIDs = new LinkedList<Long>();
			try {
				BufferedReader in = H
						.openReadFile(Configs.get().SnapshotIDListPrefix
								+ timestamp);
				String strLine;
				String[] values;
				int lineCount = 0;
				while ((strLine = in.readLine()) != null) {
					values = strLine.split("\t");
					allIDs.add(Long.valueOf(values[0]));
					if (lineCount++ % 50000 == 0) {
						H.pln("Reading all IDs: " + lineCount + " done so far");
					}
				}

				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (long enitiyID : allIDs) {
				Node entity;
				try {
					entity = db.getNodeById(enitiyID);
				} catch (NotFoundException e) {
					continue;
				}
				TreeSet<String> stream = new TreeSet<String>();
				for (Relationship followRel : entity.getRelationships(
						Direction.OUTGOING, Relations.FOLLOWS)) {
					Node friend = followRel.getEndNode();
					long friendID = friend.getId();

					for (Relationship updateRel : friend.getRelationships(
							Direction.OUTGOING, Relations.UPDATE)) {

						Node ci = updateRel.getEndNode();

						long timestamp = (Long) ci.getProperty("timestamp");

						String streamItem = generateStreamString(timestamp,
								friendID);
						stream.add(streamItem);
						if (stream.size() > k) {
							stream.pollFirst();
						}
						hops++;
					}
				}
				String fullStream = "";
				for (String s : stream) {
					fullStream += "\n" + s;
				}

				streamCnt++;
			}
		}

		private String generateStreamString(long timestamp, long friendID) {
			String tsString;
			if (timestamp < 100000000) {
				tsString = "0" + timestamp;
			} else {
				tsString = "" + timestamp;
			}

			String IDString = friendID + "";
			IDString = zeros[7 - IDString.length()] + IDString;

			return tsString + IDString;
		}
	}

}
