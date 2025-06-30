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
package dev.xorcery.alchemy.crucible;

import dev.xorcery.configuration.Configuration;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service(name = "transmutations", metadata = "enabled=crucible.enabled")
@RunLevel(20)
public class TransmutationsRunner {

    private final CompletableFuture<Void> done;

    @Inject
    public TransmutationsRunner(Configuration configuration, Transmutations transmutations, Crucible crucible, Logger logger) {
        this(TransmutationsConfiguration.get(configuration), transmutations, crucible, logger);
    }

    public TransmutationsRunner(TransmutationsConfiguration transmutationsConfiguration, Transmutations transmutations, Crucible crucible, Logger logger) {
        List<CompletableFuture<Void>> results = new ArrayList<>();
        transmutationsConfiguration.getTransmutations().forEach(transmutation -> {
            if (transmutation.isEnabled()) {
                CompletableFuture<Void> result = crucible.addTransmutation(transmutations.newTransmutation(transmutation));
                result.exceptionally(t ->{
                    logger.error("Transmutation failed:"+t);
                    return null;
                });
                results.add(result);
            }
        });
        done = CompletableFuture.allOf(results.toArray(new CompletableFuture[0]));
    }

    public CompletableFuture<Void> getDone() {
        return done;
    }
}
