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
package dev.xorcery.alchemy.file.yaml.transmute;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import dev.xorcery.alchemy.jar.*;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import org.jvnet.hk2.annotations.Service;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.util.context.ContextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.function.BiFunction;

@Service(name = "yaml", metadata = "enabled=jars.enabled")
public class YamlFileTransmuteJar
        implements TransmuteJar {

    @Override
    public Transmute newTransmute(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        return (flux, context) ->
        {
            URI fileUrl = jarConfiguration.getURI(JarContext.resultUrl).orElse(null);

            if (fileUrl == null) {
                return Flux.error(new JarException(jarConfiguration, transmutationConfiguration, "Could not find file"));
            }

            if (fileUrl.getScheme().equals("file")) {
                new File(fileUrl).getParentFile().mkdirs();
            }

            try {
                BufferedOutputStream outputStream = new BufferedOutputStream(fileUrl.getScheme().equals("file")
                        ? new FileOutputStream(new File(fileUrl).getAbsoluteFile())
                        : fileUrl.toURL().openConnection().getOutputStream());
                ObjectMapper mapper = new YAMLMapper().findAndRegisterModules()
                        .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false)
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                        .enable(SerializationFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE)
                        .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
                return flux.doOnTerminate(() ->
                {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                }).handle((item, sink) ->
                {
                    try {
                        mapper.writeValue(outputStream, item);
                        sink.next(item);
                    } catch (IOException e) {
                        sink.error(e);
                    }
                });
            } catch (Throwable e) {
                return Flux.error(new JarException(jarConfiguration, transmutationConfiguration, "Could not write YAML file", e));
            }
        };
    }
}
