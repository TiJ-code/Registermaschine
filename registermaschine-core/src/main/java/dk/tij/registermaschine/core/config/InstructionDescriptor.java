package dk.tij.registermaschine.core.config;

public record InstructionDescriptor(String mnemonic, String description) {
    @Override
    public String toString() {
        return mnemonic + "[" + description + "]";
    }
}
