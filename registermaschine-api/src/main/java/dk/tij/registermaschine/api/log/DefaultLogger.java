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
 * @since 1.1.0
 * @author TiJ
 */
public final class DefaultLogger implements Logger {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final int MAX_FILES = 5;

    private static PrintWriter writer;
    private static Path logDir;

    private String classPrefix = DefaultLogger.class.getSimpleName();

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

    @Override
    public void setClassPrefix(String prefix) {
        this.classPrefix = prefix;
    }

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

    private String format(String msg, Object... args) {
        if (args == null || args.length == 0) {
            return msg;
        }

        final String formattableString = msg.replace("{}", "%s");
        return formattableString.formatted(args);
    }

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
