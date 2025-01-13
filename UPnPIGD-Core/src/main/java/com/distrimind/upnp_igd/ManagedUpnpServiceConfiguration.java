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

package com.distrimind.upnp_igd;

import com.distrimind.upnp_igd.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp_igd.binding.xml.UDA10DeviceDescriptorBinderImpl;
import com.distrimind.upnp_igd.binding.xml.UDA10ServiceDescriptorBinderImpl;
import com.distrimind.upnp_igd.model.Constants;
import com.distrimind.upnp_igd.model.ModelUtil;
import com.distrimind.upnp_igd.model.Namespace;
import com.distrimind.upnp_igd.model.message.UpnpHeaders;
import com.distrimind.upnp_igd.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp_igd.model.meta.RemoteService;
import com.distrimind.upnp_igd.model.types.ServiceType;
import com.distrimind.upnp_igd.transport.impl.DatagramIOConfigurationImpl;
import com.distrimind.upnp_igd.transport.impl.DatagramIOImpl;
import com.distrimind.upnp_igd.transport.impl.GENAEventProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.MulticastReceiverConfigurationImpl;
import com.distrimind.upnp_igd.transport.impl.MulticastReceiverImpl;
import com.distrimind.upnp_igd.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp_igd.transport.impl.SOAPActionProcessorImpl;
import com.distrimind.upnp_igd.transport.impl.StreamClientConfigurationImpl;
import com.distrimind.upnp_igd.transport.impl.StreamClientImpl;
import com.distrimind.upnp_igd.transport.impl.StreamServerConfigurationImpl;
import com.distrimind.upnp_igd.transport.impl.StreamServerImpl;
import com.distrimind.upnp_igd.transport.spi.DatagramIO;
import com.distrimind.upnp_igd.transport.spi.DatagramProcessor;
import com.distrimind.upnp_igd.transport.spi.GENAEventProcessor;
import com.distrimind.upnp_igd.transport.spi.MulticastReceiver;
import com.distrimind.upnp_igd.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp_igd.transport.spi.SOAPActionProcessor;
import com.distrimind.upnp_igd.transport.spi.StreamClient;
import com.distrimind.upnp_igd.transport.spi.StreamServer;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * Adapter for CDI environments.
 *
 * @author Christian Bauer
 */

public class ManagedUpnpServiceConfiguration implements UpnpServiceConfiguration {

    private static final Logger log = Logger.getLogger(ManagedUpnpServiceConfiguration.class.getName());

    // TODO: All of these fields should be injected so users can provide values through CDI

    private int streamListenPort;

    private ExecutorService defaultExecutorService;

    protected DatagramProcessor datagramProcessor;

    private SOAPActionProcessor soapActionProcessor;
    private GENAEventProcessor genaEventProcessor;

    private DeviceDescriptorBinder deviceDescriptorBinderUDA10;
    private ServiceDescriptorBinder serviceDescriptorBinderUDA10;

    private Namespace namespace;
    private NetworkAddressFactory networkAddressFactory=null;
    private int multicastPort;

