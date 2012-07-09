/**
 * This class is designed to evaluate our graphity index which
 * 
 * eather a graph using the stou index can be built (inserted from file)
 *** class GraphityBuilder; and method private void BuildGrapity();
 * 
 * or streams can be retrieved.
 *** class EGT (evaluate grapity thrad); and method private void evaluate();
 * 
 * also the behavior of the growing wikipedia can be simulated
 *** class SGT (simulate graphity thread) and method private void simulate();
 *
 * @author: Rene Pickhardt, Jonas Kunze
**/

package de.metalcon.neo.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;

import de.metalcon.neo.evaluation.neo.CiContainer;
import de.metalcon.neo.evaluation.neo.NeoUtils;
import de.metalcon.neo.evaluation.neo.NodeQueueIterator;
import de.metalcon.neo.evaluation.neo.NodeTimeStampComparator;
import de.metalcon.neo.evaluation.neo.Properties;
import de.metalcon.neo.evaluation.neo.Relations;
import de.metalcon.neo.evaluation.neo.SortUtils;
import de.metalcon.neo.evaluation.utils.Configs;
import de.metalcon.neo.evaluation.utils.CopyDirectory;
import de.metalcon.neo.evaluation.utils.H;
import de.metalcon.neo.evaluation.utils.StopWatch;

public class GraphityBuilder {
	public AbstractGraphDatabase graphity;
	StopWatch watch = new StopWatch();
	NodeTimeStampComparator tsComparator = new NodeTimeStampComparator(
			"timestamp");

	public GraphityBuilder() {
		if (!Configs.get().MetalconRun) {
			if (Configs.get().ResetGraphity && Configs.get().BuildGraphity) {
				for (long ts : Configs.get().StarSnapshotTimestamps) {
					H.deleteDirectory(new File(
							Configs.get().GraphityDBDirPrefix + ts));
					new CopyDirectory(Configs.get().BlouDBDirPrefix + ts,
							Configs.get().GraphityDBDirPrefix + ts);
				}
			}
		} else {
			if (Configs.get().ResetGraphity && Configs.get().BuildGraphity) {
				H.deleteDirectory(new File(Configs.get().MetalconDB
						+ "-graphity"));
				new CopyDirectory(Configs.get().MetalconDB + "-blou",
						Configs.get().MetalconDB + "-graphity");
			}
		}
	}

	public void run() {
		if (Configs.get().BuildGraphity) {
			BuildGraphity();
			// graphity builder!InsertUpdates();
		}

		if (!Configs.get().ReadGraphityStreamsDegree
				&& Configs.get().ReadGraphityStreams) {
			for (long ts : Configs.get().StarSnapshotTimestamps) {
				Evaluate(ts, Configs.get().ReadGraphityStreamsDegree, 0);
			}
		}

		if (Configs.get().ReadGraphityStreamsDegree
				&& Configs.get().ReadGraphityStreamsDegree) {
			for (int d : Configs.get().SampleStartDegrees) {
				Evaluate(Configs.get().SampleTimestamp,
						Configs.get().ReadGraphityStreamsDegree, d);
			}
		}

		if (Configs.get().SimulateGraphity) {
			Simulate();
		}
	}

