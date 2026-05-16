package dk.tij.registermaschine.api.instructions;

import java.util.function.Function;

public interface IStepHandlerRegistry {
    void register(Class<? extends IStepHandler> clazz, IStepHandler handler);

    IStepHandler get(Class<? extends IStepHandler> clazz);

    IStepHandler getOrCreate(Class<? extends IStepHandler> clazz,
                             Function<Class<? extends IStepHandler>, IStepHandler> function);
}
