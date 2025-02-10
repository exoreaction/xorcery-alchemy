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

import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.configuration.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record RecipesConfiguration(List<TransmutationConfiguration> recipes) {

    public static RecipesConfiguration get(Configuration configuration){
        return new RecipesConfiguration(configuration.getObjectListAs("recipes",
                json -> new TransmutationConfiguration(new Configuration(json)))
                .orElse(Collections.emptyList()));
    }

    public List<TransmutationConfiguration> getRecipes() {
        return recipes;
    }

    public Optional<TransmutationConfiguration> getRecipe(String name) {
        return recipes.stream()
                .filter(recipe -> Objects.equals(recipe.getName().orElse(null), name))
                .findFirst();
    }
}
