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

import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.geomajas.geometry.service.IndexedGeometryHelper.IndexedEdge;
import org.geomajas.geometry.service.IndexedGeometryHelper.IndexedLinearRing;
import org.geomajas.geometry.service.IndexedGeometryHelper.IndexedPolygon;

/**
 * Helper class that defines indexed geometries for internal use.
 * 
 * @author Jan De Moerloose
 * 
 */
class IndexedGeometryHelper {

	private GeometryIndexService indexService = new GeometryIndexService();

	IndexedLinearRing createLinearRing(Geometry geometry) {
		return new IndexedLinearRing(geometry);
	}

	IndexedPolygon createPolygon(Geometry geometry) {
		return new IndexedPolygon(geometry);
	}

	IndexedMultiPolygon createMultiPolygon(Geometry geometry) {
		return new IndexedMultiPolygon(geometry);
	}

	IndexedLineString createLineString(Geometry geometry) {
		return new IndexedLineString(geometry);
	}

	IndexedMultiLineString createMultiLineString(Geometry geometry) {
		return new IndexedMultiLineString(geometry);
	}

	/**
	 * An edge that knows its index (index of first coordinate) in the geometry.
	 * 
	 * @author Jan De Moerloose
	 * 
	 */
	class IndexedEdge {

		private Coordinate start;

		private Coordinate end;

		private GeometryIndex index;

		private IndexedLinearRing ring;

		private Geometry geometry;

		public IndexedEdge(IndexedLinearRing ring, Coordinate start, Coordinate end, GeometryIndex index) {
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

		public GeometryIndex getIndex() {
			return index;
		}

		public String toString() {
			return "LINESTRING (" + getStart().getX() + " " + getStart().getY() + "," + getEnd().getX() + " "
					+ getEnd().getY() + ")";
		}

		public Geometry getGeometry() {
			if (geometry == null) {
				geometry = new Geometry(Geometry.LINE_STRING, ring.getGeometry().getSrid(), ring.getGeometry()
						.getPrecision());
				geometry.setCoordinates(new Coordinate[] { start, end });
			}
			return geometry;
		}

	}

	/**
	 * An intersection of 2 edges.
	 * 
	 * @author Jan De Moerloose
	 * 
	 */
	class IndexedIntersection {

		private IndexedEdge edge1;

		private IndexedEdge edge2;

		public IndexedIntersection(IndexedEdge edge1, IndexedEdge edge2) {
			this.edge1 = edge1;
			this.edge2 = edge2;
		}

		public IndexedEdge getEdge1() {
			return edge1;
		}

		public IndexedEdge getEdge2() {
			return edge2;
		}

		public String toString() {
			return edge1 + "X" + edge2;
		}

		public boolean equalEdges(IndexedIntersection other) {
			return (other.edge1 == edge1 && other.edge2 == edge2) || (other.edge1 == edge2 && other.edge2 == edge1);
		}

	}

	/**
	 * A linear ring that knows its index in the geometry.
	 * 
	 * @author Jan De Moerloose
	 * 
	 */
	class IndexedLinearRing {

		private GeometryIndex index;

		private Coordinate[] coordinates;

		private Geometry geometry;

		private IndexedPolygon polygon;

		private List<IndexedEdge> edges;

		public IndexedLinearRing(Geometry geometry) {
			this(null, geometry, null);
		}

		public IndexedLinearRing(IndexedPolygon polygon, Geometry geometry, GeometryIndex index) {
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
			return index != null && indexService.isShell(index);
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
					if (i != indexService.getValue(edge.getIndex()) || edge.getRing() != this) {
						if (MathService.intersectsLineSegment(coordinates[i], coordinates[i + 1], edge.getStart(),
								edge.getEnd())) {
							try {
								result.add(new IndexedIntersection(getEdge(indexService.create(
										GeometryIndexType.TYPE_EDGE, i)), edge));
							} catch (GeometryIndexNotFoundException e) {
								// ignore, can't happen
							}
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
					edges.add(new IndexedEdge(this, coords[i], coords[i + 1], indexService.addChildren(index,
							GeometryIndexType.TYPE_EDGE, i)));
				}
				// make sure we can always return edges....(except for empty)
				if (coords.length == 1) {
					edges.add(new IndexedEdge(this, coords[0], coords[0], indexService.addChildren(index,
							GeometryIndexType.TYPE_EDGE, 0)));
				}
			}
			return edges;
		}

		public IndexedEdge getEdge(GeometryIndex index) throws GeometryIndexNotFoundException {
			if (index.getType() == GeometryIndexType.TYPE_EDGE) {
				int i = index.getValue();
				if (i >= 0 && i < coordinates.length) {
					if (i == (coordinates.length - 1)) {
						return getEdges().get(coordinates.length - 2);
					} else {
						return getEdges().get(i);
					}
				} else {
					throw new GeometryIndexNotFoundException("Index not in range " + i + "," +
							(coordinates.length - 1));
				}
			} else {
				throw new GeometryIndexNotFoundException("Index not of type EDGE");
			}
		}

		public GeometryIndex getIndex() {
			return index;
		}

		public String toString() {
			try {
				return WktService.toWkt(geometry);
			} catch (WktException e) {
				return "Can't convert to wkt";
			}
		}

		public List<IndexedEdge> getAdjacentEdges(GeometryIndex index) throws GeometryIndexNotFoundException {
			List<GeometryIndex> indices = indexService.getAdjacentEdges(geometry, index);
			List<IndexedEdge> edges = new ArrayList<IndexedEdge>();
			for (GeometryIndex g : indices) {
				edges.add(getEdge(g));
			}
			return edges;
		}

	}

