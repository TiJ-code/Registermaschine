/**
 * Provides concrete implementations of the standard Registermaschine instructions.
 * <p>
 *     This package contains the operational logic for the machine, categorised into:
 * </p>
 * <ul>
 *     <li><b>Arithmetic:</b> Mathematical Operations such as {@code Addition}, {@code Subtraction}, {@code Division},
 *     and {@code Multiplication}.</li>
 *     <li><b>Data Movement:</b> Handling register and accumuulator state via {@code MoveInstruction}.</li>
 *     <li><b>Control Flow:</b> Altering the Program Counter (PC) via {@code JumpInstruction}.</li>
 *     <li><b>I/O:</b> Interacting with the external environment via {@code Input} and {@code Output}.</li>
 *     <li><b>System:</b> Lifecycle control such as the {@code HaltInstruction}.</li>
 * </ul>
 * <h3>Registration</h3>
 * These instructions are typically mapped to specific opcodes during the XML configuration phase
 * and stored within a {@code ConcreteInstructionSet}
 * @see dk.tij.registermaschine.core.instructions.api.AbstractInstruction
 *
 * @author TiJ
 */
package dk.tij.registermaschine.core.instructions;