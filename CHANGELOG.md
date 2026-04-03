# Version 2.0.0
## Changes
### CORE
- Added default implementation `dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledStep`
- Added instruction step precompiler in `dk.tij.registermaschine.core.compilation.internal.pre.`\
  `InstructionPrecompiler` and `InstructionStepPrecompiler` precompile instructions and their steps into `dk.tij.registermaschine.api.instructions.ChainedInstruction` instances\
  that can be reused throughout the compilation pipeline.
### API
- Introduction new execution model of instructions with additional instruction entities:
  - `dk.tij.registermaschine.api.`
    - `compilation.compiling.ICompiledStep`
    - `instructions.`
      - `ChainedInstruction`
      - `IStepHandler`
- Introducing new exception `dk.tij.registermaschine.api.error.InvalidOperandException` that should be thrown, if the configured (input and output) operands are\
  incompatible with a `dk.tij.registermaschine.api.instructions.IStepHandler` instance.
### Default Instruction Set & Handlers
- Reworked instruction handler implementations to instruction step implementations.
> [!NOTE]
> These implementations are still specifically for "Registermaschine"-Instruction set. For more precise information, refer to Prof. Dr. Tilo Strutz.
---
## Breaking Changes
### CORE
#### Minor
- Moved `dk.tij.registermaschine.core.config.ConcreteInstructionSet` to `dk.tij.regsitermaschine.core.instructions.ConcreteInstructionSet`
- Changed opcode datatype from `byte` to `int` in
  - `dk.tij.registermaschine.core.`
    - `compilation.internal.compiling.ConcreteCompiledInstruction`
#### Major
- Reworked entire implementation `dk.tij.registermaschine.core.instructions.ConcreteInstructionSet`.
- Reworked instruction parsing implementation in `dk.tij.registermaschine.core.config.internal.parsers.InstructionParser` to follow the new scheme.
- Reworked `dk.tij.registermaschine.core.runtime.Executor` to use the new instruction model.
### API
#### Minor
- Moved `dk.tij.registermaschine.api.config.ConfigInstruction` to `dk.tij.registermaschine.api.config.model.ConfigInstruction`
- Moved `dk.tij.registermaschine.api.config.ConfigOperand` to `dk.tij.registermaschine.api.config.model.ConfigOperand`
- Changed instruction opcodes datatype to `int` instead of `byte`\
  Implementations would have to adapt this changed datatype.\
  Classes that changed are:
  - `dk.tij.registermaschine.api.`
    - `compilation.compiling.ICompiledInstruction` 
    - `instructions.IInstructionSet`
    - `config.model.ConfigInstruction`
- Replaced `dk.tij.registermaschine.api.config.model.ConfigInstruction` field `AbstractInstruction handler` with `List<ConfigStep> steps`
- Added `String name` field to `dk.tij.registermaschine.api.config.model.ConfigOperand`
#### Major
- Reworked entire interface `dk.tij.registermaschine.api.instructions.IInstructionSet`
### Default Instruction Set & Handlers
#### Major
- Migrated the `default.instructions.jxml` file to the new instruction step scheme
- Renamed all `dk.tij.registermaschine.instructions.*Instruction` files to `dk.tij.registermaschine.instructions.*StepHandler`
- Migrated all `dk.tij.registermaschine.instructions.*StepHandler` to the new instruction step scheme.\
  This changed their implementation from `AbstractInstruction` to `IStepHandler`
