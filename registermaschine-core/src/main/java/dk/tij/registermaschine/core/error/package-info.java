/**
 * Defines the exception hierarchy for the Registermaschine engine.
 * <p>
 *     This package centralises error handling to ensure that failure in different
 *     stages of the machine's lifecycle-compilation, configuration, and execution
 *     are distinguishable and carry sufficient context (such as line numbers or opcodes).
 * </p>
 * <b>Exception Categories:</b>
 * <ul>
 *     <li><b>Compilation Errors:</b> Thrown when the source code violates lexical
 *     or syntactic rules (e.g., {@code SyntaxErrorException}).</li>
 *     <li><b>Configuration Errors:</b> Thrown when the {@code .jxml} instruction
 *     sets or hardware settings are malformed.</li>
 *     <li><b>Runtime errors:</b> Thrown during the execution loop, typically
 *     representing illegal state transitions or invalid register access.</li>
 * </ul>
 *
 * @since 1.0.0
 * @author TiJ
 */
package dk.tij.registermaschine.core.error;