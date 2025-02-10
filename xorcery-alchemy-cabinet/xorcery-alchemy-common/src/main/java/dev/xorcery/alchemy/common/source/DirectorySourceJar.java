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
package dev.xorcery.alchemy.common.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.alchemy.jar.SourceJar;
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import org.jvnet.hk2.annotations.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static dev.xorcery.alchemy.jar.StandardMetadata.timestamp;

@Service(name = "directory", metadata = "enabled=jars.enabled")
public class DirectorySourceJar
        implements SourceJar {

    @Override
    public Flux<MetadataJsonNode<JsonNode>> newSource(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        try {
            DirectoryConfiguration directoryConfiguration = new DirectoryConfiguration(jarConfiguration);
            Path directory = Path.of(directoryConfiguration.getPath());

            String matcherPattern = directoryConfiguration.getFilter();

            final List<Path> filePaths = new ArrayList<>();
            final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(matcherPattern);
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (matcher.matches(file)) {
                        filePaths.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            return Flux.fromIterable(filePaths).handle(this::publishFile);
        } catch (Throwable e) {
            return Flux.error(e);
        }
    }

    private void publishFile(Path path, SynchronousSink<MetadataJsonNode<JsonNode>> sink) {
        try {
            MetadataJsonNode<JsonNode> item = new MetadataJsonNode<>(
                    new Metadata.Builder()
                            .add(timestamp, path.toFile().lastModified())
                            .build(),
                    JsonNodeFactory.instance.objectNode().put("resourceUrl", path.toUri().toURL().toExternalForm()));
            sink.next(item);
        } catch (Throwable e) {
            sink.error(e);
        }
    }
}
