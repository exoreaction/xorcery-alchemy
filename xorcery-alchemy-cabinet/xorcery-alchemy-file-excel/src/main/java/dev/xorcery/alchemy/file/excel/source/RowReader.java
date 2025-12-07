package dev.xorcery.alchemy.file.excel.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.xorcery.metadata.Metadata;
import dev.xorcery.reactivestreams.api.MetadataJsonNode;
import org.dhatim.fastexcel.reader.Row;
import org.dhatim.fastexcel.reader.Sheet;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import static dev.xorcery.alchemy.file.excel.source.Cells.hasDateFormat;
import static dev.xorcery.alchemy.file.excel.source.Cells.toJsonNode;

class RowReader implements Callable<MetadataJsonNode<JsonNode>> {

    private final Iterator<Sheet> dataSheets;
    private final Metadata metadata;
    Sheet sheet;
    Iterator<Row> rowIterator;
    final List<String> headerNames;

    public RowReader(Iterator<Sheet> dataSheets, Metadata metadata) {
        this.dataSheets = dataSheets;
        this.metadata = metadata;
        headerNames = new ArrayList<>();
    }

    @Override
    public MetadataJsonNode<JsonNode> call() throws Exception {
        if (rowIterator == null || !rowIterator.hasNext()) {
            if (dataSheets.hasNext()) {
                sheet = dataSheets.next();
                rowIterator = sheet.read().iterator();
                if (!rowIterator.hasNext()) {
                    return null;
                }

                Row headers = rowIterator.next();
                headerNames.clear();
                headers.forEach(cell -> headerNames.add(hasDateFormat(cell)
                        ? cell.asDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        : cell.asString()));
                if (!rowIterator.hasNext()) {
                    return null;
                }
            } else {
                return null;
            }
        }

        ObjectNode data = JsonNodeFactory.instance.objectNode();
        Row row = rowIterator.next();
        Iterator<String> headerIterator = headerNames.stream().iterator();
        row.forEach(cell -> {
            if (headerIterator.hasNext())
                data.set(headerIterator.next(), toJsonNode(cell));
        });
        return new MetadataJsonNode<>(new Metadata.Builder()
                .add(metadata)
                .add("sheet", sheet.getName())
                .build(), data);
    }
}
