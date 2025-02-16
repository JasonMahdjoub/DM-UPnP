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

package com.distrimind.upnp.support.shared;

import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.UpnpServiceImpl;
import com.distrimind.upnp.model.message.header.STAllHeader;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.registry.Registry;
import com.distrimind.upnp.registry.RegistryListener;

/**
 * @author Christian Bauer
 */
public class Main {

	@SuppressWarnings("PMD.SystemPrintln")
	public static void main(String[] args) throws Exception {

		// UPnP discovery is asynchronous, we need a callback
		RegistryListener listener = new RegistryListener() {

			@Override
			public void remoteDeviceDiscoveryStarted(Registry registry,
													 RemoteDevice device) {
				System.out.println(
						"Discovery started: " + device.getDisplayString()
				);
			}

			@Override
			public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
				System.out.println(
						"Discovery failed: " + device.getDisplayString() + " => " + ex
				);
			}

			@Override
			public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
				System.out.println(
						"Remote device available: " + device.getDisplayString()
				);
			}

			@Override
			public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
				System.out.println(
						"Remote device updated: " + device.getDisplayString()
				);
			}

			@Override
			public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
				System.out.println(
						"Remote device removed: " + device.getDisplayString()
				);
			}

			@Override
			public void localDeviceAdded(Registry registry, LocalDevice<?> device) {
				System.out.println(
						"Local device added: " + device.getDisplayString()
				);
			}

			@Override
			public void localDeviceRemoved(Registry registry, LocalDevice<?> device) {
				System.out.println(
						"Local device removed: " + device.getDisplayString()
				);
			}

			@Override
			public void beforeShutdown(Registry registry) {
				System.out.println(
						"Before shutdown, the registry has devices: " + registry.getDevices().size()
				);
			}

			@Override
			public void afterShutdown() {
				System.out.println("Shutdown of registry complete!");

			}
		};

		// This will create necessary network resources for UPnP right away
		System.out.println("Starting DM-UPnP...");
		UpnpService upnpService = new UpnpServiceImpl(listener);

		// Send a search message to all devices and services, they should respond soon
		System.out.println("Sending SEARCH message to all devices...");
		upnpService.getControlPoint().search(new STAllHeader());

		// Let's wait 10 seconds for them to respond
		System.out.println("Waiting 10 seconds before shutting down...");
		Thread.sleep(10000);

		// Release all resources and advertise BYEBYE to other UPnP devices
		System.out.println("Stopping DM-UPnP...");
		upnpService.shutdown();
	}
}

