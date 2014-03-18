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
import org.geomajas.geometry.Bbox;
import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.geomajas.geometry.Matrix;
import org.geomajas.geometry.indexed.IndexedEdge;
import org.geomajas.geometry.indexed.IndexedIntersection;
import org.geomajas.geometry.indexed.IndexedLineString;
import org.geomajas.geometry.indexed.IndexedLinearRing;
import org.geomajas.geometry.indexed.IndexedMultiLineString;
import org.geomajas.geometry.indexed.IndexedMultiPolygon;
import org.geomajas.geometry.indexed.IndexedPolygon;

/**
 * Service definition for operations on {@link Geometry} objects. It's methods are loosely based upon the feature
 * specification for SQL.
 * 
 * @author Pieter De Graef
 * @since 1.0.0
 */
@Api(allMethods = true)
public final class GeometryService {

	/** Delta value used when comparing double values. If they are within this range, they are considered equal. */
	public static final Double DEFAULT_DOUBLE_DELTA = 1e-10;

	private static GeometryValidationContext validationContext = new GeometryValidationContext();

	private GeometryService() {
		// Final class should have a private no-argument constructor.
	}

	// ------------------------------------------------------------------------
	// Public static methods:
	// ------------------------------------------------------------------------

	/**
	 * Create a clone of the given geometry.
	 * 
	 * @param geometry The geometry to clone.
	 * @return The new clone geometry.
	 */
	public static Geometry clone(Geometry geometry) {
		if (geometry == null) {
			throw new IllegalArgumentException("Cannot clone null geometry.");
		}
		Geometry clone = new Geometry(geometry.getGeometryType(), geometry.getSrid(), geometry.getPrecision());
		if (geometry.getGeometries() != null) {
			Geometry[] clones = new Geometry[geometry.getGeometries().length];
			for (int i = 0; i < geometry.getGeometries().length; i++) {
				clones[i] = clone(geometry.getGeometries()[i]);
			}
			clone.setGeometries(clones);
		}
		if (geometry.getCoordinates() != null) {
			Coordinate[] clones = new Coordinate[geometry.getCoordinates().length];
			for (int i = 0; i < geometry.getCoordinates().length; i++) {
				clones[i] = new Coordinate(geometry.getCoordinates()[i]);
			}
			clone.setCoordinates(clones);
		}
		return clone;
	}

	/**
	 * Get the closest {@link Bbox} around the geometry.
	 * 
	 * @param geometry The geometry to calculate a bounding box for.
	 * @return The bounding box for the given geometry.
	 */
	public static Bbox getBounds(Geometry geometry) {
		if (geometry == null) {
			throw new IllegalArgumentException("Cannot get bounds for null geometry.");
		}
		if (isEmpty(geometry)) {
			return null;
		}
		Bbox bbox = null;
		if (geometry.getGeometries() != null) {
			bbox = getBounds(geometry.getGeometries()[0]);
			for (int i = 1; i < geometry.getGeometries().length; i++) {
				bbox = BboxService.union(bbox, getBounds(geometry.getGeometries()[i]));
			}
		}
		if (geometry.getCoordinates() != null) {
			double minX = Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;

			for (Coordinate coordinate : geometry.getCoordinates()) {
				if (coordinate.getX() < minX) {
					minX = coordinate.getX();
				}
				if (coordinate.getY() < minY) {
					minY = coordinate.getY();
				}
				if (coordinate.getX() > maxX) {
					maxX = coordinate.getX();
				}
				if (coordinate.getY() > maxY) {
					maxY = coordinate.getY();
				}
			}
			if (bbox == null) {
				bbox = new Bbox(minX, minY, maxX - minX, maxY - minY);
			} else {
				bbox = BboxService.union(bbox, new Bbox(minX, minY, maxX - minX, maxY - minY));
			}
		}
		return bbox;
	}

	/**
	 * Transform the given bounding box into a polygon geometry.
	 * 
	 * @param bounds The bounding box to transform.
	 * @return Returns the polygon equivalent of the given bounding box.
	 */
	public static Geometry toPolygon(Bbox bounds) {
		double minX = bounds.getX();
		double minY = bounds.getY();
		double maxX = bounds.getMaxX();
		double maxY = bounds.getMaxY();

		Geometry polygon = new Geometry(Geometry.POLYGON, 0, -1);
		Geometry linearRing = new Geometry(Geometry.LINEAR_RING, 0, -1);

		linearRing.setCoordinates(new Coordinate[] { new Coordinate(minX, minY), new Coordinate(maxX, minY),
				new Coordinate(maxX, maxY), new Coordinate(minX, maxY), new Coordinate(minX, minY) });
		polygon.setGeometries(new Geometry[] { linearRing });
		return polygon;
	}

