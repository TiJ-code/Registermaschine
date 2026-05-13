package dk.tij.registermaschine.core.memory;

import dk.tij.registermaschine.api.devices.IDevice;

import java.util.ArrayList;
import java.util.List;

public final class MemoryBus {
    private static final MemoryBus INSTANCE = new MemoryBus();

    private final List<DeviceEntry> devices = new ArrayList<>();

    private MemoryBus() {}

    public static MemoryBus instance() {
        return INSTANCE;
    }

    public void registerDevice(IDevice device, long start, long end) {
        devices.add(new DeviceEntry(device, start, end));
    }

    public void setByte(long address, byte value) {
        DeviceEntry entry = findDevice(address);

        entry.device().setByte(address - entry.start(), value);
    }

    public void setInt(long address, int value) {
        DeviceEntry entry = findDevice(address);

        entry.device().setInt(address - entry.start(), value);
    }

    public byte getByte(long address) {
        DeviceEntry entry = findDevice(address);

        return entry.device().getByte(address);
    }

    public int getInt(long address) {
        DeviceEntry entry = findDevice(address);

        return entry.device().getInt(address);
    }

    private DeviceEntry findDevice(long address) {
        for (DeviceEntry entry : devices) {
            if (address >= entry.start() && address <= entry.end())
                return entry;
        }

        throw new RuntimeException("No device mapped for address " + address);
    }

    private record DeviceEntry(IDevice device, long start, long end) {}
}
