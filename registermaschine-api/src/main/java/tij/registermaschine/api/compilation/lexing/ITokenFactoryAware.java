package tij.registermaschine.api.compilation.lexing;

/**
 * Identifies a lexing stage that accepts a token factory.
 *
 * @since 2.0.0
 * @author TiJ
 */
@FunctionalInterface
public interface ITokenFactoryAware {
    /**
     * Sets the factory used to create tokens.
     *
     * @param factory the token factory to use
     */
    void setTokenFactory(ITokenFactory factory);
}
