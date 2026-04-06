package dk.tij.registermaschine.api.log;

import java.nio.file.Path;

/**
 * General logging interface used throughout the Registermaschine.
 *
 * <p>This interface defines a structured logging system with multiple
 * severity levels, formatted message support, and optional output configuration.</p>
 *
 * <p>Implementations may choose how and where messages are written
 * (e.g., console, file, remote logging service).</p>
 *
 * <p>Message formatting follows a placeholder style where {@code {}}
 * tokens are replaced by the provided arguments.</p>
 *
 * @since 1.1.0
 * @author TiJ
 */
public interface ILogger {
    /**
     * Logs a message at {@link LogLevel#INFO} level.
     *
     * @param msg  the message template
     * @param args optional arguments replacing {@code {}} placeholder
     */
    void info(String msg, Object... args);

    /**
     * Logs a message at {@link LogLevel#DEBUG} level.
     *
     * @param msg  the message template
     * @param args optional arguments replacing {@code {}} placeholder
     */
    void debug(String msg, Object... args);

    /**
     * Logs a message at {@link LogLevel#TRACE} level.
     *
     * @param msg  the message template
     * @param args optional arguments replacing {@code {}} placeholder
     */
    void trace(String msg, Object... args);

    /**
     * Logs a message at {@link LogLevel#WARN} level.
     *
     * @param msg  the message template
     * @param args optional arguments replacing {@code {}} placeholder
     */
    void warn(String msg, Object... args);

    /**
     * Logs a message at {@link LogLevel#ERROR} level.
     *
     * @param msg  the message template
     * @param args optional arguments replacing {@code {}} placeholder
     */
    void error(String msg, Object... args);

    /**
     * Logs a message with a custom {@link LogLevel}.
     *
     * @param level the severity level
     * @param msg   the message template
     * @param args  optional arguments replacing {@code {}} placeholder
     */
    void log(LogLevel level, String msg, Object... args);

    /**
     * Forces a log message regardless of the currently configured minimum log level.
     *
     * <p>This is typically used for important system-level messages that should
     * always be visible.</p>
     *
     * @param msg  the message template
     * @param args optional arguments replacing {@code {}} placeholder
     */
    void flog(String msg, Object... args);

    /**
     * Sets a prefix (typically the class name) used to identify the source
     * of log messages
     *
     * @param prefix the prefix to prepend to log entries
     */
    void setClassPrefix(String prefix);

    /**
     * Sets the output path for this logger.
     *
     * <p>Implementations may interpret this as a file path or directory
     * depending on their design.</p>
     *
     * @param path the output path
     */
    void setPath(Path path);
}
