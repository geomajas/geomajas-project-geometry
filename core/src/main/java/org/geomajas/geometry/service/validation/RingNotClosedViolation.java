/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2015 Geosparc nv, http://www.geosparc.com/, Belgium.
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
 * Violation that indicates a ring was not closed.
 * 
 * @author Jan De Moerloose
 * @since 1.3.0
 * 
 */
@Api(allMethods = true)
public class RingNotClosedViolation implements ValidationViolation {

	private GeometryIndex ring;

	private List<GeometryIndex> indices;

	/**
	 * Constructor (internal use only).
	 * 
	 * @param ring
	 */
	public RingNotClosedViolation(GeometryIndex ring) {
		this.ring = ring;
		this.indices = Arrays.asList(ring);
	}

	/**
	 * Get the index of the ring.
	 * 
	 * @return
	 */
	public GeometryIndex getRingIndex() {
		return ring;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.RING_NOT_CLOSED;
	}


	@Override
	public List<GeometryIndex> getGeometryIndices() {
		return indices;
	}

}
