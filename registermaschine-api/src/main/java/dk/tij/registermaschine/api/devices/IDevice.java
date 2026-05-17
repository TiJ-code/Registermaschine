package dk.tij.registermaschine.api.devices;

/**
 * Represents a memory-mapped device in the Registermaschine address space.
 *
 * <p>A device is a contiguous region of memory that can store and retrieve values
 * and may expose internal memory-mapped subregions (MMIO mappings) for I/O behaviour.</p>
 *
 * <p>Devices are addressed using absolute memory addresses provided by the system
 * memory bus. Each device is responsible for translating global addresses into
 * its internal address space.</p>
 *
 * @since 2.0.0
 * @author TiJ
 */
public interface IDevice {
    /**
     * Writes a single byte to the specified device-local address.
     *
     * @param address the device-local memory address
     * @param value the byte value to store
     */
    void setByte(long address, byte value);

    /**
     * Writes a 32-bit integer (4 bytes) starting at the specified device-local address.
     *
     * <p>The byte or is little-endian.</p>
     *
     * @param address the device-local starting address
     * @param value the integer value to store
     */
    void setInt(long address, int value);

    /**
     * Reads a single byte from the specified device-local address.
     *
     * @param address the device-local memory address
     * @return the byte stored at the given address
     */
    byte getByte(long address);

    /**
     * Reads a 32-bit integer (4 bytes) starting at the specified device-local address.
     *
     * <p>The byte order is little-endian.</p>
     *
     * @param address the device-local starting address
     * @return the reconstructed integer value
     */
    int getInt(long address);

    /**
     * Registers a memory-mapped region inside this device.
     *
     * <p>Mappings allow parts of the device address space to trigger side effects
     * such as I/O operations or hardware emulation logic.</p>
     *
     * @param mapping the memory mapping implementation
     * @param startAddress the absolute start address of the mapping
     * @param endAddress the absolute end address of the mapping
     */
    void registerMapping(IMemoryMapping mapping, long startAddress, long endAddress);

    /**
     * Returns the global start address of this device in the system memory space.
     *
     * @return the start address of the device
     */
    long getStartAddress();

    /**
     * Returns the global end address of this device in the system memory space.
     *
     * @return the end address of the device
     */
    long getEndAddress();

    /**
     * Returns the total size of the device memory in bytes.
     *
     * @return the device size
     */
    long getSize();
}
