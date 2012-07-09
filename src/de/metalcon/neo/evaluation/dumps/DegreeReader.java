/**
 * class to examin node degrees of our wikipedia networks
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */

package de.metalcon.neo.evaluation.dumps;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedList;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.EmbeddedReadOnlyGraphDatabase;

import de.metalcon.neo.evaluation.neo.Relations;
import de.metalcon.neo.evaluation.utils.Configs;
import de.metalcon.neo.evaluation.utils.H;
import de.metalcon.neo.evaluation.utils.StopWatch;

public class DegreeReader {

	public void generateDegreeMaps() {

		LinkedList<Thread> threads = new LinkedList<Thread>();
		for (final long ts : Configs.get().StarSnapshotTimestamps) {

			Thread t = new Thread() {
				public void run() {
					try {
						DegreeReader.GenerateDegreeMaps(ts);
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
			threads.add(t);
			// t.run(); // do asynchronous
		}
		outer: while (true) {
			for (Thread t : threads) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (t.isAlive()) {
					continue outer;
				}
			}
			break;
		}
	}

	public void generateDegreeSamples() throws IOException,
			ClassNotFoundException {
		HashMap<Integer, LinkedList<Long>> map = DeserializeMap(Configs.get().DegreeMapPrefix
				+ Configs.get().SampleTimestamp);

		LinkedList<Long> IDs = new LinkedList<Long>();
		int degreesPerSample = Configs.get().SampleDegrees;
		int nodesPerDegree = Configs.get().SampleSize / degreesPerSample;

		try {
			for (int d : Configs.get().SampleStartDegrees) {
				IDs.clear();
				for (int degree = d; degree < d + degreesPerSample; degree++) {
					IDs.addAll(map.get(degree).subList(0, nodesPerDegree));
				}

				BufferedWriter out = H.openWriteFile(Configs.get().SamplePrefix
						+ Configs.get().SampleTimestamp + "_" + d);
				for (long id : IDs) {
					out.write(id + "\n");
				}
				out.close();
			}
		} catch (IndexOutOfBoundsException e) {
			System.err
					.println("Configs.SampleSize seems to be too big for the given timestamp!");
			H.log("Configs.SampleSize seems to be too big for the given timestamp!");
		}
	}

	public static void GenerateDegreeMaps(long timestamp) throws IOException,
			ClassNotFoundException {
		EmbeddedReadOnlyGraphDatabase db;

		if (Configs.get().MetalconRun) {
			db = new EmbeddedReadOnlyGraphDatabase(Configs.get().MetalconDB);
		} else {
			db = new EmbeddedReadOnlyGraphDatabase(
					Configs.get().CleanFriendDBPrefix + timestamp);
		}
		StopWatch watch = new StopWatch();

		HashMap<Integer, LinkedList<Long>> map = new HashMap<Integer, LinkedList<Long>>();
		LinkedList<Long> bigDegreeNodes = new LinkedList<Long>();

		int degree;
		int nodeNum = 0;
		for (Node n : db.getAllNodes()) {
			if (nodeNum++ % 10000 == 0) {
				watch.updateRate(nodeNum);
				H.pln("DegreeReader " + timestamp + ": Already " + nodeNum
						+ " nodes processed: " + watch.getRate(10) + " ps, "
						+ watch.getTotalRate() + " ps total");
			}
			degree = 0;
			for (Relationship rel : n.getRelationships(Relations.FOLLOWS,
					Direction.OUTGOING)) {
				degree++;
			}
			AddID(n.getId(), degree, map, bigDegreeNodes);
		}

		// SerializeMap(map, Configs.get().DegreeMapPrefix + timestamp);
		// map = DeserializeMap(Configs.get().DegreeMapPrefix + timestamp);

		BufferedWriter out;
		if (Configs.get().MetalconRun) {
			out = H.openWriteFile(Configs.get().MetalconDegreeDistribution);
		} else {
			out = H.openWriteFile(Configs.get().DegreeDistributionPrefix
					+ timestamp);
		}
		for (int deg : map.keySet()) {
			out.write(deg + "\t" + map.get(deg).size() + "\n");
		}
		out.close();

		if (!Configs.get().MetalconRun) {
			out = H.openWriteFile(Configs.get().LargeDegreeNodesPrefix
					+ timestamp);

			for (long id : bigDegreeNodes) {
				out.write(id + "\n");
			}
			out.close();
		}

		db.shutdown();
	}

	public static void SerializeMap(HashMap<Integer, LinkedList<Long>> map,
			String fileName) throws IOException, FileNotFoundException {
		FileOutputStream underlyingStream = new FileOutputStream(fileName);
		ObjectOutputStream serializer = new ObjectOutputStream(underlyingStream);
		serializer.writeObject(map);
		serializer.close();
	}

	public static HashMap<Integer, LinkedList<Long>> DeserializeMap(
			String fileName) throws IOException, ClassNotFoundException {
		HashMap<Integer, LinkedList<Long>> map;

		FileInputStream underlyingStream = new FileInputStream(fileName);
		ObjectInputStream deserializer = new ObjectInputStream(underlyingStream);
		map = (HashMap<Integer, LinkedList<Long>>) deserializer.readObject();
		return map;
	}

	private static void AddID(Long id, int degree,
			HashMap<Integer, LinkedList<Long>> map,
			LinkedList<Long> bigDegreeNodes) {
		LinkedList<Long> list;
		if (map.containsKey(degree)) {
			list = map.get(degree);
		} else {
			list = new LinkedList<Long>();
			map.put(degree, list);
		}
		list.add(id);

		if (degree >= Configs.get().MinimumNodeDegree) {
			bigDegreeNodes.add(id);
		}
	}
}
