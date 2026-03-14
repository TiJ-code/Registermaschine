package dk.tij.registermaschine.core.instructions.internal;

import dk.tij.registermaschine.core.instructions.api.IStepHandler;

import java.util.HashMap;
import java.util.Map;

public final class StepHandlerRegistry {
    private static final Map<Class<? extends IStepHandler>, IStepHandler> HANDLERS = new HashMap<>();

    private StepHandlerRegistry() {}

    public static void registerHandler(IStepHandler handler) {
        HANDLERS.put(handler.getClass(), handler);
    }

    public static IStepHandler getHandler(Class<? extends IStepHandler> handlerClass) {
        return HANDLERS.get(handlerClass);
    }
}
