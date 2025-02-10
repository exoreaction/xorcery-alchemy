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

import dev.xorcery.collections.Element;
import dev.xorcery.configuration.Configuration;

import java.util.Optional;

public record JarConfiguration(Configuration configuration)
    implements Element
{
    @Override
    public <T> Optional<T> get(String s) {
        return configuration.get(s);
    }

    public Optional<String> getName() {
        return configuration.getString("name");
    }

    public boolean isEnabled(){
        return configuration.getBoolean("enabled").orElse(true);
    }

    public String getJar() {
        return configuration.getString("jar").orElseThrow(Configuration.missing("jar"));
    }
}
