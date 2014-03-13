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

public class IndexedLinearRing {

	private int[] index;

	private Coordinate[] coordinates;

	private Geometry geometry;

	private IndexedPolygon polygon;

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
			for (int i = 0; i < coordinates.length - 1; i++) {
				if (i != edge.getIndex() || edge.getRing() != this) {
					if (MathService.intersectsLineSegment(coordinates[i], coordinates[i + 1], edge.getStart(),
							edge.getEnd())) {
						result.add(new IndexedIntersection(getNextEdge(i), edge));
					}
				}
			}
		}
		return result;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public boolean containsRing(IndexedLinearRing ring) {
		for (Coordinate c : ring.getGeometry().getCoordinates()) {
			if (!MathService.isWithin(geometry, c)) {
				return false;
			}
		}
		return true;
	}

	public List<IndexedEdge> getEdges() {
		List<IndexedEdge> edges = new ArrayList<IndexedEdge>();
		Coordinate[] coords = geometry.getCoordinates();
		for (int i = 0; i < coords.length - 1; i++) {
			edges.add(new IndexedEdge(this, coords[i], coords[i + 1], i));
		}
		return edges;
	}

	public IndexedEdge getNextEdge(int i) {
		if (i >= 0 && i < coordinates.length - 1) {
			return new IndexedEdge(this, coordinates[i], coordinates[i + 1], i);
		} else {
			throw new IllegalArgumentException("Not a valid index");
		}
	}

	public IndexedEdge getPreviousEdge(int i) {
		if (i >= 0 && i < coordinates.length - 1) {
			if (i == 0) {
				return new IndexedEdge(this, coordinates[coordinates.length - 2], coordinates[0],
						coordinates.length - 2);
			} else {
				return new IndexedEdge(this, coordinates[i - 1], coordinates[i], i - 1);
			}
		} else {
			throw new IllegalArgumentException("Not a valid index");
		}
	}

	public int[] getIndex() {
		return index;
	}

}
