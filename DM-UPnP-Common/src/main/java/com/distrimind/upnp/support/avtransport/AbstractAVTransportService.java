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

package com.distrimind.upnp.support.avtransport;

import com.distrimind.upnp.binding.annotations.UpnpAction;
import com.distrimind.upnp.binding.annotations.UpnpInputArgument;
import com.distrimind.upnp.binding.annotations.UpnpOutputArgument;
import com.distrimind.upnp.binding.annotations.UpnpService;
import com.distrimind.upnp.binding.annotations.UpnpServiceId;
import com.distrimind.upnp.binding.annotations.UpnpServiceType;
import com.distrimind.upnp.binding.annotations.UpnpStateVariable;
import com.distrimind.upnp.binding.annotations.UpnpStateVariables;
import com.distrimind.upnp.model.ModelUtil;
import com.distrimind.upnp.model.types.UnsignedIntegerFourBytes;
import com.distrimind.upnp.support.avtransport.lastchange.AVTransportLastChangeParser;
import com.distrimind.upnp.support.avtransport.lastchange.AVTransportVariable;
import com.distrimind.upnp.support.lastchange.LastChange;
import com.distrimind.upnp.support.lastchange.LastChangeDelegator;
import com.distrimind.upnp.support.model.DeviceCapabilities;
import com.distrimind.upnp.support.model.MediaInfo;
import com.distrimind.upnp.support.model.PlayMode;
import com.distrimind.upnp.support.model.PositionInfo;
import com.distrimind.upnp.support.model.RecordMediumWriteStatus;
import com.distrimind.upnp.support.model.RecordQualityMode;
import com.distrimind.upnp.support.model.SeekMode;
import com.distrimind.upnp.support.model.StorageMedium;
import com.distrimind.upnp.support.model.TransportAction;
import com.distrimind.upnp.support.model.TransportInfo;
import com.distrimind.upnp.support.model.TransportSettings;
import com.distrimind.upnp.support.model.TransportState;
import com.distrimind.upnp.support.model.TransportStatus;

import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.List;

/**
 * Skeleton of service with "LastChange" eventing support.
 *
 * @author Christian Bauer
 */

