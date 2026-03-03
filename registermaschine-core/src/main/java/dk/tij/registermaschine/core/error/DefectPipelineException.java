package dk.tij.registermaschine.core.error;

public class DefectPipelineException extends RuntimeException {
    public DefectPipelineException(String msg, Exception e) {
        super(msg, e);
    }
}
