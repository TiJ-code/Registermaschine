import tij.registermaschine.api.conditions.ICondition;
import tij.registermaschine.api.config.IConfigParser;
import tij.registermaschine.api.instructions.IStepHandler;

/**
 * Core orchestration for the Registermaschine system.
 * <p>
 *     This module coordinates configuration and compilation with the engine.
 * </p>
 *
 * <h2>Module Structure</h2>
 * The system is organised into five functional pillars:
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
 *     <li>Implement {@link IStepHandler}
 *     to define the execution logic.</li>
 *     <li>Register the new instruction in the {@code .jxml} configuration file
 *     mapping it to a unique opcode.</li>
 *     <li>(Optional) Implement {@link ICondition}
 *     to create specialised execution guards.</li>
 * </ol>
 *
 * <h2>Service Provider Interface (SPI)</h2>
 * The configuration system is designed for modularity. Use the {@code .api} packages
 * to implement custom logic that integrates with the central {@code CoreConfigParser}
 *
 * @uses IConfigParser
 * @author TiJ
 */
module tij.registermaschine.core {
    exports tij.registermaschine.core.compilation;
    exports tij.registermaschine.core.config;

    requires tij.registermaschine.api;
    requires tij.registermaschine.compiler;
    requires tij.registermaschine.pluginloader;
    requires transitive tij.registermaschine.engine;
    requires java.xml;
}
