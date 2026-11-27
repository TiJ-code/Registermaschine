# Registermaschine

Registermaschine is a modular, extensible virtual machine framework designed for building and executing
custom instruction sets with configurable hardware behaviour.

It combines a compilation pipeline, a virtual runtime, and a plugin-based extension system into a cleanly separated architecture, making it suitable for:

- teaching low-level computation concepts
- building custom assembly-like scripting languages
- experimenting with virtual CPUs and instruction sets
- embedding deterministic execution environments into applications

---

## Features

- Modular architecture (API, Core, Instruction Packs)
- Full plugin system (JAR-based dynamic loading)
- Custom instruction sets via configuration
- Full compilation pipeline (Lexer → Parser → Compiler)
- Virtual CPU with register-based execution
- Condition system (Boolean logic for control flow)
- XML-based configuration system (`.jxml`)
- Strict encapsulation via Java modules (JPMS)

---

## Project Structure

The project contains these Maven modules:

- `registermaschine-api`
- `registermaschine-compiler`
- `registermaschine-engine`
- `registermaschine-plugin-loader`
- `registermaschine-config`
- `registermaschine-core`

### Publishing a release

The release workflow publishes the API, compiler, engine, plugin loader, config, and core to Maven Central with self-contained POMs. It also creates a GitHub release with their JARs.

The workflow tags and pushes the release before publishing. If publishing fails, inspect the Central Portal deployments and publish only any missing artifacts from the existing release tag rather than creating the same release again; published versions cannot be overwritten.

---

# registermaschine-api

The public API layer defining all contracts of the system.

## Responsibilities

- Compilation interfaces (`ICompiler`, `ILexer`, `IParser`)
- Syntax tree abstraction
- Instruction and operand models
- Runtime interfaces (`IExecutionContext`, snapshots, listeners)
- Configuration interfaces
- Plugin interfaces (`IPlugin`, `PluginContext`)
- Error model (typed exceptions)

## Purpose

This module is implementation-agnostic and stable.

It allows you to:

- build your own compiler or runtime
- implement custom instructions
- build full instruction packs or plugins
- integrate Registermaschine into external systems

---

# registermaschine-core

The reference implementation of the Registermaschine system.

This is the central execution and extension runtime.  
It provides compilation, execution, configuration parsing, and plugin loading.

---

## Core Components

### 1. Compilation Pipeline

The `compiler` module provides `ConcreteLexer`, `ConcreteParser`, and `ConcreteCompiler`.
Core assembles the compilation stages through `Pipeline` (`core.compilation`).
When using the compiler directly, construct the lexer and compiler with a `CompilerOptions`
containing the register count, label policy, and register regex. Direct users of the old
`core.compilation` implementations must update their imports to `tij.registermaschine.compiler`.

Flow:
```
Source → Tokens → AST → Compiled Program
```


---

### 2. Runtime (`engine.runtime`)
- `ConcreteExecutionContext`
- `Executor`

Create a context with `CoreConfig.createExecutionContext()` to use core's configured register count and jump limit, or construct it directly with a register count and jump-limit supplier. Direct users of the old `core.runtime` classes must update their imports to `tij.registermaschine.engine.runtime`. Conditions, devices, instruction implementations, and memory implementations also moved from `core` to corresponding `tij.registermaschine.engine` packages.

Provides:
- register-based execution
- instruction dispatch
- step-by-step evaluation
- runtime listeners for UI/debugging

---

### 3. Instruction System
- `ConcreteInstructionRegistry`
- Instruction sets loaded dynamically via configuration and plugins

Core does not bundle a default instruction set. Applications must supply an instruction-set file and register its step handlers, for example through a plugin, before compiling and executing programs.

Plugin descriptors (`plugin.xml`) and instruction-set files are validated against the bundled DTDs. Their `DOCTYPE` declarations are optional.

Plugin discovery and lifecycle management live in `tij.registermaschine.pluginloader`.
Core initializes the loader with its configured handler registry; standalone users must
initialize `PluginLoader` with a `PluginContext` before loading and enabling plugins.
Direct users of the former `core.plugin` classes must update their imports.

---

### 4. Condition System (`engine.conditions`)
Boolean and numeric logic system:
- AND / OR / NOT
- comparisons (zero, negative, etc.)

Used for:
- conditional execution
- guarded instructions
- control flow evaluation

---

### 5. Configuration System (`core.config`)
XML-based system (`.jxml`)

Configuration lives in the `config` module; its public package remains
`tij.registermaschine.core.config`. Use `CoreStartup.init()` from core to initialize
configuration and load plugins. `CoreConfigParser.init()` now initializes configuration
only and no longer starts plugins. The `onlyInternal` initialization option has been
removed; initialization uses the user-editable configuration with bundled defaults.

- `CoreConfigParser`
- instruction definitions
- operand definitions
- condition macros
- hardware constraints

Validation uses DTDs in: `resources/dtd/`


---

### 6. Plugin System (NEW in v1.1.0)

Registermaschine supports **dynamic plugin loading from JAR files**.

#### Plugin Lifecycle

Each plugin follows a simple lifecycle:

- `onLoad()`  
  Called immediately after the plugin instance is created.

- `onEnable(PluginContext context)`  
  Called after all plugins are loaded and the system is ready to run.

#### Plugin Context

Each plugin receives a `PluginContext` during enabling.

It provides controlled access to core systems such as:

- Instruction registry
- Execution/runtime integration
- System-level hooks (limited exposure)

This ensures plugins remain decoupled from internal core implementation while still being extensible.

#### Plugin Directory

All plugins are loaded from: `./plugins`


Each plugin must be a valid `.jar` file containing a `plugin.xml` descriptor.


#### Plugin Descriptor (`plugin.xml`)

Every plugin JAR must include a descriptor file at its root:

```xml
<plugin version="1">
    <name>Example Plugin</name>
    <description>My plugin</description>
    <version>1.0.0</version>
    <author>Author</author>
    <main>your.package.MainPlugin</main>
</plugin>
```

*\*all mentioned fields are required*


#### Plugin Implementation Example
```java
public class MyPlugin implements IPlugin {

    @Override
    public void onLoad() {
        // initialization logic
    }

    @Override
    public void onEnable(PluginContext context) {
        // register instructions or extend runtime
    }
}
```

#### Java Module System
Since the Registermaschine framework relies on JPMS, plugins have to be declared as modules too.\
A plugin has to open all necessary to the core package or be declared as an open module.\
**Example 1**
```java
module myPlugin {
    exports my.packages;
    opens my.packages to tij.registermaschine.core;
    
    requires tij.registermaschine.api;
}
```

**Example 2**
```java
open module myPlugin {
    exports my.packages;

    requires tij.registermaschine.api;
}
```

#### Plugin Loading Process
The plugin system loads plugins in the following steps:
1. Scan `/plugins` directory for `.jar` files
2. Open each JAR archive
3. Locate `plugin.xml`
4. Determine plugin file version
5. Parse and validate XML using DTD
6. Load plugin using isolated `URLClassLoader`
7. Instantiate plugin main class
8. Execute lifecycle:
   - `onLoad()`
   - `onEnable(PluginContext)`

---

### 7. Upcoming Changes

... (additional in-between releases)

#### v2.0.0
- Multiple Instruction Handlers
- External Devices⁽¹⁾
- Variable Declarations & Memory⁽¹⁾

*⁽¹⁾ memory is part of the external device system*
