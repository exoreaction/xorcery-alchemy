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
import com.opencsv.CSVReader;
import dev.xorcery.reactivestreams.api.ContextViewElement;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import dev.xorcery.reactivestreams.api.ReactiveStreamsContext;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.util.concurrent.Callable;

class RowReaderStreamer
        implements Disposable {
    private final CSVReader csvReader;
    private final Callable<MetadataJsonNode<JsonNode>> itemReader;
    private final FluxSink<? super MetadataJsonNode<JsonNode>> sink;
    private long streamPosition = 0;

    public RowReaderStreamer(FluxSink<? super MetadataJsonNode<JsonNode>> sink, CSVReader csvReader, Callable<MetadataJsonNode<JsonNode>> itemReader) {
        this.sink = sink;
        this.csvReader = csvReader;
        this.itemReader = itemReader;

        // Skip until position
        long skip = new ContextViewElement(sink.contextView())
                .getLong(ReactiveStreamsContext.streamPosition)
                .map(pos -> pos + 1).orElse(0L);
        try {
            csvReader.skip((int) skip);
            streamPosition = skip;
        } catch (IOException e) {
            sink.error(e);
        }
    }

    public void request(long request) {
        try {
            if (request == 0)
                return;

            MetadataJsonNode<JsonNode> item = null;
            while (request-- > 0 && (item = itemReader.call()) != null) {
                item.metadata().json().put("timestamp", System.currentTimeMillis());
                item.metadata().json().put("streamPosition", streamPosition++);
                sink.next(item);
            }

            if (item == null) {
                sink.complete();
            }
        } catch (Throwable e) {
            sink.error(e);
        }
    }

    @Override
    public void dispose() {
        try {
            csvReader.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}
