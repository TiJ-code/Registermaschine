package dk.tij.registermaschine.api.log;

/**
 * Factory responsible for creating {@link ILogger} instances.
 *
 * <p>This factory supports a pluggable logging implementation. If no custom
 * logger is configured, a default fallback implementation is used.</p>
 *
 * <p>Each created logger instance is assigned a class-based prefix
 * for easier identification of log sources.</p>
 *
 * @since 1.1.0
 * @author TiJ
 */
public final class LoggerFactory {
    /**
     * Default logger implementation used when no custom logger is configured.
     */
    private static final Class<? extends ILogger> FALLBACK = DefaultLogger.class;

    /**
     * Custom logger implementation set by the user.
     */
    private static Class<? extends ILogger> LOGGER_INSTANCE;

    /**
     * Private constructor to prevent instantiation.
     */
    private LoggerFactory() {}

    /**
     * Creates a new {@link ILogger} instance for the given class.
     *
     * <p>The returned logger will automatically include the class name
     * as its prefix.</p>
     *
     * @param clazz the class requesting the logger
     * @return a configured logger instance, or {@code null} if creation failed
     */
    public static ILogger getLogger(Class<?> clazz) {
        try {
            ILogger logInstance;
            if (LOGGER_INSTANCE != null)
                logInstance = LOGGER_INSTANCE.getDeclaredConstructor().newInstance();
            else
                logInstance = FALLBACK.getDeclaredConstructor().newInstance();
            logInstance.setClassPrefix(clazz.getName());
            return logInstance;
        } catch (Exception e) {
            System.err.println("Failed to #getLogger(): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sets a custom logger implementation to be used by the factory.
     *
     * <p>The provided class must have a no-argument constructor.</p>
     *
     * @param clazz the logger implementation class
     */
    public static void setLoggerInstance(Class<? extends ILogger> clazz) {
        LOGGER_INSTANCE = clazz;
    }
}
