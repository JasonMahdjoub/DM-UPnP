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

package com.distrimind.upnp.protocol.async;

import com.distrimind.upnp.protocol.SendingAsync;
import com.distrimind.upnp.transport.RouterException;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.model.message.discovery.OutgoingSearchRequest;
import com.distrimind.upnp.model.message.header.MXHeader;
import com.distrimind.upnp.model.message.header.STAllHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Sending search request messages using the supplied search type.
 * <p>
 * Sends all search messages 5 times, waits 0 to 500
 * milliseconds between each sending procedure.
 * </p>
 *
 * @author Christian Bauer
 */
public class SendingSearch extends SendingAsync {

    final private static DMLogger log = Log.getLogger(SendingSearch.class);

    private final UpnpHeader<?> searchTarget;
    private final int mxSeconds;

    /**
     * Defaults to {@link STAllHeader} and an MX of 3 seconds.
     */
    public SendingSearch(UpnpService upnpService) {
        this(upnpService, new STAllHeader());
    }

    /**
     * Defaults to an MX value of 3 seconds.
     */
    public SendingSearch(UpnpService upnpService, UpnpHeader<?> searchTarget) {
        this(upnpService, searchTarget, MXHeader.DEFAULT_VALUE);
    }

    /**
     * @param mxSeconds The time in seconds a host should wait before responding.
     */
    public SendingSearch(UpnpService upnpService, UpnpHeader<?> searchTarget, int mxSeconds) {
        super(upnpService);

        if (!UpnpHeader.Type.ST.isValidHeaderType(searchTarget.getClass())) {
            throw new IllegalArgumentException(
                    "Given search target instance is not a valid header class for type ST: " + searchTarget.getClass()
            );
        }
        this.searchTarget = searchTarget;
        this.mxSeconds = mxSeconds;
    }

    public UpnpHeader<?> getSearchTarget() {
        return searchTarget;
    }

    public int getMxSeconds() {
        return mxSeconds;
    }

    @Override
	protected void execute() throws RouterException {

		if (log.isDebugEnabled()) {
            log.debug("Executing search for target: " + searchTarget.getString() + " with MX seconds: " + getMxSeconds());
		}

		OutgoingSearchRequest msg = new OutgoingSearchRequest(searchTarget, getMxSeconds());
        prepareOutgoingSearchRequest(msg);

        for (int i = 0; i < getBulkRepeat(); i++) {
            try {

                getUpnpService().getRouter().send(msg);

                // UDA 1.0 is silent about this but UDA 1.1 recommends "a few hundred milliseconds"
				if (log.isTraceEnabled()) {
					log.trace("Sleeping " + getBulkIntervalMilliseconds() + " milliseconds");
				}
				Thread.sleep(getBulkIntervalMilliseconds());

            } catch (InterruptedException ex) {
                // Interruption means we stop sending search messages, e.g. on shutdown of thread pool
                break;
            }
        }
    }

    public int getBulkRepeat() {
        return 5; // UDA 1.0 says "repeat more than once"
    }

    public int getBulkIntervalMilliseconds() {
        return 500; // That should be plenty on an ethernet LAN
    }

    /**
     * Override this to edit the outgoing message, e.g. by adding headers.
     */
    protected void prepareOutgoingSearchRequest(OutgoingSearchRequest message) {
    }

}
