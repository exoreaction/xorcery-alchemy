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
package dev.xorcery.alchemy.common.transmute;

import com.fasterxml.jackson.databind.JsonNode;
import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.alchemy.jar.TransmuteJar;
import dev.xorcery.collections.Element;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.util.context.ContextView;
import reactor.util.retry.Retry;
import reactor.util.retry.RetrySpec;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Service(name = "retry", metadata = "enabled=jars.enabled")
public class RetryTransmuteJar
        implements TransmuteJar {
    @Inject
    public RetryTransmuteJar() {
    }

    @Override
    public BiFunction<Flux<MetadataJsonNode<JsonNode>>, ContextView, Publisher<MetadataJsonNode<JsonNode>>> newTransmute(JarConfiguration configuration, TransmutationConfiguration transmutationConfiguration) {
        Retry retry = RetrySpec.backoff(
                        configuration.getLong("maxAttempts").filter(v -> v != -1).orElse(Long.MAX_VALUE),
                        Duration.parse("PT" + configuration.getString("minBackoff").orElseThrow(Element.missing("minBackoff"))))
                .maxBackoff(Duration.parse("PT" + configuration.getString("maxBackoff").orElseThrow(Element.missing("maxBackoff"))))
                .jitter(configuration.getDouble("jitter").orElse(0.5))
                .multiplier(configuration.getDouble("multiplier").orElse(2D))
                .filter(retryFilter(configuration));
        return (flux, context) -> flux.retryWhen(retry);
    }

    protected Predicate<? super Throwable> retryFilter(JarConfiguration configuration) {
        List<String> includes = configuration.configuration().getListAs("includes", JsonNode::asText).orElse(Collections.emptyList());
        List<String> excludes = configuration.configuration().getListAs("excludes", JsonNode::asText).orElse(Collections.emptyList());
        Predicate<? super Throwable> retryPredicate = includes.isEmpty()
                ? t -> true
                : t -> throwableAndCauses(t).anyMatch(throwableOrCause -> includes.stream().anyMatch(name -> throwableOrCause.getClass().getName().equals(name) || throwableOrCause.getClass().getSimpleName().equals(name)));
        return excludes.isEmpty()
                ? retryPredicate
                : retryPredicate.and(t -> throwableAndCauses((Throwable) t)
                .noneMatch(throwableOrCause -> includes.stream().noneMatch(name -> throwableOrCause.getClass().getName().equals(name) || throwableOrCause.getClass().getSimpleName().equals(name))));
    }

    protected Stream<Throwable> throwableAndCauses(Throwable throwable) {
        return Stream.iterate(throwable, t -> t.getCause() != null, Throwable::getCause);
    }
}
