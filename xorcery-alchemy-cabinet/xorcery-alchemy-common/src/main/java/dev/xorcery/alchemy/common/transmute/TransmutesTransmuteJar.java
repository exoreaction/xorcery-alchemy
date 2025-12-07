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
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.alchemy.jar.Transmute;
import dev.xorcery.alchemy.jar.TransmuteJar;
import dev.xorcery.configuration.Configuration;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import reactor.core.publisher.Flux;

import java.util.function.BiFunction;

import static dev.xorcery.configuration.Configuration.missing;

@Service(name = "transmutes", metadata = "enabled=jars.enabled")
public class TransmutesTransmuteJar
        implements TransmuteJar {
    private final Transmutations transmutations;

    @Inject
    public TransmutesTransmuteJar(Transmutations transmutations) {
        this.transmutations = transmutations;
    }

    @Override
    public Transmute newTransmute(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        return (flux, context) -> jarConfiguration.configuration().getObjectListAs("transmutes", source -> new JarConfiguration(new Configuration(source)))
                .map(transmutes -> transmutes.stream()
                        .reduce(flux, this.addTransmute(transmutationConfiguration),(f1, f2)->f2))
                .orElseThrow(missing("transmutes"));
    }

    private BiFunction<Flux<MetadataJsonNode<JsonNode>>, ? super JarConfiguration, Flux<MetadataJsonNode<JsonNode>>> addTransmute(TransmutationConfiguration transmutationConfiguration) {
        return (flux, transmute)-> transmutations.applyTransmuteFlux(flux, transmute, transmutationConfiguration).orElseThrow(()->new IllegalAccessError("No transmute named '" + transmute.getJar() + "' found"));
    }
}
