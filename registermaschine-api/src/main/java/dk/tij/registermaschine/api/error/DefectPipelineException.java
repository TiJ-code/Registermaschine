package dk.tij.registermaschine.api.error;

/**
 * Thrown when an instruction pipeline is broken or cannot be executed
 * properly due to internal errors.
 *
 * <p>This exception wraps the underlying exception that caused the pipeline
 * failure.</p>
 *
 * @since 1.0.0
 * @author TiJ
 */
public class DefectPipelineException extends RuntimeException {
    public DefectPipelineException(String msg, Exception e) {
        super(msg, e);
    }
}
