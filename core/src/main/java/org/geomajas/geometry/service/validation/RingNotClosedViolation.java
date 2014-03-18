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

import org.geomajas.geometry.Geometry;
import org.geomajas.geometry.indexed.IndexedLinearRing;
import org.geomajas.geometry.service.GeometryValidationState;

/**
 * Violation that indicates a ring was not closed.
 * 
 * @author Jan De Moerloose
 * 
 */
public class RingNotClosedViolation implements ValidationViolation {

	private IndexedLinearRing ring;

	private List<Geometry> geometries;

	public RingNotClosedViolation(IndexedLinearRing ring) {
		this.ring = ring;
		this.geometries = Arrays.asList(ring.getGeometry());
	}

	public IndexedLinearRing getRing() {
		return ring;
	}

	public void setRing(IndexedLinearRing ring) {
		this.ring = ring;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.RING_NOT_CLOSED;
	}

	@Override
	public List<Geometry> getGeometries() {
		return geometries;
	}

}
