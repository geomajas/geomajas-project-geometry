package org.geomajas.geometry.service;

import junit.framework.Assert;

import org.geomajas.geometry.Geometry;
import org.geomajas.geometry.service.validation.HoleOutsideShellViolation;
import org.geomajas.geometry.service.validation.NestedHolesViolation;
import org.geomajas.geometry.service.validation.NestedShellsViolation;
import org.geomajas.geometry.service.validation.RingNotClosedViolation;
import org.geomajas.geometry.service.validation.RingSelfIntersectionViolation;
import org.geomajas.geometry.service.validation.SelfIntersectionViolation;
import org.geomajas.geometry.service.validation.TooFewPointsViolation;
import org.geomajas.geometry.service.validation.ValidationViolation;
import org.junit.Test;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryServiceValidationTest {

	@Test
	public void testHoleOutsideShell() throws Exception {
		String wkt = "POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0),(2 2, 2 3,  3 3, 3 2, 2 2))";
		Geometry p = WktService.toGeometry(wkt);
		Polygon jts = (Polygon) toJts(p);
		Assert.assertFalse(GeometryService.isValid(p));
		Assert.assertEquals(GeometryValidationState.HOLE_OUTSIDE_SHELL, GeometryService.getValidationContext()
				.getState());
		Assert.assertEquals(1, GeometryService.getValidationContext().getViolations().size());
		ValidationViolation vv = GeometryService.getValidationContext().getViolations().get(0);
		Assert.assertTrue(vv instanceof HoleOutsideShellViolation);
		HoleOutsideShellViolation v = (HoleOutsideShellViolation) vv;
		Assert.assertEquals(jts.getInteriorRingN(0), toJts(v.getHole().getGeometry()));
		Assert.assertEquals(jts.getExteriorRing(), toJts(v.getShell().getGeometry()));
	}

	@Test
	public void testNestedHoles() throws Exception {
		String wkt = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0),(1 1, 9 1, 9 9, 1 9, 1 1),(2 2, 8 2, 8 8, 2 8, 2 2))";
		Geometry p = WktService.toGeometry(wkt);
		Polygon jts = (Polygon) toJts(p);
		Assert.assertFalse(GeometryService.isValid(p));
		Assert.assertEquals(GeometryValidationState.NESTED_HOLES, GeometryService.getValidationContext().getState());
		Assert.assertEquals(1, GeometryService.getValidationContext().getViolations().size());
		ValidationViolation vv = GeometryService.getValidationContext().getViolations().get(0);
		Assert.assertTrue(vv instanceof NestedHolesViolation);
		NestedHolesViolation v = (NestedHolesViolation) vv;
		Assert.assertEquals(jts.getInteriorRingN(0), toJts(v.getHole().getGeometry()));
		Assert.assertEquals(jts.getInteriorRingN(1), toJts(v.getNestedHole().getGeometry()));
	}

	@Test
	public void testNestedShells() throws Exception {
		String wkt = "MULTIPOLYGON (((1 1, 9 1, 9 9, 1 9, 1 1)),((2 2, 8 2, 8 8, 2 8, 2 2)))";
		Geometry p = WktService.toGeometry(wkt);
		MultiPolygon jts = (MultiPolygon) toJts(p);
		Assert.assertFalse(GeometryService.isValid(p));
		Assert.assertEquals(GeometryValidationState.NESTED_SHELLS, GeometryService.getValidationContext().getState());
		Assert.assertEquals(1, GeometryService.getValidationContext().getViolations().size());
		ValidationViolation vv = GeometryService.getValidationContext().getViolations().get(0);
		Assert.assertTrue(vv instanceof NestedShellsViolation);
		NestedShellsViolation v = (NestedShellsViolation) vv;
		Assert.assertEquals(((Polygon) jts.getGeometryN(0)).getExteriorRing(), toJts(v.getShell().getGeometry()));
		Assert.assertEquals(((Polygon) jts.getGeometryN(1)).getExteriorRing(), toJts(v.getNestedShell().getGeometry()));
	}

	@Test
	public void testRingNotClosed() throws Exception {
		String wkt = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0), (1 1, 2 1, 2 2, 1 2, 1 1),(2 2, 3 2, 3 3, 2 3),(3 3, 3 4, 4 4, 4 3, 3 3))";
		Geometry p = WktService.toGeometry(wkt);
		LineString jts = (LineString) toJts("LINESTRING (2 2, 3 2, 3 3, 2 3)");
		Assert.assertFalse(GeometryService.isValid(p));
		Assert.assertEquals(GeometryValidationState.RING_NOT_CLOSED, GeometryService.getValidationContext().getState());
		Assert.assertEquals(1, GeometryService.getValidationContext().getViolations().size());
		ValidationViolation vv = GeometryService.getValidationContext().getViolations().get(0);
		Assert.assertTrue(vv instanceof RingNotClosedViolation);
		RingNotClosedViolation v = (RingNotClosedViolation) vv;
		Assert.assertEquals(jts, toJts(v.getRing().getGeometry()));
	}

	@Test
	public void testRingSelfIntersection() throws Exception {
		String wkt = "POLYGON ((1 1, 9 1, 9 9, 1 9, 1 1),(2 2, 8 2, 2 8, 8 8, 2 2))";
		Geometry p = WktService.toGeometry(wkt);
		Assert.assertFalse(GeometryService.isValid(p));
		Assert.assertEquals(GeometryValidationState.RING_SELF_INTERSECTION, GeometryService.getValidationContext()
				.getState());
		Assert.assertEquals(1, GeometryService.getValidationContext().getViolations().size());
		ValidationViolation vv = GeometryService.getValidationContext().getViolations().get(0);
		Assert.assertTrue(vv instanceof RingSelfIntersectionViolation);
		RingSelfIntersectionViolation v = (RingSelfIntersectionViolation) vv;
		LineString line1 = (LineString) toJts("LINESTRING (8 2, 2 8)");
		LineString line2 = (LineString) toJts("LINESTRING (8 8, 2 2)");
		LineString actual1 = (LineString) toJts(v.getEdge1().getGeometry());
		LineString actual2 = (LineString) toJts(v.getEdge2().getGeometry());
		if (!line1.equals(actual1)) {
			Assert.assertTrue(line1.equals(actual2) && line2.equals(actual1));
		} else {
			Assert.assertTrue(line1.equals(actual1) && line2.equals(actual2));
		}
	}

	@Test
	public void testSelfIntersection() throws Exception {
		String wkt = "POLYGON ((0 0, 10 0, 10 10, 0 10, 0 0),(1 1, 9 1, 9 9, 1 9, 1 1),(5 0, 10 5, 5 10, 0 5, 5 0))";
		Geometry p = WktService.toGeometry(wkt);
		Assert.assertFalse(GeometryService.isValid(p));
		Assert.assertEquals(8, GeometryService.getValidationContext().getViolations().size());
		for (int i = 0; i < 8; i++) {
			ValidationViolation vv = GeometryService.getValidationContext().getViolations().get(i);
			Assert.assertTrue(vv instanceof SelfIntersectionViolation);
		}

	}
	
	@Test
	public void testTooFewPoints() throws Exception {
		String wkt = "POLYGON ((0 0, 10 0, 0 0))";
		Geometry p = WktService.toGeometry(wkt);
		Assert.assertFalse(GeometryService.isValid(p));
		Assert.assertEquals(2, GeometryService.getValidationContext().getViolations().size());
		ValidationViolation vv = GeometryService.getValidationContext().getViolations().get(0);
		Assert.assertTrue(vv instanceof TooFewPointsViolation);
		vv = GeometryService.getValidationContext().getViolations().get(1);
		Assert.assertTrue(vv instanceof RingSelfIntersectionViolation);
	}
	
	public com.vividsolutions.jts.geom.Geometry toJts(Geometry g) throws ParseException, WktException {
		return new WKTReader().read(WktService.toWkt(g));
	}

	public com.vividsolutions.jts.geom.Geometry toJts(String wkt) throws ParseException, WktException {
		return new WKTReader().read(wkt);
	}

}
