package dk.tij.registermaschine.core.config.internal;

public record ConfigOperand(Type type, Concept concept, String value) {
    public enum Type {
        REGISTER, IMMEDIATE, LABEL
    }
    
    public enum Concept {
        RESULT, OPERAND, TARGET
    } 
}