@UpnpService(
        serviceId = @UpnpServiceId("AVTransport"),
        serviceType = @UpnpServiceType(value = "AVTransport", version = 1),
        stringConvertibleTypes = LastChange.class
)
@UpnpStateVariables({
        @UpnpStateVariable(
                name = "TransportState",
                sendEvents = false,
                allowedValuesEnum = TransportState.class),
        @UpnpStateVariable(
                name = "TransportStatus",
                sendEvents = false,
                allowedValuesEnum = TransportStatus.class),
        @UpnpStateVariable(
                name = "PlaybackStorageMedium",
                sendEvents = false,
                defaultValue = "NONE",
                allowedValuesEnum = StorageMedium.class),
        @UpnpStateVariable(
                name = "RecordStorageMedium",
                sendEvents = false,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED,
                allowedValuesEnum = StorageMedium.class),
        @UpnpStateVariable(
                name = "PossiblePlaybackStorageMedia",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = "NETWORK"),
        @UpnpStateVariable(
                name = "PossibleRecordStorageMedia",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED),
        @UpnpStateVariable( // TODO
                name = "CurrentPlayMode",
                sendEvents = false,
                defaultValue = "NORMAL",
                allowedValuesEnum = PlayMode.class),
        @UpnpStateVariable( // TODO
                name = "TransportPlaySpeed",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = "1"), // 1, 1/2, 2, -1, 1/10, etc.
        @UpnpStateVariable(
                name = "RecordMediumWriteStatus",
                sendEvents = false,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED,
                allowedValuesEnum = RecordMediumWriteStatus.class),
        @UpnpStateVariable(
                name = "CurrentRecordQualityMode",
                sendEvents = false,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED,
                allowedValuesEnum = RecordQualityMode.class),
        @UpnpStateVariable(
                name = "PossibleRecordQualityModes",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED),
        @UpnpStateVariable(
                name = "NumberOfTracks",
                sendEvents = false,
                datatype = "ui4",
                defaultValue = "0"),
        @UpnpStateVariable(
                name = "CurrentTrack",
                sendEvents = false,
                datatype = "ui4",
                defaultValue = "0"),
        @UpnpStateVariable(
                name = "CurrentTrackDuration",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING), // H+:MM:SS[.F+] or H+:MM:SS[.F0/F1]
        @UpnpStateVariable(
                name = "CurrentMediaDuration",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = "00:00:00"),
        @UpnpStateVariable(
                name = "CurrentTrackMetaData",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED),
        @UpnpStateVariable(
                name = "CurrentTrackURI",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING),
        @UpnpStateVariable(
                name = AbstractAVTransportService.AVTRANSPORT_URI,
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING),
        @UpnpStateVariable(
                name = AbstractAVTransportService.AVTRANSPORT_URIMETA_DATA,
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED),
        @UpnpStateVariable(
                name = "NextAVTransportURI",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED),
        @UpnpStateVariable(
                name = "NextAVTransportURIMetaData",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING,
                defaultValue = AbstractAVTransportService.NOT_IMPLEMENTED),
        @UpnpStateVariable(
                name = "RelativeTimePosition",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING), // H+:MM:SS[.F+] or H+:MM:SS[.F0/F1] (in track)
        @UpnpStateVariable(
                name = "AbsoluteTimePosition",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING), // H+:MM:SS[.F+] or H+:MM:SS[.F0/F1] (in media)
        @UpnpStateVariable(
                name = "RelativeCounterPosition",
                sendEvents = false,
                datatype = "i4",
                defaultValue = "2147483647"), // Max value means not implemented
        @UpnpStateVariable(
                name = "AbsoluteCounterPosition",
                sendEvents = false,
                datatype = "i4",
                defaultValue = "2147483647"), // Max value means not implemented
        @UpnpStateVariable(
                name = "CurrentTransportActions",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING), // Play, Stop, Pause, Seek, Next, Previous and Record
        @UpnpStateVariable(
                name = "A_ARG_TYPE_SeekMode",
                sendEvents = false,
                allowedValuesEnum = SeekMode.class), // The 'type' of seek we can perform (or should perform)
        @UpnpStateVariable(
                name = "A_ARG_TYPE_SeekTarget",
                sendEvents = false,
                datatype = AbstractAVTransportService.STRING), // The actual seek (offset or whatever) value
        @UpnpStateVariable(
                name = "A_ARG_TYPE_InstanceID",
                sendEvents = false,
                datatype = "ui4")
})
public abstract class AbstractAVTransportService implements LastChangeDelegator {

    public static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";
    public static final String STRING = "string";
    public static final String AVTRANSPORT_URI = "AVTransportURI";
    public static final String AVTRANSPORT_URIMETA_DATA = "AVTransportURIMetaData";
    public static final String INSTANCE_ID = "InstanceID";
    @UpnpStateVariable(eventMaximumRateMilliseconds = 200)
    final private LastChange lastChange;
    final protected PropertyChangeSupport propertyChangeSupport;

