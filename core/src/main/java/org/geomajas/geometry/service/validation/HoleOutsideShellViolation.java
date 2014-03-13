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

import org.geomajas.geometry.indexed.IndexedLinearRing;
import org.geomajas.geometry.service.GeometryValidationState;

/**
 * Violation that indicates a hole outside of the shell.
 * 
 * @author Jan De Moerloose
 * 
 */
public class HoleOutsideShellViolation implements ValidationViolation {

	private IndexedLinearRing hole;

	private IndexedLinearRing shell;

	public HoleOutsideShellViolation(IndexedLinearRing hole, IndexedLinearRing shell) {
		this.hole = hole;
		this.shell = shell;
	}

	public IndexedLinearRing getHole() {
		return hole;
	}

	public IndexedLinearRing getShell() {
		return shell;
	}

	@Override
	public GeometryValidationState getState() {
		return GeometryValidationState.HOLE_OUTSIDE_SHELL;
	}

}
