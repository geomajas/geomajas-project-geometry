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
import org.geomajas.geometry.service.WktException;
import org.geomajas.geometry.service.WktService;

/**
 * A multipolygon that knows its index in the geometry.
 * 
 * @author Jan De Moerloose
 * 
 */
public class IndexedMultiPolygon {

	private List<IndexedPolygon> polygons = new ArrayList<IndexedPolygon>();

	private Geometry geometry;

	public IndexedMultiPolygon(Geometry geometry) {
		this.geometry = geometry;
		for (int i = 0; i < geometry.getGeometries().length; i++) {
			polygons.add(new IndexedPolygon(geometry.getGeometries()[i], new int[] { i }));
		}
	}

	
	public List<IndexedPolygon> getPolygons() {
		return polygons;
	}

	public String toString() {
		try {
			return WktService.toWkt(geometry);
		} catch (WktException e) {
			return "Can't convert to wkt";
		}
	}
	
	
}
