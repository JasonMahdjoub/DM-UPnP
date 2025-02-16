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

package com.distrimind.upnp.model.message.discovery;

import com.distrimind.upnp.model.Location;
import com.distrimind.upnp.model.message.header.ServiceTypeHeader;
import com.distrimind.upnp.model.message.header.ServiceUSNHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.types.NotificationSubtype;
import com.distrimind.upnp.model.types.ServiceType;

/**
 * @author Christian Bauer
 */
public class OutgoingNotificationRequestServiceType extends OutgoingNotificationRequest {

    public OutgoingNotificationRequestServiceType(Location location,
												  LocalDevice<?> device, NotificationSubtype type,
												  ServiceType serviceType) {

        super(location, device, type);

        getHeaders().add(UpnpHeader.Type.NT, new ServiceTypeHeader(serviceType));
        getHeaders().add(UpnpHeader.Type.USN, new ServiceUSNHeader(device.getIdentity().getUdn(), serviceType));
    }


}