	/**
	 * Create a new linestring based on the specified coordinates.
	 * 
	 * @param c1 first coordinate
	 * @param c2 second coordinate
	 * @return the linestring
	 * @since 1.2.0
	 */
	public static Geometry toLineString(Coordinate c1, Coordinate c2) {
		Geometry lineString = new Geometry(Geometry.LINE_STRING, 0, -1);
		lineString.setCoordinates(new Coordinate[] { (Coordinate) c1.clone(), (Coordinate) c2.clone() });
		return lineString;
	}

	/**
	 * Return the total number of coordinates within the geometry. This add up all coordinates within the
	 * sub-geometries.
	 * 
	 * @param geometry The geometry to calculate the total number of points for.
	 * @return The total number of coordinates within this geometry.
	 */
	public static int getNumPoints(Geometry geometry) {
		if (geometry == null) {
			throw new IllegalArgumentException("Cannot get total number of points for null geometry.");
		}
		int count = 0;
		if (geometry.getGeometries() != null) {
			for (Geometry child : geometry.getGeometries()) {
				count += getNumPoints(child);
			}
		}
		if (geometry.getCoordinates() != null) {
			count += geometry.getCoordinates().length;
		}
		return count;
	}

	/**
	 * This geometry is empty if there are no geometries/coordinates stored inside.
	 * 
	 * @param geometry The geometry to check.
	 * @return true or false.
	 */
	public static boolean isEmpty(Geometry geometry) {
		return (geometry.getCoordinates() == null || geometry.getCoordinates().length == 0)
				&& (geometry.getGeometries() == null || geometry.getGeometries().length == 0);
	}

