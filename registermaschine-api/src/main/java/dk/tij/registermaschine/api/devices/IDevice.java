package dk.tij.registermaschine.api.devices;

public interface IDevice {
    void setByte(long address, byte value);
    void setInt(long address, int value);

    byte getByte(long address);
    int getInt(long address);

    void registerMapping(IMemoryMapping mapping, long startAddress, long endAddress);

    long getSize();
}
