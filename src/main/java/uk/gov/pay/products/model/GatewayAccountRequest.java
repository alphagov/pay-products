package uk.gov.pay.products.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class GatewayAccountRequest {

    public static final String FIELD_OPERATION = "op";
    public static final String FIELD_OPERATION_PATH = "path";
    public static final String FIELD_VALUE = "value";

    private String op;
    private String path;
    private JsonNode value;

    public String getOp() {
        return op;
    }

    public String getPath() {
        return path;
    }

    public String valueAsString() {
        if (value != null && value.isTextual()) {
            return value.asText();
        }
        return null;
    }

    public List<String> valueAsList() {
        if (value != null && value.isArray()) {
            return newArrayList(value.elements())
                    .stream()
                    .map(node -> node.textValue())
                    .collect(toList());
        }
        return null;
    }

    public Map<String, String> valueAsObject() {
        if (value != null) {
            if ((value.isTextual() && !isEmpty(value.asText())) || (!value.isNull() && value.isObject())) {
                try {
                    return new ObjectMapper().readValue(value.traverse(), new TypeReference<Map<String, String>>() {});
                } catch (IOException e) {
                    throw new RuntimeException(format("Malformed JSON object in GatewayAccountRequest.value"), e);
                }
            }
        }
        return null;
    }


    private GatewayAccountRequest(String op, String path, JsonNode value) {
        this.op = op;
        this.path = path;
        this.value = value;
    }

    public static GatewayAccountRequest from(JsonNode payload) {
        return new GatewayAccountRequest(
                payload.get(FIELD_OPERATION).asText(),
                payload.get(FIELD_OPERATION_PATH).asText(),
                payload.get(FIELD_VALUE));

    }
}
