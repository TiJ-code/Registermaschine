/**
 * The core engine of the Registermaschine system.
 * <p>
 *     This module provides a complete virtualised execution environment, including
 *     a compilation pipeline, a configurable instruction set, and a runtime executor.
 * </p>
 *
 * <h2>Module Structure</h2>
 * The module is organised into five functional pillars:
 * <ul>
 *     <li><b>Compilation:</b> Lexing, parsing, and binary code generation.</li>
 *     <li><b>Configuration:</b> XML-based initialisation and hardware constraints.</li>
 *     <li><b>Instructions:</b> The operational logic (Arithmetic, I/O, Control Flow).</li>
 *     <li><b>Conditions:</b> Guarded execution logic using Boolean algebra.</li>
 *     <li><b>Runtime:</b> The virtual CPU, register bank, and execution loop.</li>
 * </ul>
 *
 * <h2>Encapsulation Policy</h2>
 * Internal implementation details located in {@code *.internal.*} packages
 * hidden from the module path to ensure binary compatibility and maintain
 * strict architectural boundaries.
 *
 * <h2>Extending the Instruction Set</h2>
 * To implement custom hardware operations, follow these steps:
 * <ol>
 *     <li>Extend {@link dk.tij.registermaschine.core.instructions.api.AbstractInstruction}
 *     to define the execution logic.</li>
 *     <li>Register the new instruction in the {@code .jxml} configuration file
 *     mapping it to a unique opcode.</li>
 *     <li>(Optional) Implement {@link dk.tij.registermaschine.core.conditions.api.ICondition}
 *     to create specialised execution guards.</li>
 * </ol>
 *
 * <h2>Service Provider Interface (SPI)</h2>
 * The configuration system is designed for modularity. Use the {@code .api} packages
 * to implement custom logic that integrates with the central {@code CoreConfigParser}
 *
 * @uses dk.tij.registermaschine.core.config.api.IConfigParser
 * @author TiJ
 */
module dk.tij.registermaschine.core {
    exports dk.tij.registermaschine.core.error;

    exports dk.tij.registermaschine.core.runtime;
    exports dk.tij.registermaschine.core.runtime.api;

    exports dk.tij.registermaschine.core.config;
    exports dk.tij.registermaschine.core.config.api;

    exports dk.tij.registermaschine.core.instructions;
    exports dk.tij.registermaschine.core.instructions.api;

    exports dk.tij.registermaschine.core.conditions;
    exports dk.tij.registermaschine.core.conditions.atomic;
    exports dk.tij.registermaschine.core.conditions.api;

    exports dk.tij.registermaschine.core.compilation;
    exports dk.tij.registermaschine.core.compilation.api;
    exports dk.tij.registermaschine.core.compilation.api.parsing;
    exports dk.tij.registermaschine.core.compilation.api.compiling;
    exports dk.tij.registermaschine.core.compilation.api.lexing;

    requires java.xml;
}