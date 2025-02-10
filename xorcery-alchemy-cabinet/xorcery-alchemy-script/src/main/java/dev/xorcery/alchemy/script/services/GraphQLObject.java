/*
 * Copyright © 2025 eXOReaction AS (rickard@exoreaction.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.xorcery.alchemy.script.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.xorcery.alchemy.script.JavaScriptMethodCall;
import dev.xorcery.alchemy.script.JsonNodeJSObject;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.api.scripting.JSObject;

import java.util.Map;

@Service(name="graphql")
@ContractsProvided(JSObject.class)
public class GraphQLObject
        extends AbstractJSObject
{
    private final GraphQL graphQL;

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE)
            .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

    @Inject
    public GraphQLObject(GraphQL graphQL) {
        this.graphQL = graphQL;
    }

    @Override
    public Object getMember(String name) {
        return switch (name)
        {
            case "query" -> new JavaScriptMethodCall(args -> {
                if (args[0] instanceof String query)
                {
                    Map<String, Object> context = Map.of("tenantId", args[1].toString());

                    ExecutionInput executionInput = ExecutionInput.newExecutionInput().graphQLContext(context)
                            .query(query).build();
                    ExecutionResult result = graphQL.execute(executionInput);
                    if (result.isDataPresent())
                    {
                        JsonNode jsonData = mapper.valueToTree(result.getData());
                        return new JsonNodeJSObject(jsonData);
                    }
                }
                return null;
            });

            default -> null;
        };
    }
}
