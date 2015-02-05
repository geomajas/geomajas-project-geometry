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
 * Violation that indicates a geometry has too few points.
 * 
 * @author Jan De Moerloose
 * @since 1.3.0
 * 
 */
@Api(allMethods = true)
public class TooFewPointsViolation implements ValidationViolation {

	private GeometryIndex index;

	private List<GeometryIndex> indices;

	/**
	 * Constructor (internal use only).
	 * 
	 * @param index
	 */
	public TooFewPointsViolation(GeometryIndex index) {
		this.indices = Arrays.asList(index);
	}

	/**
	 * Get the index of the geometry (linestring or linear ring that has too few points).
	 * @return
	 */
	public GeometryIndex getGeometryIndex() {
		return index;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.TOO_FEW_POINTS;
	}

	@Override
	public List<GeometryIndex> getGeometryIndices() {
		return indices;
	}

}
