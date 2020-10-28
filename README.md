# pay-products

Products integrating with GOV.UK Pay in Java(Dropwizard)

This microservice manages products (things that a Government Service would like to take payments for).

These products are integrated with GOV.UK Pay so that a user can make payments using a provided URL. 

## Environment Variables

| Variable | Description |
|----------|-------------|
| `ADMIN_PORT`                  | The port number to listen for Dropwizard admin requests on. Defaults to `8081`. |
| `BASE_URL`                    | The base URL of the [products-ui](https://github.com/alphagov/pay-products-ui) microservice. |
| `DB_HOST`                     | The hostname of the database server. Defaults to `products.db.pymnt.localdomain` |
| `DB_PASSWORD`                 | The password for the `DB_USER` user. |
| `DB_SSL_OPTION`               | To turn TLS on this value must be set as `ssl=true`. Otherwise must be empty. |
| `DB_USER`                     | The username to log into the database as. |
| `EMAIL_ADDRESS_FOR_ROTATING_API_KEYS` | The email address used for rotating API keys. |
| `JAVA_HOME`                   | The location of the JRE. Set to `/opt/java/openjdk` in the `Dockerfile`. |
| `JAVA_OPTS`                   | Commandline arguments to pass to the java runtime. Optional. |
| `JPA_LOG_LEVEL`               | The logging level to set for JPA. Defaults to `WARNING`. |
| `JPA_SQL_LOG_LEVEL`           | The logging level to set for JPA SQL logging. Defaults to `WARNING`. |
| `METRICS_HOST`                | The hostname to send graphite metrics to. Defaults to `localhost`. |
| `METRICS_PORT`                | The port number to send graphite metrics to. Defaults to `8092`. |
| `PORT`                        | The port number to listen for requests on. Defaults to `8080`. |
| `PRODUCTSUI_CONFIRMATION_URL` | The URL of the confirmation page endpoint in the [products-ui](https://github.com/alphagov/pay-products-ui) microservice. |
| `PRODUCTSUI_PAY_URL`          | The URL of the `pay` endpoint in the [products-ui](https://github.com/alphagov/pay-products-ui) microservice. |
| `PRODUCTS_FRIENDLY_BASE_URI`  | The URL of the products endpoint in the [products-ui](https://github.com/alphagov/pay-products-ui) microservice. |
| `PUBLICAPI_URL`               | The URL to the [publicapi](https://github.com/alphagov/pay-publicapi) microservice |
| `PUBLICAUTH_URL`              | The URL to the [publicauth](https://github.com/alphagov/pay-publicauth) microservice |
| `RUN_APP`                     | Set to `true` to run the application. Defaults to `true`. |
| `RUN_MIGRATION`               | Set to `true` to run a database migration. Defaults to `false`. |
| `SECURE_RETURN_URLS`          | Set to `false` to allow non-HTTPS URLs for the `return_url` field of a product. Defaults to `true`. |

## API Specification

The [API Specification](docs/api_specification.md) provides more detail on the paths and operations including examples.

The JSON naming convention follows Hypertext Application Language (HAL).
 
### API NAMESPACE

| Path                          | Supported Methods | Description                        |
| ----------------------------- | ----------------- | ---------------------------------- |
|[```/v1/api/products```](docs/api_specification.md#post-v1apiproducts)        | POST    |  Creates a new product definition            |
|[```/v1/api/gateway-account/{gatewayAccountId}/products/{productId}```](docs/api_specification.md#put-v1apigateway\-accountgatewayaccountidproductsproductid)        | PATCH    |  Updates an existing product matching the specified `productId` and belonging to the gateway account specified by `gatewayAccountId`. Returns the product only if if the update is successful.|
|[```/v1/api/products/{productId}```](docs/api_specification.md#get-v1apiproductsproductid)        | GET    |  Gets an existing product with the specified productId   |
|[```/v1/api/products/{productId}/regenerate-api-key```](docs/api_specification.md#post-v1apiproductsproductexternalidregenerate\-api\-key)        | POST    |  Regenerates a new API key and replaces an old API key with the new key for the specified `productId`.|
|[```/v1/api/gateway-account/{gatewayAccountId}/products/{productId}```](docs/api_specification.md#get-v1apigateway\-accountgatewayaccountidproductsproductid)        | GET    |  Gets an existing product with the specified productId that belong to the gateway account specified by gatewayAccountId. Returns the product only if it exists in the given gateway account. Useful to avoid insecure direct object reference. |
|[```/v1/api/gateway-account/{gatewayAccountId}/products```](docs/api_specification.md#get-v1apigateway\-accountgatewayaccountidproducts)        | GET    |  Gets lists of products that belongs to a gateway account specified by gatewayAccountId  |
|[```/v1/api/products/{productId}```](docs/api_specification.md#delete-v1apiproductsproductid)        | DELETE    |  Deletes the product with the specified productId   |
|[```/v1/api/gateway-account/{gatewayAccountId}/products/{productId}```](docs/api_specification.md#delete-v1apigateway\-accountgatewayaccountidproductsproductexternaliddisable)        | DELETE    |  Deletes the product with the specified productId that belong to the gateway account specified by gatewayAccountId. Deletes the product only if it exists in the given gateway account. Useful to avoid insecure direct object reference. |
|[```/v1/api/products?serviceNamePath={serviceNamePath}&productNamePath={productNamePath}```](docs/api_specification.md#get-v1apiproducts?productsPath)        | GET    |  Get an existing Adhoc product. |
|[```/v1/api/payments```](docs/api_specification.md#post-v1apipayments)        | POST    | Creates a new payment                        |
|[```/v1/api/payments/{paymentId}```](docs/api_specification.md#get-v1apipaymentspaymentid) |  GET  |     Gets an existing payment    |
|[```/v1/api/products/{productId}/payments```](docs/api_specification.md#get-v1apiproductsproductidpayments) | GET | Gets a list of payments that belong to a specific product specified by productId |
|[```/v1/api/gateway-account/{gatewayAccountId}```](docs/api_specification.md#get-v1apigatewayaccountgatewayaccountid) | PATCH | Updates a specific field of a given gateway-account of products specified by gatewayAccountId |  
