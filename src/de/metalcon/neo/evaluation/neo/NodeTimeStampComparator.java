/**
 * comperator for the top k n way merge and comparison uf content items
 * 
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */

package de.metalcon.neo.evaluation.neo;

import java.util.Comparator;

public class NodeTimeStampComparator extends NodeComparator<Long> {
	public NodeTimeStampComparator(String property) {
		super(property, new Comparator<Long>() {

			@Override
			public int compare(Long l1, Long l2) {
				return l2.compareTo(l1);
			}
		});
	}
}
