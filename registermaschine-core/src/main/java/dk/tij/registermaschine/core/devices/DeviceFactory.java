package dk.tij.registermaschine.core.devices;

import dk.tij.registermaschine.api.config.model.ConfigDevice;
import dk.tij.registermaschine.api.config.model.ConfigMemoryMapping;
import dk.tij.registermaschine.api.devices.IDevice;
import dk.tij.registermaschine.api.devices.IMemoryMapping;
import dk.tij.registermaschine.api.error.OutOfMemoryException;
import dk.tij.registermaschine.core.memory.MemoryBus;

import java.util.List;

/**
 * Factory responsible for constructing and initializing devices and their
 * memory mappings from configuration models.
 *
 * <p>This factory translates configuration objects into fully instantiated
 * runtime devices and registers them into the global {@link MemoryBus}.</p>
 *
 * <p>Each device is assigned a contiguous region in the global memory address
 * space. Memory mappings inside a device are resolved into absolute addresses
 * and attached to their respective device regions.</p>
 *
 * <p>All devices and mappings are instantiated via reflection using their
 * fully qualified class names defined in the configuration.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public final class DeviceFactory {
    private DeviceFactory() {}

    /**
     * Creates and registers all devices defined in the configuration list.
     *
     * <p>Each device is assigned a sequential region in the global address space.
     * After creation, devices are immediately registered with the {@link MemoryBus}.</p>
     *
     * @param configs list of device configurations to instantiate
     */
    public static void createDevices(List<ConfigDevice> configs) {
        long currentAddress = 0;

        for (ConfigDevice config : configs) {
            IDevice device = createDevice(config, currentAddress);

            long deviceStart = currentAddress;
            long deviceEnd = deviceStart + config.size() - 1;

            if (deviceEnd < deviceStart) {
                throw new OutOfMemoryException("Device size overflow detected for " + config.deviceHandler());
            }

            MemoryBus.instance().registerDevice(
                    device,
                    deviceStart,
                    deviceEnd
            );

            currentAddress = deviceEnd + 1;
        }
    }

    /**
     * Creates a single device and attaches its memory mappings.
     *
     * <p>Memory mappings are translated from device-local offsets into absolute
     * global addresses before being registered with the device.</p>
     *
     * @param config the device configuration
     * @param globalStartAddress the starting global memory address of the device
     * @return the fully initialized device instance
     */
    private static IDevice createDevice(ConfigDevice config, long globalStartAddress) {
        IDevice device = instantiateDevice(config.deviceHandler(), globalStartAddress, config.size());

        long currentOffset = 0;

        for (ConfigMemoryMapping mappingConfig : config.mappings()) {
            long size = mappingConfig.size();

            long localStart = currentOffset;
            long localEnd = localStart + size - 1;

            if (localEnd >= config.size()) {
                throw new OutOfMemoryException(
                        "Memory mapping exceeds device size. Mapping end=%d, device size=%d"
                                .formatted(localEnd, config.size())
                );
            }

            long absStart = globalStartAddress + localStart;
            long absEnd = globalStartAddress + localEnd;

            if (absEnd < absStart) {
                throw new OutOfMemoryException("Memory mapping overflow detected in device " + config.deviceHandler());
            }

            IMemoryMapping mapping = instantiateMapping(mappingConfig, absStart);

            device.registerMapping(mapping, absStart, absEnd);

            currentOffset = localEnd + 1;
        }

        return device;
    }

    /**
     * Instantiates a device using reflection.
     *
     * <p>The device class must expose a constructor with the signature:
     * {@code (long startAddress, long endAddress)}.</p>
     *
     * @param className fully qualified class name of the device implementation
     * @param startAddress global start address of the device
     * @param size size of the device in bytes
     * @return instantiated device
     * @throws RuntimeException if instantiation fails
     */
    private static IDevice instantiateDevice(String className, long startAddress, long size) {
        try {
            Class<? extends IDevice> clazz = Class.forName(className).asSubclass(IDevice.class);

            return clazz
                    .getDeclaredConstructor(long.class, long.class)
                    .newInstance(startAddress, startAddress + size - 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate device " + className, e);
        }
    }

    /**
     * Instantiates a memory mapping using reflection.
     *
     * <p>The mapping class must expose a constructor with the signature:
     * {@code (long startAddress, long size)}.</p>
     *
     * @param config memory mapping configuration
     * @param startAddress absolute start address of the mapping
     * @return instantiated memory mapping
     * @throws RuntimeException if instantiation fails
     */
    private static IMemoryMapping instantiateMapping(ConfigMemoryMapping config, long startAddress) {
        try {
            Class<? extends IMemoryMapping> clazz = Class.forName(config.memoryMappingHandler()).asSubclass(IMemoryMapping.class);

            return clazz
                    .getDeclaredConstructor(long.class, long.class)
                    .newInstance(startAddress, config.size());
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate memory mapping " + config.memoryMappingHandler(), e);
        }
    }
}
