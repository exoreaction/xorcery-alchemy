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
package dev.xorcery.alchemy.jar;

import dev.xorcery.configuration.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record TransmutationConfiguration(Configuration configuration) {
    public Optional<String> getName() {
        return configuration.getString("name");
    }

    public boolean isEnabled(){
        return configuration.getBoolean("enabled").orElse(true);
    }

    public Optional<String> getRecipe()
    {
        return configuration.getString("recipe");
    }

    public JarConfiguration getSource() {
        return new JarConfiguration(configuration.getConfiguration("source"));
    }

    public List<JarConfiguration> getTransmutes() {
        return configuration.getObjectListAs("transmutes", json -> new JarConfiguration(new Configuration(json)))
                .orElse(Collections.emptyList());
    }
}
