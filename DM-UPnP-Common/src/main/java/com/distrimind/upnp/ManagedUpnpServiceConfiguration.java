/*
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.distrimind.upnp;

import com.distrimind.flexilogxml.log.DMLogger;
import com.distrimind.upnp.binding.xml.DeviceDescriptorBinder;
import com.distrimind.upnp.binding.xml.ServiceDescriptorBinder;
import com.distrimind.upnp.model.Constants;
import com.distrimind.upnp.model.Namespace;
import com.distrimind.upnp.model.message.UpnpHeaders;
import com.distrimind.upnp.model.meta.RemoteDeviceIdentity;
import com.distrimind.upnp.model.meta.RemoteService;
import com.distrimind.upnp.model.types.ServiceType;
import com.distrimind.upnp.platform.Platform;
import com.distrimind.upnp.platform.PlatformUpnpServiceConfiguration;
import com.distrimind.upnp.transport.impl.NetworkAddressFactoryImpl;
import com.distrimind.upnp.transport.spi.DatagramIO;
import com.distrimind.upnp.transport.spi.DatagramProcessor;
import com.distrimind.upnp.transport.spi.GENAEventProcessor;
import com.distrimind.upnp.transport.spi.MulticastReceiver;
import com.distrimind.upnp.transport.spi.NetworkAddressFactory;
import com.distrimind.upnp.transport.spi.SOAPActionProcessor;
import com.distrimind.upnp.transport.spi.StreamClient;
import com.distrimind.upnp.transport.spi.StreamServer;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Adapter for CDI environments.
 *
 * @author Christian Bauer
 */

public class ManagedUpnpServiceConfiguration implements UpnpServiceConfiguration {

    final private static DMLogger log = Log.getLogger(ManagedUpnpServiceConfiguration.class);

    // TODO: All of these fields should be injected so users can provide values through CDI

    private int streamListenPort;
    private final PlatformUpnpServiceConfiguration platformUpnpServiceConfiguration;
    private ExecutorService defaultExecutorService;
    private ExecutorService defaultAndroidExecutorService;

    protected DatagramProcessor datagramProcessor;

    private SOAPActionProcessor soapActionProcessor;
    private GENAEventProcessor genaEventProcessor;

    private DeviceDescriptorBinder deviceDescriptorBinderUDA10;
    private ServiceDescriptorBinder serviceDescriptorBinderUDA10;

    private Namespace namespace;
    private NetworkAddressFactory networkAddressFactory=null;
    private int multicastPort;
    public ManagedUpnpServiceConfiguration() {
        this(Platform.getDefault());
    }
    public ManagedUpnpServiceConfiguration(Platform platform) {
        if (platform==null)
            throw new NullPointerException();
        this.platformUpnpServiceConfiguration = platform.getInstance();
    }

    public void init() throws IOException {

        this.streamListenPort = NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT;

        defaultExecutorService = createDefaultExecutorService();
        defaultAndroidExecutorService=platformUpnpServiceConfiguration.getPlatformType()==Platform.ANDROID?Platform.ANDROID.getInstance().createDefaultAndroidExecutorService():defaultExecutorService;

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
    public StreamClient<?> createStreamClient(int timeoutSeconds) {
        return platformUpnpServiceConfiguration.createStreamClient(getDefaultAndroidExecutorService(), timeoutSeconds);
    }
    protected NetworkAddressFactory getNetworkAddressFactory() {
        if (networkAddressFactory==null)
            networkAddressFactory=createNetworkAddressFactory();
        return networkAddressFactory;
    }
    @Override
    public MulticastReceiver<?> createMulticastReceiver(NetworkAddressFactory networkAddressFactory) {
        return platformUpnpServiceConfiguration.createMulticastReceiver(networkAddressFactory);
    }

    @Override
    public DatagramIO<?> createDatagramIO(NetworkAddressFactory networkAddressFactory) {
        return platformUpnpServiceConfiguration.createDatagramIO(networkAddressFactory);
    }

    @Override
    public StreamServer<?> createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return platformUpnpServiceConfiguration.createStreamServer(networkAddressFactory);
    }

    @Override
    public StreamServer<?> createStreamServer(int streamServerPort) {
        return platformUpnpServiceConfiguration.createStreamServer(streamServerPort);
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
		return platformUpnpServiceConfiguration.isReceivedSubscriptionTimeoutIgnored();
	}

    @Override
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public UpnpHeaders getDescriptorRetrievalHeaders(RemoteDeviceIdentity identity) {
        return platformUpnpServiceConfiguration.getDescriptorRetrievalHeaders(identity);
    }

    @Override
    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public UpnpHeaders getEventSubscriptionHeaders(RemoteService service) {
        return platformUpnpServiceConfiguration.getEventSubscriptionHeaders(service);
    }

    /**
     * @return Defaults to 1000 milliseconds.
     */
    @Override
    public int getRegistryMaintenanceIntervalMillis() {
        return platformUpnpServiceConfiguration.getRegistryMaintenanceIntervalMillis();
    }

    /**
     * @return Defaults to zero, disabling ALIVE flooding.
     */
    @Override
    public int getAliveIntervalMillis() {
    	return platformUpnpServiceConfiguration.getAliveIntervalMillis();
    }

    @Override
    public Integer getRemoteDeviceMaxAgeSeconds() {
        return platformUpnpServiceConfiguration.getRemoteDeviceMaxAgeSeconds();
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
        log.debug("Shutting down default executor service");
        getDefaultExecutorService().shutdownNow();
    }

    @Override
    public Platform getPlatformType() {
        return platformUpnpServiceConfiguration.getPlatformType();
    }

    protected NetworkAddressFactory createNetworkAddressFactory(int streamListenPort, int multicastPort) {
        return platformUpnpServiceConfiguration.createNetworkAddressFactory(streamListenPort, multicastPort);
    }

    protected SOAPActionProcessor createSOAPActionProcessor() {
        return platformUpnpServiceConfiguration.createSOAPActionProcessor();
    }

    protected GENAEventProcessor createGENAEventProcessor() {
        return platformUpnpServiceConfiguration.createGENAEventProcessor();
    }

    protected DeviceDescriptorBinder createDeviceDescriptorBinderUDA10() {
        return platformUpnpServiceConfiguration.createDeviceDescriptorBinderUDA10(getNetworkAddressFactory());
    }

    protected ServiceDescriptorBinder createServiceDescriptorBinderUDA10() {
        return platformUpnpServiceConfiguration.createServiceDescriptorBinderUDA10(getNetworkAddressFactory());
    }

    protected Namespace createNamespace() {
        return platformUpnpServiceConfiguration.createNamespace();
    }

    protected ExecutorService getDefaultExecutorService() {
        return defaultExecutorService;
    }

    protected ExecutorService createDefaultExecutorService() throws IOException {
        return platformUpnpServiceConfiguration.createDefaultExecutorService();
    }
    protected ExecutorService getDefaultAndroidExecutorService() {
        return defaultAndroidExecutorService;
    }
}
