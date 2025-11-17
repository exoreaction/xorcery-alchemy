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
package dev.xorcery.alchemy.common.source;

import com.fasterxml.jackson.databind.JsonNode;
import dev.xorcery.alchemy.crucible.Transmutations;
import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.alchemy.jar.SourceJar;
import dev.xorcery.alchemy.jar.Transmutation;
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.configuration.Configuration;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Service(name = "transmutation", metadata = "enabled=jars.enabled")
public class TransmutationSourceJar
        implements SourceJar {
    private final Transmutations transmutations;

    @Inject
    public TransmutationSourceJar(Transmutations transmutations) {
        this.transmutations = transmutations;
    }

    @Override
    public Flux<MetadataJsonNode<JsonNode>> newSource(JarConfiguration configuration, TransmutationConfiguration transmutationConfiguration) {
        TransmutationConfiguration sourceTransmutationConfiguration = new TransmutationConfiguration(configuration.configuration().getConfiguration("transmutation"));
        Transmutation sourceTransmutation = transmutations.newTransmutation(sourceTransmutationConfiguration);
        return sourceTransmutation.getFlux();
    }
}
