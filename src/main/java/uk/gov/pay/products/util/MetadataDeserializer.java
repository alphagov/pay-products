package uk.gov.pay.products.util;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.products.model.ProductMetadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MetadataDeserializer {
    
    public static List<ProductMetadata> extractMetadata(JsonNode payload, String fieldName) {
        List<ProductMetadata> metadataList = new ArrayList<>();
        JsonNode metadata = payload.get(fieldName);
        if (metadata != null && !metadata.isEmpty()) {
            Iterator<String> fieldNames = metadata.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                String value = metadata.get(key).textValue();
                metadataList.add(new ProductMetadata(key, value));
            }
        }
        return metadataList.isEmpty() ? null : metadataList;
    }

}
