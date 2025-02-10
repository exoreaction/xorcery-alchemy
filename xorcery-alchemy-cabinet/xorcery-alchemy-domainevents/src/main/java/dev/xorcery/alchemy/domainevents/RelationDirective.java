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
package dev.xorcery.alchemy.domainevents;

import graphql.schema.GraphQLFieldDefinition;

import java.util.Optional;

public record RelationDirective(String name, String direction) {
    public static final String RELATION = "relation";
    public static final String NAME = "name";
    public static final String DIRECTION = "direction";

    public static Optional<RelationDirective> get(GraphQLFieldDefinition fieldDefinition)
    {
        return Optional.ofNullable(fieldDefinition.getDirective(RelationDirective.RELATION)).map(dir ->
        {
            String name = GraphQLHelpers.getMandatoryArgument(dir, RelationDirective.NAME, fieldDefinition.getName());
            String direction = GraphQLHelpers.getMandatoryArgument(dir, RelationDirective.DIRECTION, "OUTGOING");
            return new RelationDirective(name, parseDirection(direction));
        });
    }

    public static String parseDirection(String name)
    {
        return switch (name.toUpperCase())
        {
            case "BOTH" -> "BOTH";
            case "IN" -> "INCOMING";
            case "INCOMING" -> "INCOMING";
            default -> "OUTGOING";
        };
    }
}
