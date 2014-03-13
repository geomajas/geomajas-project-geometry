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
package org.geomajas.geometry.indexed;

import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.geomajas.geometry.service.WktException;
import org.geomajas.geometry.service.WktService;

/**
 * A linestring that knows its index in the geometry.
 * 
 * @author Jan De Moerloose
 * 
 */
public class IndexedLineString {

	private IndexedMultiLineString multilinestring;

	private Geometry geometry;

	private int[] index;

	private Coordinate[] coordinates;

	public IndexedLineString(Geometry geometry) {
		this(null, geometry, new int[0]);
	}

	public IndexedLineString(IndexedMultiLineString multilinestring, Geometry geometry, int[] index) {
		this.multilinestring = multilinestring;
		this.geometry = geometry;
		this.index = index;
		this.coordinates = geometry.getCoordinates();
	}

	public boolean isTooFewPoints() {
		return coordinates != null && coordinates.length < 2;
	}
	
	public String toString() {
		try {
			return WktService.toWkt(geometry);
		} catch (WktException e) {
			return "Can't convert to wkt";
		}
	}

}
