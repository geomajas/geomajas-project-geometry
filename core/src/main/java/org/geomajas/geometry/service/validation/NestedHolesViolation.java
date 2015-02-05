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
 * Violation for nested holes.
 * 
 * @author Jan De Moerloose
 * @since 1.3.0
 * 
 */
@Api(allMethods = true)
public class NestedHolesViolation implements ValidationViolation {

	private GeometryIndex hole;

	private GeometryIndex nestedHole;

	private List<GeometryIndex> indices;

	/**
	 * Constructor (internal use only).
	 * 
	 * @param hole
	 * @param nestedHole
	 */
	public NestedHolesViolation(GeometryIndex hole, GeometryIndex nestedHole) {
		this.hole = hole;
		this.nestedHole = nestedHole;
		this.indices = Arrays.asList(hole, nestedHole);
	}

	/**
	 * Get the index of the hole.
	 * 
	 * @return
	 */
	public GeometryIndex getHoleIndex() {
		return hole;
	}

	/**
	 * Get the index of the nested hole.
	 * 
	 * @return
	 */
	public GeometryIndex getNestedHoleIndex() {
		return nestedHole;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.NESTED_HOLES;
	}

	@Override
	public List<GeometryIndex> getGeometryIndices() {
		return indices;
	}

}