    public void init() {

        if (ModelUtil.ANDROID_RUNTIME) {
            throw new Error("Unsupported runtime environment, use com.distrimind.upnp_igd.android.AndroidUpnpServiceConfiguration");
        }

        this.streamListenPort = NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT;

        defaultExecutorService = createDefaultExecutorService();

        soapActionProcessor = createSOAPActionProcessor();
        genaEventProcessor = createGENAEventProcessor();

        deviceDescriptorBinderUDA10 = createDeviceDescriptorBinderUDA10();
        serviceDescriptorBinderUDA10 = createServiceDescriptorBinderUDA10();
        multicastPort= Constants.UPNP_MULTICAST_PORT;
        namespace = createNamespace();
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    @Override
    public DatagramProcessor getDatagramProcessor() {
        return datagramProcessor;
    }

    @Override
    public SOAPActionProcessor getSoapActionProcessor() {
        return soapActionProcessor;
    }

    @Override
    public GENAEventProcessor getGenaEventProcessor() {
        return genaEventProcessor;
    }

    @Override
    public StreamClient<?> createStreamClient() {
        return new StreamClientImpl(
            new StreamClientConfigurationImpl(
                getSyncProtocolExecutorService()
            )
        );
    }
    protected NetworkAddressFactory getNetworkAddressFactory() {
        if (networkAddressFactory==null)
            networkAddressFactory=createNetworkAddressFactory();
        return networkAddressFactory;
    }
    @Override
    public MulticastReceiver<?> createMulticastReceiver(NetworkAddressFactory networkAddressFactory) {
        return new MulticastReceiverImpl(
                new MulticastReceiverConfigurationImpl(
                        networkAddressFactory.getMulticastGroup(),
                        networkAddressFactory.getMulticastPort()
                )
        );
    }

    @Override
    public DatagramIO<?> createDatagramIO(NetworkAddressFactory networkAddressFactory) {
        return new DatagramIOImpl(new DatagramIOConfigurationImpl());
    }

    @Override
    public StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return new StreamServerImpl(
                new StreamServerConfigurationImpl(
                        networkAddressFactory.getStreamListenPort()
                )
        );
    }

    @Override
    public Executor getMulticastReceiverExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public Executor getDatagramIOExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public ExecutorService getStreamServerExecutorService() {
        return getDefaultExecutorService();
    }

    @Override
    public DeviceDescriptorBinder getDeviceDescriptorBinderUDA10() {
        return deviceDescriptorBinderUDA10;
    }

    @Override
    public ServiceDescriptorBinder getServiceDescriptorBinderUDA10() {
        return serviceDescriptorBinderUDA10;
    }

    @Override
    public ServiceType[] getExclusiveServiceTypes() {
        return new ServiceType[0];
    }

    /**
     * @return Defaults to <code>false</code>.
     */
    @Override
	public boolean isReceivedSubscriptionTimeoutIgnored() {
		return false;
	}

    @Override
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity identity) {
        return null;
    }

    @Override
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public UpnpHeaders getEventSubscriptionHeaders(RemoteService service) {
        return null;
    }

    /**
     * @return Defaults to 1000 milliseconds.
     */
    @Override
    public int getRegistryMaintenanceIntervalMillis() {
        return 1000;
    }

    /**
     * @return Defaults to zero, disabling ALIVE flooding.
     */
    @Override
    public int getAliveIntervalMillis() {
    	return 0;
    }

    @Override
    public Integer getRemoteDeviceMaxAgeSeconds() {
        return null;
    }

    @Override
    public Executor getAsyncProtocolExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public ExecutorService getSyncProtocolExecutorService() {
        return getDefaultExecutorService();
    }

    @Override
    public Namespace getNamespace() {
        return namespace;
    }

    @Override
    public Executor getRegistryMaintainerExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public Executor getRegistryListenerExecutor() {
        return getDefaultExecutorService();
    }

    @Override
    public NetworkAddressFactory createNetworkAddressFactory() {
        return createNetworkAddressFactory(streamListenPort, multicastPort);
    }

    @Override
    public void shutdown() {
        log.fine("Shutting down default executor service");
        getDefaultExecutorService().shutdownNow();
    }

    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multicastPort) {
        return new NetworkAddressFactoryImpl(streamListenPort, multicastPort);
    }

    protected SOAPActionProcessor createSOAPActionProcessor() {
        return new SOAPActionProcessorImpl();
    }

    protected GENAEventProcessor createGENAEventProcessor() {
        return new GENAEventProcessorImpl();
    }

    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return new UDA10DeviceDescriptorBinderImpl(getNetworkAddressFactory());
    }

    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return new UDA10ServiceDescriptorBinderImpl(getNetworkAddressFactory());
    }

    protected Namespace createNamespace() {
        return new Namespace();
    }

    protected ExecutorService getDefaultExecutorService() {
        return defaultExecutorService;
    }

    protected ExecutorService createDefaultExecutorService() {
        return new DefaultUpnpServiceConfiguration.ClingExecutor();
    }
}
