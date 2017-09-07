# pay-products

Products integrating with GOV.UK Pay in Java(Dropwizard)

This microservice manages products (things that a Government Service would like to take payments for).

These products are integrated with GOVUK.Pay so that a user can make payments using a provided URL. 

## Environment Variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `PRODUCTS_UI_URL` | - | base url of the products ui micro service |
| `API_KEY`     | - | API Key for request authentication |
| `API_KEY2`    | - | Alternate API Key for request authentication |

## API Specification

The [API Specification](docs/api_specification.md) provides more detail on the paths and operations including examples.
 
### API NAMESPACE

| Path                          | Supported Methods | Description                        |
| ----------------------------- | ----------------- | ---------------------------------- |
|[```/v1/api/products```](docs/api_specification.md#post-v1apiproducts)        | POST    |  Creates a new product definition            |
|[```/v1/api/products/{productId}```](docs/api_specification.md#get-v1apiproductsproductid)        | GET    |  Gets an existing product with the specified Id   |
|[```/v1/api/products?externalServiceId={serviceId}```](docs/api_specification.md#get-v1apiproductsexternalserviceidserviceid)        | GET    |  Gets lists of products that belongs to a service   |
|[```/v1/api/products/{productId}```](docs/api_specification.md#delete-v1apiproductsproductid)        | DELETE    |  Deletes/Disables the product with the specified Id   |
|[```/v1/api/products/{productId}/items```](docs/api_specification.md#post-v1apiproductsproductiditems)        | POST    |  Creates a new instance of a product that will take a payment  |
   
  
