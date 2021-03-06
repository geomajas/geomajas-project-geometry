/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2015 Geosparc nv, http://www.geosparc.com/, Belgium.
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
import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;

/**
 * General service for Well-Known-Text to and from geometry conversion. This service also supports the EWKT format.
 * 
 * @author Pieter De Graef
 * @since 1.0.0
 */
@Api(allMethods = true)
public final class WktService {

	private static final String ERR_MSG = "Error while parsing WKT: ";

	private WktService() {
		// Final class should have a private no-argument constructor.
	}

	/**
	 * Parses a given WKT (or EWKT) string into a geometry object.
	 * 
	 * @param wkt
	 *            The Well Known Text to parse.
	 * @return Returns The resulting geometry from parsing the WKT.
	 * @throws WktException
	 *             In case something went wrong while parsing. The WKT format must be respected.
	 */
	public static Geometry toGeometry(String wkt) throws WktException {
		if (wkt != null) {
			// First we detect if the string is WKT or EWKT:
			String[] parts = wkt.split(";");
			if (parts.length == 2) { // We assume it is EWKT:
				Geometry geometry = parseWkt(parts[1]);
				recursiveSetSrid(geometry, parseSrid(parts[0]));
				return geometry;
			}
			return parseWkt(wkt);
		}
		throw new WktException(ERR_MSG + "illegal argument; no WKT");
	}

	/**
	 * Format a given geometry to Well Known Text.
	 * 
	 * @param geometry
	 *            The geometry to format.
	 * @return Returns the WKT string.
	 * @throws WktException
	 *             In case something went wrong while formatting.
	 */
	public static String toWkt(Geometry geometry) throws WktException {
		if (Geometry.POINT.equals(geometry.getGeometryType())) {
			return toWktPoint(geometry);
		} else if (Geometry.LINE_STRING.equals(geometry.getGeometryType())
				|| Geometry.LINEAR_RING.equals(geometry.getGeometryType())) {
			return toWktLineString(geometry);
		} else if (Geometry.POLYGON.equals(geometry.getGeometryType())) {
			return toWktPolygon(geometry);
		} else if (Geometry.MULTI_POINT.equals(geometry.getGeometryType())) {
			return toWktMultiPoint(geometry);
		} else if (Geometry.MULTI_LINE_STRING.equals(geometry.getGeometryType())) {
			return toWktMultiLineString(geometry);
		} else if (Geometry.MULTI_POLYGON.equals(geometry.getGeometryType())) {
			return toWktMultiPolygon(geometry);
		}
		return "";
	}

	/**
	 * Format a given geometry to EWKT. EWKT is an extension of WKT that adds the SRID into the returned string. The
	 * result is something like: "SRID=4326;POINT(-44.3 60.1)"
	 * 
	 * @param geometry
	 *            The geometry to format.
	 * @return Returns the EWKT string.
	 * @throws WktException
	 *             In case something went wrong while formatting.
	 * @since 1.1.0
	 */
	public static String toEwkt(Geometry geometry) throws WktException {
		return "SRID=" + geometry.getSrid() + ";" + WktService.toWkt(geometry);
	}

	// ------------------------------------------------------------------------
	// Private parsing methods:
	// ------------------------------------------------------------------------

	private static void recursiveSetSrid(Geometry geometry, int srid) {
		if (geometry != null) {
			geometry.setSrid(srid);
		}
		if (geometry.getGeometries() != null) {
			for (Geometry childGeometry : geometry.getGeometries()) {
				recursiveSetSrid(childGeometry, srid);
			}
		}
	}

	/** Get the SRID from a string like "SRDI=4326". Used in parsing EWKT. */
	private static int parseSrid(String ewktPart) {
		if (ewktPart != null && !"".equals(ewktPart)) {
			String[] parts = ewktPart.split("=");
			if (parts.length == 2) {
				try {
					return Integer.parseInt(parts[1]);
				} catch (Exception e) {
				}
			}
		}
		return 0;
	}

