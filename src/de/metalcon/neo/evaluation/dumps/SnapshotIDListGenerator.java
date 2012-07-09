package de.metalcon.neo.evaluation.dumps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import de.metalcon.neo.evaluation.utils.Configs;
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
public class SnapshotIDListGenerator {
	public SnapshotIDListGenerator() {
	}

	public void run() throws IOException {
		HashMap<String, String> allIDs = new HashMap<String, String>();

		BufferedReader in = H.openReadFile(Configs.get().WikiFullIDList);

		String strLine;
		String[] values;
		int lineCount = 0;
		while ((strLine = in.readLine()) != null) {
			values = strLine.split("\t");
			allIDs.put(values[0], values[1]);
			if (lineCount++ % 500000 == 0) {
				H.pln("Reading all IDs: " + lineCount + " done so far");
			}
		}
		in.close();

		
		for (long ts : Configs.get().StarSnapshotTimestamps) {
			HashMap<String, String> tsIDs = (HashMap<String, String>) allIDs
					.clone();

			in = H.openReadFile(Configs.get().CleanWikiSnapshotFriendPrefix
					+ ts);
			
			H.log("Creating NodeID list for "+ts);
			lineCount=0;
			BufferedWriter out = H
					.openWriteFile(Configs.get().SnapshotIDListPrefix + ts);
			H.pln(Configs.get().SnapshotIDListPrefix + ts);
			while ((strLine = in.readLine()) != null) {
				values = strLine.split("\t");
				if(values.length < 2){
					H.log("NodeID entry with less than 2 rows found!");
					continue;
				}

				
				String fromID = values[0];
				String toID = values[1];

				// if the entry still exists in the hash clone write to out and
				// delete the entry
				if (tsIDs.containsKey(fromID)) {
					out.write(fromID + "\t" + tsIDs.remove(fromID)+"\n");
				}
				if (tsIDs.containsKey(toID)) {
					out.write(toID + "\t" + tsIDs.remove(toID)+"\n");
				}
				
				if (lineCount++ % 500000 == 0) {
					H.pln("Creating NodeID "+ts+": " + lineCount + " read");
				}
			}
			out.close();
		}
	}
}
