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

/**
 * An edge that knows its index (index of first coordinate) in the geometry.
 * 
 * @author Jan De Moerloose
 * 
 */
public class IndexedEdge {

	private Coordinate start;

	private Coordinate end;

	private int index;

	private IndexedLinearRing ring;

	private Geometry geometry;

	public IndexedEdge(IndexedLinearRing ring, Coordinate start, Coordinate end, int index) {
		this.ring = ring;
		this.start = start;
		this.end = end;
		this.index = index;
	}

	public Coordinate getStart() {
		return start;
	}

	public Coordinate getEnd() {
		return end;
	}

	public IndexedLinearRing getRing() {
		return ring;
	}

	public int getIndex() {
		return index;
	}

	public String toString() {
		return "LINESTRING (" + getStart().getX() + " " + getStart().getY() + "," + getEnd().getX() + " "
				+ getEnd().getY() + ")";
	}

	public Geometry getGeometry() {
		if (geometry == null) {
			geometry = new Geometry(Geometry.LINE_STRING, 0, 5);
			geometry.setCoordinates(new Coordinate[] { start, end });
		}
		return geometry;
	}

}
