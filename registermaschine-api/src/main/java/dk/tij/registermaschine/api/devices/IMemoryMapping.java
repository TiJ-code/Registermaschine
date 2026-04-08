package dk.tij.registermaschine.api.devices;

public interface IMemoryMapping {
    void onUpdate(long address, int value);

    long getStartAddress();
    long getEndAddress();

    long getSize();
}
