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

package com.distrimind.upnp.transport.impl;

import com.distrimind.upnp.transport.spi.DatagramIOConfiguration;

/**
 * Settings for the default implementation.
 *
 * @author Christian Bauer
 */
public class DatagramIOConfigurationImpl implements DatagramIOConfiguration {

    private int timeToLive = 4;
    private int maxDatagramBytes = 640;

    /**
     * Defaults to TTL of '4' and maximum datagram size of 640 bytes (512 per UDA 1.0, 128 byte header).
     */
    public DatagramIOConfigurationImpl() {
    }

    public DatagramIOConfigurationImpl(int timeToLive, int maxDatagramBytes) {
        this.timeToLive = timeToLive;
        this.maxDatagramBytes = maxDatagramBytes;
    }

    @Override
	public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
	public int getMaxDatagramBytes() {
        return maxDatagramBytes;
    }

    public void setMaxDatagramBytes(int maxDatagramBytes) {
        this.maxDatagramBytes = maxDatagramBytes;
    }
}