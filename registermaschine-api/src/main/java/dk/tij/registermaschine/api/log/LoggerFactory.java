package dk.tij.registermaschine.api.log;

/**
 * @since 1.1.0
 * @author TiJ
 */
public final class LoggerFactory {
    private static final Class<? extends Logger> FALLBACK = DefaultLogger.class;

    private static Class<? extends Logger> LOGGER_INSTANCE;

    private LoggerFactory() {}

    public static Logger getLogger(Class<?> clazz) {
        try {
            Logger logInstance;
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

    public static void setLoggerInstance(Class<? extends Logger> clazz) {
        LOGGER_INSTANCE = clazz;
    }
}
