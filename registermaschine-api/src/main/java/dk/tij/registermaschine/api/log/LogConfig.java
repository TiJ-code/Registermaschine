package dk.tij.registermaschine.api.log;

import java.util.Objects;

/**
 * Global configuration holder for the logging system.
 *
 * <p>This class provides a central place to control logging behaviour,
 * such as the minimum log level.</p>
 *
 * <p>It follows a singleton pattern accessible via {@link #instance()}.</p>
 *
 * @since 1.1.0
 * @author TiJ
 */
public final class LogConfig {
    /**
     * Local logger instance for the configuration.
     */
    private static final ILogger LOGGER = LoggerFactory.getLogger(LogConfig.class);

    /**
     * Singleton instance of this class.
     */
    private static final LogConfig INSTANCE = new LogConfig();

    /**
     * Global minimum log level. Messages below this level are ignored.
     */
    private static volatile LogLevel globalMinimumLevel = LogLevel.INFO;

    /**
     * Private constructor to prevent instantiation.
     */
    private LogConfig() {}

    /**
     * Returns the currently configured global minimum log level.
     *
     * @return the minimum log level
     */
    public LogLevel getGlobalMinimumLevel() {
        return globalMinimumLevel;
    }

    /**
     * Sets the global minimum log level.
     *
     * <p>All log messages below this level will be ignored.</p>
     *
     * @param level the new minimum log level
     * @throws NullPointerException if {@code level} is {@code null}
     */
    public void setGlobalMinimumLevel(LogLevel level) {
        Objects.requireNonNull(level, "LogLevel must not be null");
        globalMinimumLevel = level;
        LOGGER.flog("Changing global log level to {}", level);
    }

    /**
     * Returns the singleton instance of the configuration.
     *
     * @return the global {@link LogConfig} instance
     */
    public static LogConfig instance() {
        return INSTANCE;
    }
}
