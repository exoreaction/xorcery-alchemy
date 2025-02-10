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

public class JarException
    extends RuntimeException
{
    private final JarConfiguration jarConfiguration;
    private final TransmutationConfiguration transmutationConfiguration;

    public JarException(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration, String message) {
        super(message);
        this.jarConfiguration = jarConfiguration;
        this.transmutationConfiguration = transmutationConfiguration;
    }
    public JarException(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration, String message, Throwable cause) {
        super(message, cause);
        this.jarConfiguration = jarConfiguration;
        this.transmutationConfiguration = transmutationConfiguration;
    }

    @Override
    public String getMessage() {
        return transmutationConfiguration.getName().map(tn -> tn+".").orElse("")+jarConfiguration.getName().orElseGet(jarConfiguration::getJar)+":"+super.getMessage();
    }
}
