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

package com.distrimind.upnp.model.types;

/**
 * @author Christian Bauer
 */
public class DoubleDatatype extends AbstractDatatype<Double> {

    public DoubleDatatype() {
    }

    @Override
    public boolean isHandlingJavaType(Class<?> type) {
        return type == Double.TYPE || Double.class.isAssignableFrom(type);
    }

    @Override
	public Double valueOf(String s) throws InvalidValueException {
        if (s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            throw new InvalidValueException("Can't convert string to number: " + s, ex);
        }
    }

}
