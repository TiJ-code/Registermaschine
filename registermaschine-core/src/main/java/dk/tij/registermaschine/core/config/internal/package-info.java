/**
 * Implementation-specific parsing logic and condition-macro resolution.
 * <p>
 *     This internal package contains the concrete logic required to transform raw XML
 *     and string data into functional machine components. It is split into two primary domains:
 * </p>
 * <p><b>Note:</b> Classes in this package are for internal use only and should not be accessed directly
 * by external modules.</p>
 * <h5>1. XML Domain (parsers)</h5>
 * Contains specialised parsers like {@code InstructionParser} and {@code SettingsParser}
 * which translate XML elements into {@link dk.tij.registermaschine.core.config.model.ConfigInstruction} objects
 * <h5>2. Condition Domain (conditions)</h5>
 * A sub-system dedicated to the recursive parsing of composite conditions. It handles the conversion
 * of string expressing (e.g., {@code !core.conditions.LessThanZeroCondition * !core.conditions.EqualsZeroCondition}
 * into executable {@link dk.tij.registermaschine.core.conditions.api.ICondition} trees using a dedicated Lexer,
 * Parser, and AST (Abstract Syntax Tree)
 *
 * @author TiJ
 */
package dk.tij.registermaschine.core.config.internal;