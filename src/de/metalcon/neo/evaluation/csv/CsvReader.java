/**
 * parser for csv files
 * 
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */

package de.metalcon.neo.evaluation.csv;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class CsvReader implements Iterator<String> {
	private final BufferedReader br;
	private String line;
	private String values[];

	public CsvReader(String fileName) throws IOException {
		FileInputStream fstream = new FileInputStream(fileName);
		DataInputStream in = new DataInputStream(fstream);
		br = new BufferedReader(new InputStreamReader(in));
	}

	public int getTimeStamp() {
		return Integer.valueOf(values[0]);
	}

	public boolean isAdd() {
		return values[1].equals("A");
	}

	public boolean isUpdate() {
		return values[1].equals("U");
	}

	public boolean isRemove() {
		return values[1].equals("R");
	}

	public String getFromKey() {
		return values[2];
	}

	public String getToKey() {
		return values[3];
	}

	public String getUpdateKey() {
		return values[2];
	}

	@Override
	public boolean hasNext() {
		// not implemented!
		return false;
	}

	@Override
	public String next() {
		try {
			line = br.readLine();
			if (line != null) {
				values = line.split("\t");
			}
			return line;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void remove() {
		// not implemented!
	}

}
