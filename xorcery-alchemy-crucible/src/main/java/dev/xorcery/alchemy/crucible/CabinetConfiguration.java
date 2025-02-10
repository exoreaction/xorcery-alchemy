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

import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.configuration.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CabinetConfiguration(List<JarConfiguration> sourceJars, List<JarConfiguration> transmuteJars) {

    public static CabinetConfiguration get(Configuration configuration)
    {
        return new CabinetConfiguration(configuration.getObjectListAs("jars.sourcejars",
                json ->new JarConfiguration(new Configuration(json))).orElse(Collections.emptyList()),
                configuration.getObjectListAs("jars.transmutejars",
                        json ->new JarConfiguration(new Configuration(json))).orElse(Collections.emptyList()));
    }

    public Optional<JarConfiguration> getSourceJar(String name) {
        return sourceJars.stream()
                .filter(sourceJar -> Objects.equals(sourceJar.getName().orElse(null), name))
                .findFirst();
    }

    public Optional<JarConfiguration> getTransmuteJar(String name) {
        return transmuteJars.stream()
                .filter(transmuteJar -> Objects.equals(transmuteJar.getName().orElse(null), name))
                .findFirst();
    }
}
