package dk.tij.registermaschine.api.config.model;

import dk.tij.registermaschine.api.conditions.ICondition;
import dk.tij.registermaschine.api.instructions.IStepHandler;

import java.util.List;

/**
 * Represents a single step within a configured instruction.
 *
 * <p>Each step consists of a handler that performs the operation, an optional
 * condition to determine if the step should execute, a list of input operand
 * names, and an optional output operand name.</p>
 *
 * @param handler   the handler instance
 * @param condition the optional condition
 * @param inputs    the optional list of input operand names
 * @param output    the optional output operand name
 */
public record ConfigStep(IStepHandler handler, ICondition condition, List<String> inputs, String output) {}
