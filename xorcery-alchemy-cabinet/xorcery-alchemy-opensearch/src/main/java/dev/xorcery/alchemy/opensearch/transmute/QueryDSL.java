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
package dev.xorcery.alchemy.opensearch.transmute;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

public interface QueryDSL {

    ObjectMapper mapper = new JsonMapper().findAndRegisterModules();

    static ObjectNode term(String name, Object value)
    {
        return instance.objectNode().set("term",
                instance.objectNode().set(name, instance.objectNode().set("value", mapper.valueToTree(value))));
    }

    static ObjectNode match_phrase(String name, Object value)
    {
        return instance.objectNode().set("match_phrase",
                instance.objectNode().set(name, mapper.valueToTree(value)));
    }
}
