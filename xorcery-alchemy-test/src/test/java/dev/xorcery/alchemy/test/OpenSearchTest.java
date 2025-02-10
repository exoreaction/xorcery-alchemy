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
package dev.xorcery.alchemy.test;

import dev.xorcery.alchemy.crucible.TransmutationsRunner;
import dev.xorcery.configuration.builder.ConfigurationBuilder;
import dev.xorcery.junit.XorceryExtension;
import dev.xorcery.opensearch.OpenSearchService;
import dev.xorcery.opensearch.client.search.Document;
import dev.xorcery.opensearch.client.search.SearchQuery;
import dev.xorcery.opensearch.client.search.SearchRequest;
import dev.xorcery.opensearch.client.search.SearchResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Disabled
class OpenSearchTest {

    @RegisterExtension
    static XorceryExtension xorcery = XorceryExtension.xorcery()
            .configuration(ConfigurationBuilder::addTestDefaults)
            .configuration(b -> b.addResource("testCsvToOpenSearch.yaml"))
            .build();

    @Test
    public void testOpenSearch(TransmutationsRunner transmutationsRunner, OpenSearchService openSearchService) throws Exception {
        transmutationsRunner.getDone().orTimeout(10, TimeUnit.SECONDS).join();

        SearchResponse response = openSearchService.getClient().search().search("people", SearchRequest.builder()
                .query(SearchQuery.match_all())
                .size(1)
                .build(), Map.of("sort", "metadata.streamPosition:desc")).orTimeout(10, TimeUnit.SECONDS).join();

        for (Document document : response.hits().documents()) {
            System.out.println(document.source().toPrettyString());
        }
    }
}