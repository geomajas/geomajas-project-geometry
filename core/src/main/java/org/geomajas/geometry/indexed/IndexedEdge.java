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

public class IndexedEdge {

	private Coordinate start;

	private Coordinate end;

	private int index;

	private IndexedLinearRing ring;

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

}
