package dk.tij.registermaschine.api.config.model;

/**
 * Configuration model representing a memory mapping inside a device.
 *
 * <p>A memory mapping defines a region of a device that is handled by a
 * specific memory-mapped handler (e.g. console output, framebuffer, I/O registers).</p>
 *
 * @param memoryMappingHandler the fully qualified class name of the memory mapping
 * @param size the size of the mapped memory region in bytes
 *
 * @since 2.0.0
 * @author TiJ
 */
public record ConfigMemoryMapping(String memoryMappingHandler, long size) {}
