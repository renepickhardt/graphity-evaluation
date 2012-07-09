/**
 * encapsulates a node to serve as a content item (status update)
 * 
 *
 * @author Jonas Kunze, Rene Pickhardt
 * 
 */

package de.metalcon.neo.evaluation.neo;

import org.neo4j.graphdb.Node;

public class CiContainer {
	private final Node ci;
	private final Node entity;

	public CiContainer(Node ci, Node entity) {
		this.ci = ci;
		this.entity = entity;
	}
	
	public Node getCi() {
		return ci;
	}

	public Node getEntity() {
		return entity;
	}
}
