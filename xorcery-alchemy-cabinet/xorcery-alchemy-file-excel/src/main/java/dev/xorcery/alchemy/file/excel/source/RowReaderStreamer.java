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
package dev.xorcery.alchemy.file.excel.source;

import com.fasterxml.jackson.databind.JsonNode;
import dev.xorcery.alchemy.jar.JarContext;
import dev.xorcery.alchemy.jar.StandardMetadata;
import dev.xorcery.reactivestreams.api.ContextViewElement;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.util.concurrent.Callable;

public class RowReaderStreamer
        implements Disposable {
    private final ReadableWorkbook workBook;
    private final Callable<MetadataJsonNode<JsonNode>> itemReader;
    private final FluxSink<? super MetadataJsonNode<JsonNode>> sink;
    private long streamPosition = 0;

    public RowReaderStreamer(FluxSink<? super MetadataJsonNode<JsonNode>> sink, ReadableWorkbook workBook, Callable<MetadataJsonNode<JsonNode>> itemReader) {
        this.sink = sink;
        this.workBook = workBook;
        this.itemReader = itemReader;

        // Skip until position
        long skip = new ContextViewElement(sink.currentContext())
                .getLong(JarContext.streamPosition)
                .map(pos -> pos + 1).orElse(0L);
        try {
            for (int i = 0; i < skip; i++) {
                itemReader.call();
            }
            streamPosition = skip;
        } catch (Throwable e) {
            sink.error(e);
        }
    }

    public void request(long request) {
        try {
            if (request == 0)
                return;

            MetadataJsonNode<JsonNode> item = null;
            while (request-- > 0 && (item = itemReader.call()) != null) {
                item.metadata().json().put(StandardMetadata.timestamp.name(), System.currentTimeMillis());
                item.metadata().json().put(StandardMetadata.streamPosition.name(), streamPosition++);
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
            workBook.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}
