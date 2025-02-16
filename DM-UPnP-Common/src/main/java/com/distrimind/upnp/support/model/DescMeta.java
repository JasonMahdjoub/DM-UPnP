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

package com.distrimind.upnp.support.model;

import com.distrimind.upnp.model.XMLUtil;

import java.net.URI;


/**
 * Descriptor metadata about an item/resource.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{<a href="https://www.w3.org/2001/XMLSchema">...</a>}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;any namespace='##other'/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{<a href="https://www.w3.org/2001/XMLSchema">...</a>}string" /&gt;
 *       &lt;attribute name="type" type="{<a href="https://www.w3.org/2001/XMLSchema">...</a>}string" /&gt;
 *       &lt;attribute name="nameSpace" use="required" type="{<a href="https://www.w3.org/2001/XMLSchema">...</a>}anyURI" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
public class DescMeta<M> {

    protected String id;
    protected String type;
    protected URI nameSpace;
    protected M metadata;

    public DescMeta() {
    }

    public DescMeta(String id, String type, URI nameSpace, M metadata) {
        this.id = id;
        this.type = type;
        this.nameSpace = nameSpace;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public URI getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(URI nameSpace) {
        this.nameSpace = nameSpace;
    }

    public M getMetadata() {
        return metadata;
    }

    public void setMetadata(M metadata) {
        this.metadata = metadata;
    }

    public XMLUtil.XMLCompleters createXMLCompleters() {
        return new XMLUtil.XMLCompleters();
    }

}
