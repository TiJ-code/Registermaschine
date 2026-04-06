package dk.tij.registermaschine.api.log;

import java.util.Arrays;

/**
 * @since 1.1.0
 * @author TiJ
 */
public enum LogLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    private static long maxCharWidth = -1;

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
