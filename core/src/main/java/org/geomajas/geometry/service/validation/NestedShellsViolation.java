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
 * Violation that indicates nesting of polygon shells.
 * 
 * @author Jan De Moerloose
 * 
 */
public class NestedShellsViolation implements ValidationViolation {

	private IndexedLinearRing shell;

	private IndexedLinearRing nestedShell;

	private List<Geometry> geometries;

	public NestedShellsViolation(IndexedLinearRing shell, IndexedLinearRing nestedShell) {
		this.shell = shell;
		this.nestedShell = nestedShell;
		this.geometries = Arrays.asList(shell.getGeometry(), nestedShell.getGeometry());
	}

	public IndexedLinearRing getShell() {
		return shell;
	}

	public IndexedLinearRing getNestedShell() {
		return nestedShell;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.NESTED_SHELLS;
	}

	@Override
	public List<Geometry> getGeometries() {
		return geometries;
	}

}