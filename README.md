# Registermaschine

Registermaschine is a modular, extensible virtual machine framework designed for building and executing
custom instruction sets with configurable hardware behaviour.

It combines a compilation pipeline, a virtual runtime, and a configuration system into a cleanly separated architecture, making it suitable for:
- teaching low-level computation concepts
- building custom assembly-like scripting languages
- experimenting with virtual CPUs and instruction sets
- embedding deterministic execution environments into applications

## Features
- Modular architecture (API, Core, Instruction Packs)
- Custom instruction sets via configuration
- Full compilation pipeline (Lexer → Parser → Compiler)
- Virtual CPU with register-based execution
- Condition system (Boolean logic for control flow)
- XML-based configuration system (`.jxml`)
- Strict encapsualtion via Java modules

## Project Structure

The project is split into three main Maven modules:
- `registermaschine-api`
- `registermaschine-core`
- `registermaschine-default-instructions`

---

### `registermaschine-api`
The public API layer defining all contracts of the system.
#### Responsibilities
- Compilation interfaces (`ICompiler`, `ILexer`, `IParser`)
- Syntax tree abstraction
- Instruction and operand models
- Runtime interfaces (`IExecutionContext`, snapshots, listeners)
- Configuration interfaces
- Error model (typed exceptions)

#### Purpose
This module is implementation-agnostic and stable.

It allows you to:
- build your own compiler or runtime
- implement custom instructions
- integrate Registermaschine into external systems

---

### `registermaschine-core`
The reference implementation of the Registermaschine system.\
This is the central connection point and should be extended upon not done from scratch.
#### Core Components
1. Compilation Pipeline\
  Located in `core.compilation`:
   - `ConcreteLexer`
   - `ConcreteParser`
   - `ConcreteCompiler`
   - `Pipeline`\
Handles `Source → Tokens → AST → Compiled Program`
2. Runtime\
   Located in `core.runtime`
   - `ConcreteExecutionContext`
   - `Executor`

   Provides:
   - register-based execution
   - instruction dispatch
   - step-by-step evaluation
   - listener support for UI/debugging

3. Instruction System
   -  `ConcreteInstructionSet`
   - Dynamically loaded via configuration

4. Condition System\
   Located in `core.conditions`
   - Boolean logic (AND, OR, NOT)
   - Numeric checks (e.g. zero, negative)

   Used for:
   - conditional execution
   - guarded instructions
5. Configuration System\
   Located in `core.config`:
   - XML-based
   - Parsed via `CoreConfigParser`
   
    Supports:
   - instruction definitions
   - operand definitions
   - condition macros
   - hardware constraints

#### Encapsualtion
All internal implementation details are placed in:
`*.internal.*`

These packages are not exported via JPMS.

This ensures:
- API stability
- safe-refactoring
- clear separation between API and implementation

---

### `registermaschine-default-instructions`
A standard instruction pack providing a basic instruction set.
#### Included Instructions
- Arithmetic: `ADD`, `SUB`, `MUL`, `DIV`
- Data movement: `MOV`
- Control flow: `JMP`, `HLT`
- I/O: `INP`, `OUT`
#### Purpose
- Acts as a reference implementation
- Provides a ready-to-use instruction set
- Can be replaced or extended with custom instructions
- Tailored by Prof. Dr. Tilo Strutz

---

## Configuration `.jxml`
Registermaschine uses a custom XML-based format for defining:
- instructions
- operands
- condition macros
- system constraints

Examples:
- `configuration.jxml` 
- `core_condition_macros.jxml` 
- `default.instructions.jxml`

Validation is performed using DTDs:
`resources/dtd/`

---

## Execution Model
1. Load configuration (`.jxml`)
2. Build instruction set
3. Compile source code
4. Execute program in virtual runtime

---

## Extending the System
### Custom Instruction Handler
Dependency: `registermaschine-api`
1. Extend `AbstractInstruction`
2. Implement execution logic
3. Register in `.jxml` instruction set
   ```xml
   <instruction name="MULADD" handler="your.package.CustomInstruction">...</instruction> 
   ```
   
_Look in the Wiki for more information_

### Custom Conditions
Dependency: `registermaschine-api`
1. Implement `ICondition`
2. Register via configuration or macro system

_Look in the Wiki for more information_

---

## Upcoming:
### v1.0.1
- Several bug fixes (#46 #47 #48 #80)
- source documentation (#78)

### v1.1.0
- Logging API (#77)
- Plugin System (#69)

### tbd
...

### v2.0.0
- Multiple Instruction Handler (#28)
- External Devices (#67)
- Variables