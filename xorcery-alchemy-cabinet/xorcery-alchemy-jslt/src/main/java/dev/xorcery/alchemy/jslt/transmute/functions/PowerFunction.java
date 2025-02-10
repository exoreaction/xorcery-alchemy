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
package dev.xorcery.alchemy.jslt.transmute.functions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.schibsted.spt.data.jslt.Function;

public class PowerFunction
    implements Function
{
    public PowerFunction() {
    }

    @Override
    public String getName() {
        return "power";
    }

    @Override
    public int getMinArguments() {
        return 2;
    }

    @Override
    public int getMaxArguments() {
        return 2;
    }

    @Override
    public JsonNode call(JsonNode input, JsonNode[] params) {
        int base = params[0].asInt();
        int power = params[1].asInt();

        int result = 1;
        for (int ix = 0; ix < power; ix++)
            result = result * base;

        return new IntNode(result);
    }
}
