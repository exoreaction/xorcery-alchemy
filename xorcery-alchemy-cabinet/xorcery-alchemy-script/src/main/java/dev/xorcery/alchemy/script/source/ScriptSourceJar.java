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
package dev.xorcery.alchemy.script.source;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.alchemy.jar.JarException;
import dev.xorcery.alchemy.jar.SourceJar;
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.alchemy.script.*;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import jakarta.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

@Service(name = "script", metadata = "enabled=jars.enabled")
public class ScriptSourceJar
        implements SourceJar {

    private final ObjectMapper mapper = new ObjectMapper();
    private final ServiceLocator serviceLocator;
    private final LoggerContext loggerContext;

    @Inject
    public ScriptSourceJar(ServiceLocator serviceLocator, LoggerContext loggerContext) {
        this.serviceLocator = serviceLocator;
        this.loggerContext = loggerContext;
    }

    @Override
    public Flux<MetadataJsonNode<JsonNode>> newSource(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        String engineName = jarConfiguration.getString("engine").orElse("nashorn");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(engineName);
        if (engine == null) {
            return Flux.error(new JarException(jarConfiguration, transmutationConfiguration, String.format("No script engine named '%s' found", engineName)));
        }

        try {
            Map<String, Object> bindings = mapper.treeToValue(jarConfiguration.configuration().getConfiguration("bindings").json(), Map.class);
            Bindings globalBindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
            globalBindings.put("bindings", bindings);
            globalBindings.put("services", new ServicesJSObject(serviceLocator));
            ByteArrayOutputStream out = new ByteArrayOutputStreamWithoutNewLine();
            engine.getContext().setWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            ScriptSource scriptSource = new ScriptSource(engine, jarConfiguration, transmutationConfiguration, out, loggerContext.getLogger(jarConfiguration.getName().orElse("script")));
            return Flux.generate(scriptSource, scriptSource).publishOn(Schedulers.boundedElastic());
        } catch (JsonProcessingException e) {
            return Flux.error(new JarException(jarConfiguration, transmutationConfiguration, "Cannot parse bindings", e));
        }
    }

    static class ScriptSource
            implements
            Callable<Bindings>,
            BiFunction<Bindings, SynchronousSink<MetadataJsonNode<JsonNode>>, Bindings> {
        private final ByteArrayOutputStream out;
        private final Logger logger;
        private final ScriptExecutor subscribe;
        private final ScriptExecutor next;

        private final ScriptEngine engine;
        private final JarConfiguration configuration;
        private final TransmutationConfiguration transmutationConfiguration;

        public ScriptSource(ScriptEngine engine, JarConfiguration configuration, TransmutationConfiguration transmutationConfiguration, ByteArrayOutputStream out, Logger logger) {
            this.out = out;
            this.logger = logger;
            this.subscribe = configuration.getString("subscribe").map(script -> ScriptExecutor.getScriptExecutor(engine, script)).orElse(null);
            this.next = configuration.getString("next").map(script -> ScriptExecutor.getScriptExecutor(engine, script)).orElse(null);
            this.engine = engine;
            engine.getContext().getWriter();
            this.configuration = configuration;
            this.transmutationConfiguration = transmutationConfiguration;
        }

        @Override
        public Bindings call() throws Exception {
            Bindings bindings = engine.createBindings();
            bindings.put("metadata", new JsonNodeJSObject(JsonNodeFactory.instance.objectNode()));
            if (subscribe != null)
            {
                subscribe.call(bindings);
                if (out.size() > 0) {
                    logger.info(out.toString(StandardCharsets.UTF_8));
                    out.reset();
                }
            }
            return bindings;
        }

        @Override
        public Bindings apply(Bindings bindings, SynchronousSink<MetadataJsonNode<JsonNode>> sink) {
            try {
                MetadataJsonNode<JsonNode> item = new MetadataJsonNode<>(new Metadata(JsonNodeFactory.instance.objectNode()), ((JsonNodeJSObject)bindings.get("metadata")).getJsonNode().deepCopy());
                ObjectNode itemJson = JsonNodeFactory.instance.objectNode();
                itemJson.set("metadata", item.metadata().json());
                itemJson.set("data", item.data());
                bindings.put("item", new JsonNodeJSObject(itemJson));
                bindings.put("sink", new JavaScriptSynchronousSink(sink));
                next.call(bindings);

                if (out.size() > 0) {
                    logger.info(out.toString(StandardCharsets.UTF_8));
                    out.reset();
                }
            } catch (Exception e) {
                sink.error(new JarException(configuration, transmutationConfiguration, "Script failed", e));
            }
            return bindings;
        }
    }
}
