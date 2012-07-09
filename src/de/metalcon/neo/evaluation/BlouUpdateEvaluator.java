/**
 * This class is designed to evaluate our STOU index which serves as a baseline to graphity
 * 
 * eather a graph using the stou index can be built (inserted from file)
 *** class InsertUpdates; and method private void InsertUpdates();
 * 
 * or streams can be retrieved.
 *** class Evaluate; and method private void evaluate();
 * 
 * also the behavior of the growing wikipedia can be simulated
 *** class BlouSimulater and method private void simulate();
 *
 * @author: Rene Pickhardt, Jonas Kunze
**/


package de.metalcon.neo.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.channels.NonWritableChannelException;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;

import de.metalcon.neo.evaluation.neo.CiContainer;
import de.metalcon.neo.evaluation.neo.NeoUtils;
import de.metalcon.neo.evaluation.neo.Properties;
import de.metalcon.neo.evaluation.neo.Relations;
import de.metalcon.neo.evaluation.neo.SortUtils;
import de.metalcon.neo.evaluation.utils.Configs;
import de.metalcon.neo.evaluation.utils.CopyDirectory;
import de.metalcon.neo.evaluation.utils.H;
import de.metalcon.neo.evaluation.utils.StopWatch;

public class BlouUpdateEvaluator {

	public long[] timestamps;
	StopWatch watch = new StopWatch();

	public BlouUpdateEvaluator() {
	}

	public void run(boolean buildOrEval, boolean runOnDegreeSample) {
		if (buildOrEval && Configs.get().BlouUpdateEvaluatorInsertUpdates) {
			InsertUpdates();
		}

		if (!runOnDegreeSample && !buildOrEval
				&& Configs.get().BlouUpdateEvaluatorEvaluate) {
			for (long ts : Configs.get().StarSnapshotTimestamps) {
				Evaluate(ts, runOnDegreeSample, 0);
			}
		}

		if (runOnDegreeSample && !buildOrEval
				&& Configs.get().BlouUpdateEvaluatorEvaluateDegree
				&& !Configs.get().MetalconRun) {
			for (int d : Configs.get().SampleStartDegrees) {
				Evaluate(Configs.get().SampleTimestamp, runOnDegreeSample, d);
			}
		}

		if (!buildOrEval && Configs.get().BlouUpdateEvaluatorSimulate) {
			Simulate();
		}
	}

	class InsertUpdates extends Thread {
		private AbstractGraphDatabase bl;
		private long ts;

		public int nodeCnt;
		public int notFound = 0;

		public InsertUpdates(AbstractGraphDatabase bl, long ts) {
			this.bl = bl;
			this.ts = ts;
			nodeCnt = 0;
		}

