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

The project is split into three main Maven modules:

- `registermaschine-api`
- `registermaschine-core`
- `registermaschine-default-instructions`

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

### 1. Compilation Pipeline (`core.compilation`)
- `ConcreteLexer`
- `ConcreteParser`
- `ConcreteCompiler`
- `Pipeline`

Flow:
```
Source → Tokens → AST → Compiled Program
```


---

### 2. Runtime (`core.runtime`)
- `ConcreteExecutionContext`
- `Executor`

Provides:
- register-based execution
- instruction dispatch
- step-by-step evaluation
- runtime listeners for UI/debugging

---

### 3. Instruction System
- `ConcreteInstructionRegistry`
- Instruction sets loaded dynamically via configuration and plugins

---

### 4. Condition System (`core.conditions`)
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
    opens my.packages to dk.tij.registermaschine.core;
    
    requires dk.tij.registermaschine.api;
}
```

**Example 2**
```java
open module myPlugin {
    exports my.packages;

    requires dk.tij.registermaschine.api;
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
