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
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.Optional;

@Service
public class Recipes {

    private final RecipesConfiguration recipesConfiguration;

    @Inject
    public Recipes(Configuration configuration) {
        recipesConfiguration = RecipesConfiguration.get(configuration);
    }

    public List<TransmutationConfiguration> getRecipes() {
        return recipesConfiguration.getRecipes();
    }

    public Optional<TransmutationConfiguration> getRecipeByName(String name)
    {
        return recipesConfiguration.getRecipe(name);
    }
}
