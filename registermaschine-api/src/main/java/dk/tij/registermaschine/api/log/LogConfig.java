package dk.tij.registermaschine.api.log;

import java.util.Objects;

/**
 * @since 1.1.0
 * @author TiJ
 */
public final class LogConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogConfig.class);
    private static final LogConfig INSTANCE = new LogConfig();

    private static volatile LogLevel globalMinimumLevel = LogLevel.INFO;

    private LogConfig() {}

    public LogLevel getGlobalMinimumLevel() {
        return globalMinimumLevel;
    }

    public void setGlobalMinimumLevel(LogLevel level) {
        Objects.requireNonNull(level, "LogLevel must not be null");
        globalMinimumLevel = level;
        LOGGER.flog("Changing global log level to {}", level);
    }

    public static LogConfig instance() {
        return INSTANCE;
    }
}
