package dk.tij.registermaschine.core.memory;

import dk.tij.registermaschine.api.devices.IDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Central memory bus responsible for managing the global address space.
 *
 * <p>The {@link MemoryBus} acts as the memory management unit (MMU) of the
 * Registermaschine runtime. It maps global memory addresses to registered
 * devices and forwards all memory operations to the appropriate device.</p>
 *
 * <p>Each device occupies a continuous region in the global address space.
 * The bus performs address translation by converting global addresses into
 * device-local addresses before delegating operations.</p>
 *
 * <p>This implementation currently uses a linear search for address resolution
 * and therefore has O(n) lookup complexity.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class MemoryBus {
    private static final MemoryBus INSTANCE = new MemoryBus();

    private final List<DeviceEntry> devices = new ArrayList<>();

    private MemoryBus() {}

    /**
     * Returns the singleton instance of the memory bus.
     *
     * @return the global {@link MemoryBus} instance
     */
    public static MemoryBus instance() {
        return INSTANCE;
    }

    /**
     * Registers a device in the global memory address space.
     *
     * <p>The device will be mapped to the address range
     * {@code [start, end]} (inclusive).</p>
     *
     * @param device the device to register
     * @param start the starting global memory address
     * @param end the end global memory address
     */
    public void registerDevice(IDevice device, long start, long end) {
        devices.add(new DeviceEntry(device, start, end));
    }

    /**
     * Writes a single byte to the specified global memory address.
     *
     * <p>The bus resolves the target device and forwards the write
     * using a device-local address.</p>
     *
     * @param address the global memory address
     * @param value the byte value to write
     */
    public void setByte(long address, byte value) {
        DeviceEntry entry = findDevice(address);

        entry.device().setByte(address - entry.start(), value);
    }

    /**
     * Writes a 32-bit integer (4 bytes) to the specified global memory address.
     *
     * <p>The value is forwarded to the target device, which defines the
     * actual byte layout (typically little-endian).</p>
     *
     * @param address the global memory address
     * @param value the integer value to write
     */
    public void setInt(long address, int value) {
        DeviceEntry entry = findDevice(address);

        entry.device().setInt(address - entry.start(), value);
    }

    /**
     * Reads a single byte from the specified global memory address.
     *
     * <p>The bus resolves the correct device and translates the address
     * into the device-local address space.</p>
     *
     * @param address the global memory address
     * @return the byte stored at the given address
     */
    public byte getByte(long address) {
        DeviceEntry entry = findDevice(address);

        return entry.device().getByte(address - entry.start());
    }

    /**
     * Reads a 32-bit integer (4 bytes) from the specified global memory address.
     *
     * <p>The device determines the internal byte order used for reconstruction
     * of the integer value.</p>
     *
     * @param address the global memory address
     * @return the reconstructed integer value
     */
    public int getInt(long address) {
        DeviceEntry entry = findDevice(address);

        return entry.device().getInt(address - entry.start());
    }

    /**
     * Resolves a global memory address to the corresponding device entry.
     *
     * @param address the global memory address
     * @return the device entry responsible for the address
     * @throws RuntimeException if no device is mapped to the address
     */
    private DeviceEntry findDevice(long address) {
        for (DeviceEntry entry : devices) {
            if (address >= entry.start() && address <= entry.end())
                return entry;
        }

        throw new RuntimeException("No device mapped for address " + address);
    }

    /**
     * Internal representation of a device mapping in the global address space.
     *
     * @param device the device instance
     * @param start the start address (inclusive)
     * @param end the end address (inclusive)
     */
    private record DeviceEntry(IDevice device, long start, long end) {}
}
