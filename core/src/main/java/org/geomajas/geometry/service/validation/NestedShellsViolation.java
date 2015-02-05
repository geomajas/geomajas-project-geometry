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
 * Violation that indicates nesting of polygon shells.
 * 
 * @author Jan De Moerloose
 * @since 1.3.0
 * 
 */
@Api(allMethods = true)
public class NestedShellsViolation implements ValidationViolation {

	private GeometryIndex shell;

	private GeometryIndex nestedShell;

	private List<GeometryIndex> indices;

	/**
	 * Constructor (internal use only).
	 * 
	 * @param shell
	 * @param nestedShell
	 */
	public NestedShellsViolation(GeometryIndex shell, GeometryIndex nestedShell) {
		this.shell = shell;
		this.nestedShell = nestedShell;
		this.indices = Arrays.asList(shell, nestedShell);
	}

	/**
	 * Get the index of the shell.
	 * 
	 * @return
	 */
	public GeometryIndex getShellIndex() {
		return shell;
	}

	/**
	 * Get the index of the nested shell.
	 * 
	 * @return
	 */
	public GeometryIndex getNestedShellIndex() {
		return nestedShell;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.NESTED_SHELLS;
	}


	@Override
	public List<GeometryIndex> getGeometryIndices() {
		return indices;
	}

}