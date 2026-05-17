# Default Instruction Set


## Breaking Changes

### Minor
- added RegistermaschinePlugin class, that describes the functionality of this default plugin. And enforces to use as plugin structure.
- added plugin.xml, that is the configuration of this default instruction set plugin.

---

# Development Tools

- added Versioning dev-tool, that does the version bumps
- added ConfigPatchParser dev-tool, that cumulates all patches into one `cumulated.xml`
- added ConfigFileBuilder dev-tool, that builds the CHANGELOG.md from the cumulated XML file
---

# API

- added IPlugin interface, that describes the plugin structure
- added PluginContext record, that communicates registered instructions between loader and plugin
- added IInstructionRegistry interface, that describes the layout of instruction registry implementations
- added ILogger interface for logger instances
- added DefaultLogger, that provides a default implementation of a logger
- added LoggerFactory, to create class specific instances
- added LogConfig, a global config Logger instances must conform to
- added LogLevel enum to display severity of log messages
---

# Core

- added PluginConfig, that describes plugin config file structure
- added PluginConfigParser, XML parser that parses file into instances of PluginConfig
- added PluginConstants, XML constants class

## Breaking Changes

### Trivial
- added PluginLoader class, that can load (valid) plugins

### Minor
- added ConcreteInstructionRegistry, that is a global container of all possible instruction handlers registered at runtime

