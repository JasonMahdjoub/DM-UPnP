package example.binarylight;

import com.distrimind.flexilogxml.concurrent.ThreadType;
import com.distrimind.upnp.controlpoint.ActionCallback;
import com.distrimind.upnp.model.action.ActionInvocation;
import com.distrimind.upnp.model.message.UpnpResponse;
import com.distrimind.upnp.model.message.header.STAllHeader;
import com.distrimind.upnp.model.meta.RemoteDevice;
import com.distrimind.upnp.model.meta.Service;
import com.distrimind.upnp.model.types.InvalidValueException;
import com.distrimind.upnp.model.types.ServiceId;
import com.distrimind.upnp.model.types.UDAServiceId;
import com.distrimind.upnp.registry.DefaultRegistryListener;
import com.distrimind.upnp.registry.Registry;
import com.distrimind.upnp.registry.RegistryListener;
import com.distrimind.upnp.UpnpService;
import com.distrimind.upnp.UpnpServiceImpl;

@SuppressWarnings({"PMD.SystemPrintln", "PMD.DoNotTerminateVM"})
public class BinaryLightClient implements Runnable {

    public static void main(String[] args) throws Exception {
        // Start a user thread that runs the UPnP stack
        Thread clientThread = ThreadType.VIRTUAL_THREAD_IF_AVAILABLE.startThread(new BinaryLightClient());
        clientThread.setDaemon(false);
        clientThread.start();

    }

    @Override
	public void run() {
        try {

            UpnpService upnpService = new UpnpServiceImpl();

            // Add a listener for device registration events
            upnpService.getRegistry().addListener(
                    createRegistryListener(upnpService)
            );

            // Broadcast a search message for all devices
            upnpService.getControlPoint().search(
                    new STAllHeader()
            );

        } catch (Exception ex) {
            System.err.println("Exception occured: " + ex);
            System.exit(1);
        }
    }

    // DOC: REGISTRYLISTENER
    RegistryListener createRegistryListener(final UpnpService upnpService) {
        return new DefaultRegistryListener() {

            final ServiceId serviceId = new UDAServiceId("SwitchPower");

            @Override
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

                Service<?, ?, ?> switchPower;
                if ((switchPower = device.findService(serviceId)) != null) {

                    System.out.println("Service discovered: " + switchPower);
                    executeAction(upnpService, switchPower);

                }

            }

            @Override
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                Service<?, ?, ?> switchPower;
                if ((switchPower = device.findService(serviceId)) != null) {
                    System.out.println("Service disappeared: " + switchPower);
                }
            }

        };
    }
    // DOC: REGISTRYLISTENER
    // DOC: EXECUTEACTION
    void executeAction(UpnpService upnpService, Service<?, ?, ?> switchPowerService) {

            ActionInvocation<?> setTargetInvocation =
                    new SetTargetActionInvocation(switchPowerService);

            // Executes asynchronous in the background
            upnpService.getControlPoint().execute(
                    new ActionCallback(setTargetInvocation) {

                        @Override
                        public void success(ActionInvocation<?> invocation) {
                            assert invocation.getOutput().isEmpty();
                            System.out.println("Successfully called action!");
                        }

                        @Override
                        public void failure(ActionInvocation<?> invocation,
                                            UpnpResponse operation,
                                            String defaultMsg) {
                            System.err.println(defaultMsg);
                        }
                    }
            );

    }

    static class SetTargetActionInvocation extends ActionInvocation<Service<?, ?, ?>> {

        SetTargetActionInvocation(Service<?, ?, ?> service) {
            super(service.getAction("SetTarget"));
            try {

                // Throws InvalidValueException if the value is of wrong type
                setInput("NewTargetValue", true);

            } catch (InvalidValueException ex) {
                System.err.println(ex.getMessage());
                System.exit(1);
            }
        }
    }
    // DOC: EXECUTEACTION
}
