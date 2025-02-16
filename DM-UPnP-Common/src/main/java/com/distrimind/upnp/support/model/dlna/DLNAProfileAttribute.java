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
package com.distrimind.upnp.support.model.dlna;

/**
 * @author Mario Franco
 */
public class DLNAProfileAttribute extends DLNAAttribute<DLNAProfiles> {
    
    public DLNAProfileAttribute() {
        setValue(DLNAProfiles.NONE);
    }
    
    public DLNAProfileAttribute(DLNAProfiles profile) {
        setValue(profile);
    }
    
    @Override
	public void setString(String s, String cf) throws InvalidDLNAProtocolAttributeException {
        DLNAProfiles value = DLNAProfiles.valueOf(s, cf);
        if (value == null) {
            throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA profile from: " + s);
        }
        setValue(value);
    }
    
    @Override
	public String getString() {
        return getValue().getCode();
    }
}
