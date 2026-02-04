package dk.tij.registermaschine.core.config.internal.conditions;

import dk.tij.registermaschine.core.conditions.api.ICondition;
import dk.tij.registermaschine.core.conditions.internal.AndCondition;
import dk.tij.registermaschine.core.conditions.internal.NotCondition;
import dk.tij.registermaschine.core.conditions.internal.OrCondition;
import dk.tij.registermaschine.core.config.CoreConfig;
import dk.tij.registermaschine.core.config.internal.conditions.nodes.*;
import dk.tij.registermaschine.core.error.ClassInstantiationException;
import dk.tij.registermaschine.core.error.ConditionParseException;

import java.util.*;

public final class ConditionBuilder {
    private ConditionBuilder() {}

    public static ICondition build(String conditionString)
           throws ConditionParseException, ClassInstantiationException {
        if (conditionString == null || conditionString.isEmpty())
            return null;

        List<ConditionToken> tokens = ConditionLexer.tokenize(conditionString);
        ConditionNode syntaxTree = ConditionParser.parse(tokens);

        return new ConditionBuilder().buildCondition(syntaxTree);
    }

    private ICondition buildCondition(ConditionNode node)
            throws ConditionParseException, ClassInstantiationException {
        return buildCondition(node, new HashSet<>());
    }

    private ICondition buildCondition(ConditionNode node, Set<String> expansionChain)
            throws ConditionParseException, ClassInstantiationException {
        if (node instanceof MacroNode(String macroName)) {
            String macroValue = CoreConfig.CONDITION_MACROS.get(macroName);

            if (expansionChain.contains(macroValue)) {
                throw new IllegalStateException("Circular macro dependency detected: " + expansionChain + " -> " + macroName);
            }

            if (macroValue == null) {
                throw new IllegalStateException("Unknown condition macro: " + macroName);
            }

            expansionChain.add(macroName);

            List<ConditionToken> tokens = ConditionLexer.tokenize(macroValue);
            ConditionNode macroSyntaxTree = ConditionParser.parse(tokens);

            ICondition result = buildCondition(macroSyntaxTree, expansionChain);

            expansionChain.remove(macroName);

            return result;
        }

        if (node instanceof LeafNode(String className)) {
            try {
                Class<?> clazz;
                if (className.startsWith(CoreConfig.CORE_IMPLEMENTATION_PREFIX))
                    clazz = Class.forName(CoreConfig.CORE_CLASS_PATH_PREFIX + className);
                else
                    clazz = Class.forName(className);
                return (ICondition) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new ClassInstantiationException("Could not instantiate condition: " + className, e);
            }
        }

        if (node instanceof NotNode(ConditionNode inner)) {
            return new NotCondition(buildCondition(inner));
        }

        if (node instanceof OrNode(ConditionNode left, ConditionNode right)) {
            return new OrCondition(
                    buildCondition(left),
                    buildCondition(right)
            );
        }

        if (node instanceof AndNode(ConditionNode left, ConditionNode right)) {
            return new AndCondition(
                    buildCondition(left),
                    buildCondition(right)
            );
        }

        throw new IllegalStateException("Unknown ConditionNode: " + node);
    }
}
