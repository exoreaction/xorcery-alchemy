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
package dev.xorcery.alchemy.file.json.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.alchemy.jar.JarContext;
import dev.xorcery.alchemy.jar.SourceJar;
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import dev.xorcery.reactivestreams.extras.publishers.JsonPublisher;
import dev.xorcery.reactivestreams.extras.publishers.ResourcePublisherContext;
import org.jvnet.hk2.annotations.Service;
import reactor.core.publisher.Flux;

@Service(name = "json", metadata = "enabled=jars.enabled")
public class JsonFileSourceJar
        implements SourceJar {

    @Override
    public Flux<MetadataJsonNode<JsonNode>> newSource(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        return Flux.from(new JsonPublisher<JsonNode>(JsonNode.class))
                .contextCapture()
                .contextWrite(context ->
                        jarConfiguration.getString(JarContext.sourceUrl).map(url ->
                                context.put(ResourcePublisherContext.resourceUrl, url)).orElse(context))
                .map(json ->
                {
                    ObjectNode metadata = JsonNodeFactory.instance.objectNode();
                    metadata.set("timestamp", JsonNodeFactory.instance.numberNode(System.currentTimeMillis()));
                    return new MetadataJsonNode<>(new Metadata(metadata), json);
                });
    }
}
