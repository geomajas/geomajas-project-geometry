package org.geomajas.geometry.service;

import org.geomajas.geometry.Coordinate;
import org.geomajas.geometry.Geometry;
import org.junit.Assert;
import org.junit.Test;

public class GeometryServiceLinearRingIndexTest {

	private GeometryIndexService indexService = new GeometryIndexService();

	@Test
	public void testPolygonOneHole() throws WktException {
		Geometry poly = WktService.toGeometry("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))");
		// inside
		GeometryIndex index = GeometryService.getLinearRingIndex(poly, new Coordinate(0.5, 0.5));
		Assert.assertEquals(indexService.create(GeometryIndexType.TYPE_GEOMETRY, 0), index);
		// outside
		index = GeometryService.getLinearRingIndex(poly, new Coordinate(5, 5));
		Assert.assertEquals(null, index);
		// border = inside
		index = GeometryService.getLinearRingIndex(poly, new Coordinate(0, 0));
		Assert.assertEquals(indexService.create(GeometryIndexType.TYPE_GEOMETRY, 0), index);
	}

	@Test
	public void testPolygonMultiHoles() throws WktException {
		Geometry poly = WktService
				.toGeometry("POLYGON ((-1 -1, 10 -1, 10 10, -1 10, -1 -1),(1 1, 2 1, 2 2, 1 2, 1 1),(2 2, 3 2, 3 3, 2 3, 2 2))");
		// inside hole 2
		GeometryIndex index = GeometryService.getLinearRingIndex(poly, new Coordinate(2.5, 2.5));
		Assert.assertEquals(indexService.create(GeometryIndexType.TYPE_GEOMETRY, 2), index);
		// inside shell
		index = GeometryService.getLinearRingIndex(poly, new Coordinate(0, 0));
		Assert.assertEquals(indexService.create(GeometryIndexType.TYPE_GEOMETRY, 0), index);
		// outside
		index = GeometryService.getLinearRingIndex(poly, new Coordinate(20, 20));
		Assert.assertEquals(null, index);
	}

	@Test
	public void testMultiPolygonOneHole() throws WktException {
		Geometry poly = WktService.toGeometry("MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)))");
		// inside
		GeometryIndex index = GeometryService.getLinearRingIndex(poly, new Coordinate(0.5, 0.5));
		Assert.assertEquals(indexService.create(GeometryIndexType.TYPE_GEOMETRY, 0, 0), index);
		// outside
		index = GeometryService.getLinearRingIndex(poly, new Coordinate(5, 5));
		Assert.assertEquals(null, index);
		// border = inside
		index = GeometryService.getLinearRingIndex(poly, new Coordinate(0, 0));
		Assert.assertEquals(indexService.create(GeometryIndexType.TYPE_GEOMETRY, 0, 0), index);
	}

	@Test
	public void testMultiPolygonMultiPolygonsAndHoles() throws WktException {
		Geometry poly = WktService
				.toGeometry("MULTIPOLYGON (((-5 -5, -4 -5, -4 -4, -5 -4, -5 -5)), ((-1 -1, 10 -1, 10 10, -1 10, -1 -1),(1 1, 2 1, 2 2, 1 2, 1 1),(2 2, 3 2, 3 3, 2 3, 2 2)))");
		// inside hole 2
		GeometryIndex index = GeometryService.getLinearRingIndex(poly, new Coordinate(2.5, 2.5));
		Assert.assertEquals(indexService.create(GeometryIndexType.TYPE_GEOMETRY, 1, 2), index);
		// inside shell
		index = GeometryService.getLinearRingIndex(poly, new Coordinate(0, 0));
		Assert.assertEquals(indexService.create(GeometryIndexType.TYPE_GEOMETRY, 1, 0), index);
		// outside
		index = GeometryService.getLinearRingIndex(poly, new Coordinate(20, 20));
		Assert.assertEquals(null, index);
	}

}
