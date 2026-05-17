package dk.tij.registermaschine.api.devices;

/**
 * Represents a memory-mapped I/O region inside a device.
 *
 * <p>A memory mapping reacts to memory writes performed on a specific address range.
 * It is typically used to implement hardware behaviour such as consoles, keyboards,
 * framebuffers, timers, or other I/O peripherals.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public interface IMemoryMapping {
    /**
     * Called when a byte is written to a mapped memory address.
     *
     * @param address the absolute memory address that was written to
     * @param value the byte value written to the address
     */
    void onUpdate(long address, int value);

    /**
     * Returns the absolute start address of this memory-mapped region.
     *
     * @return the absolute start address of the mapping
     */
    long getStartAddress();

    /**
     * Returns the absolute end address of this memory-mapped region.
     *
     * @return the absolute end address of the mapping
     */
    long getEndAddress();

    /**
     * Returns the size of this memory-mapped region.
     *
     * @return the size of the mapping
     */
    long getSize();
}
