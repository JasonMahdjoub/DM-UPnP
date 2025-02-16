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

package com.distrimind.upnp.support.lastchange;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.util.*;

import com.distrimind.upnp.Log;
import com.distrimind.upnp.model.XMLUtil;
import com.distrimind.upnp.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp.support.shared.AbstractMap;
import com.distrimind.upnp.util.io.IO;
import com.distrimind.upnp.util.Exceptions;
import com.distrimind.upnp.xml.SAXParser;
import com.distrimind.flexilogxml.exceptions.XMLStreamException;
import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.flexilogxml.xml.IXmlWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Reads and writes the "LastChange" XML content.
 * <p>
 * Validates against a schema if the {@link #getSchemaSources()} method
 * doesn't return <code>null</code>.
 * </p>
 * <p>
 * Note: This is broken on most devices and with most services out in the wild. In fact,
 * you might want to use polling the service with actions, to get its status, instead of
 * GENA. Polling can be expensive on low-power control points, however.
 * </p>
 *
 * @author Christian Bauer
 * @author Jason Mahdjoub, use XML Parser instead of Document
 */
public abstract class LastChangeParser extends SAXParser {

    final private static DMLogger log = Log.getLogger(LastChangeParser.class);

    public enum CONSTANTS {
        Event,
        InstanceID,
        val;

        @SuppressWarnings("PMD")
        public boolean equals(String s) {
            return this.name().equals(s);
        }
    }

    abstract protected String getNamespace();

    protected Set<Class<? extends EventedValue<?>>> getEventedVariables() {
        return Collections.emptySet();
    }

    protected EventedValue<?> createValue(String name, List<Map.Entry<String, String>> attributes) throws Exception {
        for (Class<? extends EventedValue<?>> evType : getEventedVariables()) {
            if (evType.getSimpleName().equals(name)) {
                Constructor<? extends EventedValue<?>> ctor = evType.getConstructor(List.class);
                return ctor.newInstance(attributes);
            }
        }
        return null;
    }

    /**
     * Uses the current thread's context classloader to read and unmarshall the given resource.
     *
     * @param resource The resource on the classpath.
     * @return The unmarshalled Event model.
     */
    public Event parseResource(String resource) throws Exception {
		try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
			return parse(IO.readLines(is));
		}
    }

    public Event parse(String xml) throws Exception {

        if (xml == null || xml.isEmpty()) {
            throw new RuntimeException("Null or empty XML");
        }

        Event event = new Event();
        new RootHandler(event, this);

        if (log.isDebugEnabled()) {
            log.debug("Parsing 'LastChange' event XML content");
            log.debug("===================================== 'LastChange' BEGIN ============================================");
            log.debug(xml);
            log.debug("====================================== 'LastChange' END  ============================================");
        }
        parse(new InputSource(new StringReader(xml)));

		if (log.isDebugEnabled()) {
            log.debug("Parsed event with instances IDs: " + event.getInstanceIDs().size());
		}
		if (log.isTraceEnabled()) {
            for (InstanceID instanceID : event.getInstanceIDs()) {
                log.trace("InstanceID '" + instanceID.getId() + "' has values: " + instanceID.getValues().size());
                for (EventedValue<?> eventedValue : instanceID.getValues()) {
                    log.trace(eventedValue.getName() + " => " + eventedValue.getValue());
                }
            }
        }

        return event;
    }

    class RootHandler extends Handler<Event> {

        RootHandler(Event instance, SAXParser parser) {
            super(instance, parser);
        }

        RootHandler(Event instance) {
            super(instance);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (CONSTANTS.InstanceID.equals(localName)) {
                String valAttr = attributes.getValue(CONSTANTS.val.name());
                if (valAttr != null) {
                    InstanceID instanceID = new InstanceID(new UnsignedIntegerFourBytes(valAttr));
                    getInstance().getInstanceIDs().add(instanceID);
                    new InstanceIDHandler(instanceID, this);
                }
            }
        }
    }

    class InstanceIDHandler extends Handler<InstanceID> {

        InstanceIDHandler(InstanceID instance, Handler<?> parent) {
            super(instance, parent);
        }

        @Override
        public void startElement(String uri, String localName, String qName, final Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            int s=attributes.getLength();
            List<Map.Entry<String, String>> attributeMap = new ArrayList<>(s);
            for (int i = 0; i < s; i++) {
                attributeMap.add(
                        new AbstractMap.SimpleEntry<>(
                                attributes.getLocalName(i),
                                attributes.getValue(i)
                        ));
            }
            try {
                EventedValue<?> esv = createValue(localName, attributeMap);
                if (esv != null)
                    getInstance().getValues().add(esv);
            } catch (Exception ex) {
                // Don't exit, just log a warning
                if (log.isWarnEnabled()) log.warn("Error reading event XML, ignoring value: ", Exceptions.unwrap(ex));
            }
        }

        @Override
        protected boolean isLastElement(String uri, String localName, String qName) {
            return CONSTANTS.InstanceID.equals(localName);
        }
    }

    public String generate(Event event) throws Exception {
        return buildXMLString(event);
    }

    protected String buildXMLString(Event event) throws Exception {
        return XMLUtil.generateXMLToString(xmlStreamWriter -> generateRoot(event, xmlStreamWriter));
    }

    protected void generateRoot(Event event, IXmlWriter xmlWriter) throws XMLStreamException {
        xmlWriter.writeStartElement(getNamespace(), CONSTANTS.Event.name());
        generateInstanceIDs(event, xmlWriter);
    }

    protected void generateInstanceIDs(Event event, IXmlWriter xmlWriter) throws XMLStreamException {
        for (InstanceID instanceID : event.getInstanceIDs()) {
            if (instanceID.getId() == null) continue;
            xmlWriter.writeStartElement(CONSTANTS.InstanceID.name());
            xmlWriter.writeAttribute(CONSTANTS.val.name(), instanceID.getId().toString());

            for (EventedValue<?> eventedValue : instanceID.getValues()) {
                generateEventedValue(eventedValue, xmlWriter);
            }
            xmlWriter.writeEndElement();
        }
    }

    protected void generateEventedValue(EventedValue<?> eventedValue, IXmlWriter xmlWriter) throws XMLStreamException {
        String name = eventedValue.getName();
        List<Map.Entry<String, String>> attributes = eventedValue.getAttributes();
        if (attributes != null && !attributes.isEmpty()) {
            xmlWriter.writeStartElement(name);
            for (Map.Entry<String, String> attr : attributes) {
                xmlWriter.writeAttribute(attr.getKey(), XMLUtil.escape(attr.getValue()));
            }
            xmlWriter.writeEndElement();
        }
    }

}
