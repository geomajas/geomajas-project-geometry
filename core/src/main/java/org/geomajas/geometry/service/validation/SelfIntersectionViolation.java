/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2014 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the Apache
 * License, Version 2.0. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.geometry.service.validation;

import org.geomajas.geometry.indexed.IndexedEdge;
import org.geomajas.geometry.service.GeometryValidationState;

/**
 * Violation that indicates a geometry self-intersects.
 * 
 * @author Jan De Moerloose
 * 
 */
public class SelfIntersectionViolation implements ValidationViolation {

	private IndexedEdge edge1;

	private IndexedEdge edge2;

	public SelfIntersectionViolation(IndexedEdge edge1, IndexedEdge edge2) {
		this.edge1 = edge1;
		this.edge2 = edge2;
	}

	public IndexedEdge getEdge1() {
		return edge1;
	}

	public IndexedEdge getEdge2() {
		return edge2;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.SELF_INTERSECTION;
	}

}
