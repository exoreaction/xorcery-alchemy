
# Xorcery Alchemy

Xorcery Alchemy is a powerful data transformation and processing framework that provides a flexible way to handle various data formats and transformations in Java applications.

## Overview

Xorcery Alchemy is designed to handle complex data transformations with a modular approach, supporting various input formats, transformation methods, and output targets. The project is structured around the concept of "transmutations" - transforming data from one form to another.

## Modules

### Core Modules

- **xorcery-alchemy-jar**: Core module providing the fundamental JAR handling and transformation infrastructure
- **xorcery-alchemy-crucible**: Main processing engine for executing transformations
- **xorcery-alchemy-common**: Common utilities and shared functionality

### File Format Support

- **xorcery-alchemy-file-csv**: CSV file reading and processing
- **xorcery-alchemy-file-excel**: Excel file processing capabilities
- **xorcery-alchemy-file-yaml**: YAML file processing support

### Integration Modules

- **xorcery-alchemy-domainevents**: GraphQL integration for domain events
- **xorcery-alchemy-neo4jprojection**: Neo4j database integration
- **xorcery-alchemy-opensearch**: OpenSearch integration
- **xorcery-alchemy-jslt**: JSLT transformation support
- **xorcery-alchemy-script**: JavaScript-based transformation support

### Support Modules

- **xorcery-alchemy-bom**: Bill of Materials for dependency management
- **xorcery-alchemy-parent**: Parent POM with common build configuration
- **xorcery-alchemy-test**: Test utilities and integration tests
- **xorcery-alchemy-log**: Logging infrastructure

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

Add the following to your `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>dev.xorcery.alchemy</groupId>
            <artifactId>xorcery-alchemy-bom</artifactId>
            <version>${xorcery-alchemy.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then add the specific modules you need:

```xml
<dependencies>
    <dependency>
        <groupId>dev.xorcery.alchemy</groupId>
        <artifactId>xorcery-alchemy-crucible</artifactId>
    </dependency>
    <!-- Add other modules as needed -->
</dependencies>
```

## Usage

### Basic Example

Here's a simple example of using Xorcery Alchemy to transform data:

```yaml
# transformation.yaml
source:
  type: "csv"
  file: "input.csv"
transmutations:
  - type: "jslt"
    transform: "transform.jslt"
  - type: "opensearch"
    index: "output-index"
```

### Supported Transformations

- CSV to JSON
- Excel to JSON
- YAML processing
- GraphQL transformations
- Neo4j projections
- OpenSearch operations
- Custom JavaScript transformations
- JSLT transformations

## Building from Source

```bash
git clone https://github.com/exoreaction/xorcery-alchemy.git
cd xorcery-alchemy
mvn clean install
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Repository Information

All artifacts are published to the Quadim repository:

- Release Repository: https://mvnrepo.cantara.no/content/repositories/quadimrelease/
- Snapshot Repository: https://mvnrepo.cantara.no/content/repositories/quadimsnapshot/

## Project Status

This project is actively maintained by eXOReaction AS.

## Support

For support and questions, please open an issue in the GitHub repository..
```
