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
package org.geomajas.geometry.service.validation;

import java.util.List;

import org.geomajas.annotation.Api;
import org.geomajas.geometry.service.GeometryIndex;
import org.geomajas.geometry.service.GeometryValidationState;

/**
 * Violation occurred during validation.
 * 
 * @author Jan De Moerloose
 * 
 * @since 1.3.0
 */
@Api(allMethods = true)
public interface ValidationViolation {

	/**
	 * Get the state of the validation.
	 * 
	 * @return the state
	 */
	GeometryValidationState getState();

	/**
	 * Get the list of geometry indices of the sub-geometries/edges that are causing the invalid state.
	 * 
	 * @return
	 */
	List<GeometryIndex> getGeometryIndices();
}
