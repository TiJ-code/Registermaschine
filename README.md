# Registermaschine

Registermaschine is a modular virtual machine system designed to execute custom instruction sets with configurable hardware behaviour. It provides a complete runtime environment, compilation pipeline, and APIs for building interactive or console-based applications.

## Project Structure

The project is split into three main modules:

### `registermaschine-core`
The core engine of the Registermaschine system.

#### Overview
This module provides:

- A virtual CPU and register bank
- A compilation pipeline (lexing, parsing, code generation)
- A configurable instruction set
- Runtime execution loop

#### Module Structure
The core is organised into five functional pillars:

1. **Compilation** – Lexing, parsing, and binary code generation.
2. **Configuration** – XML-based initialisation and hardware constraints.
3. **Instructions** – Operational logic including arithmetic, I/O, and control flow.
4. **Conditions** – Guarded execution logic using Boolean algebra.
5. **Runtime** – The virtual CPU, register bank, and execution loop.

#### Encapsulation Policy
Internal implementation details are located in `*.internal.*` packages and hidden from the module path to ensure binary compatibility and maintain strict architectural boundaries.

#### Extending the Instruction Set
To implement custom hardware operations:

1. Extend `AbstractInstruction` to define execution logic.
2. Register the new instruction in the `.jxml` configuration file, mapping it to a unique opcode.
3. Optionally, implement `ICondition` to create specialised execution guards.

#### Service Provider Interface (SPI)
The configuration system is modular. Use the `.api` packages to implement custom logic integrating with the central `CoreConfigParser`.

---

### `registermaschine-console`
A console-based implementation similar to a compiler like `gcc`.

- Compiles standard JASM programs targeting the default instruction set.
- Provides a CLI interface for running and testing programs.

---

### `registermaschine-ui`
A base IDE-like application for interactive execution.

- Supports any instruction set compatible with `registermaschine-core`.
- Includes code editor, register display, and slowed-down execution.
---

## Building

This is a multi-module Maven project:

```bash
mvn clean install
```

- `core` builds the main runtime and API
- `console` depends on `core` and builds the CLI tool
- `ui` depends on `core` and builds the JavaFX-based IDE

---

## Usage

For the sake of simplicity, consider using a prepackaged bundle in a GitHub Release.