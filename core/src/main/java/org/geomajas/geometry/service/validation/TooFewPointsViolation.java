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
import org.geomajas.geometry.indexed.IndexedLineString;
import org.geomajas.geometry.indexed.IndexedLinearRing;
import org.geomajas.geometry.service.GeometryValidationState;

/**
 * Violation that indicates a geometry has too few points.
 * 
 * @author Jan De Moerloose
 * 
 */
public class TooFewPointsViolation implements ValidationViolation {

	private IndexedLinearRing ring;

	private IndexedLineString lineString;

	private List<Geometry> geometries;

	public TooFewPointsViolation(IndexedLinearRing ring) {
		this.ring = ring;
		this.geometries = Arrays.asList(ring.getGeometry());
	}

	public TooFewPointsViolation(IndexedLineString lineString) {
		this.lineString = lineString;
	}

	public IndexedLinearRing getRing() {
		return ring;
	}

	public void setRing(IndexedLinearRing ring) {
		this.ring = ring;
	}

	public IndexedLineString getLineString() {
		return lineString;
	}

	public void setLineString(IndexedLineString lineString) {
		this.lineString = lineString;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.TOO_FEW_POINTS;
	}

	@Override
	public List<Geometry> getGeometries() {
		return geometries;
	}

}
