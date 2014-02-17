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

/**
 * Indicates the valid/invalid state of a geometry and its cause (javadoc copied from JTS).
 * 
 * @author Jan De Moerloose
 * 
 */
public enum GeometryValidationState {
	/**
	 * Indicates a valid geometry.
	 */
	VALID(0, true),

	/**
	 * Indicates that a hole of a polygon lies partially or completely in the exterior of the shell.
	 */
	HOLE_OUTSIDE_SHELL(1, false),

	/**
	 * Indicates that a hole lies in the interior of another hole in the same polygon.
	 */
	NESTED_HOLES(2, false),

	/**
	 * Indicates that the interior of a polygon is disjoint (often caused by set of contiguous holes splitting the
	 * polygon into two parts).
	 */
	DISCONNECTED_INTERIOR(3, false),

	/**
	 * Indicates that two rings of a polygonal geometry intersect.
	 */
	SELF_INTERSECTION(4, false),

	/**
	 * Indicates that a ring self-intersects.
	 */
	RING_SELF_INTERSECTION(5, false),

	/**
	 * Indicates that a polygon component of a MultiPolygon lies inside another polygonal component.
	 */
	NESTED_SHELLS(6, false),

	/**
	 * Indicates that a polygonal geometry contains two rings which are identical.
	 */
	DUPLICATE_RINGS(7, false),

	/**
	 * Indicates that either:...
	 * <ul>
	 * <li>a LineString contains a single point
	 * <li>a LinearRing contains 2 or 3 points
	 * </ul>
	 */
	TOO_FEW_POINTS(8, false),

	/**
	 * Indicates that the <code>X</code> or <code>Y</code> ordinate of a Coordinate is not a valid numeric value (e.g.
	 * {@link Double#NaN} ).
	 */
	INVALID_COORDINATE(9, false),

	/**
	 * Indicates that a ring is not correctly closed (the first and the last coordinate are different).
	 */
	RING_NOT_CLOSED(10, false);

	private boolean valid;

	private int code;

	private GeometryValidationState(int code, boolean valid) {
		this.code = code;
		this.valid = valid;
	}

	/**
	 * Returns true when the geometry is valid.
	 * 
	 * @return true if valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Returns the unique code for the validation state.
	 * 
	 * @return
	 */
	public int getCode() {
		return code;
	}

}
