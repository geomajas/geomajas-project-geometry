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
 * Violation that indicates a hole outside of the shell.
 * 
 * @author Jan De Moerloose
 * @since 1.3.0
 * 
 */
@Api(allMethods = true)
public class HoleOutsideShellViolation implements ValidationViolation {

	private GeometryIndex hole;

	private GeometryIndex shell;

	private List<GeometryIndex> indices;

	/**
	 * Constructor (internal use only).
	 * 
	 * @param hole
	 * @param shell
	 */
	public HoleOutsideShellViolation(GeometryIndex hole, GeometryIndex shell) {
		this.hole = hole;
		this.shell = shell;
		indices = Arrays.asList(hole, shell);
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
	 * Get the index of the shell.
	 * 
	 * @return
	 */
	public GeometryIndex getShellIndex() {
		return shell;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.HOLE_OUTSIDE_SHELL;
	}

	@Override
	public List<GeometryIndex> getGeometryIndices() {
		return indices;
	}

}
