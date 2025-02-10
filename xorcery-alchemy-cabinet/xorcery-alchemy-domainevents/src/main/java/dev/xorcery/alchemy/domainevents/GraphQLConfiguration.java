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

import com.fasterxml.jackson.databind.JsonNode;
import dev.xorcery.configuration.Configuration;

import java.util.Collections;
import java.util.List;

public record GraphQLConfiguration(Configuration configuration) {

    public static GraphQLConfiguration get(Configuration configuration)
    {
        return new GraphQLConfiguration(configuration.getConfiguration("graphql"));
    }

    public List<String> getSchemas()
    {
        return configuration.getListAs("schemas", JsonNode::asText).orElseGet(Collections::emptyList);
    }
}