	/**
	 * build graphity for all enitites e get e.getRelationships(FOLLOW,
	 * Direction.OUTGOING) for the egonetwork sort by last update! now insert
	 * ego:e relations AAAAAAAAAAAAAAAAAAND Done!
	 */
	public void BuildGraphity() {
		for (long ts : Configs.get().StarSnapshotTimestamps) {
			String dbDir;
			if (Configs.get().MetalconRun) {
				dbDir = Configs.get().MetalconDB + "-graphity";
			} else {
				dbDir = Configs.get().GraphityDBDirPrefix + ts;
			}
			graphity = NeoUtils.getAbstractGraphDatabase(dbDir);
			BGT bgt = new BGT(graphity, ts);

			bgt.start();
			try {
				StopWatch watch = new StopWatch();
				while (bgt.isAlive()) {
					Thread.sleep(5000);
					watch.updateRate(bgt.egoNW);
					H.log("Calculating Graphity on SN " + ts
							+ " Already processed " + bgt.nodeCnt
							+ " nodes and " + bgt.egoNW + " egonetworks ("
							+ (float) bgt.nodeCnt / bgt.egoNW
							+ " nodes per ego network) " + watch.getRate(10)
							+ " egoNW ps : " + watch.getTotalRate()
							+ " egoNW ps total");

				}
				watch.updateRate(bgt.egoNW);
				H.strongLog("Calculating Graphity on SN " + ts
						+ " Already processed " + bgt.egoNW + " nodes and "
						+ bgt.nodeCnt + " egonetworks (" + (float) bgt.nodeCnt
						/ bgt.egoNW + " nodes per ego network) "
						+ watch.getRate(10) + " egoNW ps : "
						+ watch.getTotalRate() + " egoNW ps total");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			NeoUtils.ShutdownDB(graphity, dbDir);
		}
	}

	class BGT extends Thread {
		private AbstractGraphDatabase graphity;
		private long ts;

		public int egoNW;
		public int nodeCnt;

		public BGT(AbstractGraphDatabase graphity, long ts) {
			this.graphity = graphity;
			this.ts = ts;
			egoNW = 0;
			nodeCnt = 0;
		}

		@Override
		public void run() {
			LinkedList<Long> allIds = NeoUtils.GetAllNodeIds(ts);
			for (long id : allIds) {
				// TODO: catch next line, what if this is
				Node n;
				try {
					n = graphity.getNodeById(id);
				} catch (NotFoundException e) {
					continue;
				}
				egoNW++;
				List<Node> friends = new ArrayList<Node>();
				for (Relationship r : n.getRelationships(Direction.OUTGOING,
						Relations.FOLLOWS)) {
					Node friend = r.getEndNode();
					friends.add(friend);
				}
				// TODO: comment out next line + syntax!!!
				friends = SortUtils.SortNodes(friends,
						new NodeTimeStampComparator("newestupdate"));
				Transaction tx = graphity.beginTx();
				try {
					DynamicRelationshipType egoType = DynamicRelationshipType
							.withName("ego:" + id);
					Iterator<Node> it = friends.iterator();
					while (it.hasNext()) {
						nodeCnt++;
						Node tmp = it.next();
						n.createRelationshipTo(tmp, egoType);
						n = tmp;
					}
					tx.success();
				} finally {
					tx.finish();
				}
			}
		}
	};

	/**
	 * Evaluate Graphity
	 */
	public void Evaluate(long timestamp, boolean runOnDegreeSample,
			int sampleStartDegree) {
		String dbDir;
		if (Configs.get().MetalconRun) {
			dbDir = Configs.get().MetalconDB + "-graphity";
		} else {
			dbDir = Configs.get().GraphityDBDirPrefix + timestamp;
		}
		AbstractGraphDatabase graphity = NeoUtils
				.getAbstractGraphDatabase(dbDir);

		for (int i = 0; i < Configs.get().runs; i++) {
			EGT egt = new EGT(graphity, timestamp, runOnDegreeSample,
					sampleStartDegree);
			StopWatch watch = new StopWatch();
			egt.start();
			try {
				while (egt.isAlive()) {
					Thread.sleep(5000);
					watch.updateRate(egt.egoNW);
					if (runOnDegreeSample) {
						H.log("Graphity-Evaluating (" + i + ") degree "
								+ sampleStartDegree + "; ts = " + timestamp
								+ " Created " + egt.egoNW
								+ " streams and processed " + egt.nodeCnt
								+ " nodes (" + ((float) egt.nodeCnt)
								/ egt.egoNW
								+ " average nodes per stream) so far: "
								+ watch.getRate(10) + "ps : "
								+ watch.getTotalRate() + "ps total");
					} else {
						H.log("Graphity-Evaluating (" + i + ") " + timestamp
								+ " Created " + egt.egoNW
								+ " streams and processed " + egt.nodeCnt
								+ " nodes (" + ((float) egt.nodeCnt)
								/ egt.egoNW
								+ " average nodes per stream) so far: "
								+ watch.getRate(10) + "ps : "
								+ watch.getTotalRate() + "ps total");
					}

				}
				watch.updateRate(egt.egoNW);
				if (runOnDegreeSample) {
					H.strongLog("Graphity-Evaluating (" + i + ") degree "
							+ sampleStartDegree + "; ts = " + timestamp
							+ " finished: Created " + egt.egoNW
							+ " streams and processed " + egt.nodeCnt
							+ " nodes (" + ((float) egt.nodeCnt) / egt.egoNW
							+ " average nodes per stream) so far: "
							+ watch.getTotalRate() + "ps total");
				} else {
					H.strongLog("Graphity-Evaluating (" + i + ") " + timestamp
							+ " finished: Created " + egt.egoNW
							+ " streams and processed " + egt.nodeCnt
							+ " nodes (" + ((float) egt.nodeCnt) / egt.egoNW
							+ " average nodes per stream) so far: "
							+ watch.getTotalRate() + "ps total");
				}

			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

		}

		NeoUtils.ShutdownDB(graphity, dbDir);
	}

	class EGT extends Thread {
		private AbstractGraphDatabase graphity;
		private LinkedList<Node> allNodes;

		private final boolean runOnDegreeSample;

		private long ts;

		public int egoNW;
		public int nodeCnt;

		public EGT(AbstractGraphDatabase graphity, long ts,
				boolean runOnDegreeSample, int sampleStartDegree) {
			this.runOnDegreeSample = runOnDegreeSample;
			this.graphity = graphity;
			this.ts = ts;
			egoNW = 0;
			nodeCnt = 0;

			LinkedList<Long> allIds;
			if (runOnDegreeSample) {
				allIds = NeoUtils.GetAllNodeIds(sampleStartDegree);
			} else {
				allIds = NeoUtils.GetAllNodeIds(ts);
			}
			allNodes = NeoUtils.GetAllNodes(graphity, allIds);
		}

		public void run() {
			int repeats = 0;
			if (runOnDegreeSample) {
				repeats = Configs.get().SampleRepeatRuns - 1;
			}
			for (int i = 0; i < repeats + 1; i++) {
				for (Node n : allNodes) {
					GenerateStream(n);
					egoNW++;

				}
			}
		}

		private void GenerateStream(Node n) {
			DynamicRelationshipType egoType = DynamicRelationshipType
					.withName("ego:" + n.getId());

			LinkedList<CiContainer> ci = SortUtils.MergeSortCiQueue(Configs
					.get().k, new NodeQueueIterator(n, egoType,
					Direction.OUTGOING), tsComparator, Relations.UPDATE);

			String stream = "";
			for (CiContainer c : ci) {
				nodeCnt++;
				stream += "\t" + c.getCi().getProperty("timestamp") + " from "
						+ c.getEntity().getProperty("title") + "\n";
			}
		}
	};

	/**
	 * Simulate graphity
	 */
	public void Simulate() {
		for (long ts : Configs.get().StarSnapshotTimestamps) {
			new CopyDirectory(Configs.get().GraphityDBDirPrefix + ts,
					Configs.get().GraphityDBDirPrefix + "simulate" + ts);

			AbstractGraphDatabase graphity = NeoUtils
					.getAbstractGraphDatabase(Configs.get().GraphityDBDirPrefix
							+ "simulate" + ts);

			SGT sgt = new SGT(graphity, ts);

			StopWatch watch = new StopWatch();
			sgt.start();
			try {
				while (sgt.isAlive()) {
					Thread.sleep(5000);
					watch.updateRate(sgt.queries);
					H.log("Simulating-graphity " + ts + " Adds: " + sgt.adds
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
				H.strongLog("Simulating-graphity " + ts + " Adds " + sgt.adds
						+ " adds not found: " + sgt.addsNotFound + " updates: "
						+ sgt.updates + " updates not found: "
						+ sgt.updatesNotFound + " removes: " + sgt.removes
						+ " queries " + " removes not found: "
						+ sgt.removesNotFound + " queries: " + sgt.queries
						+ " " + watch.getRate(10) + " ps : "
						+ watch.getTotalRate() + " ps total which took "
						+ watch.getSecondsPassed() + "\n" + "Add freq: "
						+ 1000000.0 / sgt.addWatch.getAverageNanos() + " kps "
						+ "Update freq: " + 1000000.0
						/ sgt.updateWatch.getAverageNanos() + " kps "
						+ "Remove freq: " + 1000000.0
						/ sgt.removeWatch.getAverageNanos() + " kps ");

				watch.updateRate(sgt.queries);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			NeoUtils.ShutdownDB(graphity, Configs.get().GraphityDBDirPrefix
					+ "simulate" + ts, false);
			H.deleteDirectory(new File(Configs.get().GraphityDBDirPrefix
					+ "simulate" + ts));
		}
	}

	class SGT extends Thread {
		private AbstractGraphDatabase graphity;
		private long ts;

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

		public SGT(AbstractGraphDatabase graphity, long ts) {
			this.graphity = graphity;
			this.ts = ts;
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
				Transaction tx = graphity.beginTx();
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
				e = graphity.getNodeById(key);
			} catch (NotFoundException ex) {
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
				ci = graphity.createNode();
				ci.setProperty("timestamp", timestamp);
				e.createRelationshipTo(ci, Relations.UPDATE);
			} else {
				Node newNode = graphity.createNode();
				newNode.setProperty(Properties.timestamp, timestamp);
				e.getSingleRelationship(Relations.UPDATE, Direction.OUTGOING)
						.delete();
				e.createRelationshipTo(newNode, Relations.UPDATE);
				newNode.createRelationshipTo(ci, Relations.UPDATE);
			}
			e.setProperty(Properties.newestupdate, timestamp);
			UpdateEgoNetwork(e, timestamp);
		}

		/**
		 * double checked the workflow of this function twice. but I can only
		 * test it once I also update ego networks if followed nodes are created
		 * (-:
		 */
		private void UpdateEgoNetwork(Node e, long timestamp) {
			// look at all nodes that follow me!
			for (Relationship rel : e.getRelationships(Relations.FOLLOWS,
					Direction.INCOMING)) {
				Node currentNode = rel.getStartNode();
				DynamicRelationshipType egoType = DynamicRelationshipType
						.withName("ego:" + currentNode.getId());
				Node prev = NeoUtils.getPrevSingleNode(e, egoType); // will allways exist!
				if (prev == null) {
					H.strongLog("This is impossible! mistake while updating ego network and retrieving prev node");
				} else if (prev.equals(currentNode)) {// nothing to do here, just update time stamp!
				} else { // we are somewhere in the chain and prev != start node
					// next and prev we have currentNode -> ... -> prev
					// -> e -> next -> ... with respect to
					// "ego:currentNode.key"

					Node next = NeoUtils.getNextSingleNode(e, egoType);
					if (next == null) {	// we are the last node in the index just
								// delete relationship to prev node
						e.getSingleRelationship(egoType, Direction.INCOMING)
								.delete();
					} else { 	// remove prev and current relationship and create
							// a new one
						e.getSingleRelationship(egoType, Direction.INCOMING)
								.delete();
						e.getSingleRelationship(egoType, Direction.OUTGOING)
								.delete();

						Relationship tmp = prev.createRelationshipTo(next,
								egoType);

					}
					// now insert e to the beginning of currents ego network!
					Node tmp = NeoUtils.getNextSingleNode(currentNode, egoType);
					currentNode.getSingleRelationship(egoType,
							Direction.OUTGOING).delete();
					Relationship r = currentNode.createRelationshipTo(e,
							egoType);
					r = e.createRelationshipTo(tmp, egoType);
				}
			}
		}

		private void RemoveEdge(long timestamp, long from, long to) {
			try {
				Node fromNode;
				Node toNode;
				try {
					fromNode = graphity.getNodeById(from);
					toNode = graphity.getNodeById(to);
				} catch (NotFoundException e) {
					removesNotFound++;
					return;
				}

				RemoveFromEgoNetwork(fromNode, toNode);
				for (Relationship rel : fromNode.getRelationships(
						Relations.FOLLOWS, Direction.OUTGOING)) {
					if (rel.getEndNode().equals(toNode)) {
						rel.delete();
						return;
					}
				}
				System.out.println("REM unsuccessfull (rel not found)"
						+ timestamp + " " + from + " " + to + " ");
			} catch (NullPointerException e) {
				e.printStackTrace();
				return;
			}
		}

		private void RemoveFromEgoNetwork(Node from, Node to) {
			DynamicRelationshipType egoType = DynamicRelationshipType
					.withName("ego:" + from.getId());
			Node prev = NeoUtils.getPrevSingleNode(to, egoType); // never null!

			prev.getSingleRelationship(egoType, Direction.OUTGOING).delete();

			Node next = NeoUtils.getNextSingleNode(to, egoType);
			if (next != null) {
				next.getRelationships(egoType, Direction.INCOMING).iterator()
						.next().delete();
				prev.createRelationshipTo(next, egoType);
			}
		}

		private void AddEdge(long ts, long from, long to) {
			Node fromNode;
			Node toNode;
			try {
				fromNode = graphity.getNodeById(from);
				toNode = graphity.getNodeById(to);
			} catch (NotFoundException e) {
				addsNotFound++;
				return;
			}

			// Create star topology
			fromNode.createRelationshipTo(toNode, Relations.FOLLOWS);

			/**
			 * adding to egonetwork should also work now!
			 */
			DynamicRelationshipType egoType = DynamicRelationshipType
					.withName("ego:" + from);

			if (NeoUtils.getNextSingleNode(fromNode, egoType) == null) {// easy: first friend in ego network!
				Relationship rel = fromNode.createRelationshipTo(toNode,
						egoType);
			} else { // ego network of from alread exists let's have fun!
				Node n = fromNode;
				Node tmp = n;

				long ToTimestamp = 0;
				if (toNode.hasProperty(Properties.newestupdate)) {
					ToTimestamp = (Long) toNode
							.getProperty(Properties.newestupdate);
				}
				boolean inserted = false;
				do { 
					long curTimestamp = 0;
					if (n.hasProperty(Properties.newestupdate)) {
						curTimestamp = (Long) n
								.getProperty(Properties.newestupdate);
					}
					if (ToTimestamp > curTimestamp) { 
						if (tmp.equals(n)) { // fix for insertion at the beginning
							n = NeoUtils.getNextSingleNode(n, egoType);
						}
						tmp.getSingleRelationship(egoType, Direction.OUTGOING)
								.delete();
						Relationship rel = tmp.createRelationshipTo(toNode,
								egoType);
						rel = toNode.createRelationshipTo(n, egoType);
						inserted = true;
						break;
					}
					tmp = n;
					n = NeoUtils.getNextSingleNode(n, egoType);

				} while (n != null);
				if (inserted == false) {
					Relationship rel = tmp
							.createRelationshipTo(toNode, egoType);
				}
			}
		}
	};
}
