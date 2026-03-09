# Xorcery Alchemy

## Purpose
Xorcery Alchemy is a data transformation and processing framework built on the Xorcery reactive platform. It handles complex data "transmutations" — transforming data between formats (CSV, SQL, events, etc.) using a modular pipeline architecture. Core use case: migrating legacy data into the co-events event stream infrastructure.

## Tech Stack
- Language: Java 21
- Framework: Xorcery (reactive, event-sourced)
- Build: Maven (multi-module)
- Key dependencies: xorcery-core, xorcery-alchemy-crucible (processing engine)

## Architecture
Multi-module Maven project with a crucible (processing engine) at the center. Data flows through input adapters → transmutation pipeline → output targets. Modular format support: CSV, SQL, events, binary formats.

## Key Entry Points
- `xorcery-alchemy-crucible` — main processing engine
- `xorcery-alchemy-jar` — JAR transformation infrastructure
- `xorcery-alchemy-common` — shared utilities

## Development
```bash
# Build
mvn clean install

# Test
mvn test
```

## Domain Context
Data migration and transformation tooling for the eXOReaction/co-* event streaming platform. Used to onboard legacy datasets (CatalystOne MSSQL, etc.) into the Xorcery event-sourced architecture.
