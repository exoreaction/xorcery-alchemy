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

import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.xorcery.alchemy.crucible.Crucible;
import dev.xorcery.alchemy.crucible.Transmutations;
import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.alchemy.jar.Transmute;
import dev.xorcery.alchemy.jar.TransmuteJar;
import dev.xorcery.configuration.Configuration;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

@Service(name = "crucible", metadata = "enabled=jars.enabled")
public class CrucibleTransmuteJar
        implements TransmuteJar {
    private final Transmutations transmutations;
    private final Crucible crucible;

    @Inject
    public CrucibleTransmuteJar(Transmutations transmutations, Crucible crucible) {
        this.transmutations = transmutations;
        this.crucible = crucible;
    }

    @Override
    public Transmute newTransmute(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        return (flux, context) -> flux.handle((item, sink) ->
        {
            try {
                if (item.data() instanceof ObjectNode transmutationJson) {
                    crucible.addTransmutation(transmutations.newTransmutation(new TransmutationConfiguration(new Configuration(transmutationJson))));
                }
                sink.next(item);
            } catch (Throwable t) {
                sink.error(t);
            }
        });
    }
}
