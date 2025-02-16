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
package com.distrimind.upnp.mock;

import com.distrimind.upnp.model.action.ActionInvocation;
import com.distrimind.upnp.model.gena.LocalGENASubscription;
import com.distrimind.upnp.model.gena.RemoteGENASubscription;
import com.distrimind.upnp.model.message.IncomingDatagramMessage;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.protocol.ProtocolCreationException;
import com.distrimind.upnp.protocol.ProtocolFactory;
import com.distrimind.upnp.protocol.ReceivingAsync;
import com.distrimind.upnp.protocol.ReceivingSync;
import com.distrimind.upnp.protocol.async.SendingNotificationAlive;
import com.distrimind.upnp.protocol.async.SendingNotificationByebye;
import com.distrimind.upnp.protocol.async.SendingSearch;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.protocol.sync.SendingAction;
import com.distrimind.upnp.protocol.sync.SendingEvent;
import com.distrimind.upnp.protocol.sync.SendingRenewal;
import com.distrimind.upnp.protocol.sync.SendingSubscribe;
import com.distrimind.upnp.protocol.sync.SendingUnsubscribe;

import jakarta.enterprise.inject.Alternative;
import java.net.URL;

/**
 * @author Christian Bauer
 */
@Alternative
public class MockProtocolFactory implements ProtocolFactory {

    @Override
    public UpnpService getUpnpService() {
        return null;
    }

    @Override
    public ReceivingAsync<?> createReceivingAsync(IncomingDatagramMessage<?> message) throws ProtocolCreationException {
        return null;
    }

    @Override
    public ReceivingSync<?, ?> createReceivingSync(StreamRequestMessage requestMessage) throws ProtocolCreationException {
        return null;
    }

    @Override
    public <T> SendingNotificationAlive createSendingNotificationAlive(LocalDevice<T> localDevice) {
        return null;
    }

    @Override
    public <T> SendingNotificationByebye createSendingNotificationByebye(LocalDevice<T> localDevice) {
        return null;
    }

    @Override
    public SendingSearch createSendingSearch(UpnpHeader<?> searchTarget, int mxSeconds) {
        return null;
    }

    @Override
    public SendingAction createSendingAction(ActionInvocation<?> actionInvocation, URL controlURL) {
        return null;
    }

    @Override
    public SendingSubscribe createSendingSubscribe(RemoteGENASubscription subscription) {
        return null;
    }

    @Override
    public SendingRenewal createSendingRenewal(RemoteGENASubscription subscription) {
        return null;
    }

    @Override
    public SendingUnsubscribe createSendingUnsubscribe(RemoteGENASubscription subscription) {
        return null;
    }

    @Override
    public SendingEvent createSendingEvent(LocalGENASubscription<?> subscription) {
        return null;
    }
}
