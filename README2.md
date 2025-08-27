# Xorcery Alchemy

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture Overview](#architecture-overview)
3. [Module Structure](#module-structure)
4. [Technology Stack](#technology-stack)
5. [Build and Development](#build-and-development)
6. [Configuration Management](#configuration-management)
7. [Observability](#observability)
8. [Extension Points](#extension-points)
9. [Development Guidelines](#development-guidelines)

## Project Overview

### Purpose
Xorcery Alchemy is a powerful data transformation and processing framework designed to handle complex data transformations with a modular approach. It supports various input formats, transformation methods, and output targets through a concept called "transmutations" - transforming data from one form to another.

### Key Features
- **Multi-format Support**: CSV, Excel, YAML, JSON, and Parquet file processing
- **Database Integration**: Neo4j projections and OpenSearch operations
- **Transformation Engines**: JSLT transformations and JavaScript-based processing
- **Domain Events**: GraphQL integration for event-driven architectures
- **Observability**: Built-in OpenTelemetry support for monitoring and tracing
- **Modular Design**: Pluggable architecture allowing custom extensions

### Target Use Cases
- ETL (Extract, Transform, Load) operations
- Data migration between different formats and systems
- Real-time data processing pipelines
- Event-driven data transformations
- Integration between disparate data sources

## Architecture Overview

### Core Design Principles

1. **Modular Architecture**: Each capability is separated into focused modules
2. **Dependency Injection**: Uses HK2 for service discovery and lifecycle management
3. **Reactive Streams**: Built on reactive programming principles for scalability
4. **Configuration-Driven**: YAML-based configuration for transformation pipelines
5. **Observability-First**: Integrated telemetry and monitoring capabilities

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                        │
├─────────────────────────────────────────────────────────────┤
│                  Xorcery Alchemy Cabinet                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ File Format │  │Integration  │  │ Transformation      │  │
│  │ Processors  │  │ Modules     │  │ Engines             │  │
│  │ • CSV       │  │ • Neo4j     │  │ • JSLT              │  │
│  │ • Excel     │  │ • OpenSearch│  │ • JavaScript        │  │
│  │ • YAML      │  │ • Events    │  │ • Common Utils      │  │
│  │ • Parquet   │  │             │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                  Xorcery Alchemy Crucible                   │
│              (Main Processing Engine)                       │
├─────────────────────────────────────────────────────────────┤
│                   Xorcery Framework                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ Core        │  │ Dependency  │  │ Observability       │  │
│  │ Libraries   │  │ Injection   │  │ • OpenTelemetry     │  │
│  │             │  │ (HK2)       │  │ • Logging           │  │
│  │             │  │             │  │ • Metrics           │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                      Java Runtime                           │
└─────────────────────────────────────────────────────────────┘
```

## Module Structure

### Core Modules

#### xorcery-alchemy-jar
- **Purpose**: Foundation module providing core JAR handling and transformation infrastructure
- **Responsibilities**:
    - Basic transformation contracts and interfaces
    - Core data model definitions
    - Utility classes for data handling

#### xorcery-alchemy-crucible
- **Purpose**: Main processing engine for executing transformations
- **Dependencies**: xorcery-alchemy-jar, Xorcery framework modules
- **Responsibilities**:
    - Transformation pipeline orchestration
    - Service lifecycle management
    - Integration with Xorcery's reactive streams
    - OpenTelemetry integration for observability

#### xorcery-alchemy-parent
- **Purpose**: Parent POM providing shared build configuration
- **Key Configurations**:
    - Java 21 compiler settings
    - Dependency version management
    - Plugin configurations for HK2 annotation processing
    - Common build profiles and repository configurations

### File Format Modules

#### xorcery-alchemy-file-csv
- **Purpose**: CSV file reading and processing capabilities
- **Features**: Configurable delimiters, header handling, type conversion

#### xorcery-alchemy-file-excel
- **Purpose**: Excel file processing (XLS/XLSX formats)
- **Features**: Multi-sheet support, formula evaluation, cell type handling

#### xorcery-alchemy-file-yaml
- **Purpose**: YAML file processing and transformation
- **Features**: Complex nested structure handling, type-safe parsing

#### xorcery-alchemy-file-parquet
- **Purpose**: Apache Parquet file format support
- **Features**: Column-oriented data processing, schema evolution

### Integration Modules

#### xorcery-alchemy-neo4jprojection
- **Purpose**: Neo4j graph database integration
- **Capabilities**: Graph projections, Cypher query execution, relationship mapping

#### xorcery-alchemy-opensearch
- **Purpose**: OpenSearch/Elasticsearch integration
- **Capabilities**: Document indexing, search operations, bulk processing

#### xorcery-alchemy-domainevents
- **Purpose**: GraphQL integration for domain events
- **Capabilities**: Event streaming, schema-based transformations

### Transformation Engines

#### xorcery-alchemy-jslt
- **Purpose**: JSLT (JSON Stream Language for Transformations) support
- **Capabilities**: Declarative JSON transformations, template-based processing

#### xorcery-alchemy-script
- **Purpose**: JavaScript-based transformation support
- **Capabilities**: Custom transformation logic, scriptable data processing

### Support Modules

#### xorcery-alchemy-common
- **Purpose**: Common utilities and shared functionality
- **Contents**: Shared data structures, utility methods, common constants

#### xorcery-alchemy-log
- **Purpose**: Logging infrastructure and utilities
- **Features**: Structured logging, correlation IDs, performance metrics

#### xorcery-alchemy-bom
- **Purpose**: Bill of Materials for dependency management
- **Function**: Centralized version management for all project dependencies

#### xorcery-alchemy-test
- **Purpose**: Test utilities and integration test support
- **Contents**: Test fixtures, mock implementations, testing utilities

## Technology Stack

### Core Technologies
- **Java 21**: Target runtime with modern language features
- **Maven 3.6+**: Build and dependency management
- **Xorcery Framework 0.164.0**: Core infrastructure framework

### Key Dependencies
- **HK2 3.1.1**: Dependency injection and service management
- **Jackson 2.19.2**: JSON processing and data binding
- **ASM 9.8**: Bytecode manipulation and analysis
- **OpenTelemetry**: Distributed tracing and observability

### External Integrations
- **Neo4j**: Graph database connectivity
- **OpenSearch/Elasticsearch**: Search and analytics platform
- **Apache Parquet**: Columnar storage format

## Build and Development

### Project Structure
```
xorcery-alchemy/
├── pom.xml                     # Root aggregator POM
├── xorcery-alchemy-bom/        # Dependency management
├── xorcery-alchemy-parent/     # Parent POM with build config
├── xorcery-alchemy-jar/        # Core foundation
├── xorcery-alchemy-crucible/   # Main engine
├── xorcery-alchemy-cabinet/    # Feature modules
│   ├── xorcery-alchemy-common/
│   ├── xorcery-alchemy-file-*/
│   ├── xorcery-alchemy-*/
│   └── xorcery-alchemy-cabinet-all/  # Convenience aggregate
└── xorcery-alchemy-test/       # Test utilities
```

### Build Commands
```bash
# Full build with tests
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Generate Javadocs
mvn javadoc:aggregate

# Run specific module tests
mvn test -pl xorcery-alchemy-crucible
```

### Development Setup
1. **Prerequisites**: Java 21, Maven 3.6+
2. **IDE Configuration**: Enable annotation processing for HK2
3. **Code Style**: Project uses standard Java formatting
4. **Testing**: JUnit 5 with tree reporter for clear test output

## Configuration Management

### Configuration Sources
- **YAML Files**: Primary configuration format
- **System Properties**: Runtime overrides
- **Environment Variables**: Container/deployment specific settings

### Configuration Schema
The project uses Xorcery's configuration system with JSON Schema validation:
- Configuration classes are annotated for automatic schema generation
- Runtime validation ensures configuration correctness
- IDE support through generated schemas

### Example Configuration
```yaml
# transformation.yaml
source:
  type: "csv"
  file: "input.csv"
  options:
    delimiter: ","
    header: true

transmutations:
  - type: "jslt"
    transform: "transform.jslt"
  - type: "opensearch"
    index: "output-index"
    options:
      batch_size: 1000
```

## Observability

### Telemetry Integration
- **OpenTelemetry SDK**: Comprehensive observability framework
- **Distributed Tracing**: Request flow tracking across components
- **Metrics Collection**: Performance and health metrics
- **Log Correlation**: Structured logging with trace correlation

### Monitoring Capabilities
- **JVM Metrics**: Memory, garbage collection, thread pools
- **Application Metrics**: Transformation performance, error rates
- **Custom Instrumentation**: Module-specific observability

### Export Options
- **WebSocket Exporter**: Real-time telemetry streaming
- **Standard Exporters**: OTLP, Jaeger, Prometheus compatible

## Extension Points

### Custom Transformation Modules
1. **Create Module**: Extend `xorcery-alchemy-parent`
2. **Implement Interfaces**: Use core transformation contracts
3. **HK2 Services**: Register services via annotation processing
4. **Configuration**: Define configuration schemas

### Plugin Architecture
The framework supports runtime plugin discovery through:
- **Service Discovery**: HK2-based service registration
- **Module Loading**: Dynamic module composition
- **Configuration Integration**: Plugin-specific configuration sections

### Custom File Formats
```java
@Service
public class CustomFileProcessor implements FileProcessor {
    @Override
    public Stream<Record> process(InputStream input, Configuration config) {
        // Custom processing logic
    }
}
```

## Development Guidelines

### Code Organization
- **Package Structure**: Follow module-based packaging
- **Service Contracts**: Define clear interfaces for all services
- **Configuration**: Use record classes with validation annotations
- **Testing**: Comprehensive unit and integration tests

### Best Practices
1. **Reactive Streams**: Use non-blocking I/O patterns
2. **Resource Management**: Proper resource cleanup with try-with-resources
3. **Error Handling**: Structured error handling with proper logging
4. **Performance**: Consider memory usage for large data sets
5. **Observability**: Instrument all major operations

### Dependency Management
- **Version Control**: All versions managed through BOM
- **Scope Management**: Appropriate dependency scopes
- **Transitive Dependencies**: Careful management to avoid conflicts
- **Security**: Regular dependency updates via Renovate
