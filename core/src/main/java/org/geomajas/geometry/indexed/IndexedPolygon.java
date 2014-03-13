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

import java.util.ArrayList;
import java.util.List;

import org.geomajas.geometry.Geometry;

public class IndexedPolygon {

	private IndexedLinearRing shell;

	private List<IndexedLinearRing> holes = new ArrayList<IndexedLinearRing>();

	private int[] index;

	public IndexedPolygon(Geometry geometry) {
		this(geometry, new int[0]);
	}

	public IndexedPolygon(Geometry geometry, int[] index) {
		this.index = index;
		shell = new IndexedLinearRing(this, geometry.getGeometries()[0], copyAppend(index, 0));

		for (int i = 1; i < geometry.getGeometries().length; i++) {
			holes.add(new IndexedLinearRing(this, geometry.getGeometries()[i], copyAppend(index, i)));
		}
	}

	public IndexedLinearRing getShell() {
		return shell;
	}

	public List<IndexedLinearRing> getHoles() {
		return holes;
	}

	public IndexedLinearRing getRing(int index) {
		if (index >= 0 && index <= holes.size()) {
			return index == 0 ? shell : holes.get(index - 1);
		} else {
			throw new IllegalArgumentException("Invalid index");
		}
	}

	public int[] getIndex() {
		return index;
	}

	private int[] copyAppend(int[] orig, int i) {
		int[] copy = new int[orig.length + 1];
		System.arraycopy(orig, 0, copy, 0, orig.length);
		copy[orig.length] = i;
		return copy;
	}

}
