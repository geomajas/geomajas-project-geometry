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
package org.geomajas.geometry.service;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.geometry.indexed.IndexedIntersection;
import org.geomajas.geometry.indexed.IndexedLineString;
import org.geomajas.geometry.indexed.IndexedLinearRing;
import org.geomajas.geometry.service.validation.HoleOutsideShellViolation;
import org.geomajas.geometry.service.validation.NestedHolesViolation;
import org.geomajas.geometry.service.validation.NestedShellsViolation;
import org.geomajas.geometry.service.validation.RingNotClosedViolation;
import org.geomajas.geometry.service.validation.RingSelfIntersectionViolation;
import org.geomajas.geometry.service.validation.SelfIntersectionViolation;
import org.geomajas.geometry.service.validation.TooFewPointsViolation;
import org.geomajas.geometry.service.validation.ValidationViolation;

/**
 * Context used during validation, gathers all the {@link ValidationViolation} incidents.
 * 
 * @author Jan De Moerloose
 * 
 */
public class GeometryValidationContext {

	private List<ValidationViolation> violations = new ArrayList<ValidationViolation>();

	public void addHoleOutsideShell(IndexedLinearRing hole, IndexedLinearRing shell) {
		violations.add(new HoleOutsideShellViolation(hole, shell));
	}

	public void addNestedHoles(IndexedLinearRing hole, IndexedLinearRing nestedHole) {
		violations.add(new NestedHolesViolation(hole, nestedHole));
	}

	public void addNestedShells(IndexedLinearRing shell, IndexedLinearRing nestedShell) {
		violations.add(new NestedShellsViolation(shell, nestedShell));
	}

	public void addNonClosedRing(IndexedLinearRing ring) {
		violations.add(new RingNotClosedViolation(ring));
	}

	public void addTooFewPoints(IndexedLinearRing ring) {
		violations.add(new TooFewPointsViolation(ring));
	}

	public void addTooFewPoints(IndexedLineString lineString) {
		violations.add(new TooFewPointsViolation(lineString));
	}

	public void addSelfIntersection(IndexedIntersection intersection) {
		violations.add(new SelfIntersectionViolation(intersection.getEdge1(), intersection.getEdge2()));
	}

	public void addRingSelfIntersection(IndexedIntersection intersection) {
		violations.add(new RingSelfIntersectionViolation(intersection.getEdge1(), intersection.getEdge2()));
	}

	public void clear() {
		violations.clear();
	}

	public boolean isValid() {
		return violations.isEmpty();
	}

	public GeometryValidationState getState() {
		if (violations.isEmpty()) {
			return GeometryValidationState.VALID;
		} else {
			return violations.get(0).getState();
		}
	}

}
