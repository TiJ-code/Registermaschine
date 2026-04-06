# Version 1.0.1
## Changes
### CORE
- Fixed bug with custom tags in `configuration.jxml` files
- Added support for comments that appear after instructions but still in the same line
- Added support for underscores in labels in default implementations for `ConcreteLexer` and `ConcreteParser`
- Added support for negative immediate values in `ConcreteLexer` Added support for negative immediate values in `ConcreteLexer`
- Added Documentation
### API
- Added Documentation
### Default Instruction Set & Handlers
- Added Documentation
## Breaking Changes
### API
#### Minor
- Introducing another `TokenType` called `ADDRESS` to differentiate between actual labels and real hard addresses.\
    `ILexer` and `IParser` implementations need to accommodate for this new type.
