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
package dev.xorcery.alchemy.common.transmute;

import com.fasterxml.jackson.databind.JsonNode;
import dev.xorcery.alchemy.crucible.Transmutations;
import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.alchemy.jar.SourceJar;
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.alchemy.jar.TransmuteJar;
import dev.xorcery.configuration.Configuration;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.util.context.ContextView;

import java.util.Optional;
import java.util.function.BiFunction;

import static dev.xorcery.configuration.Configuration.missing;

@Service(name = "oneOf", metadata = "enabled=jars.enabled")
public class OneOfTransmuteJar
        implements TransmuteJar {
    private final Transmutations transmutations;

    @Inject
    public OneOfTransmuteJar(Transmutations transmutations) {
        this.transmutations = transmutations;
    }

    @Override
    public BiFunction<Flux<MetadataJsonNode<JsonNode>>, ContextView, Publisher<MetadataJsonNode<JsonNode>>> newTransmute(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        return (flux, context) -> jarConfiguration.configuration().getObjectListAs("transmutes", source ->
                        new JarConfiguration(new Configuration(source))).map(transmutes ->
                        jarConfiguration.getString("transmute").map(name -> transmutes.stream()
                                        .filter(transmute -> transmute.getName().orElseGet(transmute::getJar).equals(name))
                                        .findFirst()
                                        .flatMap(transmute -> transmutations.applyTransmuteFlux(flux, transmute, transmutationConfiguration))
                                        .orElseGet(() -> flux.handle((item, sink) -> sink.error(new IllegalArgumentException("No transmute named '" + name + "' found")))))
                                .orElseGet(() -> Flux.error(missing("transmute").get())))
                .orElse(Flux.empty());
    }
}
