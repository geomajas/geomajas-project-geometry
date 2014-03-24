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

import java.util.Arrays;
import java.util.List;

import org.geomajas.annotation.Api;
import org.geomajas.geometry.service.GeometryIndex;
import org.geomajas.geometry.service.GeometryValidationState;

/**
 * Violation that indicates a ring self-intersects.
 * 
 * @author Jan De Moerloose
 * @since 1.3.0
 * 
 */
@Api(allMethods = true)
public class RingSelfIntersectionViolation implements ValidationViolation {

	private GeometryIndex edge1;

	private GeometryIndex edge2;

	private List<GeometryIndex> indices;

	/**
	 * Constructor (internal use only).
	 * 
	 * @param edge1
	 * @param edge2
	 */
	public RingSelfIntersectionViolation(GeometryIndex edge1, GeometryIndex edge2) {
		this.edge1 = edge1;
		this.edge2 = edge2;
		this.indices = Arrays.asList(edge1, edge2);
	}

	/**
	 * Get the index of the first intersecting edge.
	 * 
	 * @return
	 */
	public GeometryIndex getEdge1Index() {
		return edge1;
	}

	/**
	 * Get the index of the second intersecting edge.
	 * 
	 * @return
	 */
	public GeometryIndex getEdge2Index() {
		return edge2;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.RING_SELF_INTERSECTION;
	}

	@Override
	public List<GeometryIndex> getGeometryIndices() {
		return indices;
	}

}
