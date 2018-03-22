# pay-products

Products integrating with GOV.UK Pay in Java(Dropwizard)

This microservice manages products (things that a Government Service would like to take payments for).

These products are integrated with GOVUK.Pay so that a user can make payments using a provided URL. 

## Environment Variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `PRODUCTS_UI_URL` | - | base url of the products ui micro service |

## API Specification

The [API Specification](docs/api_specification.md) provides more detail on the paths and operations including examples.

The JSON naming convention follows Hypertext Application Language (HAL).
 
### API NAMESPACE

| Path                          | Supported Methods | Description                        |
| ----------------------------- | ----------------- | ---------------------------------- |
|[```/v1/api/products```](docs/api_specification.md#post-v1apiproducts)        | POST    |  Creates a new product definition            |
|[```/v1/api/products/{productId}```](docs/api_specification.md#get-v1apiproductsproductid)        | GET    |  Gets an existing product with the specified productId   |
|[```/v1/api/gateway-account/{gatewayAccountId}/products/{productId}```](docs/api_specification.md#get-v1apigateway\-accountgatewayaccountidproductsproductid)        | GET    |  Gets an existing product with the specified productId that belong to the gateway account specified by gatewayAccountId. Returns the product only if it exists in the given gateway account. Useful to avoid insecure direct object reference. |
|[```/v1/api/gateway-account/{gatewayAccountId}/products```](docs/api_specification.md#get-v1apigateway\-accountgatewayaccountidproducts)        | GET    |  Gets lists of products that belongs to a gateway account specified by gatewayAccountId  |
|[```/v1/api/products/{productId}```](docs/api_specification.md#delete-v1apiproductsproductid)        | DELETE    |  Deletes/Disables the product with the specified productId   |
|[```/v1/api/gateway-account/{gatewayAccountId}/products/{productId}```](docs/api_specification.md#delete-v1apigateway\-accountgatewayaccountidproductsproductexternaliddisable)        | DELETE    |  Deletes/Disables the product with the specified productId that belong to the gateway account specified by gatewayAccountId. Deletes/Disables the product only if it exists in the given gateway account. Useful to avoid insecure direct object reference. |
|[```/v1/api/products?serviceNamePath={serviceNamePath}&productNamePath={productNamePath}```](docs/api_specification.md#get-v1apiproducts?productsPath)        | GET    |  Get an existing Adhoc product. |
|[```/v1/api/payments```](docs/api_specification.md#post-v1apipayments)        | POST    | Creates a new payment                        |
|[```/v1/api/payments/{paymentId}```](docs/api_specification.md#get-v1apipaymentspaymentid) |  GET  |     Gets an existing payment    |
|[```/v1/api/products/{productId}/payments```](docs/api_specification.md#get-v1apiproductsproductidpayments) | GET | Gets a list of payments that belong to a specific product specified by productId |
|[```/v1/api/gateway-account/{gatewayAccountId}```](docs/api_specification.md#get-v1apigatewayaccountgatewayaccountid) | PATCH | Updates a specific field of a given gateway-account of products specified by gatewayAccountId |  
