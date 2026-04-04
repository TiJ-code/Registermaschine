package dk.tij.registermaschine.api.compilation.pre;

public interface IPrecompilerStage<I, O> {
    O precompile(I i);
}
