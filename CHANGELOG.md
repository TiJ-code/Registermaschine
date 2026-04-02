# Version 1.0.1
## Changes
### CORE
- Added support for underscores in labels in default implementations for `ConcreteLexer` and `ConcreteParser`
## Breaking Changes
### API
#### Minor
- Introducing another `TokenType` called `ADDRESS` to differentiate between actual labels and real hard addresses.\
    `ILexer` and `IParser` implementations need to accommodate for this new type.
