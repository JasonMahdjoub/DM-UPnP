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

package com.distrimind.upnp.protocol.sync;

import com.distrimind.upnp.protocol.ReceivingSync;
import com.distrimind.upnp.registry.Registry;
import com.distrimind.upnp.transport.RouterException;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.binding.xml.DescriptorBindingException;
import com.distrimind.upnp.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp.model.message.StreamRequestMessage;
import com.distrimind.upnp.model.message.StreamResponseMessage;
import com.distrimind.upnp.model.message.UpnpResponse;
import com.distrimind.upnp.model.message.header.ContentTypeHeader;
import com.distrimind.upnp.model.message.header.ServerHeader;
import com.distrimind.upnp.model.message.header.UpnpHeader;
import com.distrimind.upnp.model.meta.Icon;
import com.distrimind.upnp.model.meta.LocalDevice;
import com.distrimind.upnp.model.meta.LocalService;
import com.distrimind.upnp.model.resource.DeviceDescriptorResource;
import com.distrimind.upnp.model.resource.IconResource;
import com.distrimind.upnp.model.resource.Resource;
import com.distrimind.upnp.model.resource.ServiceDescriptorResource;
import com.distrimind.upnp.util.Exceptions;

import java.net.URI;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * Handles reception of device/service descriptor and icon retrieval messages.
 *
 * <p>
 * Requested device and service XML descriptors are generated on-the-fly for every request.
 * </p>
 * <p>
 * Descriptor XML is dynamically generated depending on the control point - some control
 * points require different metadata than others for the same device and services.
 * </p>
 *
 * @author Christian Bauer
 */
public class ReceivingRetrieval extends ReceivingSync<StreamRequestMessage, StreamResponseMessage> {

    final private static DMLogger log = Log.getLogger(ReceivingRetrieval.class);

    public ReceivingRetrieval(UpnpService upnpService, StreamRequestMessage inputMessage) {
        super(upnpService, inputMessage);
    }

    @Override
	protected StreamResponseMessage executeSync() throws RouterException {

        if (!getInputMessage().hasHostHeader()) {
			if (log.isDebugEnabled()) {
				log.debug("Ignoring message, missing HOST header: " + getInputMessage());
			}
			return new StreamResponseMessage(new UpnpResponse(UpnpResponse.Status.PRECONDITION_FAILED));
        }

        URI requestedURI = getInputMessage().getOperation().getURI();

        Resource<?> foundResource = getUpnpService().getRegistry().getResource(requestedURI);

        if (foundResource == null) {
            foundResource = onResourceNotFound(requestedURI);
            if (foundResource == null) {
				if (log.isDebugEnabled()) {
					log.debug("No local resource found: " + getInputMessage());
				}
				return null;
            }
        }

        return createResponse(requestedURI, foundResource);
    }

    protected StreamResponseMessage createResponse(URI requestedURI, Resource<?> resource) {

        StreamResponseMessage response;

        try {

            if (DeviceDescriptorResource.class.isAssignableFrom(resource.getClass())) {

				if (log.isDebugEnabled()) {
					log.debug("Found local device matching relative request URI: " + requestedURI);
				}
				LocalDevice<?> device = (LocalDevice<?>) resource.getModel();

                DeviceDescriptorBinder deviceDescriptorBinder =
                        getUpnpService().getConfiguration().getDeviceDescriptorBinderUDA10();
                String deviceDescriptor = deviceDescriptorBinder.generate(
                        device,
                        getRemoteClientInfo(),
                        getUpnpService().getConfiguration().getNamespace()
                );
                response = new StreamResponseMessage(
                        deviceDescriptor,
                        new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE)
                );
            } else if (ServiceDescriptorResource.class.isAssignableFrom(resource.getClass())) {


				if (log.isDebugEnabled()) {
					log.debug("Found local service matching relative request URI: " + requestedURI);
				}
				LocalService<?> service = (LocalService<?>) resource.getModel();

                ServiceDescriptorBinder serviceDescriptorBinder =
                        getUpnpService().getConfiguration().getServiceDescriptorBinderUDA10();
                String serviceDescriptor = serviceDescriptorBinder.generate(service);
                response = new StreamResponseMessage(
                        serviceDescriptor,
                        new ContentTypeHeader(ContentTypeHeader.DEFAULT_CONTENT_TYPE)
                );

            } else if (IconResource.class.isAssignableFrom(resource.getClass())) {

				if (log.isDebugEnabled()) {
					log.debug("Found local icon matching relative request URI: " + requestedURI);
				}
				Icon icon = (Icon) resource.getModel();
                response = new StreamResponseMessage(icon.getData(), icon.getMimeType());

            } else {

				if (log.isDebugEnabled()) {
					log.debug("Ignoring GET for found local resource: " + resource);
				}
				return null;
            }

        } catch (DescriptorBindingException ex) {
			if (log.isWarnEnabled()) {
				log.warn("Error generating requested device/service descriptor: ", ex);
				log.warn("Exception root cause: ", Exceptions.unwrap(ex));
			}
            response = new StreamResponseMessage(UpnpResponse.Status.INTERNAL_SERVER_ERROR);
        }
        
        response.getHeaders().add(UpnpHeader.Type.SERVER, new ServerHeader());

        return response;
    }

    /**
     * Called if the {@link Registry} had no result.
     *
     * @param requestedURIPath The requested URI path
     * @return <code>null</code> or your own {@link Resource}
     */
    protected Resource<?> onResourceNotFound(URI requestedURIPath) {
        return null;
    }
}
