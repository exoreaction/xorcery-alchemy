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
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;

import java.io.IOException;
import java.util.concurrent.Callable;

public class RowReaderStreamer
        implements Subscription {
    private final ReadableWorkbook workBook;
    private final Callable<MetadataJsonNode<JsonNode>> itemReader;
    private final CoreSubscriber<? super MetadataJsonNode<JsonNode>> subscriber;
    private long streamPosition = 0;

    public RowReaderStreamer(CoreSubscriber<? super MetadataJsonNode<JsonNode>> subscriber, ReadableWorkbook workBook, Callable<MetadataJsonNode<JsonNode>> itemReader) {
        this.subscriber = subscriber;
        this.workBook = workBook;
        this.itemReader = itemReader;

        // Skip until position
        long skip = new ContextViewElement(subscriber.currentContext())
                .getLong(JarContext.streamPosition)
                .map(pos -> pos + 1).orElse(0L);
        try {
            for (int i = 0; i < skip; i++) {
                itemReader.call();
            }
            streamPosition = skip;
        } catch (Throwable e) {
            subscriber.onError(e);
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
                subscriber.onNext(item);
            }

            if (item == null) {
                workBook.close();
                subscriber.onComplete();
            }
        } catch (Throwable e) {
            try {
                workBook.close();
            } catch (IOException ex) {
                // Ignore
            }
            subscriber.onError(e);
        }
    }

    @Override
    public void cancel() {
        try {
            workBook.close();
        } catch (IOException e) {
            subscriber.onError(e);
        }
    }

}
