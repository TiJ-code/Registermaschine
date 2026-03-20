package dk.tij.registermaschine.api.error;

public class DefectPipelineException extends RuntimeException {
    public DefectPipelineException(String msg, Exception e) {
        super(msg, e);
    }
}
