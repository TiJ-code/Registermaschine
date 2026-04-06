package dk.tij.registermaschine.core.config.internal.conditions;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.error.ClassInstantiationException;
import dk.tij.registermaschine.api.error.ConditionParseException;
import dk.tij.registermaschine.core.conditions.AndCondition;
import dk.tij.registermaschine.core.conditions.NotCondition;
import dk.tij.registermaschine.core.conditions.OrCondition;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.AndNode;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.ConditionToken;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.LeafNode;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.MacroNode;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.NotNode;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.OrNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The high-level builder responsible for transforming condition
 * strings into executable {@link ICondition} objects.
 *
 * <p>This class coordinates the full compilation pipeline:</p>
 * <ol>
 *     <li><b>Lexing:</b> Breaking the string into tokens.</li>
 *     <li><b>Parsing:</b> Creating an Abstract Syntax Tree (AST).</li>
 *     <li><b>Assembly:</b> Recursively building real condition objects, resolving macros,
 *                          and instantiating classes via reflection.</li>
 * </ol>
 *
 * @since 1.0.0
 * @author TiJ
 */
public final class ConditionBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionBuilder.class);

    /**
     * Private constructor to enforce static entry point usage
     */
    private ConditionBuilder() {}

    /**
     * Entry point to create a condition from a string.
     *
     * @param conditionString The raw logic string (e.g., "FeatureA * !@MACRO_B")
     * @return An executable {@link ICondition} or {@code null} if the input is empty.
     * @throws ConditionParseException if the syntax is malformed
     * @throws ClassInstantiationException if a referenced condition class cannot be loaded or instantiated
     */
    public static ICondition build(String conditionString)
           throws ConditionParseException, ClassInstantiationException {
        if (conditionString == null || conditionString.isEmpty())
            return null;

        LOGGER.debug("Running conditionString tokenization");
        List<ConditionToken> tokens = ConditionLexer.tokenize(conditionString);
        LOGGER.debug("Finished running conditionString tokenization");

        LOGGER.debug("Running token parsing");
        ConditionNode syntaxTree = ConditionParser.parse(tokens);
        LOGGER.debug("Finished token parsing");

        LOGGER.debug("Building actual condition tree");
        return new ConditionBuilder().buildCondition(syntaxTree);
    }

    /**
     * Overload for initial recursive call
     * @param node The root AST node
     * @return The built {@link ICondition}
     * @throws ConditionParseException if the syntax is malformed
     * @throws ClassInstantiationException if a referenced condition class cannot be loaded or instantiated
     */
    private ICondition buildCondition(ConditionNode node)
            throws ConditionParseException, ClassInstantiationException {
        LOGGER.debug("Building condition tree with empty expansionChain");
        return buildCondition(node, new HashSet<>());
    }

    /**
     * Recursively transform AST nodes into functional Condition objects.
     *
     * @param node The current AST node being processed
     * @param expansionChain A set used to track macro resolutions and prevent infinite loop
     * @return The built {@link ICondition}
     * @throws ConditionParseException if the syntax is malformed
     * @throws ClassInstantiationException if a referenced condition class cannot be loaded or instantiated
     */
    private ICondition buildCondition(ConditionNode node, Set<String> expansionChain)
            throws ConditionParseException, ClassInstantiationException {
        if (node instanceof MacroNode(String macroName)) {
            LOGGER.debug("Building macro condition");
            String macroValue = CoreConfig.CONDITION_MACROS.get(macroName);

            if (expansionChain.contains(macroValue)) {
                throw new IllegalStateException("Circular macro dependency detected: " + expansionChain + " -> " + macroName);
            }

            if (macroValue == null) {
                throw new IllegalStateException("Unknown condition macro: " + macroName);
            }

            expansionChain.add(macroName);
            LOGGER.debug("Adding macro condition {} to expansion chain to prevent circular dependencies", node);

            LOGGER.debug("Running macroValue tokenization");
            List<ConditionToken> tokens = ConditionLexer.tokenize(macroValue);
            LOGGER.debug("Finished running macroValue tokenization");

            LOGGER.debug("Running macro token parsing");
            ConditionNode macroSyntaxTree = ConditionParser.parse(tokens);
            LOGGER.debug("Finished running macro token parsing");

            LOGGER.debug("Building actual macro condition tree");
            ICondition result = buildCondition(macroSyntaxTree, expansionChain);

            expansionChain.remove(macroName);
            LOGGER.debug("Removing macro condition {} from expansion chain", node);

            return result;
        }

        if (node instanceof LeafNode(String className)) {
            try {
                Class<?> clazz;
                if (className.startsWith(CoreConfig.CORE_IMPLEMENTATION_PREFIX))
                    clazz = Class.forName(CoreConfig.CORE_CLASS_PATH_PREFIX + className);
                else
                    clazz = Class.forName(className);
                LOGGER.debug("Loading conditional class {}", clazz);
                return (ICondition) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new ClassInstantiationException("Could not instantiate condition: " + className, e);
            }
        }

        if (node instanceof NotNode(ConditionNode inner)) {
            LOGGER.debug("Building unary NOT condition tree");
            return new NotCondition(buildCondition(inner));
        }

        if (node instanceof OrNode(ConditionNode left, ConditionNode right)) {
            LOGGER.debug("Building binary OR condition tree");
            return new OrCondition(
                    buildCondition(left),
                    buildCondition(right)
            );
        }

        if (node instanceof AndNode(ConditionNode left, ConditionNode right)) {
            LOGGER.debug("Building binary AND condition tree");
            return new AndCondition(
                    buildCondition(left),
                    buildCondition(right)
            );
        }

        throw new IllegalStateException("Unknown ConditionNode: " + node);
    }
}
