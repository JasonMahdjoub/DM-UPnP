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

package com.distrimind.upnp.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

import com.distrimind.upnp.registry.Registry;
import com.distrimind.upnp.model.meta.Device;
import com.distrimind.upnp.model.meta.Icon;
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.resource.Resource;
import com.distrimind.upnp.util.URIUtil;

/**
 * Enforces path conventions for all locally offered resources (descriptors, icons, etc.)
 * <p>
 * Every descriptor, icon, event callback, or action message is send to a URL. This namespace
 * defines how the path of this URL will look like and it will build the path for a given
 * resource.
 * </p>
 * <p>
 * By default, the namespace is organized as follows:
 * </p>
 * <pre>{@code
 * http://host:port/dev/<udn>/desc.xml
 * http://host:port/dev/<udn>/svc/<svcIdNamespace>/<svcId>/desc.xml
 * http://host:port/dev/<udn>/svc/<svcIdNamespace>/<svcId>/action
 * http://host:port/dev/<udn>/svc/<svcIdNamespace>/<svcId>/event
 * http://host:port/dev/<ThisIsEitherRootUDN>/svc/<svcIdNamespace>/<svcId>/event/cb.xml
 * http://host:port/dev/<OrEvenAnEmbeddedDevicesUDN>/svc/<svcIdNamespace>/<svcId>/action
 * ...
 * }</pre>
 * <p>
 * The namespace is also used to discover and create all {@link Resource}s
 * given a {@link Device}'s metadata. This procedure is typically
 * invoked once, when the device is added to the {@link Registry}.
 * </p>
 *
 * @author Christian Bauer
 */
public class Namespace {

    final private static DMLogger log = Log.getLogger(Namespace.class);

    public static final String DEVICE = "/dev";
    public static final String SERVICE = "/svc";
    public static final String CONTROL = "/action";
    public static final String EVENTS = "/event";
    public static final String DESCRIPTOR_FILE = "/desc";
    public static final String CALLBACK_FILE = "/cb";

    final protected URI basePath;
    final protected String decodedPath;

    public Namespace() {
        this("");
    }

    public Namespace(String basePath) {
        this(URI.create(basePath));
    }

    public Namespace(URI basePath) {
        this.basePath = basePath;
        this.decodedPath = basePath.getPath();
    }

    public URI getBasePath() {
        return basePath;
    }

    public URI getPath(Device<?, ?, ?> device) {
        return appendPathToBaseURI(getDevicePath(device));
    }

    public URI getPath(Service<?, ?, ?> service) {
        return appendPathToBaseURI(getServicePath(service));
    }

    public URI getDescriptorPath(Device<?, ?, ?> device) {
        return appendPathToBaseURI(getDevicePath(device.getRoot()) + DESCRIPTOR_FILE);
    }

    /**
     * Performance optimization, avoids URI manipulation.
     */
    public String getDescriptorPathString(Device<?, ?, ?> device) {
        return decodedPath + getDevicePath(device.getRoot()) + DESCRIPTOR_FILE;
    }

    public URI getDescriptorPath(Service<?, ?, ?> service) {
        return appendPathToBaseURI(getServicePath(service) + DESCRIPTOR_FILE);
    }

    public URI getControlPath(Service<?, ?, ?> service) {
        return appendPathToBaseURI(getServicePath(service) + CONTROL);
    }

    public URI getIconPath(Icon icon) {
        return appendPathToBaseURI(getDevicePath(icon.getDevice()) + "/" + icon.getUri().toString());
    }

    public URI getEventSubscriptionPath(Service<?, ?, ?> service) {
        return appendPathToBaseURI(getServicePath(service) + EVENTS);
    }

    public URI getEventCallbackPath(Service<?, ?, ?> service) {
        return appendPathToBaseURI(getServicePath(service) + EVENTS + CALLBACK_FILE);
    }

    /**
     * Performance optimization, avoids URI manipulation.
     */
    public String getEventCallbackPathString(Service<?, ?, ?> service) {
        return decodedPath + getServicePath(service) + EVENTS + CALLBACK_FILE;
    }

    public URI prefixIfRelative(Device<?, ?, ?> device, URI uri) {
        if (!uri.isAbsolute() && !uri.getPath().startsWith("/")) {
            return appendPathToBaseURI(getDevicePath(device) + "/" + uri);
        }
        return uri;
    }

    public boolean isControlPath(URI uri) {
        return uri.toString().endsWith(Namespace.CONTROL);
    }

    public boolean isEventSubscriptionPath(URI uri) {
        return uri.toString().endsWith(Namespace.EVENTS);
    }

    public boolean isEventCallbackPath(URI uri) {
        return uri.toString().endsWith(Namespace.CALLBACK_FILE);
    }

    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public Collection<Resource<?>> getResources(Device<?, ?, ?> device) throws ValidationException {
        if (!device.isRoot()) return null;

        Set<Resource<?>> resources = new HashSet<>();
        List<ValidationError> errors = new ArrayList<>();

        log.debug("Discovering local resources of device graph");
        Collection<Resource<?>> discoveredResources = device.discoverResources(this);
        for (Resource<?> resource : discoveredResources) {
			if (log.isTraceEnabled()) {
				log.trace("Discovered: " + resource);
			}
			if (!resources.add(resource)) {
                log.trace("Local resource already exists, queueing validation error");
                errors.add(new ValidationError(
                    getClass(),
                    "resources",
                    "Local URI namespace conflict between resources of device: " + resource
                ));
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation of device graph failed, call getErrors() on exception", errors);
        }
        return resources;
    }

    protected URI appendPathToBaseURI(String path) {
        try {
            // not calling getBasePath() on purpose since we're not sure if all DalvikVMs will inline it correctly
            return
                new URI(
                    basePath.getScheme(),
                    null,
                    basePath.getHost(),
                    basePath.getPort(),
                    decodedPath + path,
                    null,
                    null
                );
        } catch (URISyntaxException e) {
            return URI.create(basePath + path);
        }
    }

    protected String getDevicePath(Device<?, ?, ?> device) {
        if (device.getIdentity().getUdn() == null) {
            throw new IllegalStateException("Can't generate local URI prefix without UDN");
        }

		return DEVICE + "/" +
				URIUtil.encodePathSegment(device.getIdentity().getUdn().getIdentifierString());
    }

    protected String getServicePath(Service<?, ?, ?> service) {
        if (service.getServiceId() == null) {
            throw new IllegalStateException("Can't generate local URI prefix without service ID");
        }
		String s = SERVICE +
				"/" +
				service.getServiceId().getNamespace() +
				"/" +
				service.getServiceId().getId();
        return getDevicePath(service.getDevice()) + s;
    }
}
