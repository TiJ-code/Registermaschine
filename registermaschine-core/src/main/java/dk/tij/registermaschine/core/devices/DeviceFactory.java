package dk.tij.registermaschine.core.devices;

import dk.tij.registermaschine.api.config.model.ConfigDevice;
import dk.tij.registermaschine.api.config.model.ConfigMemoryMapping;
import dk.tij.registermaschine.api.devices.IDevice;
import dk.tij.registermaschine.api.devices.IMemoryMapping;
import dk.tij.registermaschine.core.memory.MemoryBus;

import java.util.List;

public final class DeviceFactory {
    private DeviceFactory() {}

    public static void createDevices(List<ConfigDevice> configs) {
        long currentAddress = 0;

        for (ConfigDevice config : configs) {
            IDevice device = createDevice(config, currentAddress);

            MemoryBus.instance().registerDevice(
                    device,
                    currentAddress,
                    currentAddress + config.size() - 1
            );

            currentAddress += config.size();
        }
    }

    private static IDevice createDevice(ConfigDevice config, long globalStartAddress) {
        IDevice device = instantiateDevice(config.deviceHandler(), globalStartAddress, config.size());

        long currentOffset = 0;

        for (ConfigMemoryMapping mappingConfig : config.mappings()) {
            long size = mappingConfig.size();

            long localStart = currentOffset;
            long localEnd = localStart + size - 1;

            if (localEnd >= config.size()) {
                throw new RuntimeException(
                        "Memory mapping exceeds device size. Mapping end=%d, device size=%d"
                                .formatted(localEnd, config.size())
                );
            }

            long absStart = globalStartAddress + localStart;
            long absEnd = globalStartAddress + localEnd;

            IMemoryMapping mapping = instantiateMapping(mappingConfig, absStart);

            device.registerMapping(mapping, absStart, absEnd);

            currentOffset = localEnd + 1;
        }

        return device;
    }

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
