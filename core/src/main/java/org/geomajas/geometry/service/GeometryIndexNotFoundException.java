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

import org.geomajas.annotation.Api;

/**
 * Exception that is thrown when a certain {@link GeometryIndex} could not be found within a certain
 * {@link org.geomajas.geometry.Geometry}. Used mainly in the {@link GeometryIndexService}.
 * 
 * @author Pieter De Graef
 * @since 1.3.0
 */
@Api(allMethods = true)
public class GeometryIndexNotFoundException extends Exception {

	private static final long serialVersionUID = 100L;

	/**
	 * Default constructor.
	 */
	public GeometryIndexNotFoundException() {
		super();
	}

	/**
	 * Constructor with a message.
	 * @param message message of this exception
	 */
	public GeometryIndexNotFoundException(String message) {
		super(message);
	}

	/**
	 * Constructor with a message and cause.
	 * @param message message of this exception
	 * @param cause cause of this exception
	 */
	public GeometryIndexNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}