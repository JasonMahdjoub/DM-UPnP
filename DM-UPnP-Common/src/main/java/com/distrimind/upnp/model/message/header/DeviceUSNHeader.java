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

package com.distrimind.upnp.model.message.header;

import com.distrimind.upnp.model.types.DeviceType;
import com.distrimind.upnp.model.types.NamedDeviceType;
import com.distrimind.upnp.model.types.UDN;

/**
 * @author Christian Bauer
 */
public class DeviceUSNHeader extends UpnpHeader<NamedDeviceType> {

    public DeviceUSNHeader() {
    }

    public DeviceUSNHeader(UDN udn, DeviceType deviceType) {
        setValue(new NamedDeviceType(udn, deviceType));
    }

    public DeviceUSNHeader(NamedDeviceType value) {
        setValue(value);
    }

    @Override
	public void setString(String s) throws InvalidHeaderException {
        try {
            setValue(NamedDeviceType.valueOf(s));
        } catch (Exception ex) {
            throw new InvalidHeaderException("Invalid device USN header value, " + ex.getMessage());
        }
    }

    @Override
	public String getString() {
        return getValue().toString();
    }

}