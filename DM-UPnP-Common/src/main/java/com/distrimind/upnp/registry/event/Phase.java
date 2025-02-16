/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.distrimind.upnp.registry.event;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Christian Bauer
 */

public class Phase {

    public static AnnotationLiteral<Alive> ALIVE = new AnnotationLiteral<>() {
		private static final long serialVersionUID = 1L;
	};

	public static AnnotationLiteral<Complete> COMPLETE = new AnnotationLiteral<>() {
		private static final long serialVersionUID = 1L;
	};

	public static AnnotationLiteral<Byebye> BYEBYE = new AnnotationLiteral<>() {
		private static final long serialVersionUID = 1L;
	};

	public static AnnotationLiteral<Updated> UPDATED = new AnnotationLiteral<>() {
		private static final long serialVersionUID = 1L;
	};


    @Qualifier
    @Target({FIELD, PARAMETER})
    @Retention(RUNTIME)
	public @interface Alive {

    }

    @Qualifier
    @Target({FIELD, PARAMETER})
    @Retention(RUNTIME)
	public @interface Complete {

    }

    @Qualifier
    @Target({FIELD, PARAMETER})
    @Retention(RUNTIME)
	public @interface Byebye {

    }

    @Qualifier
    @Target({FIELD, PARAMETER})
    @Retention(RUNTIME)
	public @interface Updated {

    }

}
