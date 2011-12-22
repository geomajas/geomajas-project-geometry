/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.geometry.service;

import org.geomajas.geometry.Bbox;
import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * <p>
 * The purpose of this class is to test the {@link GeometryService} for MultiPolygon types of geometry. We do this by
 * comparing them to JTS results.
 * </p>
 * 
 * @author Pieter De Graef
 */
public class GeometryServiceMultiPolygonTest {

	private static final int SRID = 4326;

	private static final double DELTA = 1E-10;

	private com.vividsolutions.jts.geom.GeometryFactory jtsFactory;

	private Geometry gwt;

	private com.vividsolutions.jts.geom.MultiPolygon jts;

	// -------------------------------------------------------------------------
	// Constructor, initializes the 2 MultiPolygon geometries:
	// -------------------------------------------------------------------------

	@Before
	public void setUp() {
		Geometry gwtRing1 = new Geometry(Geometry.LINEAR_RING, SRID, 0);
		gwtRing1.setCoordinates(new Coordinate[] { new Coordinate(10.0, 10.0), new Coordinate(20.0, 10.0),
				new Coordinate(20.0, 20.0), new Coordinate(10.0, 10.0) });
		Geometry gwtPolygon1 = new Geometry(Geometry.POLYGON, SRID, 0);
		gwtPolygon1.setGeometries(new Geometry[] { gwtRing1 });

		Geometry gwtRing2 = new Geometry(Geometry.LINEAR_RING, SRID, 0);
		gwtRing2.setCoordinates(new Coordinate[] { new Coordinate(10.0, 20.0), new Coordinate(30.0, 10.0),
				new Coordinate(40.0, 10.0), new Coordinate(10.0, 20.0) });
		Geometry gwtPolygon2 = new Geometry(Geometry.POLYGON, SRID, 0);
		gwtPolygon2.setGeometries(new Geometry[] { gwtRing2 });

		gwt = new Geometry(Geometry.MULTI_POLYGON, SRID, 0);
		gwt.setGeometries(new Geometry[] { gwtPolygon1, gwtPolygon2 });

		jtsFactory = new com.vividsolutions.jts.geom.GeometryFactory(new PrecisionModel(), SRID);
		com.vividsolutions.jts.geom.LinearRing jtsRing1 = jtsFactory
				.createLinearRing(new com.vividsolutions.jts.geom.Coordinate[] {
						new com.vividsolutions.jts.geom.Coordinate(10.0, 10.0),
						new com.vividsolutions.jts.geom.Coordinate(20.0, 10.0),
						new com.vividsolutions.jts.geom.Coordinate(20.0, 20.0),
						new com.vividsolutions.jts.geom.Coordinate(10.0, 10.0) });
		com.vividsolutions.jts.geom.Polygon jtsPolygon1 = jtsFactory.createPolygon(jtsRing1, null);
		com.vividsolutions.jts.geom.LinearRing jtsRing2 = jtsFactory
				.createLinearRing(new com.vividsolutions.jts.geom.Coordinate[] {
						new com.vividsolutions.jts.geom.Coordinate(10.0, 20.0),
						new com.vividsolutions.jts.geom.Coordinate(30.0, 10.0),
						new com.vividsolutions.jts.geom.Coordinate(40.0, 10.0),
						new com.vividsolutions.jts.geom.Coordinate(10.0, 20.0) });
		com.vividsolutions.jts.geom.Polygon jtsPolygon2 = jtsFactory.createPolygon(jtsRing2, null);
		jts = jtsFactory.createMultiPolygon(new com.vividsolutions.jts.geom.Polygon[] { jtsPolygon1, jtsPolygon2 });
	}

	// -------------------------------------------------------------------------
	// The actual test cases:
	// -------------------------------------------------------------------------

	@Test
	public void getCentroid() {
		Assert.assertTrue(jts.getCentroid().getCoordinate().x - GeometryService.getCentroid(gwt).getX() < DELTA);
		Assert.assertTrue(jts.getCentroid().getCoordinate().y - GeometryService.getCentroid(gwt).getY() < DELTA);
	}

	@Test
	public void getBounds() {
		Envelope env = jts.getEnvelopeInternal();
		Bbox bbox = GeometryService.getBounds(gwt);
		Assert.assertEquals(env.getMinX(), bbox.getX(), DELTA);
		Assert.assertEquals(env.getMinY(), bbox.getY(), DELTA);
		Assert.assertEquals(env.getMaxX(), bbox.getMaxX(), DELTA);
		Assert.assertEquals(env.getMaxY(), bbox.getMaxY(), DELTA);
	}

	@Test
	public void getNumPoints() {
		Assert.assertEquals(jts.getNumPoints(), GeometryService.getNumPoints(gwt));
	}

	@Test
	public void isEmpty() {
		Assert.assertEquals(jts.isEmpty(), GeometryService.isEmpty(gwt));
	}

	@Test
	public void isSimple() {
		Assert.assertEquals(jts.isSimple(), GeometryService.isSimple(gwt));
	}

	@Test
	public void isValid() {
		Assert.assertEquals(jts.isValid(), GeometryService.isValid(gwt));
	}

	@Test
	public void intersects() {
		com.vividsolutions.jts.geom.LineString jtsLine1 = jtsFactory
				.createLineString(new com.vividsolutions.jts.geom.Coordinate[] {
						new com.vividsolutions.jts.geom.Coordinate(0, 0),
						new com.vividsolutions.jts.geom.Coordinate(15, 0) });
		// com.vividsolutions.jts.geom.LineString jtsLine2 = jtsFactory
		// .createLineString(new com.vividsolutions.jts.geom.Coordinate[] {
		// new com.vividsolutions.jts.geom.Coordinate(15, 5),
		// new com.vividsolutions.jts.geom.Coordinate(15, 25) });
		// com.vividsolutions.jts.geom.LineString jtsLine3 = jtsFactory
		// .createLineString(new com.vividsolutions.jts.geom.Coordinate[] {
		// new com.vividsolutions.jts.geom.Coordinate(0, 0),
		// new com.vividsolutions.jts.geom.Coordinate(15, 15) });

		Geometry gwtLine1 = new Geometry(Geometry.LINE_STRING, SRID, 0);
		gwtLine1.setCoordinates(new Coordinate[] { new Coordinate(0, 0), new Coordinate(15, 0) });
		// LineString gwtLine2 = gwtFactory.createLineString(new Coordinate[] { new Coordinate(15, 5),
		// new Coordinate(15, 25) });
		// LineString gwtLine3 = gwtFactory.createLineString(new Coordinate[] { new Coordinate(0, 0),
		// new Coordinate(15, 15) });

		Assert.assertEquals(jts.intersects(jtsLine1), GeometryService.intersects(gwt, gwtLine1)); // No intersection
		// Assert.assertEquals(jts.intersects(jtsLine2), gwt.intersects(gwtLine2)); // crosses LineSegment
		// Assert.assertEquals(jts.intersects(jtsLine3), gwt.intersects(gwtLine3)); // touches point
	}

	@Test
	public void getArea() {
		Assert.assertTrue((jts.getArea() - GeometryService.getArea(gwt)) < DELTA);
	}

	@Test
	public void getLength() {
		Assert.assertTrue((jts.getLength() - GeometryService.getLength(gwt)) < DELTA);
	}
}