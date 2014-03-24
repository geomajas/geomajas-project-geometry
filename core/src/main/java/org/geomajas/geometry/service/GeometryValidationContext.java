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

import org.geomajas.annotation.Api;
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
 * @since 1.3.0
 */
@Api
public class GeometryValidationContext {

	private List<ValidationViolation> violations = new ArrayList<ValidationViolation>();

	private List<IndexPair> intersections = new ArrayList<IndexPair>();

	public void addHoleOutsideShell(GeometryIndex hole, GeometryIndex shell) {
		violations.add(new HoleOutsideShellViolation(hole, shell));
	}

	public void addNestedHoles(GeometryIndex hole, GeometryIndex nestedHole) {
		violations.add(new NestedHolesViolation(hole, nestedHole));
	}

	public void addNestedShells(GeometryIndex shell, GeometryIndex nestedShell) {
		violations.add(new NestedShellsViolation(shell, nestedShell));
	}

	public void addNonClosedRing(GeometryIndex ring) {
		violations.add(new RingNotClosedViolation(ring));
	}

	public void addTooFewPoints(GeometryIndex geometry) {
		violations.add(new TooFewPointsViolation(geometry));
	}

	public void addSelfIntersection(GeometryIndex edge1, GeometryIndex edge2) {
		if (!intersectionHandled(edge1, edge2)) {
			violations.add(new SelfIntersectionViolation(edge1, edge2));
		}
	}

	public void addRingSelfIntersection(GeometryIndex edge1, GeometryIndex edge2) {
		if (!intersectionHandled(edge1, edge2)) {
			violations.add(new RingSelfIntersectionViolation(edge1, edge2));
		}
	}

	public void clear() {
		violations.clear();
		intersections.clear();
	}

	/**
	 * Is the geometry valid ?
	 * 
	 * @return
	 */
	@Api
	public boolean isValid() {
		return violations.isEmpty();
	}

	/**
	 * Get the list of violations. Warning: this list will be cleared when the next validation is started !
	 * 
	 * @return
	 */
	@Api
	public List<ValidationViolation> getViolations() {
		return violations;
	}

	/**
	 * Get the validation state of the geometry. We take the state of the first violation encountered.
	 * 
	 * @return the invalid state or {@link GeometryValidationState.VALID} if there are no violations.
	 */
	@Api
	public GeometryValidationState getState() {
		if (violations.isEmpty()) {
			return GeometryValidationState.VALID;
		} else {
			return violations.get(0).getState();
		}
	}

	private boolean intersectionHandled(GeometryIndex index1, GeometryIndex index2) {
		IndexPair pair = new IndexPair(index1, index2);

		for (IndexPair i : intersections) {
			if (i.equalPair(pair)) {
				return true;
			}
		}
		intersections.add(pair);
		return false;
	}

	/**
	 * A pair of indices for checking equal pairs.
	 * 
	 * @author Jan De Moerloose
	 * 
	 */
	private class IndexPair {

		private GeometryIndex index1;

		private GeometryIndex index2;

		public IndexPair(GeometryIndex index1, GeometryIndex index2) {
			super();
			this.index1 = index1;
			this.index2 = index2;
		}

		public boolean equalPair(IndexPair other) {
			return (other.index1.equals(index1) && other.index2.equals(index2) || other.index1.equals(index2)
					&& other.index2.equals(index1));
		}

	}

}
