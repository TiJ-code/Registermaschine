package dk.tij.registermaschine.api.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Default file-based implementation of the {@link ILogger} interface.
 *
 * <p>This logger writes formatted log messages to a rotating log file located
 * in a configurable directory. Log entries include a timestamp, log level,
 * class prefix, and formatted message.</p>
 *
 * <p>Features:</p>
 * <ul>
 *     <li>Thread-safe logging via class-level synchronisation</li>
 *     <li>Configurable global log level via {@link LogConfig}</li>
 *     <li>Automatic log file rotation based on file size</li>
 *     <li>Simple placeholder-based message formatting using {@code {}}</li>
 * </ul>
 *
 * <p>Log rotation strategy:</p>
 * <ul>
 *     <li>Maximum file size: {@value #MAX_FILE_SIZE} bytes</li>
 *     <li>Maximum number of rotated files: {@value #MAX_FILES}</li>
 *     <li>Files are renamed incrementally (e.g. {@code .1.log}, {@code .2.log})</li>
 * </ul>
 *
 * @since 1.1.0
 * @author TiJ
 */
public final class DefaultLogger implements ILogger {
    /**
     * Date format used for timestamps in log entries.
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Maximum size of a single log file before rotation occurs.
     */
    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    /**
     * Maximum number of rotated files to keep.
     */
    private static final int MAX_FILES = 5;

    /**
     * Shared writer instance used for all logger instances.
     */
    private static PrintWriter writer;

    /**
     * Directory where log files are stored.
     */
    private static Path logDir;

    /**
     * Prefix identifying the source class of log messages.
     */
    private String classPrefix = DefaultLogger.class.getSimpleName();

    /**
     * Creates a new {@link DefaultLogger} instance.
     *
     * <p>Initialises the log writer if necessary and checks whether
     * log rotation is required.</p>
     */
    public DefaultLogger() {
        initWriterIfNeeded();
        rotateIfNeeded();
    }

    @Override
    public void info(String msg, Object... args) {
        log(LogLevel.INFO, msg, args);
    }

    @Override
    public void debug(String msg, Object... args) {
        log(LogLevel.DEBUG, msg, args);
    }

    @Override
    public void trace(String msg, Object... args) {
        log(LogLevel.TRACE, msg, args);
    }

    @Override
    public void warn(String msg, Object... args) {
        log(LogLevel.WARN, msg, args);
    }

    @Override
    public void error(String msg, Object... args) {
        log(LogLevel.ERROR, msg, args);
    }

    /**
     * Sets the class prefix used in log output.
     *
     * @param prefix the class name or identifier
     */
    @Override
    public void setClassPrefix(String prefix) {
        this.classPrefix = prefix;
    }

    /**
     * Configures the directory where log files will be written.
     *
     * <p>The actual log directory will be a {@code logs} subdirectory
     * of thje provided path.</p>
     *
     * @param path the base path
     */
    @Override
    public void setPath(Path path) {
        if (logDir == null) {
            logDir = path.resolve("logs");
            initWriterIfNeeded();
        }
    }

    @Override
    public void log(LogLevel level, String msg, Object... args) {
        if (level.ordinal() < LogConfig.instance().getGlobalMinimumLevel().ordinal()) {
            return;
        }

        String formatted = format(msg, args);
        String timestamp = DATE_FORMAT.format(new Date());
        String lineToFormat = "%s [%" + LogLevel.getMaxCharWidth() + "s] [%s] %s";

        String line = lineToFormat.formatted(timestamp, level, classPrefix, formatted);

        synchronized (DefaultLogger.class) {
            if (writer != null) {
                writer.println(line);
                writer.flush();
                rotateIfNeeded();
            }
        }
    }

    @Override
    public void flog(String msg, Object... args) {
        String formatted = format(msg, args);
        String timestamp = DATE_FORMAT.format(new Date());
        String lineToFormat = "%s [%" + LogLevel.getMaxCharWidth() + "s] [%s] %s";
        String line = lineToFormat.formatted(timestamp, "LOG", classPrefix, formatted);

        synchronized (DefaultLogger.class) {
            if (writer != null) {
                writer.println(line);
                writer.flush();
                rotateIfNeeded();
            }
        }
    }

    /**
     * Formats a message by replacing {@code {}} placeholders
     * with the provided arguments.
     *
     * @param msg  the message template
     * @param args arguments to insert
     * @return the formatted message
     */
    private String format(String msg, Object... args) {
        if (args == null || args.length == 0) {
            return msg;
        }

        final String formattableString = msg.replace("{}", "%s");
        return formattableString.formatted(args);
    }

    /**
     * Checks whether the current log file exceeds the maximum size
     * and performs rotation if necessary.
     *
     * <p>Rotation renames existing log files and creates a new active log file.</p>
     */
    private void rotateIfNeeded() {
        if (logDir == null)
            return;

        File logFile = logDir.resolve("registermaschine.log").toFile();
        if (logFile.length() < MAX_FILE_SIZE)
            return;

        for (int i = MAX_FILES - 1; i >= 1; i--) {
            File f1 = logDir.resolve("registermaschine." + i + ".log").toFile();
            File f2 = logDir.resolve("registermaschine." + (i + 1) + ".log").toFile();
            if (f1.exists())
                f1.renameTo(f2);
        }

        File first = logDir.resolve("registermaschine.log").toFile();
        File firstRotated = logDir.resolve("registermaschine.1.log").toFile();
        first.renameTo(firstRotated);

        try {
            writer.close();
            writer = new PrintWriter(new FileWriter(first, true), true);
        } catch (IOException e) {
            System.err.println("Failed to rotate log file: " + e.getMessage());
        }
    }

    /**
     * Initialises the log writer if it has not already been created.
     *
     * <p>This method ensures that the log directory exists and opens
     * the log file in append mode.</p>
     */
    private static synchronized void initWriterIfNeeded() {
        if (logDir == null)
            return;

        if (writer != null)
            return;

        try {
            File logFile = logDir.resolve("registermaschine.log").toFile();
            logFile.getParentFile().mkdirs();
            writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true)), true);
        } catch (IOException e) {
            System.err.printf("Failed to initialise %s: %s%n", DefaultLogger.class.getSimpleName(), e.getMessage());
            e.printStackTrace();
        }
    }
}
