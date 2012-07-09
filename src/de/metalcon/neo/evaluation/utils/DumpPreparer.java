package de.metalcon.neo.evaluation.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;

import de.metalcon.neo.evaluation.csv.CsvReader;

/**
 * DumpPreparer takes a "social network dump" extracted from wikipedia and checks for consistancy
 * the dump format is:
 * <timestamp> <mode> <node1> [<node2>] with mode \in {U,R,A}
 * 
 * the result is written to filename+".clean"
 * 
 * following lines from the input filename 
 * 
 * if multiedges are created these lines will be ignored
 * if edges are supposed to be 
 * @author Rene Pickhardt, Jonas Kunze
 *
 */
public class DumpPreparer {
	private CsvReader reader = null;
	private String fileName = null;

	public DumpPreparer(String fileName) {
		try {
			this.fileName = fileName;
			reader = new CsvReader(fileName);
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() throws IOException {
		BufferedWriter bw = H.openWriteFile(Configs.get().CleanWikiTransactionsFile);
		HashSet<String> edges = new HashSet<String>();
		
		BufferedReader in = H.openReadFile(fileName);
		String strLine;
		int i = 0;
		int cnt = 0;
		while ((strLine = in.readLine()) != null)   {	
			String[] values = strLine.split("\t");
			i++;
			if (i%500000==0){
				H.pln(""+i);
				bw.flush();
			}
			if (values[1].equals("U")) {
				bw.write(strLine + "\n");
				cnt++;
				continue;
			}

			String edge = values[2] + ";" + values[3];
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
			bw.write(strLine + "\n");
			cnt++;
		}
		H.log("lines processed: " + i + " lines corrupt: " + (i-cnt));
	}

}