    protected AbstractAVTransportService() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = new LastChange(new AVTransportLastChangeParser());
    }

    protected AbstractAVTransportService(LastChange lastChange) {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.lastChange = lastChange;
    }

    protected AbstractAVTransportService(PropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = new LastChange(new AVTransportLastChangeParser());
    }

    protected AbstractAVTransportService(PropertyChangeSupport propertyChangeSupport, LastChange lastChange) {
        this.propertyChangeSupport = propertyChangeSupport;
        this.lastChange = lastChange;
    }

    @Override
    public LastChange getLastChange() {
        return lastChange;
    }

    @Override
    public void appendCurrentState(LastChange lc, UnsignedIntegerFourBytes instanceId) throws Exception {

        MediaInfo mediaInfo = getMediaInfo(instanceId);
        TransportInfo transportInfo = getTransportInfo(instanceId);
        TransportSettings transportSettings = getTransportSettings(instanceId);
        PositionInfo positionInfo = getPositionInfo(instanceId);
        DeviceCapabilities deviceCaps = getDeviceCapabilities(instanceId);

        lc.setEventedValue(
                instanceId,
                new AVTransportVariable.AVTransportURI(URI.create(mediaInfo.getCurrentURI())),
                new AVTransportVariable.AVTransportURIMetaData(mediaInfo.getCurrentURIMetaData()),
                new AVTransportVariable.CurrentMediaDuration(mediaInfo.getMediaDuration()),
                new AVTransportVariable.CurrentPlayMode(transportSettings.getPlayMode()),
                new AVTransportVariable.CurrentRecordQualityMode(transportSettings.getRecQualityMode()),
                new AVTransportVariable.CurrentTrack(positionInfo.getTrack()),
                new AVTransportVariable.CurrentTrackDuration(positionInfo.getTrackDuration()),
                new AVTransportVariable.CurrentTrackMetaData(positionInfo.getTrackMetaData()),
                new AVTransportVariable.CurrentTrackURI(URI.create(positionInfo.getTrackURI())),
                new AVTransportVariable.CurrentTransportActions(getCurrentTransportActions(instanceId)),
                new AVTransportVariable.NextAVTransportURI(URI.create(mediaInfo.getNextURI())),
                new AVTransportVariable.NextAVTransportURIMetaData(mediaInfo.getNextURIMetaData()),
                new AVTransportVariable.NumberOfTracks(mediaInfo.getNumberOfTracks()),
                new AVTransportVariable.PossiblePlaybackStorageMedia(deviceCaps.getPlayMedia()),
                new AVTransportVariable.PossibleRecordQualityModes(deviceCaps.getRecQualityModes()),
                new AVTransportVariable.PossibleRecordStorageMedia(deviceCaps.getRecMedia()),
                new AVTransportVariable.RecordMediumWriteStatus(mediaInfo.getWriteStatus()),
                new AVTransportVariable.RecordStorageMedium(mediaInfo.getRecordMedium()),
                new AVTransportVariable.TransportPlaySpeed(transportInfo.getCurrentSpeed()),
                new AVTransportVariable.TransportState(transportInfo.getCurrentTransportState()),
                new AVTransportVariable.TransportStatus(transportInfo.getCurrentTransportStatus())
        );
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public static UnsignedIntegerFourBytes getDefaultInstanceID() {
        return new UnsignedIntegerFourBytes(0);
    }

    @UpnpAction
    public abstract void setAVTransportURI(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId,
                                           @UpnpInputArgument(name = "CurrentURI", stateVariable = AVTRANSPORT_URI) String currentURI,
                                           @UpnpInputArgument(name = "CurrentURIMetaData", stateVariable = AVTRANSPORT_URIMETA_DATA) String currentURIMetaData)
            throws AVTransportException;

    @UpnpAction
    public abstract void setNextAVTransportURI(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId,
                                               @UpnpInputArgument(name = "NextURI", stateVariable = AVTRANSPORT_URI) String nextURI,
                                               @UpnpInputArgument(name = "NextURIMetaData", stateVariable = AVTRANSPORT_URIMETA_DATA) String nextURIMetaData)
            throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "NrTracks", stateVariable = "NumberOfTracks", getterName = "getNumberOfTracks"),
            @UpnpOutputArgument(name = "MediaDuration", stateVariable = "CurrentMediaDuration", getterName = "getMediaDuration"),
            @UpnpOutputArgument(name = "CurrentURI", stateVariable = AVTRANSPORT_URI, getterName = "getCurrentURI"),
            @UpnpOutputArgument(name = "CurrentURIMetaData", stateVariable = AVTRANSPORT_URIMETA_DATA, getterName = "getCurrentURIMetaData"),
            @UpnpOutputArgument(name = "NextURI", stateVariable = "NextAVTransportURI", getterName = "getNextURI"),
            @UpnpOutputArgument(name = "NextURIMetaData", stateVariable = "NextAVTransportURIMetaData", getterName = "getNextURIMetaData"),
            @UpnpOutputArgument(name = "PlayMedium", stateVariable = "PlaybackStorageMedium", getterName = "getPlayMedium"),
            @UpnpOutputArgument(name = "RecordMedium", stateVariable = "RecordStorageMedium", getterName = "getRecordMedium"),
            @UpnpOutputArgument(name = "WriteStatus", stateVariable = "RecordMediumWriteStatus", getterName = "getWriteStatus")
    })
    public abstract MediaInfo getMediaInfo(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "CurrentTransportState", stateVariable = "TransportState", getterName = "getCurrentTransportState"),
            @UpnpOutputArgument(name = "CurrentTransportStatus", stateVariable = "TransportStatus", getterName = "getCurrentTransportStatus"),
            @UpnpOutputArgument(name = "CurrentSpeed", stateVariable = "TransportPlaySpeed", getterName = "getCurrentSpeed")
    })
    public abstract TransportInfo getTransportInfo(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "Track", stateVariable = "CurrentTrack", getterName = "getTrack"),
            @UpnpOutputArgument(name = "TrackDuration", stateVariable = "CurrentTrackDuration", getterName = "getTrackDuration"),
            @UpnpOutputArgument(name = "TrackMetaData", stateVariable = "CurrentTrackMetaData", getterName = "getTrackMetaData"),
            @UpnpOutputArgument(name = "TrackURI", stateVariable = "CurrentTrackURI", getterName = "getTrackURI"),
            @UpnpOutputArgument(name = "RelTime", stateVariable = "RelativeTimePosition", getterName = "getRelTime"),
            @UpnpOutputArgument(name = "AbsTime", stateVariable = "AbsoluteTimePosition", getterName = "getAbsTime"),
            @UpnpOutputArgument(name = "RelCount", stateVariable = "RelativeCounterPosition", getterName = "getRelCount"),
            @UpnpOutputArgument(name = "AbsCount", stateVariable = "AbsoluteCounterPosition", getterName = "getAbsCount")
    })
    public abstract PositionInfo getPositionInfo(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "PlayMedia", stateVariable = "PossiblePlaybackStorageMedia", getterName = "getPlayMediaString"),
            @UpnpOutputArgument(name = "RecMedia", stateVariable = "PossibleRecordStorageMedia", getterName = "getRecMediaString"),
            @UpnpOutputArgument(name = "RecQualityModes", stateVariable = "PossibleRecordQualityModes", getterName = "getRecQualityModesString")
    })
    public abstract DeviceCapabilities getDeviceCapabilities(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction(out = {
            @UpnpOutputArgument(name = "PlayMode", stateVariable = "CurrentPlayMode", getterName = "getPlayMode"),
            @UpnpOutputArgument(name = "RecQualityMode", stateVariable = "CurrentRecordQualityMode", getterName = "getRecQualityMode")
    })
    public abstract TransportSettings getTransportSettings(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction
    public abstract void stop(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction
    public abstract void play(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId,
                              @UpnpInputArgument(name = "Speed", stateVariable = "TransportPlaySpeed") String speed)
            throws AVTransportException;

    @UpnpAction
    public abstract void pause(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction
    public abstract void record(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction
    public abstract void seek(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId,
                              @UpnpInputArgument(name = "Unit", stateVariable = "A_ARG_TYPE_SeekMode") String unit,
                              @UpnpInputArgument(name = "Target", stateVariable = "A_ARG_TYPE_SeekTarget") String target)
            throws AVTransportException;

    @UpnpAction
    public abstract void next(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction
    public abstract void previous(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException;

    @UpnpAction
    public abstract void setPlayMode(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId,
                                     @UpnpInputArgument(name = "NewPlayMode", stateVariable = "CurrentPlayMode") String newPlayMode)
            throws AVTransportException;

    @UpnpAction
    public abstract void setRecordQualityMode(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId,
                                              @UpnpInputArgument(name = "NewRecordQualityMode", stateVariable = "CurrentRecordQualityMode") String newRecordQualityMode)
            throws AVTransportException;

    @UpnpAction(name = "GetCurrentTransportActions", out = @UpnpOutputArgument(name = "Actions", stateVariable = "CurrentTransportActions"))
    public String getCurrentTransportActionsString(@UpnpInputArgument(name = INSTANCE_ID) UnsignedIntegerFourBytes instanceId)
            throws AVTransportException {
        try {
            return ModelUtil.toCommaSeparatedList(getCurrentTransportActions(instanceId));
        } catch (Exception ex) {
            return ""; // TODO: Empty string is not defined in spec but seems reasonable for no available action?
        }
    }

    protected abstract List<TransportAction> getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws Exception;
}
