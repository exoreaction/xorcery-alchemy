package dev.xorcery.alchemy.file.excel.source;

import com.fasterxml.jackson.databind.JsonNode;
import org.dhatim.fastexcel.reader.Cell;

import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

public interface Cells {
     static JsonNode toJsonNode(Cell cell) {
        return switch (cell.getType()) {
            case NUMBER -> hasDateFormat(cell)
                    ? instance.textNode(cell.asDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    : instance.numberNode(cell.asNumber());
            case STRING -> instance.textNode(cell.asString());
            case BOOLEAN -> instance.booleanNode(cell.asBoolean());
            default -> instance.missingNode();
        };
    }

    static boolean hasDateFormat(Cell cell) {
        String dataFormatString = cell.getDataFormatString();
        return dataFormatString != null && (dataFormatString.contains("y") || dataFormatString.contains("m") || dataFormatString.contains("d"));
    }
}
