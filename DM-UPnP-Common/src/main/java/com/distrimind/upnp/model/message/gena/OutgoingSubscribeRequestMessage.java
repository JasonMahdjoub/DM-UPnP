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

package com.distrimind.upnp.model.message.gena;

import com.distrimind.upnp.model.message.IUpnpHeaders;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.UpnpRequest;
import com.distrimind.upnp.model.message.header.CallbackHeader;
import com.distrimind.upnp.model.message.header.NTEventHeader;
import com.distrimind.upnp.model.message.header.TimeoutHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.gena.RemoteGENASubscription;

import java.net.URL;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class OutgoingSubscribeRequestMessage extends StreamRequestMessage {

    public OutgoingSubscribeRequestMessage(RemoteGENASubscription subscription,
                                           List<URL> callbackURLs,
                                           IUpnpHeaders extraHeaders) {

        super(UpnpRequest.Method.SUBSCRIBE, subscription.getEventSubscriptionURL());

        getHeaders().add(
                UpnpHeader.Type.CALLBACK,
                new CallbackHeader(callbackURLs)
        );

        getHeaders().add(
                UpnpHeader.Type.NT,
                new NTEventHeader()
        );

        getHeaders().add(
                UpnpHeader.Type.TIMEOUT,
                new TimeoutHeader(subscription.getRequestedDurationSeconds())
        );

        if (extraHeaders != null)
            getHeaders().putAll(extraHeaders);
    }

    public boolean hasCallbackURLs() {
        CallbackHeader callbackHeader =
                getHeaders().getFirstHeader(UpnpHeader.Type.CALLBACK, CallbackHeader.class);
        return !callbackHeader.getValue().isEmpty();
    }

}
