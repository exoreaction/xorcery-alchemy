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
package dev.xorcery.alchemy.file.csv.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.*;
import dev.xorcery.alchemy.jar.*;
import dev.xorcery.collections.Element;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import org.jvnet.hk2.annotations.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Callable;

@Service(name = "csv", metadata = "enabled=jars.enabled")
public class CSVFileSourceJar
        implements SourceJar {

    private static final Scheduler scheduler = Schedulers.newSingle("CSV");

    @Override
    public Flux<MetadataJsonNode<JsonNode>> newSource(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        try {
            Object sourceUrl = jarConfiguration.get(JarContext.sourceUrl)
                    .orElseThrow(Element.missing(JarContext.sourceUrl));
            URL csvResource = sourceUrl instanceof URL url ? url : new URL(sourceUrl.toString());
            String csvResourceUrl = csvResource.toExternalForm();

            CSVParserBuilder parserBuilder = new CSVParserBuilder();

            jarConfiguration.getString("escape").ifPresent(c -> parserBuilder.withEscapeChar(c.charAt(0)));
            jarConfiguration.getString("separator").ifPresent(c -> parserBuilder.withSeparator(c.charAt(0)));
            jarConfiguration.getString("quote").ifPresent(c -> parserBuilder.withQuoteChar(c.charAt(0)));
            CSVParser csvParser = parserBuilder.build();

            return Flux.<MetadataJsonNode<JsonNode>>create(sink -> {

                try {
                    if (jarConfiguration.getBoolean("headers").orElse(false)) {
                        CSVReaderHeaderAware csvReader = new CSVReaderHeaderAwareBuilder(new BufferedReader(new InputStreamReader(csvResource.openStream(), StandardCharsets.UTF_8)))
                                .withCSVParser(csvParser)
                                .build();
                        Callable<MetadataJsonNode<JsonNode>> itemReader = () ->
                        {
                            Map<String, String> map = csvReader.readMap();
                            if (map == null)
                                return null;
                            ObjectNode data = JsonNodeFactory.instance.objectNode();
                            map.forEach(data::put);
                            return new MetadataJsonNode<>(new Metadata.Builder()
                                    .add(StandardMetadata.sourceUrl, csvResourceUrl)
                                    .build(), data);
                        };
                        RowReaderStreamer streamer = new RowReaderStreamer(sink, csvReader, itemReader);
                        sink.onDispose(streamer);
                        sink.onRequest(streamer::request);
                    } else {
                        CSVReader csvReader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(csvResource.openStream(), StandardCharsets.UTF_8)))
                                .withCSVParser(csvParser)
                                .build();
                        Callable<MetadataJsonNode<JsonNode>> itemReader = () ->
                        {
                            String[] row = csvReader.readNext();
                            if (row == null)
                                return null;
                            ArrayNode data = JsonNodeFactory.instance.arrayNode();
                            for (String column : row) {
                                data.add(column);
                            }
                            return new MetadataJsonNode<>(new Metadata.Builder()
                                    .add(StandardMetadata.sourceUrl, csvResourceUrl)
                                    .build(), data);
                        };
                        RowReaderStreamer streamer = new RowReaderStreamer(sink, csvReader, itemReader);
                        sink.onDispose(streamer);
                        sink.onRequest(streamer::request);
                    }
                } catch (Throwable e) {
                    sink.error(e);
                }
            }).subscribeOn(scheduler, true);
        } catch (Throwable e) {
            return Flux.error(e);
        }
    }
}