		@Override
		public void run() {
			BufferedReader in;
			if (Configs.get().MetalconRun) {
				in = H.openReadFile(Configs.get().MetalconUpdatesSorted);
			} else {
				in = H.openReadFile(Configs.get().CleanWikiSnapshotUpdatePrefix
						+ ts);
			}

			String strLine;
			String[] values;

			try {
				Transaction tx = bl.beginTx();
				try {
					while ((strLine = in.readLine()) != null) {
						if (nodeCnt % 10000 == 0) {
							tx.success();
							tx.finish();
							tx = bl.beginTx();
						}
						values = strLine.split("\t");
						if (values.length != 2)
							continue;
						try {
							Node n = bl.getNodeById(Long.parseLong(values[1]));
							long ts = Long.parseLong(values[0]);
							NeoUtils.InsertContentItemAsOrderedList(bl, n, ts,
									Relations.UPDATE);
							n.setProperty(Properties.newestupdate, ts);
							nodeCnt++;

						} catch (NotFoundException e) {
							notFound++;
							continue;
						}
					}
					tx.success();
				} finally {
					tx.finish();
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};

	private void InsertUpdates() {
		for (long ts : Configs.get().StarSnapshotTimestamps) {
			String dbPath;
			if (Configs.get().MetalconRun) {
				dbPath = Configs.get().MetalconDB + "-blou";
				H.deleteDirectory(new File(dbPath));
				new CopyDirectory(Configs.get().MetalconDB, dbPath);
			} else {
				dbPath = Configs.get().BlouDBDirPrefix + ts;
				H.deleteDirectory(new File(dbPath));
				new CopyDirectory(Configs.get().CleanFriendDBPrefix + ts,
						dbPath);
			}
			AbstractGraphDatabase bl = NeoUtils
					.getAbstractGraphDatabase(dbPath);

			InsertUpdates iu = new InsertUpdates(bl, ts);
			StopWatch watch = new StopWatch();
			iu.start();
			try {
				while (iu.isAlive()) {
					Thread.sleep(5000);
					watch.updateRate(iu.nodeCnt);
					H.log("BLOU-Updates " + ts + " Added " + iu.nodeCnt
							+ " nodes so far: " + iu.notFound + " not found; "
							+ watch.getRate(10) + "ps : "
							+ watch.getTotalRate() + "ps total");

				}
				watch.updateRate(iu.nodeCnt);
				H.strongLog("BLOU-Updates " + ts + " Added " + iu.nodeCnt
						+ " nodes so far: " + watch.getTotalRate() + "ps total");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			NeoUtils.ShutdownDB(bl, dbPath);
		}
	}

	class Evaluate extends Thread {
		private AbstractGraphDatabase bl;
		private long ts;

		public int lineCount;
		public int nodeCnt;

		private LinkedList<Node> allNodes;
		private final boolean runOnDegreeSample;

		public Evaluate(AbstractGraphDatabase bl, long ts,
				boolean runOnDegreeSample, int sampleDegree) {
			this.runOnDegreeSample = runOnDegreeSample;
			this.bl = bl;
			this.ts = ts;
			lineCount = 0;
			nodeCnt = 0;

			LinkedList<Long> allIds;
			if (runOnDegreeSample) {
				allIds = NeoUtils.GetAllNodeIds(sampleDegree);
			} else {
				allIds = NeoUtils.GetAllNodeIds(ts);
			}
			allNodes = NeoUtils.GetAllNodes(bl, allIds);
		}

		public void run() {
			int repeats = 0;
			if (runOnDegreeSample) {
				repeats = Configs.get().SampleRepeatRuns - 1;
			}
			for (int i = 0; i < repeats + 1; i++) {
				for (Node n : allNodes) {
					GenerateStream(n);
					lineCount++;
					// if (lineCount > 1000) {
					// H.pln(watch.getAverageNanos() / 1000);
					// break;
					// }
				}
			}
		}

		private void GenerateStream(Node n) {
			LinkedList<Node> unsortedEntities = new LinkedList<Node>();

			try {
				for (Relationship followRel : n.getRelationships(
						Direction.OUTGOING, Relations.FOLLOWS)) {
					Node node = followRel.getEndNode();

					if (node.hasProperty("newestupdate")) {
						unsortedEntities.add(node);
					}
				}
			} catch (NonWritableChannelException e) {
			}

			// watch.restart();
			List<CiContainer> stream = SortUtils.MergeSortBlou(Configs.get().k,
					unsortedEntities);
			// watch.stop();

			// H.pln(n.getProperty(Properties.title)+"!!!"+stream.size());
			String streamText = "";
			for (CiContainer streamNode : stream) {
				nodeCnt++;
				streamText += "\t"
						+ streamNode.getCi().getProperty(Properties.timestamp)
						+ " from "
						+ streamNode.getEntity().getProperty("title") + "\n";
			}
			// H.pln(streamText);
		}
	}

	private void Evaluate(long timestamp, boolean runOnDegreeSample,
			int sampleStartDegree) {
		String dbPath = "";
		if (Configs.get().MetalconRun) {
			dbPath = Configs.get().MetalconDB + "-blou";
		} else {
			dbPath = Configs.get().BlouDBDirPrefix + timestamp;
		}

		AbstractGraphDatabase bl = NeoUtils.getAbstractGraphDatabase(dbPath,
				true);

		for (int i = 0; i < Configs.get().runs; i++) {
			Evaluate e = new Evaluate(bl, timestamp, runOnDegreeSample,
					sampleStartDegree);
			e.start();
			try {
				StopWatch watch = new StopWatch();
				while (e.isAlive()) {
					Thread.sleep(5000);
					watch.updateRate(e.lineCount);
					if (runOnDegreeSample) {
						H.log("BLOU-Evaluating  (" + i + ") degree "
								+ sampleStartDegree + "; ts = " + timestamp
								+ " Created " + e.lineCount
								+ " streams and processed " + e.nodeCnt
								+ " nodes (" + ((float) e.nodeCnt)
								/ e.lineCount
								+ " average nodes per stream) so far: "
								+ watch.getRate(10) + "ps : "
								+ watch.getTotalRate() + "ps total");
					} else {
						H.log("BLOU-Evaluating (" + i + ") " + timestamp
								+ " Created " + e.lineCount
								+ " streams and processed " + e.nodeCnt
								+ " nodes (" + ((float) e.nodeCnt)
								/ e.lineCount
								+ " average nodes per stream) so far: "
								+ watch.getRate(10) + "ps : "
								+ watch.getTotalRate() + "ps total");
					}

				}
				watch.updateRate(e.lineCount);
				if (runOnDegreeSample) {
					H.strongLog("BLOU-Evaluating (" + i + ") degree "
							+ sampleStartDegree + "; ts = " + timestamp
							+ " finished: Created " + e.lineCount
							+ " streams and processed " + e.nodeCnt
							+ " nodes (" + ((float) e.nodeCnt) / e.lineCount
							+ " average nodes per stream) so far: "
							+ watch.getTotalRate() + "ps total");
				} else {
					H.strongLog("BLOU-Evaluating (" + i + ") " + timestamp
							+ " finished: Created " + e.lineCount
							+ " streams and processed " + e.nodeCnt
							+ " nodes (" + ((float) e.nodeCnt) / e.lineCount
							+ " average nodes per stream) so far: "
							+ watch.getTotalRate() + "ps total");
				}

			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

		}
		NeoUtils.ShutdownDB(bl, dbPath);
	}

	/**
	 * Simulate blou
	 */
	public void Simulate() {
		for (long ts : Configs.get().StarSnapshotTimestamps) {
			new CopyDirectory(Configs.get().GraphityDBDirPrefix + ts,
					Configs.get().GraphityDBDirPrefix + "simulate" + ts);

			// NeoUtils.FillNodesInGraphitySimulation(
			// Configs.get().GraphityDBDirPrefix + "simulate" + ts,
			// Configs.get().GraphityDBDirPrefix + ts, ts);

			AbstractGraphDatabase blou = NeoUtils
					.getAbstractGraphDatabase(Configs.get().GraphityDBDirPrefix
							+ "simulate" + ts);

			// kick of process and call simulator class! (this class should be
			// very similar to the one i had in wiki importer
			BlouSimulater sgt = new BlouSimulater(blou, ts);

			StopWatch watch = new StopWatch();
			sgt.start();
			try {
				while (sgt.isAlive()) {
					Thread.sleep(5000);
					watch.updateRate(sgt.queries);
					H.log("Simulating Blou " + ts + " Adds: " + sgt.adds
							+ " adds not found: " + sgt.addsNotFound
							+ " updates: " + sgt.updates
							+ " updates not found: " + sgt.updatesNotFound
							+ " removes: " + sgt.removes
							+ " removes not found: " + sgt.removesNotFound
							+ " queries: " + sgt.queries + " "
							+ watch.getRate(10) + " ps : "
							+ watch.getTotalRate() + " ps total" + "\n"
							+ "Add freq: " + 1000000000
							/ sgt.addWatch.getAverageNanos() + " ps "
							+ "Update freq: " + 1000000000
							/ sgt.updateWatch.getAverageNanos() + " ps "
							+ "Remove freq: " + 1000000000
							/ sgt.removeWatch.getAverageNanos() + " ps ");
				}
				H.strongLog("Simulating Blou " + ts + " Adds " + sgt.adds
						+ " adds not found: " + sgt.addsNotFound + " updates: "
						+ sgt.updates + " updates not found: "
						+ sgt.updatesNotFound + " removes: " + sgt.removes
						+ " queries " + " removes not found: "
						+ sgt.removesNotFound + " queries: " + sgt.queries
						+ " " + watch.getRate(10) + " ps : "
						+ watch.getTotalRate() + " ps total which took "
						+ watch.getSecondsPassed() + "\n" + "Add freq: "
						+ 1000000.0 / sgt.addWatch.getAverageNanos() + " ps "
						+ "Update freq: " + 1000000.0
						/ sgt.updateWatch.getAverageNanos() + " kps "
						+ "Remove freq: " + 1000000.0
						/ sgt.removeWatch.getAverageNanos() + " kps ");

				watch.updateRate(sgt.queries);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			NeoUtils.ShutdownDB(blou, Configs.get().GraphityDBDirPrefix
					+ "simulate" + ts, false);
			H.deleteDirectory(new File(Configs.get().GraphityDBDirPrefix
					+ "simulate" + ts));
		}
	}

	class BlouSimulater extends Thread {
		private AbstractGraphDatabase blou;

		public int egoNW;
		public int updatesNotFound;
		public int addsNotFound;
		public int removesNotFound;
		public int updates = 0;
		public int adds = 0;
		public int removes = 0;
		public int queries = 0;
		public StopWatch addWatch = new StopWatch();
		public StopWatch updateWatch = new StopWatch();
		public StopWatch removeWatch = new StopWatch();

		public LinkedList<String[]> transactions = null;

		public BlouSimulater(AbstractGraphDatabase blou, long ts) {
			this.blou = blou;
			egoNW = 0;
			updatesNotFound = 0;
			updates = 0;
			addsNotFound = 0;
			removesNotFound = 0;
			adds = 0;
			removes = 0;
			queries = 0;
			transactions = NeoUtils.GetSimulateEvents(
					Configs.get().numberOfEventsToSimulate, ts);
		}

		public void run() {
			for (String[] values : transactions) {
				Transaction tx = blou.beginTx();
				try {
					long ts = Long.parseLong(values[0]);
					long from = Long.parseLong(values[2]);
					queries++;
					if (values.length == 3) { // Update
						updateWatch.restart();
						Update(ts, from);
						updateWatch.stop();
						updates++;
					} else if (values.length == 4) { // Add/Remove
						long to = Long.parseLong(values[3]);
						// if (true) {
						// continue;
						// }
						if (from == to)
							continue;
						if (values[1].equals("A")) {
							addWatch.restart();
							AddEdge(ts, from, to);
							addWatch.stop();
							adds++;
						} else {
							removeWatch.restart();
							RemoveEdge(ts, from, to);
							removeWatch.stop();
							removes++;
						}
					}
					tx.success();
				} finally {
					tx.finish();
				}
			}
		}

		private void Update(long timestamp, long key) {
			Node e;
			try {
				e = blou.getNodeById(key);
			} catch (NotFoundException ex) {
				// ex.printStackTrace();
				updatesNotFound++;
				return;
			}

			if (e == null) {
				H.log("UPDATE unsuccessfull (node not found)" + timestamp + " "
						+ key);
				return;
			}

			Node ci = NeoUtils.getNextSingleNode(e, Relations.UPDATE);
			if (ci == null) {// no updates available yet!
				ci = blou.createNode();
				ci.setProperty("timestamp", timestamp);
				e.createRelationshipTo(ci, Relations.UPDATE);
			} else {
				Node newNode = blou.createNode();
				newNode.setProperty(Properties.timestamp, timestamp);
				e.getSingleRelationship(Relations.UPDATE, Direction.OUTGOING)
						.delete();
				e.createRelationshipTo(newNode, Relations.UPDATE);
				newNode.createRelationshipTo(ci, Relations.UPDATE);
			}
			e.setProperty(Properties.newestupdate, timestamp);
			// UpdateEgoNetwork(e, timestamp);!
		}

		private void RemoveEdge(long timestamp, long from, long to) {
			try {
				Node fromNode;
				Node toNode;
				try {
					fromNode = blou.getNodeById(from);
					toNode = blou.getNodeById(to);
				} catch (NotFoundException e) {
					removesNotFound++;
					return;
				}

				try {
					for (Relationship rel : fromNode.getRelationships(
							Relations.FOLLOWS, Direction.OUTGOING)) {
						if (rel.getEndNode().equals(toNode)) {
							rel.delete();
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("REM unsuccessfull (rel not found)"
						+ timestamp + " " + from + " " + to + " ");
			} catch (NullPointerException e) {
				e.printStackTrace();
				return;
			}
		}

		private void AddEdge(long ts, long from, long to) {
			Node fromNode;
			Node toNode;
			try {
				fromNode = blou.getNodeById(from);
				toNode = blou.getNodeById(to);
			} catch (NotFoundException e) {
				addsNotFound++;
				return;
			}

			// Create star topology
			fromNode.createRelationshipTo(toNode, Relations.FOLLOWS);
		}
	}
}