	/**
	 * A linestring that knows its index in the geometry.
	 * 
	 * @author Jan De Moerloose
	 * 
	 */
	class IndexedLineString {

		private IndexedMultiLineString multilinestring;

		private Geometry geometry;

		private GeometryIndex index;

		private Coordinate[] coordinates;

		public IndexedLineString(Geometry geometry) {
			this(null, geometry, null);
		}

		public IndexedLineString(IndexedMultiLineString multilinestring, Geometry geometry, GeometryIndex index) {
			this.multilinestring = multilinestring;
			this.geometry = geometry;
			this.index = index;
			this.coordinates = geometry.getCoordinates();
		}

		public IndexedMultiLineString getMultilinestring() {
			return multilinestring;
		}

		public Geometry getGeometry() {
			return geometry;
		}

		public GeometryIndex getIndex() {
			return index;
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

	/**
	 * A multilinestring that knows its index in the geometry.
	 * 
	 * @author Jan De Moerloose
	 * 
	 */
	class IndexedMultiLineString {

		private List<IndexedLineString> linestrings = new ArrayList<IndexedLineString>();

		private Geometry geometry;

		public IndexedMultiLineString(Geometry geometry) {
			this.geometry = geometry;
			for (int i = 0; i < geometry.getGeometries().length; i++) {
				linestrings.add(new IndexedLineString(this, geometry.getGeometries()[i], indexService.create(
						GeometryIndexType.TYPE_GEOMETRY, i)));
			}
		}

		public List<IndexedLineString> getLineStrings() {
			return linestrings;
		}

		public String toString() {
			try {
				return WktService.toWkt(geometry);
			} catch (WktException e) {
				return "Can't convert to wkt";
			}
		}

	}

	/**
	 * A polygon that knows its index in the geometry.
	 * 
	 * @author Jan De Moerloose
	 * 
	 */
	class IndexedPolygon {

		private IndexedLinearRing shell;

		private List<IndexedLinearRing> holes = new ArrayList<IndexedLinearRing>();

		private GeometryIndex index;

		private Geometry geometry;

		public IndexedPolygon(Geometry geometry) {
			this(geometry, null);
		}

		public IndexedPolygon(Geometry geometry, GeometryIndex index) {
			this.geometry = geometry;
			this.index = index;
			if (geometry.getGeometries() != null) {
				shell = new IndexedLinearRing(this, geometry.getGeometries()[0], indexService.addChildren(index,
						GeometryIndexType.TYPE_GEOMETRY, 0));

				for (int i = 1; i < geometry.getGeometries().length; i++) {
					holes.add(new IndexedLinearRing(this, geometry.getGeometries()[i], indexService.addChildren(index,
							GeometryIndexType.TYPE_GEOMETRY, i)));
				}
			}
		}

		public IndexedLinearRing getShell() {
			return shell;
		}

		public List<IndexedLinearRing> getHoles() {
			return holes;
		}

		public IndexedLinearRing getRing(GeometryIndex index) throws GeometryIndexNotFoundException {
			if (index.getType() == GeometryIndexType.TYPE_GEOMETRY) {
				int i = index.getValue();
				if (i >= 0 && i <= holes.size()) {
					return i == 0 ? shell : holes.get(i - 1);
				} else {
					throw new GeometryIndexNotFoundException("Index not in range " + i + "," + holes.size());
				}
			} else {
				throw new GeometryIndexNotFoundException("Index not of type GEOMETRY");
			}
		}

		public GeometryIndex getIndex() {
			return index;
		}

		public boolean isEmpty() {
			return geometry.getGeometries() == null;
		}

		public String toString() {
			try {
				return WktService.toWkt(geometry);
			} catch (WktException e) {
				return "Can't convert to wkt";
			}
		}

		public IndexedEdge getEdge(GeometryIndex index) throws GeometryIndexNotFoundException {
			IndexedLinearRing ring = getRing(indexService.getParent(index));
			return ring.getEdge(index.getChild());
		}

		public List<IndexedEdge> getAdjacentEdges(GeometryIndex index) throws GeometryIndexNotFoundException {
			IndexedLinearRing ring = getRing(indexService.getParent(index));
			return ring.getAdjacentEdges(index);
		}

	}

	/**
	 * A multipolygon that knows its index in the geometry.
	 * 
	 * @author Jan De Moerloose
	 * 
	 */
	class IndexedMultiPolygon {

		private List<IndexedPolygon> polygons = new ArrayList<IndexedPolygon>();

		private Geometry geometry;

		public IndexedMultiPolygon(Geometry geometry) {
			this.geometry = geometry;
			if (geometry.getGeometries() != null) {
				for (int i = 0; i < geometry.getGeometries().length; i++) {
					polygons.add(new IndexedPolygon(geometry.getGeometries()[i], indexService.create(
							GeometryIndexType.TYPE_GEOMETRY, i)));
				}
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

		public IndexedPolygon getPolygon(GeometryIndex index) throws GeometryIndexNotFoundException {
			if (index.getType() == GeometryIndexType.TYPE_GEOMETRY) {
				int i = index.getValue();
				if (i >= 0 && i < polygons.size()) {
					return polygons.get(i);
				} else {
					throw new GeometryIndexNotFoundException("Index not in range " + i + "," + (polygons.size() - 1));
				}
			} else {
				throw new GeometryIndexNotFoundException("Index not of type GEOMETRY");
			}
		}

		public IndexedEdge getEdge(GeometryIndex index) throws GeometryIndexNotFoundException {
			return getPolygon(index).getEdge(index);
		}

		public List<IndexedEdge> getAdjacentEdges(GeometryIndex index) throws GeometryIndexNotFoundException {
			return getPolygon(index).getAdjacentEdges(index);
		}

	}
}