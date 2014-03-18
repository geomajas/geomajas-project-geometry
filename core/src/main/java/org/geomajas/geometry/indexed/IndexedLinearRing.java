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

import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.geomajas.geometry.service.MathService;
import org.geomajas.geometry.service.WktException;
import org.geomajas.geometry.service.WktService;

/**
 * A linear ring that knows its index in the geometry.
 * 
 * @author Jan De Moerloose
 * 
 */
public class IndexedLinearRing {

	private int[] index;

	private Coordinate[] coordinates;

	private Geometry geometry;

	private IndexedPolygon polygon;

	private List<IndexedEdge> edges;

	public IndexedLinearRing(Geometry geometry) {
		this(null, geometry, new int[0]);
	}

	public IndexedLinearRing(IndexedPolygon polygon, Geometry geometry, int[] index) {
		this.polygon = polygon;
		this.geometry = geometry;
		this.index = index;
		this.coordinates = geometry.getCoordinates();
	}

	public boolean isEmpty() {
		return coordinates == null;
	}

	public boolean isTooFewPoints() {
		return coordinates != null && coordinates.length < 4;
	}

	public boolean isClosed() {
		if (coordinates != null && coordinates.length > 1) {
			Coordinate first = coordinates[0];
			Coordinate last = coordinates[coordinates.length - 1];
			return first.equals(last);
		}
		return false;
	}

	public boolean isShell() {
		return index[index.length - 1] == 0;
	}

	public boolean isHole() {
		return !isShell();
	}

	public IndexedPolygon getPolygon() {
		return polygon;
	}

	public List<IndexedIntersection> getIntersections(IndexedEdge edge) {
		List<IndexedIntersection> result = new ArrayList<IndexedIntersection>();
		if (!isEmpty()) {
			// no self-intersections if we have only 1 segment !!!
			if (edge.getRing() == this) {
				if (coordinates.length <= 2 || (isClosed() && coordinates.length <= 3)) {
					return result;
				}
			}
			for (int i = 0; i < coordinates.length - 1; i++) {
				if (i != edge.getIndex() || edge.getRing() != this) {
					if (MathService.intersectsLineSegment(coordinates[i], coordinates[i + 1], edge.getStart(),
							edge.getEnd())) {
						result.add(new IndexedIntersection(getEdge(i), edge));
					}
				}
			}
		}
		return result;
	}

	public boolean containsCoordinate(Coordinate coordinate) {
		return MathService.isWithin(geometry, coordinate) || MathService.touches(geometry, coordinate);
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public boolean containsRing(IndexedLinearRing ring) {
		if (isEmpty() || ring.isEmpty()) {
			return false;
		}
		for (Coordinate c : ring.getGeometry().getCoordinates()) {
			if (!(MathService.isWithin(geometry, c) || MathService.touches(geometry, c))) {
				return false;
			}
		}
		return true;
	}

	public List<IndexedEdge> getEdges() {
		if (edges == null) {
			edges = new ArrayList<IndexedEdge>();
			Coordinate[] coords = geometry.getCoordinates();
			for (int i = 0; i < coords.length - 1; i++) {
				edges.add(new IndexedEdge(this, coords[i], coords[i + 1], i));
			}
			// make sure we can always return edges....(except for empty)
			if (coords.length == 1) {
				edges.add(new IndexedEdge(this, coords[0], coords[0], 0));
			}
		}
		return edges;
	}

	public IndexedEdge getEdge(int i) {
		if (i >= 0 && i < coordinates.length) {
			if (i == (coordinates.length - 1)) {
				return getEdges().get(coordinates.length - 2);
			} else {
				return getEdges().get(i);
			}
		} else {
			throw new IllegalArgumentException("Not a valid index");
		}
	}

	public int[] getIndex() {
		return index;
	}

	public String toString() {
		try {
			return WktService.toWkt(geometry);
		} catch (WktException e) {
			return "Can't convert to wkt";
		}
	}

}
