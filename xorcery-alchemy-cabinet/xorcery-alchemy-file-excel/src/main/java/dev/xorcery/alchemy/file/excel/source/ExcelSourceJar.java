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
import dev.xorcery.alchemy.jar.*;
import dev.xorcery.configuration.Configuration;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import org.dhatim.fastexcel.reader.*;
import org.jvnet.hk2.annotations.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

@Service(name = "excel", metadata = "enabled=jars.enabled")
public class ExcelSourceJar
        implements SourceJar
{
    @Override
    public Flux<MetadataJsonNode<JsonNode>> newSource(JarConfiguration jarConfiguration, TransmutationConfiguration transmutationConfiguration) {
        return Flux.push(sink ->{
            try {
                Object sourceUrl = jarConfiguration.get(JarContext.sourceUrl)
                        .orElseThrow(Configuration.missing(JarContext.sourceUrl.name()));
                URL excelResource = sourceUrl instanceof URL url ? url : new URL(sourceUrl.toString());

                // Get metadata first
                Metadata.Builder metadataBuilder = new Metadata.Builder();
                metadataBuilder.add(StandardMetadata.sourceUrl, excelResource.toExternalForm());
                try (InputStream resourceIn = excelResource.openStream(); ReadableWorkbook wb = new ReadableWorkbook(resourceIn, new ReadingOptions(true, false))) {
                    wb.getSheets().filter(sheet -> sheet.getName().equals("Metadata")).findFirst().ifPresent(metadataSheet ->
                    {
                        try {
                            Iterator<Row> rowIterator = metadataSheet.openStream().iterator();
                            List<String> metadataNames = new ArrayList<>();
                            Row headers = rowIterator.next();
                            headers.forEach(cell -> metadataNames.add(cell.asString()));
                            Iterator<String> nameIterator = metadataNames.iterator();
                            Row metadataValueRow = rowIterator.next();
                            metadataValueRow.forEach(cell -> metadataBuilder.add(nameIterator.next(), Cells.toJsonNode(cell)));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                }
                Metadata metadata = metadataBuilder.build();

                // Stream data
                InputStream resourceIn = excelResource.openStream();
                String dataSheetName = jarConfiguration.getString("data").orElse(null);
                ReadableWorkbook wb = new ReadableWorkbook(resourceIn, new ReadingOptions(true, false));
                Iterator<Sheet> dataSheets = wb.getSheets().filter(sheet -> !sheet.getName().equals("Metadata") &&
                        (dataSheetName == null || sheet.getName().equals(dataSheetName))).iterator();
                Callable<MetadataJsonNode<JsonNode>> itemReader = new RowReader(dataSheets, metadata);

                RowReaderStreamer rowReaderStreamer = new RowReaderStreamer(sink, wb, itemReader);
                sink.onDispose(rowReaderStreamer);
                sink.onRequest(rowReaderStreamer::request);
            } catch (Throwable e) {
                sink.error(new JarException(jarConfiguration, transmutationConfiguration, "Excel parsing failed", e));
            }
        });
    }
}
