/**
 * core functionalities of graphity and stou (sorting of ego network)
 * are encoded in this class
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */

package de.metalcon.neo.evaluation.neo;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import de.metalcon.neo.evaluation.utils.H;
import de.metalcon.neo.evaluation.utils.StopWatch;

public class SortUtils {

	public static StopWatch watch = new StopWatch();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Node> SortNodes(List<Node> nodes, NodeComparator comp) {
		Collections.sort(nodes, comp);
		return nodes;
	}

	/**
	 * 
	 * @param nodes
	 * @param comp
	 * @param maxNodes
	 * @return A sorted Set with the first 'maxNodes' nodes out of 'nodes'
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TreeSet<Node> SortNodes(List<Node> nodes,
			NodeComparator comp, int maxNodes) {
		TreeSet<Node> set = new TreeSet<Node>(comp);

		for (Node n : nodes) {
			set.add(n);
			if (set.size() > maxNodes) {
				set.pollLast();
			}
		}
		return set;
	}

	public static LinkedList<CiContainer> MergeSortBlou(int k,
			List<Node> unsortedEntities) {
		if (unsortedEntities.isEmpty()) {
			return new LinkedList<CiContainer>();
		}

		/**
		 * You need all entities (more than only k) because some could be
		 * "empty" as in not having an update ci
		 */
		List<Node> entities = SortNodes(unsortedEntities,
				new NodeTimeStampComparator("newestupdate"));

		// for (Node n : entities) {
		// H.pln("\t" + n.getProperty(Properties.newestupdate) + "\t"
		// + n.getProperty(Properties.title));
		// }
		return MergeSortCiQueue(k, entities.iterator(),
				new NodeTimeStampComparator(Properties.timestamp),
				Relations.UPDATE);
	}

	/**
	 * Call like MergeSort(k, new NodeQueuIterator(startNode, rel, dir), comp,
	 * rel);
	 * 
	 * @param k
	 * @param entities
	 * @param ciComp
	 * @param rel
	 * @return
	 */
	public static LinkedList<CiContainer> MergeSortCiQueue(int k,
			Iterator<Node> entities, Comparator<Node> ciComp,
			RelationshipType rel) {

		LinkedList<CiContainer> mergedNodes = new LinkedList<CiContainer>();
		if (!entities.hasNext()) {
			return mergedNodes;
		}

		TreeSet<CiContainer> sorter = new TreeSet<CiContainer>(
				new CiContainerComparator(ciComp));

		CiContainer lastEntFirstCi = null;
		Node entity;

//		watch.restart();
		entity = entities.next(); // first entity (0,-1)
//		watch.stop();
//		H.pln(watch.getAverageNanos());
		
		Node first = NeoUtils.getNextSingleNode(entity, rel);// first ci (0,0)
		if (first != null) {
			mergedNodes.add(new CiContainer(first, entity));
		} else {
			return mergedNodes;
		}
		// second ci of first entity (0,1):
		Node tmp = NeoUtils.getNextSingleNode(first, rel);
		if (tmp != null) {
			sorter.add(new CiContainer(tmp, entity));
		}

		if (entities.hasNext()) {
			entity = entities.next(); // second entity
			// first ci of second entity (1,0):
			tmp = NeoUtils.getNextSingleNode(entity, rel);
			if (tmp != null) {
				lastEntFirstCi = new CiContainer(tmp, entity);
				sorter.add(lastEntFirstCi);
			} else {
				tmp = NeoUtils.getNextSingleNode(first, rel);

				while (tmp != null && mergedNodes.size() < k) {
					mergedNodes.add(new CiContainer(tmp, mergedNodes.getFirst()
							.getEntity()));
					tmp = NeoUtils.getNextSingleNode(tmp, rel);
				}
				return mergedNodes;
			}
		} else if (tmp == null) { // Only one friend with only one update
			return mergedNodes;
		}

		CiContainer next;
		/*
		 * now we have elements (0,1) and (1,0) of the entity-ci matrix in
		 * sorter (E1C1, E2C1, E3C1), (E1C2, E2C2, E3C2), (E1C3, E2C3, E3C3)
		 */

		while (mergedNodes.size() < k && !sorter.isEmpty()) {
			next = sorter.pollFirst();
			mergedNodes.add(next);
			if (next == lastEntFirstCi) {
				if (entities.hasNext()) {
					// go down at current lastEntFirstCi
					tmp = NeoUtils.getNextSingleNode(lastEntFirstCi.getCi(),
							rel);
					if (tmp != null) {
						sorter.add(new CiContainer(tmp, lastEntFirstCi
								.getEntity()));
					}

					entity = entities.next(); // go right

					tmp = NeoUtils.getNextSingleNode(entity, rel);
					if (tmp != null) {
						lastEntFirstCi = new CiContainer(tmp, entity);
						sorter.add(lastEntFirstCi);
					} else {
						lastEntFirstCi = null;
					}
				}
			} else {
				// go down
				tmp = NeoUtils.getNextSingleNode(next.getCi(), rel);
				if (tmp != null) {
					sorter.add(new CiContainer(tmp, next.getEntity()));
				}
			}
		}

		return mergedNodes;
	}
}