	/**
	 * Basically this function checks if the geometry is self-intersecting or not.
	 * 
	 * @param geometry The geometry to check.
	 * @return True or false. True if there are no self-intersections in the geometry.
	 */
	public static boolean isSimple(Geometry geometry) {
		if (isEmpty(geometry)) {
			return true;
		}
		if (geometry.getGeometries() != null) {
			for (Geometry child : geometry.getGeometries()) {
				if (!isSimple(child)) {
					return false;
				}
				if (Geometry.MULTI_LINE_STRING.equals(geometry.getGeometryType())) {
					for (Geometry child2 : geometry.getGeometries()) {
						if (child != child2 && intersects(child, child2)) {
							return false;
						}
					}
				}
			}
		}
		if (geometry.getCoordinates() != null) {
			List<Coordinate> coords1 = new ArrayList<Coordinate>();
			List<Coordinate> coords2 = new ArrayList<Coordinate>();
			getAllCoordinates(geometry, coords1);
			getAllCoordinates(geometry, coords2);

			// Search for any intersection:
			if (coords1.size() > 1 && coords2.size() > 1) {
				for (int i = 0; i < coords2.size() - 1; i++) {
					for (int j = 0; j < coords1.size() - 1; j++) {
						if ((i != j)
								&& MathService.intersectsLineSegment(coords2.get(i), coords2.get(i + 1),
										coords1.get(j), coords1.get(j + 1))) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Validates a geometry, focusing on changes at a specific sub-level of the geometry. The sublevel is indicated by
	 * passing an array of indexes. The array should uniquely determine a coordinate or subgeometry (linear ring) by
	 * recursing through the geometry tree. The only checks are on intersection and containment, we don't check on too
	 * few coordinates as we want to support incremental creation of polygons.
	 * 
	 * @param geometry The geometry to check.
	 * @param index an array of indexes, points to vertex, ring, polygon, etc...
	 * @return True or false.
	 * @since 1.2.0
	 */
	public static boolean isValid(Geometry geometry, int[] index) {
		validate(geometry, index);
		return validationContext.isValid();
	}

	/**
	 * Validates a geometry, focusing on changes at a specific sub-level of the geometry. The sublevel is indicated by
	 * passing an array of indexes. The array should uniquely determine a coordinate or subgeometry (linear ring) by
	 * recursing through the geometry tree. The only checks are on intersection (for coordinates) and containment (for
	 * subgeometries), we don't check on too few coordinates as we want to support incremental creation of polygons.
	 * 
	 * @param geometry The geometry to check.
	 * @param index an array of indexes, points to vertex, ring, polygon, etc...
	 * @return validation state.
	 * @since 1.2.0
	 */
	public static GeometryValidationState validate(Geometry geometry, int[] index) {
		validationContext.clear();
		if (Geometry.LINEAR_RING.equals(geometry.getGeometryType())) {
			IndexedLinearRing ring = new IndexedLinearRing(geometry);
			if (index.length != 1) {
				throw new IllegalArgumentException("Invalid index");
			}
			// validate edges
			validateLinearRing(ring, ring.getEdge(index[0]));
		} else if (Geometry.POLYGON.equals(geometry.getGeometryType())) {
			IndexedPolygon polygon = new IndexedPolygon(geometry);
			if (index.length == 1) {
				// validate containment
				IndexedLinearRing ring = polygon.getRing(index[0]);
				validateContainment(polygon, ring);
			} else if (index.length == 2) {
				IndexedLinearRing ring = polygon.getRing(index[0]);
				// validate edges
				validatePolygon(polygon, ring.getEdge(index[1]));
			}
		} else if (Geometry.MULTI_POLYGON.equals(geometry.getGeometryType())) {
			IndexedMultiPolygon multipolygon = new IndexedMultiPolygon(geometry);
			if (index.length == 1) {
				// validate containment
				IndexedPolygon polygon = multipolygon.getPolygon(index[0]);
				validateContainment(multipolygon, polygon);
			} else if (index.length == 2) {
				// validate containment
				IndexedPolygon polygon = multipolygon.getPolygon(index[0]);
				IndexedLinearRing ring = polygon.getRing(index[1]);
				validateContainment(polygon, ring);
			} else if (index.length == 3) {
				// validate edges
				IndexedPolygon polygon = multipolygon.getPolygon(index[0]);
				IndexedLinearRing ring = polygon.getRing(index[1]);
				validateMultiPolygon(multipolygon, ring.getEdge(index[2]));
			}
		}
		return validationContext.getState();
	}

	/**
	 * Get the index of the (smallest) linear ring of the geometry that contains this coordinate.
	 * 
	 * @param geometry
	 * @param c
	 * @return the index (empty array indicates no containment)
	 * @since 1.2.1
	 */
	public static int[] getLinearRingIndex(Geometry geometry, Coordinate c) {
		if (Geometry.LINEAR_RING.equals(geometry.getGeometryType())) {
			IndexedLinearRing ring = new IndexedLinearRing(geometry);
			if (ring.containsCoordinate(c)) {
				return ring.getIndex();
			}
		} else if (Geometry.POLYGON.equals(geometry.getGeometryType())) {
			IndexedPolygon polygon = new IndexedPolygon(geometry);
			for (IndexedLinearRing hole : polygon.getHoles()) {
				if (hole.containsCoordinate(c)) {
					return hole.getIndex();
				}
			}
			if (polygon.getShell().containsCoordinate(c)) {
				return polygon.getShell().getIndex();
			}
		} else if (Geometry.MULTI_POLYGON.equals(geometry.getGeometryType())) {
			IndexedMultiPolygon multipolygon = new IndexedMultiPolygon(geometry);
			for (IndexedPolygon polygon : multipolygon.getPolygons()) {
				for (IndexedLinearRing hole : polygon.getHoles()) {
					if (hole.containsCoordinate(c)) {
						return hole.getIndex();
					}
				}
				if (polygon.getShell().containsCoordinate(c)) {
					return polygon.getShell().getIndex();
				}
			}
		}
		return new int[0];
	}

	/**
	 * Returns the current validation context. This is a mutable singleton that will be cleared after each validation !
	 * 
	 * @return the context
	 * @since 1.2.1
	 */
	public static GeometryValidationContext getValidationContext() {
		return validationContext;
	}

	/**
	 * Is the geometry a valid one? Different rules apply to different geometry types.
	 * 
	 * @param geometry The geometry to check.
	 * @return True or false.
	 */
	public static boolean isValid(Geometry geometry) {
		return validate(geometry).isValid();
	}

	/**
	 * Validate this geometry.
	 * 
	 * @param geometry The geometry to check.
	 * @return validation state.
	 * @since 1.2.0
	 */
	public static GeometryValidationState validate(Geometry geometry) {
		validationContext.clear();
		if (Geometry.LINE_STRING.equals(geometry.getGeometryType())) {
			IndexedLineString lineString = new IndexedLineString(geometry);
			validateLineString(lineString);
		} else if (Geometry.LINEAR_RING.equals(geometry.getGeometryType())) {
			IndexedLinearRing ring = new IndexedLinearRing(geometry);
			validateLinearRing(ring);
		} else if (Geometry.POLYGON.equals(geometry.getGeometryType())) {
			IndexedPolygon polygon = new IndexedPolygon(geometry);
			validatePolygon(polygon);
		} else if (Geometry.MULTI_LINE_STRING.equals(geometry.getGeometryType())) {
			IndexedMultiLineString multiLineString = new IndexedMultiLineString(geometry);
			validateMultiLineString(multiLineString);
		} else if (Geometry.MULTI_POLYGON.equals(geometry.getGeometryType())) {
			IndexedMultiPolygon multiPolygon = new IndexedMultiPolygon(geometry);
			validateMultiPolygon(multiPolygon);
		}
		return validationContext.getState();
	}

	/**
	 * Calculate whether or not two given geometries intersect each other.
	 * 
	 * @param one The first geometry to check for intersection with the second.
	 * @param two The second geometry to check for intersection with the first.
	 * @return Returns true or false.
	 */
	public static boolean intersects(Geometry one, Geometry two) {
		if (one == null || two == null || isEmpty(one) || isEmpty(two)) {
			return false;
		}
		if (Geometry.POINT.equals(one.getGeometryType())) {
			return intersectsPoint(one, two);
		} else if (Geometry.LINE_STRING.equals(one.getGeometryType())) {
			return intersectsLineString(one, two);
		} else if (Geometry.MULTI_POINT.equals(one.getGeometryType())
				|| Geometry.MULTI_LINE_STRING.equals(one.getGeometryType())) {
			return intersectsMultiSomething(one, two);
		}
		List<Coordinate> coords1 = new ArrayList<Coordinate>();
		List<Coordinate> coords2 = new ArrayList<Coordinate>();
		getAllCoordinates(one, coords1);
		getAllCoordinates(two, coords2);

		for (int i = 0; i < coords1.size() - 1; i++) {
			for (int j = 0; j < coords2.size() - 1; j++) {
				if (MathService.intersectsLineSegment(coords1.get(i), coords1.get(i + 1), coords2.get(j),
						coords2.get(j + 1))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Return the area of the geometry. If a polygon should contain a hole, the area of such a hole will be subtracted.
	 * 
	 * @param geometry The other geometry to calculate the area for.
	 * @return The total area within this geometry.
	 */
	public static double getArea(Geometry geometry) {
		return Math.abs(getSignedArea(geometry));
	}

	/**
	 * Return the length of the geometry. This adds up the length of all edges within the geometry.
	 * 
	 * @param geometry The other geometry to calculate the length for.
	 * @return The total length of all edges of the given geometry.
	 */
	public static double getLength(Geometry geometry) {
		double length = 0;
		if (geometry.getGeometries() != null) {
			for (Geometry child : geometry.getGeometries()) {
				length += getLength(child);
			}
		}
		if (geometry.getCoordinates() != null
				&& (Geometry.LINE_STRING.equals(geometry.getGeometryType()) || Geometry.LINEAR_RING.equals(geometry
						.getGeometryType()))) {
			for (int i = 0; i < geometry.getCoordinates().length - 1; i++) {
				double deltaX = geometry.getCoordinates()[i + 1].getX() - geometry.getCoordinates()[i].getX();
				double deltaY = geometry.getCoordinates()[i + 1].getY() - geometry.getCoordinates()[i].getY();
				length += Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			}
		}
		return length;
	}

	/**
	 * The centroid is also known as the "center of gravity" or the "center of mass".
	 * 
	 * @param geometry The other geometry to calculate the centroid for.
	 * 
	 * @return Return the center point.
	 */
	public static Coordinate getCentroid(Geometry geometry) {
		if (Geometry.POINT.equals(geometry.getGeometryType())) {
			return getCentroidPoint(geometry);
		} else if (Geometry.LINE_STRING.equals(geometry.getGeometryType())) {
			return getCentroidLineString(geometry);
		} else if (Geometry.LINEAR_RING.equals(geometry.getGeometryType())) {
			return getCentroidLinearRing(geometry);
		} else if (Geometry.POLYGON.equals(geometry.getGeometryType())) {
			return getCentroidPolygon(geometry);
		} else if (Geometry.MULTI_POINT.equals(geometry.getGeometryType())) {
			return getCentroidMultiPoint(geometry);
		} else if (Geometry.MULTI_LINE_STRING.equals(geometry.getGeometryType())) {
			return getCentroidMultiLineString(geometry);
		} else if (Geometry.MULTI_POLYGON.equals(geometry.getGeometryType())) {
			return getCentroidMultiPolygon(geometry);
		}
		return null;
	}

	/**
	 * Return the minimal distance between a coordinate and any vertex of a geometry.
	 * 
	 * @param geometry The other geometry to calculate the distance for.
	 * @param coordinate The coordinate for which to calculate the distance to the geometry.
	 * @return Return the minimal distance
	 */
	public static double getDistance(Geometry geometry, Coordinate coordinate) {
		double minDistance = Double.MAX_VALUE;
		if (coordinate != null && geometry != null) {
			if (geometry.getGeometries() != null) {
				for (Geometry child : geometry.getGeometries()) {
					double distance = getDistance(child, coordinate);
					if (distance < minDistance) {
						minDistance = distance;
					}
				}
			}
			if (geometry.getCoordinates() != null) {
				if (geometry.getCoordinates().length == 1) {
					double distance = MathService.distance(coordinate, geometry.getCoordinates()[0]);
					if (distance < minDistance) {
						minDistance = distance;
					}
				} else if (geometry.getCoordinates().length > 1) {
					for (int i = 0; i < geometry.getCoordinates().length - 1; i++) {
						double distance = MathService.distance(geometry.getCoordinates()[i],
								geometry.getCoordinates()[i + 1], coordinate);
						if (distance < minDistance) {
							minDistance = distance;
						}
					}
				}
			}
		}
		return minDistance;
	}

	/**
	 * Perform a matrix transformation of a geometry. The geometry passed will be left untouched.
	 * 
	 * @param geometry the geometry to transform
	 * @param matrix the matrix to use
	 * @return a transformed copy of the geometry
	 * @since 1.2.0
	 */
	public static Geometry transform(Geometry geometry, Matrix matrix) {
		Geometry copy = GeometryService.clone(geometry);
		transformInplace(copy, matrix);
		return copy;
	}

	// ------------------------------------------------------------------------
	// Private methods:
	// ------------------------------------------------------------------------

	private static void getAllCoordinates(Geometry geometry, List<Coordinate> coordinates) {
		if (geometry.getGeometries() != null) {
			for (Geometry child : geometry.getGeometries()) {
				getAllCoordinates(child, coordinates);
			}
		}
		if (geometry.getCoordinates() != null) {
			for (Coordinate coordinate : geometry.getCoordinates()) {
				coordinates.add(coordinate);
			}
		}
	}

	private static double getSignedArea(Geometry geometry) {
		double area = 0;
		if (geometry.getGeometries() != null) {
			if (Geometry.POLYGON.equals(geometry.getGeometryType())) {
				for (Geometry child : geometry.getGeometries()) {
					if (child == geometry.getGeometries()[0]) {
						area += getArea(child);
					} else {
						area -= getArea(child);
					}
				}
			} else {
				for (Geometry child : geometry.getGeometries()) {
					area += getArea(child);
				}
			}
		}
		if (geometry.getCoordinates() != null && Geometry.LINEAR_RING.equals(geometry.getGeometryType())) {
			double temp = 0;
			for (int i = 1; i < geometry.getCoordinates().length; i++) {
				double x1 = geometry.getCoordinates()[i - 1].getX();
				double y1 = geometry.getCoordinates()[i - 1].getY();
				double x2 = geometry.getCoordinates()[i].getX();
				double y2 = geometry.getCoordinates()[i].getY();
				temp += x1 * y2 - x2 * y1;
			}
			area += (temp / 2);
		}
		return area;
	}

	private static Coordinate getCentroidPoint(Geometry geometry) {
		if (geometry.getCoordinates() != null) {
			return geometry.getCoordinates()[0];
		}
		return null;
	}

	private static Coordinate getCentroidLineString(Geometry geometry) {
		if (geometry.getCoordinates() == null) {
			return null;
		}
		double sumX = 0;
		double sumY = 0;
		double totalLength = 0;
		for (int i = 0; i < geometry.getCoordinates().length - 1; i++) {
			double length = geometry.getCoordinates()[i].distance(geometry.getCoordinates()[i + 1]);
			totalLength += length;
			double midx = (geometry.getCoordinates()[i].getX() + geometry.getCoordinates()[i + 1].getX()) / 2;
			sumX += length * midx;
			double midy = (geometry.getCoordinates()[i].getY() + geometry.getCoordinates()[i + 1].getY()) / 2;
			sumY += length * midy;
		}
		return new Coordinate(sumX / totalLength, sumY / totalLength);
	}

	private static Coordinate getCentroidLinearRing(Geometry geometry) {
		if (geometry.getCoordinates() == null) {
			return null;
		}
		double area = getSignedArea(geometry);
		double x = 0;
		double y = 0;
		for (int i = 1; i < geometry.getCoordinates().length; i++) {
			double x1 = geometry.getCoordinates()[i - 1].getX();
			double y1 = geometry.getCoordinates()[i - 1].getY();
			double x2 = geometry.getCoordinates()[i].getX();
			double y2 = geometry.getCoordinates()[i].getY();
			x += (x1 + x2) * (x1 * y2 - x2 * y1);
			y += (y1 + y2) * (x1 * y2 - x2 * y1);
		}
		x = x / (6 * area);
		y = y / (6 * area);
		return new Coordinate(x, y);
	}

	private static Coordinate getCentroidPolygon(Geometry geometry) {
		if (geometry.getGeometries() == null) {
			return null;
		}
		return getCentroidLinearRing(geometry.getGeometries()[0]);
	}

	private static Coordinate getCentroidMultiPoint(Geometry geometry) {
		if (geometry.getGeometries() == null) {
			return null;
		}
		double sumX = 0;
		double sumY = 0;
		double numPoints = geometry.getGeometries().length;
		for (Geometry point : geometry.getGeometries()) {
			sumX += point.getCoordinates()[0].getX();
			sumY += point.getCoordinates()[0].getY();
		}
		return new Coordinate(sumX / numPoints, sumY / numPoints);
	}

	private static Coordinate getCentroidMultiLineString(Geometry geometry) {
		if (isEmpty(geometry)) {
			return null;
		}
		double sumX = 0;
		double sumY = 0;
		double totalLength = 0;
		Coordinate[] coordinates = new Coordinate[geometry.getGeometries().length];
		for (int i = 0; i < geometry.getGeometries().length; i++) {
			coordinates[i] = getCentroidLineString(geometry.getGeometries()[i]);
		}

		if (1 == coordinates.length) {
			return coordinates[0];
		}

		for (int i = 0; i < coordinates.length - 1; i++) {
			double length = coordinates[i].distance(coordinates[i + 1]);
			totalLength += length;
			double midx = (coordinates[i].getX() + coordinates[i + 1].getX()) / 2;
			sumX += length * midx;
			double midy = (coordinates[i].getY() + coordinates[i + 1].getY()) / 2;
			sumY += length * midy;
		}
		return new Coordinate(sumX / totalLength, sumY / totalLength);
	}

	private static Coordinate getCentroidMultiPolygon(Geometry geometry) {
		if (geometry.getGeometries() == null) {
			return null;
		}
		double sumX = 0;
		double sumY = 0;
		double totalLength = 0;
		Coordinate[] coordinates = new Coordinate[geometry.getGeometries().length];
		for (int i = 0; i < geometry.getGeometries().length; i++) {
			coordinates[i] = getCentroidPolygon(geometry.getGeometries()[i]);
		}
		if (coordinates.length == 1) { // Fix for GEOM-14.
			return coordinates[0];
		}
		for (int i = 0; i < coordinates.length - 1; i++) {
			double length = coordinates[i].distance(coordinates[i + 1]);
			totalLength += length;
			double midx = (coordinates[i].getX() + coordinates[i + 1].getX()) / 2;
			sumX += length * midx;
			double midy = (coordinates[i].getY() + coordinates[i + 1].getY()) / 2;
			sumY += length * midy;
		}
		return new Coordinate(sumX / totalLength, sumY / totalLength);
	}

	private static void validateMultiPolygon(IndexedMultiPolygon multipolygon, IndexedEdge edge) {
		for (IndexedPolygon polygon : multipolygon.getPolygons()) {
			validatePolygon(polygon, edge);
		}
	}

	private static void validateMultiPolygon(IndexedMultiPolygon multiPolygon) {
		// validate topology (nesting)
		validateContainment(multiPolygon);
		// validate polygons
		for (IndexedPolygon polygon : multiPolygon.getPolygons()) {
			validatePolygon(polygon);
		}
		// validate edges (intersection)
		for (IndexedPolygon polygon : multiPolygon.getPolygons()) {
			for (IndexedPolygon polygon2 : multiPolygon.getPolygons()) {
				for (IndexedEdge edge : polygon2.getShell().getEdges()) {
					validatePolygon(polygon, edge);
				}
				for (IndexedLinearRing ring : polygon2.getHoles()) {
					for (IndexedEdge edge : ring.getEdges()) {
						validatePolygon(polygon, edge);
					}
				}
			}
		}
	}

	private static void validateContainment(IndexedMultiPolygon multiPolygon, IndexedPolygon polygon) {
		// no nested shells
		for (IndexedPolygon p2 : multiPolygon.getPolygons()) {
			if (p2 != polygon) {
				if (polygon.getShell().containsRing(p2.getShell())) {
					validationContext.addNestedShells(polygon.getShell(), p2.getShell());
				} else if (p2.getShell().containsRing(polygon.getShell())) {
					validationContext.addNestedShells(p2.getShell(), polygon.getShell());
				}
			}
		}
		// validate containment for polygons
		validateContainment(polygon);
	}

	private static void validateContainment(IndexedMultiPolygon multiPolygon) {
		for (IndexedPolygon p : multiPolygon.getPolygons()) {
			// no nested shells
			for (IndexedPolygon p2 : multiPolygon.getPolygons()) {
				if (p2 != p) {
					if (p.getShell().containsRing(p2.getShell())) {
						validationContext.addNestedShells(p.getShell(), p2.getShell());
					}
				}
			}
			// validate containment for polygons
			validateContainment(p);
		}
	}

	private static void validatePolygon(IndexedPolygon polygon) {
		// validate rings
		validateLinearRing(polygon.getShell());
		for (IndexedLinearRing ring : polygon.getHoles()) {
			validateLinearRing(ring);
		}
		// validate containment
		validateContainment(polygon);
		// validate edges (intersection)
		for (IndexedEdge edge : polygon.getShell().getEdges()) {
			validatePolygon(polygon, edge);
		}
		for (IndexedLinearRing ring : polygon.getHoles()) {
			for (IndexedEdge edge : ring.getEdges()) {
				validatePolygon(polygon, edge);
			}
		}
	}

	private static void validateContainment(IndexedPolygon polygon, IndexedLinearRing ring) {
		if (ring.isHole()) {
			// hole in shell
			if (!polygon.getShell().containsRing(ring) && !ring.isEmpty()) {
				validationContext.addHoleOutsideShell(ring, polygon.getShell());
			}
			// no nested holes
			for (IndexedLinearRing hole2 : polygon.getHoles()) {
				if (hole2 != ring) {
					if (ring.containsRing(hole2)) {
						validationContext.addNestedHoles(ring, hole2);
					} else if (hole2.containsRing(ring)) {
						validationContext.addNestedHoles(hole2, ring);
					}
				}
			}
		} else if (ring.isShell()) {
			// holes in shell
			for (IndexedLinearRing hole : polygon.getHoles()) {
				if (!ring.containsRing(hole)) {
					validationContext.addHoleOutsideShell(hole, ring);
				}
			}
		}
	}

	private static void validateContainment(IndexedPolygon polygon) {
		for (IndexedLinearRing hole : polygon.getHoles()) {
			// holes in shell
			if (!polygon.getShell().containsRing(hole)) {
				validationContext.addHoleOutsideShell(hole, polygon.getShell());
			}
			// no nested holes
			for (IndexedLinearRing hole2 : polygon.getHoles()) {
				if (hole2 != hole && hole.containsRing(hole2)) {
					validationContext.addNestedHoles(hole, hole2);
				}
			}
		}
	}

	private static void validatePolygon(IndexedPolygon polygon, IndexedEdge edge) {
		validateLinearRing(polygon.getShell(), edge);
		for (IndexedLinearRing hole : polygon.getHoles()) {
			validateLinearRing(hole, edge);
		}
	}

	private static void validateLinearRing(IndexedLinearRing ring) {
		if (!ring.isClosed()) {
			validationContext.addNonClosedRing(ring);
		}
		if (ring.isTooFewPoints()) {
			validationContext.addTooFewPoints(ring);
		}
		for (IndexedEdge edge : ring.getEdges()) {
			validateLinearRing(ring, edge);
		}
	}

	private static void validateLinearRing(IndexedLinearRing ring, IndexedEdge edge) {
		List<IndexedIntersection> intersections = ring.getIntersections(edge);
		for (IndexedIntersection intersection : intersections) {
			if (ring == edge.getRing()) {
				validationContext.addRingSelfIntersection(intersection);
			} else {
				validationContext.addSelfIntersection(intersection);
			}
		}
	}

	private static void validateMultiLineString(IndexedMultiLineString multiLineString) {
		for (IndexedLineString lineString : multiLineString.getLineStrings()) {
			validateLineString(lineString);
		}
	}

	private static void validateLineString(IndexedLineString lineString) {
		if (lineString.isTooFewPoints()) {
			validationContext.addTooFewPoints(lineString);
		}
	}

	// We assume neither is null or empty.
	private static boolean intersectsPoint(Geometry point, Geometry geometry) {
		if (geometry.getGeometries() != null) {
			for (Geometry child : geometry.getGeometries()) {
				if (intersectsPoint(point, child)) {
					return true;
				}
			}
		}
		if (geometry.getCoordinates() != null) {
			Coordinate coordinate = point.getCoordinates()[0];
			if (geometry.getCoordinates().length == 1) {
				return coordinate.equals(geometry.getCoordinates()[0]);
			} else {
				for (int i = 0; i < geometry.getCoordinates().length - 1; i++) {
					double distance = MathService.distance(geometry.getCoordinates()[i],
							geometry.getCoordinates()[i + 1], coordinate);
					if (distance < DEFAULT_DOUBLE_DELTA) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean intersectsLineString(Geometry lineString, Geometry geometry) {
		if (geometry.getGeometries() != null) {
			for (Geometry child : geometry.getGeometries()) {
				if (intersectsLineString(lineString, child)) {
					return true;
				}
			}
		}
		if (geometry.getCoordinates() != null) {
			if (lineString.getCoordinates().length > 1 && geometry.getCoordinates().length > 1) {
				for (int i = 0; i < lineString.getCoordinates().length - 1; i++) {
					for (int j = 0; j < geometry.getCoordinates().length - 1; j++) {
						if (MathService.intersectsLineSegment(lineString.getCoordinates()[i],
								lineString.getCoordinates()[i + 1], geometry.getCoordinates()[j],
								geometry.getCoordinates()[j + 1])) {
							return true;
						}
					}
					if (MathService.touches(geometry, lineString.getCoordinates()[i])) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean intersectsMultiSomething(Geometry multiSomething, Geometry geometry) {
		if (isEmpty(multiSomething)) {
			return false;
		}
		if (multiSomething.getGeometries() != null) {
			for (Geometry child : multiSomething.getGeometries()) {
				if (intersects(child, geometry)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void transformInplace(Geometry geometry, Matrix matrix) {
		if (geometry.getGeometries() != null) {
			for (Geometry g : geometry.getGeometries()) {
				transformInplace(g, matrix);
			}
		} else if (geometry.getCoordinates() != null) {
			for (Coordinate c : geometry.getCoordinates()) {
				double x = c.getX() * matrix.getXx() + c.getY() * matrix.getXy() + matrix.getDx();
				double y = c.getX() * matrix.getYx() + c.getY() * matrix.getYy() + matrix.getDy();
				c.setX(x);
				c.setY(y);
			}
		}
	}

}