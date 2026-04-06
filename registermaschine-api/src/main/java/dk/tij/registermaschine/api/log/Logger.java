package dk.tij.registermaschine.api.log;

import java.nio.file.Path;

/**
 * @since 1.1.0
 * @author TiJ
 */
public interface Logger {
    void info(String msg, Object... args);
    void debug(String msg, Object... args);
    void trace(String msg, Object... args);
    void warn(String msg, Object... args);
    void error(String msg, Object... args);
    void log(LogLevel level, String msg, Object... args);
    void flog(String msg, Object... args);
    void setClassPrefix(String prefix);
    void setPath(Path path);
}
