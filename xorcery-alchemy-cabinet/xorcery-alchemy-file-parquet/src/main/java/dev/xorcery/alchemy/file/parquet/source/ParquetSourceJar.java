package dev.xorcery.alchemy.file.parquet.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jerolba.carpet.CarpetReader;
import com.jerolba.carpet.CloseableIterator;
import dev.xorcery.alchemy.jar.JarConfiguration;
import dev.xorcery.alchemy.jar.JarContext;
import dev.xorcery.alchemy.jar.SourceJar;
import dev.xorcery.alchemy.jar.TransmutationConfiguration;
import dev.xorcery.configuration.Configuration;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import dev.xorcery.util.Resources;
import org.apache.parquet.column.page.PageReadStore;
import org.jvnet.hk2.annotations.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.Map;
import java.util.function.LongConsumer;

@Service(name = "parquet", metadata = "enabled=jars.enabled")
public class ParquetSourceJar
        implements SourceJar {

    @Override
    public Flux<MetadataJsonNode<JsonNode>> newSource(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        ObjectMapper objectMapper = new JsonMapper().findAndRegisterModules();

        return Flux.push(sink -> {
            try {
                Object sourceUrl = jarConfiguration.get(JarContext.sourceUrl)
                        .orElseThrow(Configuration.missing(JarContext.sourceUrl.name()));
                File parquetResource = Resources.getResource(sourceUrl.toString()).map(url -> new File(url.getFile()).getAbsoluteFile()).orElseThrow();

                CloseableIterator<Map> iterator = new CarpetReader<>(parquetResource, Map.class).iterator();
                sink.onRequest(new LongConsumer() {
                    PageReadStore rowGroup;

                    @Override
                    public void accept(long requests) {
                        try {
                            while (requests-- > 0 && iterator.hasNext()){
                                Map record = iterator.next();
                                JsonNode jsonNode = objectMapper.valueToTree(record);
                                sink.next(new MetadataJsonNode<>(new Metadata.Builder().build(), jsonNode));
                            }
                            if (!iterator.hasNext())
                            {
                                iterator.close();
                                sink.complete();
                            }
                        } catch (Throwable e) {
                            sink.error(e);
                        }
                    }
                });
            } catch (Throwable t) {
                sink.error(t);
            }
        });
    }
}
