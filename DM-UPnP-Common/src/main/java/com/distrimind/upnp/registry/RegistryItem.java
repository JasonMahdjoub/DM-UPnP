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

package com.distrimind.upnp.registry;

import com.distrimind.upnp.model.ExpirationDetails;

/**
 * Internal class, required by {@link RegistryImpl}.
 *
 * @author Christian Bauer
 */
class RegistryItem<K, I> {

    private final K key;
    private I item;
    private ExpirationDetails expirationDetails = new ExpirationDetails();

    RegistryItem(K key) {
        if (key==null)
            throw new NullPointerException();
        this.key = key;
    }

    RegistryItem(K key, I item, int maxAgeSeconds) {
        if (key==null)
            throw new NullPointerException();
        this.key = key;
        this.item = item;
        this.expirationDetails = new ExpirationDetails(maxAgeSeconds);
    }

    public K getKey() {
        return key;
    }

    public I getItem() {
        return item;
    }

    public ExpirationDetails getExpirationDetails() {
        return expirationDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegistryItem<?, ?> that = (RegistryItem<?, ?>) o;

        return key.equals(that.key);
    }
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "("+getClass().getSimpleName()+") " + getExpirationDetails() + " KEY: " + getKey() + " ITEM: " + getItem();
    }
}
