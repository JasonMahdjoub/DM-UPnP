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

package com.distrimind.upnp.model.profile;

import com.distrimind.upnp.model.message.IUpnpHeaders;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.transport.spi.StreamServer;
import com.distrimind.upnp.model.message.Connection;
import com.distrimind.upnp.model.message.UpnpHeaders;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.message.header.UserAgentHeader;
import com.distrimind.upnp.http.RequestInfo;

import java.net.InetAddress;

/**
 * Encapsulates information about a remote control point, the client.
 *
 * <p>
 * The {@link #getExtraResponseHeaders()} method offers modifiable HTTP headers which will
 * be added to the responses and returned to the client.
 * </p>
 *
 * @author Christian Bauer
 */
public class RemoteClientInfo extends ClientInfo {

    final protected Connection connection;
    final protected IUpnpHeaders extraResponseHeaders = new UpnpHeaders();

    public RemoteClientInfo() {
        this(null);
    }

    public RemoteClientInfo(StreamRequestMessage requestMessage) {
        this(requestMessage != null ? requestMessage.getConnection() : null,
            requestMessage != null ? requestMessage.getHeaders() : new UpnpHeaders());
    }

    public RemoteClientInfo(Connection connection, IUpnpHeaders requestHeaders) {
        super(requestHeaders);
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * <p>
     * Check if the remote client's connection is still open.
   
     * <p>
     * How connection checking is actually performed is transport-implementation dependent. Usually,
     * the {@link StreamServer} will send meaningless heartbeat
     * data to the client on its (open) socket. If that fails, the client's connection has been
     * closed. Note that some HTTP clients can <em>NOT</em> handle such garbage data in HTTP
     * responses, hence calling this method might cause compatibility issues.
   
     * @return <code>true</code> if the remote client's connection was closed.
     */
    public boolean isRequestCancelled() {
        return !getConnection().isOpen();
    }

    /**
     * @throws InterruptedException if {@link #isRequestCancelled()} returns <code>true</code>.
     */
    public void throwIfRequestCancelled() throws InterruptedException{
        if(isRequestCancelled())
             throw new InterruptedException("Client's request cancelled");
    }

    public InetAddress getRemoteAddress() {
        return getConnection().getRemoteAddress();
    }

    public InetAddress getLocalAddress() {
        return getConnection().getLocalAddress();
    }

    public IUpnpHeaders getExtraResponseHeaders() {
        return extraResponseHeaders;
    }

    public void setResponseUserAgent(String userAgent) {
        setResponseUserAgent(new UserAgentHeader(userAgent));
    }

    public void setResponseUserAgent(UserAgentHeader userAgentHeader) {
        getExtraResponseHeaders().add(
            UpnpHeader.Type.USER_AGENT,
            userAgentHeader
        );
    }

    // TODO: Remove this once we know how ClientProfile will look like
    public boolean isWMPRequest() {
        return RequestInfo.isWMPRequest(getRequestUserAgent());
    }

    public boolean isXbox360Request() {
        return RequestInfo.isXbox360Request(
            getRequestUserAgent(),
            getRequestHeaders().getFirstHeaderString(UpnpHeader.Type.SERVER)
        );
    }

    public boolean isPS3Request() {
    	return RequestInfo.isPS3Request(
            getRequestUserAgent(),
            getRequestHeaders().getFirstHeaderString(UpnpHeader.Type.EXT_AV_CLIENT_INFO)
        );
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") Remote Address: " + getRemoteAddress();
    }
}
