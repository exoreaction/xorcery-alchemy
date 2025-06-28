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
package dev.xorcery.alchemy.script.transmute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.xorcery.alchemy.jar.*;
import dev.xorcery.alchemy.script.ByteArrayOutputStreamWithoutNewLine;
import dev.xorcery.alchemy.script.ScriptFlux;
import dev.xorcery.alchemy.script.ServicesJSObject;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import jakarta.inject.Inject;
import org.apache.logging.log4j.spi.LoggerContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.util.context.ContextView;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.BiFunction;

@Service(name = "script", metadata = "enabled=jars.enabled")
public class ScriptTransmuteJar
        implements TransmuteJar {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ServiceLocator serviceLocator;
    private final LoggerContext loggerContext;

    @Inject
    public ScriptTransmuteJar(ServiceLocator serviceLocator, LoggerContext loggerContext) {
        this.serviceLocator = serviceLocator;
        this.loggerContext = loggerContext;
    }

    @Override
    public Transmute newTransmute(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        String engineName = jarConfiguration.getString("engine").orElse("nashorn");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(engineName);
        if (engine == null) {
            return (m, c) -> Flux.error(new IllegalArgumentException(String.format("No script engine named '%s' found", engineName)));
        }
        try {
            Map<String, Object> bindings = mapper.treeToValue(jarConfiguration.configuration().getConfiguration("bindings").json(), Map.class);
            Bindings globalBindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
            globalBindings.put("bindings", bindings);
            globalBindings.put("services", new ServicesJSObject(serviceLocator));
            ByteArrayOutputStream out = new ByteArrayOutputStreamWithoutNewLine();
            engine.getContext().setWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

            return new ScriptFlux(engine, jarConfiguration, transmutationConfiguration, loggerContext.getLogger(jarConfiguration.getName().orElse("script")));
        } catch (JsonProcessingException e) {
            return (metadataJsonNodeFlux, contextView) -> Flux.error(new JarException(jarConfiguration, transmutationConfiguration, "Cannot parse bindings", e));
        }
    }
}
