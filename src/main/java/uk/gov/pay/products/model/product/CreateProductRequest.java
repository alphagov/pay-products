package uk.gov.pay.products.model.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.products.util.ProductType;

import java.util.Map;

/**
 * Class for OpenAPI specs only. Currently create product resource uses JsonNode payload which can potentially be replaced by this class.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateProductRequest {

    @JsonProperty("gateway_account_id")
    @Schema(example = "1", required = true, description = "gateway account id of the Gateway Account as identified by adminusers.")
    private String gatewayAccountId;

    @JsonProperty("pay_api_token")
    @Schema(example = "api_test_5meusgv5ufclsbvde78mdf35bfdhnhm1307euq94kcf0lcqcqrovbjovib", required = true,
            description = "valid api token for the gateway account of above service which this product takes payments for")
    private String payApiToken;

    @JsonProperty("name")
    @Schema(example = "A name for the product", required = true, description = "Name of the product. This will be passed as the name when creating the charge")
    private String name;

    @JsonProperty("type")
    @Schema(example = "DEMO", required = true, description = "Type of the product")
    private ProductType type;

    @Schema(example = "1050", description = "Price for the product in pence. This will be passed as the amount when creating charge. Mandatory for Non-ADHOC and Non-AGENT_INITIATED_MOTO products")
    @JsonProperty("price")
    private Long price;

    @Schema(example = "Description of the product", description = "Description of the product. This will be passed as the description when creating the charge")
    @JsonProperty("description")
    private String description;

    @JsonProperty("return_url")
    @Schema(example = "https://some.valid.url/", description = "(https only) where to redirect to upon completion of a payment. If not provided, pay-products will generate a default url to itself when creating a charge")
    private String returnUrl;

    @JsonProperty("service_name_path")
    @Schema(example = "some-awesome-government-service", description = "Service Name Path part of Product Path. Required for Adhoc type only.")
    private String serviceNamePath;

    @JsonProperty("product_name_path")
    @Schema(example = "name-for-product", description = "Product Name Path part of Product Path. Required for Adhoc type only.")
    private String productNamePath;

    @JsonProperty("reference_enabled")
    @Schema(example = "true", description = "Flag to set whether payment reference is auto generated or entered by user. True means that user enters reference at the beginning of a user journey.")
    private Boolean referenceEnabled;

    @JsonProperty("reference_label")
    @Schema(example = "Amount for your licence", description = "Only required if reference_enabled is true. Label for the reference entry text box.")
    private String referenceLabel;

    @JsonProperty("reference_hint")
    @Schema(example = "This can be found on your letter", description = "Hint text for reference entry text box. Optional field when reference enabled. Ignored if reference_enabled is set to false.")
    private String referenceHint;

    @JsonProperty("language")
    @Schema(example = "en", description = "The language pages for the product will be in. If not provided, defaults to 'en'. Allowed values 'en', 'cy'")
    private String language;

    @JsonProperty("metadata")
    private Map<String, String> metadata;
}
