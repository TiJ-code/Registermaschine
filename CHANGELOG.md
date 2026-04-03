# Version 2.0.0
## Changes
### CORE
- Added default implementation `dk.tij.registermaschine.core.compilation.internal.compiling.ConcreteCompiledStep`
### API
- Introduction new execution model of instructions with additional instruction entities:
  - `dk.tij.registermaschine.api.`
    - `compilation.compiling.ICompiledStep`
    - `instructions.`
      - `ChainedInstruction`
      - `IStepHandler`
---
## Breaking Changes
### CORE
#### Minor
- Moved `dk.tij.registermaschine.core.config.ConcreteInstructionSet` to `dk.tij.regsitermaschine.core.instructions.ConcreteInstructionSet`
#### Major
- Reworked entire implementation `dk.tij.registermaschine.core.instructions.ConcreteInstructionSet`
### API
#### Minor
- Moved `dk.tij.registermaschine.api.config.ConfigInstruction` to `dk.tij.registermaschine.api.config.model.ConfigInstruction`
- Moved `dk.tij.registermaschine.api.config.ConfigOperand` to `dk.tij.registermaschine.api.config.model.ConfigOperand`
- Changed instruction opcodes datatype to `int` instead of `byte`\
  Implementations would have to adapt this changed datatype.\
  Classes that changed are:
  - `dk.tij.registermaschine.api.`
    - `instructions.IInstructionSet`
    - `config.model.ConfigInstruction`
#### Major
- Reworked entire interface `dk.tij.registermaschine.api.instructions.IInstructionSet`
