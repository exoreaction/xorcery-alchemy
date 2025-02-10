package dev.xorcery.alchemy.domainevents;

import dev.xorcery.configuration.Configuration;
import dev.xorcery.util.Resources;
import graphql.language.Description;
import graphql.scalar.GraphqlStringCoercing;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import jakarta.inject.Inject;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Optional;

import static graphql.schema.idl.ScalarInfo.GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS;

@Service
@Rank(-1)
public class GraphQLSchemaFactory
        implements Factory<GraphQLSchema> {

    private final GraphQLConfiguration graphQLConfiguration;

    @Inject
    public GraphQLSchemaFactory(Configuration configuration) {
        graphQLConfiguration = GraphQLConfiguration.get(configuration);
    }

    @Override
    public GraphQLSchema provide() {
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeDefinitionRegistry = null;
        for (String schemaResource : graphQLConfiguration.getSchemas()) {
            URL url = Resources.getResource(schemaResource).orElseThrow(() -> new IllegalArgumentException("Cannot find schema file:" + schemaResource));
            try (InputStream schemaFile = url.openStream()) {
                TypeDefinitionRegistry fileTypeDefinitionRegistry = schemaParser.parse(schemaFile);
                typeDefinitionRegistry = typeDefinitionRegistry == null
                        ? fileTypeDefinitionRegistry
                        : fileTypeDefinitionRegistry.merge(typeDefinitionRegistry);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not parse GraphQL schema file:" + url, e);
            }
        }

        RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
        GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

        registerScalars(runtimeWiringBuilder, typeDefinitionRegistry);

        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(
                typeDefinitionRegistry,
                runtimeWiringBuilder.codeRegistry(codeRegistryBuilder).build()
        );
        return graphQLSchema;
    }

    @Override
    public void dispose(GraphQLSchema instance) {

    }

    private void registerScalars(RuntimeWiring.Builder runtimeWiringBuilder, TypeDefinitionRegistry typeDefinitionRegistry) {
        typeDefinitionRegistry.scalars()
                .forEach((name, definition) ->
                {
                    if (GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS.containsKey(name))
                        return;

                    runtimeWiringBuilder.scalar(GraphQLScalarType.newScalar()
                            .name(name)
                            .description(Optional.ofNullable(definition.getDescription()).map(Description::getContent).orElse("Scalar " + name))
                            .withDirectives(definition.getDirectives().stream().map(directive ->
                                    GraphQLDirective.newDirective().name(directive.getName())
                                            .replaceArguments(directive.getArguments().stream()
                                                    .map(arg -> GraphQLArgument.newArgument()
                                                            .name(arg.getName())
                                                            .build()).toList())
                                            .build()).toList().toArray(new GraphQLDirective[0]))
                            .definition(definition)
                            .coercing(new GraphqlStringCoercing())
                            .build());
                });
    }

}
