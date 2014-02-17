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
		return validate(geometry, index).isValid();
	}

	/**
	 * Validates a geometry, focusing on changes at a specific sub-level of the geometry. The sublevel is indicated by
	 * passing an array of indexes. The array should uniquely determine a coordinate or subgeometry (linear ring) by
	 * recursing through the geometry tree. The only checks are on intersection and containment, we don't check on too
	 * few coordinates as we want to support incremental creation of polygons.
	 * 
	 * @param geometry The geometry to check.
	 * @param index an array of indexes, points to vertex, ring, polygon, etc...
	 * @return validation state.
	 * @since 1.2.0
	 */
	public static GeometryValidationState validate(Geometry geometry, int[] index) {
		if (Geometry.POINT.equals(geometry.getGeometryType())) {
			return GeometryValidationState.VALID;
		} else if (Geometry.LINE_STRING.equals(geometry.getGeometryType())) {
			return GeometryValidationState.VALID;
		} else if (Geometry.LINEAR_RING.equals(geometry.getGeometryType())) {
			return validateLinearRing(geometry, index);
		} else if (Geometry.POLYGON.equals(geometry.getGeometryType())) {
			return validatePolygon(geometry, index);
		} else if (Geometry.MULTI_POINT.equals(geometry.getGeometryType())) {
			return GeometryValidationState.VALID;
		} else if (Geometry.MULTI_LINE_STRING.equals(geometry.getGeometryType())) {
			return validateMultiLineString(geometry);
		} else if (Geometry.MULTI_POLYGON.equals(geometry.getGeometryType())) {
			return validateMultiPolygon(geometry, index);
		}
		throw new IllegalArgumentException("Unknown geometry type");
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
		if (Geometry.POINT.equals(geometry.getGeometryType())) {
			return GeometryValidationState.VALID;
		} else if (Geometry.LINE_STRING.equals(geometry.getGeometryType())) {
			if (isEmpty(geometry) || (geometry.getCoordinates().length != 1)) {
				return GeometryValidationState.VALID;
			} else {
				return GeometryValidationState.TOO_FEW_POINTS;
			}
		} else if (Geometry.LINEAR_RING.equals(geometry.getGeometryType())) {
			return validateLinearRing(geometry);
		} else if (Geometry.POLYGON.equals(geometry.getGeometryType())) {
			return validatePolygon(geometry);
		} else if (Geometry.MULTI_POINT.equals(geometry.getGeometryType())) {
			return GeometryValidationState.VALID;
		} else if (Geometry.MULTI_LINE_STRING.equals(geometry.getGeometryType())) {
			return validateMultiLineString(geometry);
		} else if (Geometry.MULTI_POLYGON.equals(geometry.getGeometryType())) {
			return validateMultiPolygon(geometry);
		}
		throw new IllegalArgumentException("Unknown geometry type");
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

	private static boolean isClosed(Geometry geometry) {
		if (geometry.getCoordinates() != null && geometry.getCoordinates().length > 1) {
			Coordinate first = geometry.getCoordinates()[0];
			Coordinate last = geometry.getCoordinates()[geometry.getCoordinates().length - 1];
			return first.equals(last);
		}
		return false;
	}

	private static GeometryValidationState validateLinearRing(Geometry geometry) {
		if (isEmpty(geometry)) {
			return GeometryValidationState.VALID;
		}
		if (!isClosed(geometry)) {
			return GeometryValidationState.RING_NOT_CLOSED;
		}
		Coordinate[] coordinates = geometry.getCoordinates();
		if (coordinates.length < 4) {
			return GeometryValidationState.TOO_FEW_POINTS;
		}
		for (int i = 0; i < coordinates.length - 1; i++) {
			for (int j = 0; j < coordinates.length - 1; j++) {
				if ((i != j)
						&& MathService.intersectsLineSegment(coordinates[i], coordinates[i + 1], coordinates[j],
								coordinates[j + 1])) {
					return GeometryValidationState.RING_SELF_INTERSECTION;
				}
			}
		}
		return GeometryValidationState.VALID;
	}

	/**
	 * Incremental validation, focusing on index and excluding {@link GeometryValidationState#TOO_FEW_POINTS}.
	 * 
	 * @param geometry
	 * @param index
	 * @return
	 */
	private static GeometryValidationState validateLinearRing(Geometry geometry, int[] index) {
		if (isEmpty(geometry)) {
			return GeometryValidationState.VALID;
		}
		if (!isClosed(geometry)) {
			return GeometryValidationState.RING_NOT_CLOSED;
		}
		Coordinate[] coordinates = geometry.getCoordinates();
		if (coordinates.length < 4) {
			return GeometryValidationState.VALID;
		}
		if (index.length == 1 && index[0] <= coordinates.length - 1) {
			// find the 2 segments connected to index[0]
			int i1 = 0;
			int i2 = 0;
			if (index[0] == (coordinates.length - 1) || index[0] == 0) {
				i1 = 0;
				i2 = coordinates.length - 2;
			} else {
				i1 = index[0];
				i2 = index[0] - 1;
			}
			// check 2 segments: i -> i + 1 and i - 1 -> i
			int[] is = new int[] { i1, i2 };
			for (int j = 0; j < coordinates.length - 1; j++) {
				for (int i : is) {
					if (i != j) {
						if (MathService.intersectsLineSegment(coordinates[i], coordinates[i + 1], coordinates[j],
								coordinates[j + 1])) {
							return GeometryValidationState.RING_SELF_INTERSECTION;
						}
					}
				}
			}
			return GeometryValidationState.VALID;
		} else {
			throw new IllegalArgumentException("Invalid index");
		}
	}

	private static GeometryValidationState validatePolygon(Geometry geometry) {
		if (isEmpty(geometry)) {
			return GeometryValidationState.VALID;
		}
		for (Geometry ring1 : geometry.getGeometries()) {
			GeometryValidationState s = validateLinearRing(ring1);
			if (!s.isValid()) {
				return s;
			}
			for (Geometry ring2 : geometry.getGeometries()) {
				if (!ring1.equals(ring2) && intersects(ring1, ring2)) {
					return GeometryValidationState.SELF_INTERSECTION;
				}
			}
		}
		return GeometryValidationState.VALID;
	}

	private static GeometryValidationState validatePolygon(Geometry geometry, int[] index) {
		if (isEmpty(geometry)) {
			return GeometryValidationState.VALID;
		} else if (index.length == 1) {
			// a new (supposedly valid) ring has been added
			int rIndex = index[0];
			if (rIndex < geometry.getGeometries().length) {
				Geometry ring = geometry.getGeometries()[rIndex];
				// our editing controller starts with an empty ring
				if (isEmpty(ring)) {
					return GeometryValidationState.VALID;
				}
				// test containment
				if (rIndex == 0) {
					// shell contains all holes
					for (Geometry ring2 : geometry.getGeometries()) {
						if (ring != ring2 && !ringContains(ring, ring2)) {
							return GeometryValidationState.HOLE_OUTSIDE_SHELL;
						}
					}
				} else {
					// hole contained by shell
					Geometry shell = geometry.getGeometries()[0];
					if (!ringContains(shell, ring)) {
						return GeometryValidationState.HOLE_OUTSIDE_SHELL;
					}
					// no intersection with other holes
					for (Geometry ring2 : geometry.getGeometries()) {
						if (shell != ring2 && ring != ring2 && intersects(ring, ring2)) {
							return GeometryValidationState.SELF_INTERSECTION;
						}
					}
				}
				return GeometryValidationState.VALID;
			} else {
				throw new IllegalArgumentException("Invalid index");
			}
		} else if (index.length == 2) {
			// a new line segment has been created (by addition or removal of a vertex)
			int rIndex = index[0];
			int cIndex = index[1];
			if (rIndex < geometry.getGeometries().length) {
				// test the ring
				Geometry ring = geometry.getGeometries()[rIndex];
				if (isEmpty(ring)) {
					return GeometryValidationState.VALID;
				}
				// if we come from an empty ring, test the containment
				if (ring.getCoordinates().length < 3 && rIndex > 0) {
					// hole contained by shell
					Geometry shell = geometry.getGeometries()[0];
					if (!ringContains(shell, ring)) {
						return GeometryValidationState.HOLE_OUTSIDE_SHELL;
					}
					// hole not contained by other holes
					for (Geometry ring2 : geometry.getGeometries()) {
						if (shell != ring2 && ring != ring2 && ringContains(ring2, ring)) {
							return GeometryValidationState.NESTED_HOLES;
						}
					}
				}
				GeometryValidationState v = validateLinearRing(ring, new int[] { cIndex });
				if (!v.isValid()) {
					return v;
				}
				// test intersection with other rings
				if (cIndex == ring.getCoordinates().length - 1) {
					cIndex--;
				}
				Geometry segment = toLineString(ring.getCoordinates()[cIndex], ring.getCoordinates()[cIndex + 1]);
				for (Geometry ring2 : geometry.getGeometries()) {
					if (!ring.equals(ring2) && intersects(segment, ring2)) {
						return GeometryValidationState.SELF_INTERSECTION;
					}
				}
				return GeometryValidationState.VALID;
			} else {
				throw new IllegalArgumentException("Invalid index");
			}
		} else {
			throw new IllegalArgumentException("Invalid index");
		}
	}

	private static boolean ringContains(Geometry ring1, Geometry ring2) {
		for (Coordinate c : ring2.getCoordinates()) {
			if (!MathService.isWithin(ring1, c)) {
				return false;
			}
		}
		return true;
	}

	private static GeometryValidationState validateMultiLineString(Geometry geometry) {
		if (isEmpty(geometry)) {
			return GeometryValidationState.VALID;
		}
		for (Geometry lineString : geometry.getGeometries()) {
			GeometryValidationState v = validate(lineString);
			if (!v.isValid()) {
				return v;
			}
		}
		return GeometryValidationState.VALID;
	}

	private static GeometryValidationState validateMultiPolygon(Geometry geometry) {
		if (!isEmpty(geometry)) {
			for (int i = 0; i < geometry.getGeometries().length; i++) {
				GeometryValidationState v = validate(geometry.getGeometries()[i]);
				if (!v.isValid()) {
					return v;
				}
				for (int j = i + 1; j < geometry.getGeometries().length; j++) {
					if (intersects(geometry.getGeometries()[i], geometry.getGeometries()[j])) {
						return GeometryValidationState.SELF_INTERSECTION;
					}
				}
			}
		}
		return GeometryValidationState.VALID;
	}

	private static GeometryValidationState validateMultiPolygon(Geometry geometry, int[] index) {
		if (isEmpty(geometry)) {
			return GeometryValidationState.VALID;
		} else if (index.length == 1) {
			// a new polygon has been added
			int pIndex = index[0];
			if (pIndex < geometry.getGeometries().length) {
				Geometry poly = geometry.getGeometries()[pIndex];
				// our editing controller starts with an empty ring
				if (isEmpty(poly)) {
					return GeometryValidationState.VALID;
				}
				// no intersection with other polygons
				for (Geometry poly2 : geometry.getGeometries()) {
					if (poly != poly2 && intersects(poly, poly2)) {
						return GeometryValidationState.SELF_INTERSECTION;
					}
				}
				// shell not contained by other shells
				for (Geometry poly2 : geometry.getGeometries()) {
					if (poly != poly2 && ringContains(poly2.getGeometries()[0], poly.getGeometries()[0])) {
						return GeometryValidationState.NESTED_HOLES;
					}
				}
				return GeometryValidationState.VALID;
			} else {
				throw new IllegalArgumentException("Invalid index");
			}
		} else if (index.length > 2) {
			// a new ring or vertex has been added/deleted
			int pIndex = index[0];
			if (pIndex < geometry.getGeometries().length) {
				// test the poly
				Geometry poly = geometry.getGeometries()[pIndex];
				int[] subIndex = new int[index.length - 1];
				for (int i = 0; i < subIndex.length; i++) {
					subIndex[i] = index[i + 1];
				}
				return validate(poly, subIndex);
			} else {
				throw new IllegalArgumentException("Invalid index");
			}
		} else {
			throw new IllegalArgumentException("Invalid index");
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