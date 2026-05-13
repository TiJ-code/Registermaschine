package dk.tij.registermaschine.core.devices;

import dk.tij.registermaschine.api.devices.IDevice;

import java.util.ArrayList;
import java.util.List;

public final class DeviceHandler {
    private static final DeviceHandler INSTANCE = new DeviceHandler();

    private List<IDevice> registeredDevices = new ArrayList<>();

    private DeviceHandler() {}

    public void registerDevice(IDevice device) {
        registeredDevices.add(device);
    }

    public static DeviceHandler instance() {
        return INSTANCE;
    }
}
