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
package dev.xorcery.alchemy.opensearch.transmute;

import dev.xorcery.alchemy.jar.*;
import dev.xorcery.lang.Exceptions;
import dev.xorcery.opensearch.OpenSearchService;
import dev.xorcery.opensearch.client.search.Document;
import dev.xorcery.opensearch.client.search.SearchRequest;
import dev.xorcery.opensearch.client.search.SearchResponse;
import dev.xorcery.util.UUIDs;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service(name="opensearch", metadata = "enabled=jars.enabled")
public class OpenSearchTransmuteJar
    implements TransmuteJar
{
    private final OpenSearchService openSearchService;

    @Inject
    public OpenSearchTransmuteJar(OpenSearchService openSearchService) {
        this.openSearchService = openSearchService;
    }

    @Override
    public Transmute newTransmute(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        return (flux, context)-> flux.contextWrite(ctx ->
        {
            // Find last metadata.streamPosition for this sourceUrl under the given alias
            String sourceUrl = jarConfiguration.getString(JarContext.sourceUrl).orElse(null);
            if (sourceUrl == null)
                return ctx;

            String alias = jarConfiguration.getString("alias").orElse(null);
            if (alias == null)
                return ctx;

            try {
                SearchRequest request = SearchRequest.builder()
                        .query(QueryDSL.match_phrase("metadata.sourceUrl", sourceUrl))
                        .build();
                SearchResponse response = openSearchService.getClient().search().search(alias, request, Map.of("size", "1", "sort", "metadata.streamPosition:desc")).orTimeout(10, TimeUnit.SECONDS).join();

                List<Document> documents = response.hits().documents();
                if (!documents.isEmpty())
                {
                    long streamPosition = documents.get(0).json().path("_source").path("metadata").path("streamPosition").longValue();
                    return ctx.put(JarContext.streamPosition.name(), streamPosition);
                }
                return ctx;
            } catch (Throwable e) {
                if (Exceptions.unwrap(e) instanceof NotFoundException)
                {
                    // Ok!
                    return ctx;
                } else {
                    throw (RuntimeException)e;
                }
            }
        }).transformDeferredContextual(openSearchService.documentUpdates(item -> UUIDs.newId(), Function.identity()));
    }
}
