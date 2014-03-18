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
 * Violation for nested holes.
 * 
 * @author Jan De Moerloose
 * 
 */
public class NestedHolesViolation implements ValidationViolation {

	private IndexedLinearRing hole;

	private IndexedLinearRing nestedHole;

	private List<Geometry> geometries;

	public NestedHolesViolation(IndexedLinearRing hole, IndexedLinearRing nestedHole) {
		this.hole = hole;
		this.nestedHole = nestedHole;
		this.geometries = Arrays.asList(hole.getGeometry(), nestedHole.getGeometry());
	}

	public IndexedLinearRing getHole() {
		return hole;
	}

	public IndexedLinearRing getNestedHole() {
		return nestedHole;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.NESTED_HOLES;
	}

	@Override
	public List<Geometry> getGeometries() {
		return geometries;
	}

}