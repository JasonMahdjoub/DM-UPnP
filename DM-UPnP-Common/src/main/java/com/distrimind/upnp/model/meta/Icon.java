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

package com.distrimind.upnp.model.meta;


import com.distrimind.upnp.model.Validatable;
import com.distrimind.upnp.model.ValidationError;
import com.distrimind.upnp.model.types.BinHexDatatype;
import com.distrimind.upnp.util.io.IO;
import com.distrimind.upnp.util.MimeType;
import com.distrimind.upnp.util.URIUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.Log;

/**
 * The metadata of a device icon, might include the actual image data of a local icon.
 *
 * <p>
 * Note that validation of icons is lax on purpose, a valid <code>Icon</code> might still
 * return <code>null</code> from {@link #getMimeType()}, {@link #getWidth()},
 * {@link #getHeight()}, and {@link #getDepth()}. However, {@link #getUri()} will return
 * a valid URI for a valid <code>Icon</code>.
 * </p>
 *
 * @author Christian Bauer
 */
public class Icon implements Validatable {

    final private static DMLogger log = Log.getLogger(Icon.class);
    public static final String UPN_P_SPECIFICATION_VIOLATION_OF = "UPnP specification violation of: ";

    final private MimeType mimeType;
    final private int width;
    final private int height;
    final private int depth;
    final private URI uri;
    final private byte[] data;

    // Package mutable state
    private Device<?, ?, ?> device;

    /**
     * Used internally by DM-UPnP when {@link RemoteDevice} is discovered, you shouldn't have to call this.
     */
    public Icon(String mimeType, int width, int height, int depth, URI uri) {
        this(mimeType != null && !mimeType.isEmpty() ? MimeType.valueOf(mimeType) : null, width, height, depth, uri, null);
    }

    /**
     * Use this constructor if your local icon data can be resolved on the classpath, for
     * example: <code>MyClass.class.getResource("/my/icon.png)</code>
     *
     * @param url A URL of the icon data that can be read with <code>new File(url.toURI())</code>.
     */
    public Icon(String mimeType, int width, int height, int depth, URL url) throws IOException{
        this(mimeType, width, height, depth, new File(URIUtil.toURI(url)));
    }

    /**
     * Use this constructor if your local icon data can be resolved with a <code>File</code>, the file's
     * name must be unique within the scope of a device.
     */
    public Icon(String mimeType, int width, int height, int depth, File file) throws IOException {
        this(mimeType, width, height, depth, file.getName(), IO.readBytes(file));
    }
    /**
     * Use this constructor if your local resource icon data, loaded from resource class
     */
    public Icon(String mimeType, int width, int height, int depth, String resourceName, Class<?> resourceClass) throws IOException {
        this(mimeType, width, height, depth, resourceName, IO.readBytes(resourceClass.getResourceAsStream(resourceName)));
    }

    /**
     * Use this constructor if your local icon data is an <code>InputStream</code>.
     *
     * @param uniqueName Must be a valid URI path segment and unique within the scope of a device.
     */
    public Icon(String mimeType, int width, int height, int depth, String uniqueName, InputStream is) throws IOException {
        this(mimeType, width, height, depth, uniqueName, IO.readBytes(is));
    }

    /**
     * Use this constructor if your local icon data is in a <code>byte[]</code>.
     *
     * @param uniqueName Must be a valid URI path segment and unique within the scope of a device.
     */
    public Icon(String mimeType, int width, int height, int depth, String uniqueName, byte[] data) {
        this(mimeType != null && !mimeType.isEmpty() ? MimeType.valueOf(mimeType) : null, width, height, depth, URI.create(uniqueName), data);
    }

    /**
     * Use this constructor if your local icon is binary data encoded with <em>BinHex</em>.

     * @param uniqueName Must be a valid URI path segment and unique within the scope of a device.
     * @param binHexEncoded The icon bytes encoded as BinHex.
     */
    public Icon(String mimeType, int width, int height, int depth, String uniqueName, String binHexEncoded) {
        this(
                mimeType, width, height, depth, uniqueName,
                binHexEncoded != null && !binHexEncoded.isEmpty() ? new BinHexDatatype().valueOf(binHexEncoded) : null
        );
    }

    protected Icon(MimeType mimeType, int width, int height, int depth, URI uri, byte[] data) {
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.uri = uri;
        this.data = data==null?null:data.clone();
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public URI getUri() {
        return uri;
    }

    public byte[] getData() {
        return data==null?null:data.clone();
    }

    public Device<?, ?, ?> getDevice() {
        return device;
    }

    void setDevice(Device<?, ?, ?> device) {
        if (this.device != null)
            throw new IllegalStateException("Final value has been set already, model is immutable");
        this.device = device;
    }

    @Override
	public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();
        if (log.isWarnEnabled()) {

            if (getMimeType() == null) {
                if (log.isWarnEnabled()) {
                    log.warn(UPN_P_SPECIFICATION_VIOLATION_OF + getDevice());
                    log.warn("Invalid icon, missing mime type: " + this);
                }
            }
            if (getWidth() == 0) {
                if (log.isWarnEnabled()) {
                    log.warn(UPN_P_SPECIFICATION_VIOLATION_OF + getDevice());
                    log.warn("Invalid icon, missing width: " + this);
                }
            }
            if (getHeight() == 0) {
                if (log.isWarnEnabled()) {
                    log.warn(UPN_P_SPECIFICATION_VIOLATION_OF + getDevice());
                    log.warn("Invalid icon, missing height: " + this);
                }
            }
            if (getDepth() == 0) {
                if (log.isWarnEnabled()) {
                    log.warn(UPN_P_SPECIFICATION_VIOLATION_OF + getDevice());
                    log.warn("Invalid icon, missing bitmap depth: " + this);
                }
            }

        }
        if (getUri() == null) {
            errors.add(new ValidationError(
                    getClass(),
                    "uri",
                    "URL is required"
            ));
        } else {
        	try {
        		URL testURI = getUri().toURL();
        		if (testURI == null)
        			throw new MalformedURLException();
        	} catch (MalformedURLException ex) {
        		errors.add(new ValidationError(
        				getClass(),
        				"uri",
        				"URL must be valid: " + ex.getMessage())
        				);
        	} catch (IllegalArgumentException ignored) {
        		// Relative URI is fine here!
        	}
        }

        return errors;
    }

    public Icon deepCopy() {
        return new Icon(
                getMimeType(),
                getWidth(),
                getHeight(),
                getDepth(),
                getUri(),
                getData()
        );
    }

    @Override
    public String toString() {
        return "Icon(" + getWidth() + "x" + getHeight() + ", MIME: " + getMimeType() + ") " + getUri();
    }
}