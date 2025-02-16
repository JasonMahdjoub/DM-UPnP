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

package com.distrimind.upnp.controlpoint;

import com.distrimind.upnp.UpnpServiceConfiguration;
import com.distrimind.upnp.controlpoint.event.ExecuteAction;
import com.distrimind.upnp.controlpoint.event.Search;
import com.distrimind.upnp.model.message.header.MXHeader;
import com.distrimind.upnp.model.message.header.STAllHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.protocol.ProtocolFactory;
import com.distrimind.upnp.registry.Registry;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Default implementation.
 * <p>
 * This implementation uses the executor returned by
 * {@link UpnpServiceConfiguration#getSyncProtocolExecutorService()}.
 * </p>
 *
 * @author Christian Bauer
 */
@ApplicationScoped
public class ControlPointImpl implements ControlPoint {

    final private static DMLogger log = Log.getLogger(ControlPointImpl.class);

    protected UpnpServiceConfiguration configuration;
    protected ProtocolFactory protocolFactory;
    protected Registry registry;

    protected ControlPointImpl() {
    }

    @Inject
    public ControlPointImpl(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, Registry registry) {
		if (log.isDebugEnabled()) {
            log.debug("Creating ControlPoint: " + getClass().getName());
		}

		this.configuration = configuration;
        this.protocolFactory = protocolFactory;
        this.registry = registry;
    }

    @Override
	public UpnpServiceConfiguration getConfiguration() {
        return configuration;
    }

    @Override
	public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    @Override
	public Registry getRegistry() {
        return registry;
    }

    public void search(@Observes Search search) {
        search(search.getSearchType(), search.getMxSeconds());
    }

    @Override
	public void search() {
        search(new STAllHeader(), MXHeader.DEFAULT_VALUE);
    }

    @Override
	public void search(UpnpHeader<?> searchType) {
        search(searchType, MXHeader.DEFAULT_VALUE);
    }

    @Override
	public void search(int mxSeconds) {
        search(new STAllHeader(), mxSeconds);
    }

    @Override
	public void search(UpnpHeader<?> searchType, int mxSeconds) {
		if (log.isDebugEnabled()) {
            log.debug("Sending asynchronous search for: " + searchType.getString());
		}
		getConfiguration().getAsyncProtocolExecutor().execute(
                getProtocolFactory().createSendingSearch(searchType, mxSeconds)
        );
    }

    public void execute(ExecuteAction executeAction) {
        execute(executeAction.getCallback());
    }

    @Override
	@SuppressWarnings("PMD.CloseResource")
	public Future<?> execute(ActionCallback callback) {
		if (log.isDebugEnabled()) {
            log.debug("Invoking action in background: " + callback);
		}
		callback.setControlPoint(this);
        ExecutorService executor = getConfiguration().getSyncProtocolExecutorService();
        return executor.submit(callback);
    }

    @Override
	public void execute(SubscriptionCallback callback) {
		if (log.isDebugEnabled()) {
            log.debug("Invoking subscription in background: " + callback);
		}
		callback.setControlPoint(this);
        getConfiguration().getSyncProtocolExecutorService().execute(callback);
    }
}
