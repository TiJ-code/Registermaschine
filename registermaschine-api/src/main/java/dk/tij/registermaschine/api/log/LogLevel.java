package dk.tij.registermaschine.api.log;

import java.util.Arrays;

/**
 * Represents the severity levels used by the logging system.
 *
 * <p>The order of declaration reflects increasing importance:</p>
 * <ul>
 *     <li>{@link #TRACE} - most detailed</li>
 *     <li>{@link #DEBUG}</li>
 *     <li>{@link #INFO}</li>
 *     <li>{@link #WARN}</li>
 *     <li>{@link #ERROR} - most severe</li>
 * </ul>
 *
 * @since 1.1.0
 * @author TiJ
 */
public enum LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    /**
     * Cached maximum width of all enum names.
     */
    private static long maxCharWidth = -1;

    /**
     * Returns the maximum character width of all {@link LogLevel} names.
     *
     * <p>This can be used for aligned log formatting.</p>
     *
     * @return the maximum length of any log level name
     */
    public static long getMaxCharWidth() {
        if (maxCharWidth != -1) {
            return maxCharWidth;
        }

        maxCharWidth = Arrays.stream(LogLevel.values())
                .mapToInt(l -> l.toString().length())
                .max().orElse(0);

        return maxCharWidth;
    }
}
