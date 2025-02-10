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
package dev.xorcery.alchemy.script;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.openjdk.nashorn.api.scripting.JSObject;
import reactor.core.publisher.FluxSink;

import javax.script.ScriptException;

public class JavaScriptFluxSink
    extends AbstractJSObject
{
    private final FluxSink<MetadataJsonNode<JsonNode>> sink;

    public JavaScriptFluxSink(FluxSink<MetadataJsonNode<JsonNode>> sink) {
        this.sink = sink;
    }

    @Override
    public Object getMember(String name) {
        return switch (name)
        {
            case "next" -> new JavaScriptMethodCall(args -> {
                if (args[0] instanceof JsonNodeJSObject jsObject)
                {
                    if (jsObject.getJsonNode() instanceof ObjectNode itemJson)
                    {
                        sink.next(new MetadataJsonNode<>(new Metadata(itemJson.get("metadata").deepCopy()), itemJson.get("data").deepCopy()));
                    }
                } else if (args[0] instanceof JSObject jsObject)
                {
                    if (JsonNodeJSObject.unwrap(jsObject) instanceof ObjectNode itemJson)
                    {
                        sink.next(new MetadataJsonNode<>(new Metadata(itemJson.get("metadata").deepCopy()), itemJson.get("data").deepCopy()));
                    }
                }
                return null;
            });
            case "complete" -> new JavaScriptMethodCall(args -> {
                sink.complete();
                return null;
            });
            case "error" -> new JavaScriptMethodCall(args -> {
                if (args[0] instanceof String msg)
                {
                    sink.error(new ScriptException(msg));
                }
                return null;
            });

            default -> null;
        };
    }
}
