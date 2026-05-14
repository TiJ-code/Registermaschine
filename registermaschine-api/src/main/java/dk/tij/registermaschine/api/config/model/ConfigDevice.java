package dk.tij.registermaschine.api.config.model;

import java.util.List;

/**
 * Configuration model representing an external device in the system.
 *
 * <p>A device is a memory-mapped component that occupies a contiguous region
 * of the global address space and may contain one or more internal memory mappings
 * (e.g. MMIO regions).</p>
 *
 * @param deviceHandler the fully qualified class name of the device implementation
 * @param size the total size of the device in bytes
 * @param mappings the list of memory mappings contained within this device
 *
 * @since 2.0.0
 * @author TiJ
 */
public record ConfigDevice(String deviceHandler, long size, List<ConfigMemoryMapping> mappings) {}
