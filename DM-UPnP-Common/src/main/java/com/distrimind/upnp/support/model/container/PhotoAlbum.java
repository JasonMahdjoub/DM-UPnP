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

package com.distrimind.upnp.support.model.container;

import com.distrimind.upnp.support.model.item.Item;
import com.distrimind.upnp.support.model.item.Photo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class PhotoAlbum extends Album {

    public static final Class CLASS = new Class("object.container.album.photoAlbum");

    public PhotoAlbum() {
        setClazz(CLASS);
    }

    public PhotoAlbum(Container other) {
        super(other);
    }

    public PhotoAlbum(String id, Container parent, String title, String creator, Integer childCount) {
        this(id, parent.getId(), title, creator, childCount, new ArrayList<>());
    }

    public PhotoAlbum(String id, Container parent, String title, String creator, Integer childCount, List<Photo> photos) {
        this(id, parent.getId(), title, creator, childCount, photos);
    }

    public PhotoAlbum(String id, String parentID, String title, String creator, Integer childCount) {
        this(id, parentID, title, creator, childCount, new ArrayList<>());
    }

    public PhotoAlbum(String id, String parentID, String title, String creator, Integer childCount, List<Photo> photos) {
        super(id, parentID, title, creator, childCount);
        setClazz(CLASS);
        addPhotos(photos);
    }

    public List<Photo> getPhotos() {
        List<Photo> list = new ArrayList<>();
        for (Item item : getItems()) {
            if (item instanceof Photo) list.add((Photo)item);
        }
        return list;
    }


    public void addPhotos(List<Photo> photos) {
        if (photos != null) {
            for (Photo photo : photos) {
                photo.setAlbum(getTitle());
                addItem(photo);
            }
        }
    }

}
