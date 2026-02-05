package dk.tij.registermaschine.core.config;

public record ConfigOperand(Type type, Concept concept, String value) {
    public enum Type {
        REGISTER, IMMEDIATE, LABEL
    }
    
    public enum Concept {
        RESULT, OPERAND, TARGET
    } 
}