	/** Parse a WKT string. No EWKT here! */
	private static Geometry parseWkt(String wkt) throws WktException {
		if (wkt != null) {
			int i1 = wkt.indexOf('(');
			int i2 = wkt.indexOf(' ');
			// allow both '(' and ' ('
			int i = Math.min(i1, i2);
			if (i < 0) {
				i = (i1 > 0 ? i1 : i2);
			}
			String type = null;
			if (i >= 0) {
				type = typeWktToGeom(wkt.substring(0, i).trim());
			}
			if (type == null) {
				throw new WktException(ERR_MSG + "type of geometry not supported");
			}
			if (wkt.indexOf("EMPTY") >= 0) {
				return new Geometry(type, 0, 0);
			}
			Geometry geometry = new Geometry(type, 0, 0);
			String result = parse(wkt.substring(wkt.indexOf('(')), geometry);
			if (result.length() != 0) {
				throw new WktException(ERR_MSG + "unexpected ending \"" + result + "\"");
			}
			return geometry;
		}
		throw new WktException(ERR_MSG + "illegal argument; no WKT");
	}

	private static String parse(String wkt, Geometry geometry) throws WktException {
		String childType = getChildType(geometry.getGeometryType());
		String scopeWkt = wkt.substring(1);
		if (scopeWkt.charAt(0) == '(') {
			// Type has sub-geometries, so there must be a child type:
			if (childType == null) {
				throw new WktException(ERR_MSG + "no child geometry type could be found for type "
						+ geometry.getGeometryType());
			}

			List<Geometry> geometries = new ArrayList<Geometry>();
			while (scopeWkt.charAt(0) == '(') {
				Geometry childGeometry = new Geometry(childType, 0, 0);
				geometries.add(childGeometry);
				scopeWkt = parse(scopeWkt, childGeometry);
				if (scopeWkt.charAt(0) == ',') {
					scopeWkt = scopeWkt.substring(1).trim();
				}
			}
			geometry.setGeometries(geometries.toArray(new Geometry[geometries.size()]));
			scopeWkt = scopeWkt.substring(1);
		} else {
			// Type has no sub-geometries, but should contain coordinates directly:
			if (childType != null) {
				// No Polygons, MultiPoints, MultiLineStrings or MultiPolygons:
				throw new WktException(ERR_MSG + "Geometry of type \"" + geometry.getGeometryType()
						+ "\" has no direct coordinates.");
			}
			// Format coordinates:
			String[] coordStrings = scopeWkt.substring(0, scopeWkt.indexOf(')')).split(",");
			if (Geometry.POINT.equals(geometry.getGeometryType()) && coordStrings.length > 1) {
				throw new WktException(ERR_MSG + "a point can have only one coordinate");
			}

			Coordinate[] coordinates = new Coordinate[coordStrings.length];
			for (int i = 0; i < coordStrings.length; i++) {
				String token = coordStrings[i].trim();
				String[] values = token.split(" ");
				if (values.length != 2) {
					throw new WktException(ERR_MSG + "only 2D coordinates are supported");
				}
				try {
					coordinates[i] = new Coordinate(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
				} catch (Exception e) { // NOSONAR
					throw new WktException(ERR_MSG + "could not parse X and Y values (" + token + ")", e);
				}
			}
			geometry.setCoordinates(coordinates);
			scopeWkt = scopeWkt.substring(scopeWkt.indexOf(')') + 1);
		}
		return scopeWkt;
	}

	private static String typeWktToGeom(String wktType) {
		if (Geometry.POINT.toUpperCase().equals(wktType)) {
			return Geometry.POINT;
		} else if (Geometry.LINE_STRING.equalsIgnoreCase(wktType)) {
			return Geometry.LINE_STRING;
		} else if (Geometry.POLYGON.equalsIgnoreCase(wktType)) {
			return Geometry.POLYGON;
		} else if (Geometry.MULTI_POINT.equalsIgnoreCase(wktType)) {
			return Geometry.MULTI_POINT;
		} else if (Geometry.MULTI_LINE_STRING.equalsIgnoreCase(wktType)) {
			return Geometry.MULTI_LINE_STRING;
		} else if (Geometry.MULTI_POLYGON.equalsIgnoreCase(wktType)) {
			return Geometry.MULTI_POLYGON;
		}
		return null;
	}

	private static String getChildType(String parentType) {
		if (Geometry.POINT.equals(parentType) || Geometry.LINE_STRING.equals(parentType)
				|| Geometry.LINEAR_RING.equals(parentType)) {
			return null;
		} else if (Geometry.POLYGON.equals(parentType)) {
			return Geometry.LINEAR_RING;
		} else if (Geometry.MULTI_POINT.equals(parentType)) {
			return Geometry.POINT;
		} else if (Geometry.MULTI_LINE_STRING.equals(parentType)) {
			return Geometry.LINE_STRING;
		} else if (Geometry.MULTI_POLYGON.equals(parentType)) {
			return Geometry.POLYGON;
		}
		return null;
	}

	// ------------------------------------------------------------------------
	// Private methods for WKT formatting:
	// ------------------------------------------------------------------------

	private static boolean isEmpty(Geometry geometry) {
		return (geometry.getCoordinates() == null || geometry.getCoordinates().length == 0)
				&& (geometry.getGeometries() == null || geometry.getGeometries().length == 0);
	}

	private static String toWktPoint(Geometry geometry) {
		StringBuilder builder = new StringBuilder();
		builder.append("POINT ");
		appendCoordinates(geometry, builder);
		return builder.toString();
	}

	private static String toWktPolygon(Geometry geometry) {
		StringBuilder builder = new StringBuilder();
		builder.append("POLYGON ");
		appendCoordinates(geometry, builder);
		return builder.toString();
	}

	private static String toWktLineString(Geometry geometry) {
		StringBuilder builder = new StringBuilder();
		builder.append("LINESTRING ");
		appendCoordinates(geometry, builder);
		return builder.toString();
	}

	private static String toWktMultiPoint(Geometry geometry) {
		StringBuilder builder = new StringBuilder();
		builder.append("MULTIPOINT ");
		appendCoordinates(geometry, builder);
		return builder.toString();
	}

	private static String toWktMultiPolygon(Geometry geometry) {
		StringBuilder builder = new StringBuilder();
		builder.append("MULTIPOLYGON ");
		appendCoordinates(geometry, builder);
		return builder.toString();
	}

	private static String toWktMultiLineString(Geometry geometry) {
		StringBuilder builder = new StringBuilder();
		builder.append("MULTILINESTRING ");
		appendCoordinates(geometry, builder);
		return builder.toString();
	}

	private static void appendCoordinates(Geometry geometry, StringBuilder builder) {
		if (isEmpty(geometry)) {
			builder.append("EMPTY");
		} else {
			// multi-geometry
			if (geometry.getGeometries() != null) {
				builder.append("(");
				for (int i = 0; i < geometry.getGeometries().length; i++) {
					if (i > 0) {
						builder.append(", ");
					}
					appendCoordinates(geometry.getGeometries()[i], builder);
				}
				builder.append(")");
			// single geometry
			} else {
				builder.append("(");
				for (int i = 0; i < geometry.getCoordinates().length; i++) {
					if (i > 0) {
						builder.append(", ");
					}
					appendCoordinate(geometry.getCoordinates()[i], builder);
				}
				builder.append(")");
			}
		}
	}

	private static void appendCoordinate(Coordinate coordinate, StringBuilder builder) {
		builder.append(coordinate.getX());
		builder.append(" ");
		builder.append(coordinate.getY());
	}
}