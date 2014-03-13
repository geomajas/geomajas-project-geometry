package org.geomajas.geometry.service;

import junit.framework.Assert;

import org.geomajas.geometry.Geometry;
import org.junit.Test;

public class GeometryServiceValidationTest {

	@Test
	public void testHoleOutsideShell() throws WktException {
		Geometry p = WktService.toGeometry("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0),(2 2, 2 3,  3 3, 3 2, 2 2))");
		Assert.assertFalse(GeometryService.isValid(p));
		Assert.assertEquals(GeometryService.getValidationContext().getState(), GeometryValidationState.HOLE_OUTSIDE_SHELL);
	}

}
