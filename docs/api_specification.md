# API Specification

## POST /v1/api/products

This endpoint creates a new product in pay-products.

### Request example

```
POST /v1/api/products
Content-Type: application/json

{
    "gateway_account_id" :     "1234",
    "pay_api_token" :           "5meusgv5ufclsbvde78mdf35bfdhnhm1307euq94kcf0lcqcqrovbjovib",
    "name" :                    "A name for the product",
    "description" :             "Description of the product",
    "price" :                   1050,
    "type" :                    "DEMO",
    "service_name":             "Some awesome government service",
    "return_url" :              "https://some.valid.url/",
    "service_name_path" :       "some-awesome-government-service",
    "product_name_path" :       "name-for-product"
}
```

#### Request body description

| Field                    | required | Description                                                      | Supported Values     |
| ------------------------ |:--------:| ---------------------------------------------------------------- |----------------------|
| `gateway_account_id`     |    X     | gateway account id of the Gateway Account as identified by adminusers.  |   |
| `pay_api_token`          |    X     | valid api token for the gateway account of above service which this product takes payments for |  |
| `name`                   |    X     | Name of the product. This will be passed as the `name` when creating the charge | |
| `price`                  |          | Price for the product in pence. This will be passed as the  `amount` when creating charge. Mandatory for Non-ADHOC products    | |
| `description`            |          | Description of the product. This will be passed as the `description` when creating the charge | |
| `service_name`           |    X     | The name of the service associated that the product is to be associated with    |   |
| `return_url`             |          | (https only) where to redirect to upon completion of a payment. If not provided, `pay-products` will generate a default url to itself when creating a charge | |
| `service_name_path`      |          | Service Name Path part of Product Path. Required for Adhoc type only.  |   |
| `product_name_path`      |          | Product Name Path part of Product Path. Required for Adhoc type only.   |   |

### Response example

```
201 OK
Content-Type: application/json
{
    "external_id": "874h5c87834659q345698495",
    "gateway_account_id" : "1234",
    "description":         "Description of the product",
    "price":               1050,
    "type":                "DEMO,
    "service_name":             "Some awesome government service",
    "return_url" :         "https://some.valid.url/",
    "_links": [
    {
        "href": "https://govukpay-products.cloudapps.digital/v1/api/products/874h5c87834659q345698495",
        "rel" : "self",
        "method" : "GET"
    },
    {
         "href": "https://govukpay-products-ui.cloudapps.digital/pay/874h5c87834659q345698495",
         "rel" : "pay",
         "method" : "GET"
    },
    {
        "href": "https://govukpay-products-ui.cloudapps.digital/products?serviceNamePath=some-awesome-government-service&productNamePath=pay-for-my-product",
        "rel" : "friendly",
        "method" : "GET"
    }]
}
```

#### Response field description

| Field                    | always present | Description                                   |
| ------------------------ |:--------------:| --------------------------------------------- |
| `external_id`            | X              | external id of the new product                |
| `gateway_account_id `    | X              | gateway account id of the Gateway    as identified by adminusers.  |
| `description`            | X              | Description of the product |
| `price`                  | X              | Price for the product in pence      |
| `service_name`           | X              | The name of the service with which the product is associated  |
| `return_url`             |                | return url provided. _(not be available if it was not provided)_   |
| `_links.self`            | X              | self GET link to the product. |
| `_links.pay`             | X              | The link in `pay-products-ui` where a charge for this product will be generated and redirected to GOV.UK Pay |
| `_links.friendly`        |                | The friendly link to be used for Adhoc payments |

## PUT /v1/api/gateway-account/{gatewayAccountId}/products/{productId}


This endpoint updates an existing product matching the specified `productId` 
and belonging to the gateway account specified by `gatewayAccountId`.
Note that only the following 3 fields are updatable for now, all other 
fields passed in simply being ignored: `name`, `description` and `price`.

Returns the product only if it exists and the update is successful. 

### Request example

```
PUT /v1/api/gateway-account/1234/products/874h5c87834659q345698495
Content-Type: application/json

{
    "gateway_account_id" :     "1234",
    "pay_api_token" :           "5meusgv5ufclsbvde78mdf35bfdhnhm1307euq94kcf0lcqcqrovbjovib",
    "name" :                    "A name for the product",
    "description" :             "Description of the product",
    "price" :                   1050,
    "type" :                    "DEMO",
    "service_name":             "Some awesome government service",
    "return_url" :              "https://some.valid.url/",
    "service_name_path" :       "some-awesome-government-service",
    "product_name_path" :       "name-for-product"
}
```

#### Request body description

| Field                    | required | Description                                                      | Supported Values     |
| ------------------------ |:--------:| ---------------------------------------------------------------- |----------------------|
| `gateway_account_id`     |    X     | gateway account id of the Gateway Account as identified by adminusers.  |   |
| `pay_api_token`          |    X     | valid api token for the gateway account of above service which this product takes payments for |  |
| `name`                   |    X     | Name of the product. This will be passed as the `name` when creating the charge | |
| `price`                  |          | Price for the product in pence. This will be passed as the  `amount` when creating charge. Mandatory for Non-ADHOC products    | |
| `description`            |          | Description of the product. This will be passed as the `description` when creating the charge | |
| `service_name`           |    X     | The name of the service associated that the product is to be associated with    |   |
| `return_url`             |          | (https only) where to redirect to upon completion of a payment. If not provided, `pay-products` will generate a default url to itself when creating a charge | |
| `service_name_path`      |          | Service Name Path part of Product Path. Required for Adhoc type only.  |   |
| `product_name_path`      |          | Product Name Path part of Product Path. Required for Adhoc type only.   |   |

### Response example

```
201 OK
Content-Type: application/json
{
    "external_id": "874h5c87834659q345698495",
    "gateway_account_id" : "1234",
    "description":         "Description of the product",
    "price":               1050,
    "type":                "DEMO,
    "service_name":             "Some awesome government service",
    "return_url" :         "https://some.valid.url/",
    "_links": [
    {
        "href": "https://govukpay-products.cloudapps.digital/v1/api/products/874h5c87834659q345698495",
        "rel" : "self",
        "method" : "GET"
    },
    {
         "href": "https://govukpay-products-ui.cloudapps.digital/pay/874h5c87834659q345698495",
         "rel" : "pay",
         "method" : "GET"
    },
    {
        "href": "https://govukpay-products-ui.cloudapps.digital/products?serviceNamePath=some-awesome-government-service&productNamePath=pay-for-my-product",
        "rel" : "friendly",
        "method" : "GET"
    }]
}
```

#### Response field description

| Field                    | always present | Description                                   |
| ------------------------ |:--------------:| --------------------------------------------- |
| `external_id`            | X              | external id of the new product                |
| `gateway_account_id `    | X              | gateway account id of the Gateway    as identified by adminusers.  |
| `description`            | X              | Description of the product |
| `price`                  | X              | Price for the product in pence      |
| `service_name`           | X              | The name of the service with which the product is associated  |
| `return_url`             |                | return url provided. _(not be available if it was not provided)_   |
| `_links.self`            | X              | self GET link to the product. |
| `_links.pay`             | X              | The link in `pay-products-ui` where a charge for this product will be generated and redirected to GOV.UK Pay |
| `_links.friendly`        |                | The friendly link to be used for Adhoc payments |

## GET /v1/api/products/{productId}

This endpoint finds a product with the specified productId

```
GET /v1/api/products/874h5c87834659q345698495
```  

### Response example

```
200 OK
Content-Type: application/json
{
    "external_id": "874h5c87834659q345698495",
    "description":         "Description of the product",
    "price":               1050,
    "return_url" :         "https://some.valid.url/"
    "service_name":        "Some awesome government service",
    "_links": [
    {
        "href": "https://govukpay-products.cloudapps.digital/v1/api/products/874h5c87834659q345698495",
        "rel" : "self",
        "method" : "GET"
    },
    {
         "href": "https://govukpay-products-ui.cloudapps.digital/pay/874h5c87834659q345698495",
         "rel" : "pay",
         "method" : "GET"
    },
    {
         "href": "https://govukpay-products-ui.cloudapps.digital/products?serviceNamePath=some-awesome-government-service&productNamePath=pay-for-my-product",
         "rel" : "friendly",
         "method" : "GET"
     }]
}
```
#### Response field description 
same as above(docs/api_specification.md#post-v1apiproducts)

## GET /v1/api/gateway-account/{gatewayAccountId}/products/{productId}

This endpoint finds a product with the specified productId that belongs to the gateway account specified by gatewayAccountId

Returns the product only if it exists in the given gateway account. Useful to avoid insecure direct object reference.

```
GET /v1/api/gateway-account/1234/products/874h5c87834659q345698495
```  

### Response example

```
200 OK
Content-Type: application/json
{
    "external_id": "874h5c87834659q345698495",
    "description":         "Description of the product",
    "price":               1050,
    "return_url" :         "https://some.valid.url/"
    "service_name":        "Some awesome government service",
    "_links": [
    {
        "href": "https://govukpay-products.cloudapps.digital/v1/api/products/874h5c87834659q345698495",
        "rel" : "self",
        "method" : "GET"
    },
    {
         "href": "https://govukpay-products-ui.cloudapps.digital/pay/874h5c87834659q345698495",
         "rel" : "pay",
         "method" : "POST"
    },
    {
         "href": "https://govukpay-products-ui.cloudapps.digital/products?serviceNamePath=some-awesome-government-service&productNamePath=pay-for-my-product",
         "rel" : "friendly",
         "method" : "GET"
    }]
}
```
#### Response field description 
same as above(docs/api_specification.md#post-v1apiproducts)

## GET /v1/api/products?serviceNamePath={serviceNamePath}&productNamePath={productNamePath}

This endpoint finds a product with the product path (friendly url)

```
GET /v1/api/products?serviceNamePath=my-service&productNamePath=my-product
```  

### Response example

```
200 OK
Content-Type: application/json
{
    "external_id": "874h5c87834659q345698495",
    "description":         "Description of the product",
    "price":               1050,
    "return_url" :         "https://some.valid.url/"
    "service_name":        "Some awesome government service",
    "_links": [
    {
        "href": "https://govukpay-products.cloudapps.digital/v1/api/products/874h5c87834659q345698495",
        "rel" : "self",
        "method" : "GET"
    },
    {
         "href": "https://govukpay-products-ui.cloudapps.digital/pay/874h5c87834659q345698495",
         "rel" : "pay",
         "method" : "POST"
    },
    {
         "href": "https://govukpay-products-ui.cloudapps.digital/products?serviceNamePath=some-awesome-government-service&productNamePath=pay-for-my-product",
         "rel" : "friendly",
         "method" : "GET"
    }]
}
```
#### Response field description 
same as above(docs/api_specification.md#post-v1apiproducts) except that `_links.friendly` is always present

## GET /v1/api/gateway-account/{gatewayAccountId}/products

This endpoint retrieves list of products that belongs to the specified gatewayAccountId.

```
GET /v1/api/gateway-account/1234/products
```  

### Response example

```
200 OK
Content-Type: application/json
    [{
        "external_id": "874h5c87834659q345698495",
        "description":         "Description 1",
        "price":               9999,
        "return_url" :         "https://some.valid.url/"
        "service_name":        "Some awesome government service",
        "_links": [
        {
            "href": "https://govukpay-products.cloudapps.digital/v1/api/products/874h5c87834659q345698495",
            "rel" : "self",
            "method" : "GET"
        },
        {
             "href": "https://govukpay-products-ui.cloudapps.digital/pay/874h5c87834659q345698495",
             "rel" : "pay",
             "method" : "POST"
        }]
    },
    {
        "external_id": "h6347634cwb67wii7b6ciueroytw",
        "description":         "Description 2",
        "price":               1050,
        "return_url" :         "https://some.valid.url/"
        "service_name":             "Some awesome government service",
        "_links": [
        {
            "href": "https://govukpay-products.cloudapps.digital/v1/api/products/h6347634cwb67wii7b6ciueroytw",
            "rel" : "self",
            "method" : "GET"
        },
        {
             "href": "https://govukpay-products-ui.cloudapps.digital/pay/h6347634cwb67wii7b6ciueroytw",
             "rel" : "pay",
             "method" : "POST"
        }]
    }]
```
#### Response field description 
same as above(docs/api_specification.md#post-v1apiproducts)


## PATCH /v1/api/products/{productExternalId}/disable

This endpoint disables a product with the specified external product id

Deletes/Disables the product only if it exists in the given gateway account. Useful to avoid insecure direct object reference.

```
PATCH /v1/api/products/uier837y735n837475y3847534/disable
```  

### Response example

```
204 OK
``` 


## GET /v1/api/payments/{paymentId}

This endpoint finds a payment with the specified external payment id

```
GET /v1/api/payments/h6347634cwb67wii7b6ciueroytw
```  

### Response example

```
200 OK
Content-Type: application/json
{
        "external_id": "h6347634cwb67wii7b6ciueroytw",
        "next_url": "https://some.valid.url/paid",
        "product_external_id": "uier837y735n837475y3847534",
        "status": "CREATED",
        "amount" : 1050,
        "_links": [
            {
                "rel": "self",
                "method": "GET",
                "href": "https://govukpay-products.cloudapps.digital/v1/api/payments/h6347634cwb67wii7b6ciueroytw"
            },
            {
                "rel": "next",
                "method": "GET",
                "href": "https://some.valid.url/paid"
            } 
        ]
    }
```
#### Response field description 
| Field                    | always present | Description                                   |
| ------------------------ |:--------------:| --------------------------------------------- |
| `external_id`            | X              | external id of the payment                |
| `product_external_id `   | X              | product external id which owns this payment  |   |
| `status`                 | X              | Status of the payment      |
| `amount`                 | X              | amount of the payment in pence. |
| `_links.self`            | X              | self GET link to the payment. |
| `_links.next`            | X              | next GET link |

## PATCH /v1/api/gateway-account/{gatewayAccountId}/products/{productExternalId}/disable

This endpoint disables a product with the specified productExternalId that belongs to the gateway account specified by gatewayAccountId

```
PATCH /v1/api/gateway-account/1234/products/uier837y735n837475y3847534/disable
```  

### Response example

```
204 OK
``` 


## GET /v1/api/payments/{paymentId}

This endpoint finds a payment with the specified external payment id

```
GET /v1/api/payments/h6347634cwb67wii7b6ciueroytw
```  

### Response example

```
200 OK
Content-Type: application/json
{
        "external_id": "h6347634cwb67wii7b6ciueroytw",
        "next_url": "https://some.valid.url/paid",
        "product_external_id": "uier837y735n837475y3847534",
        "status": "CREATED",
        "amount" : 1050,
        "_links": [
            {
                "rel": "self",
                "method": "GET",
                "href": "https://govukpay-products.cloudapps.digital/v1/api/payments/h6347634cwb67wii7b6ciueroytw"
            },
            {
                "rel": "next",
                "method": "GET",
                "href": "https://some.valid.url/paid"
            } 
        ]
    }
```
#### Response field description 
| Field                    | always present | Description                                   |
| ------------------------ |:--------------:| --------------------------------------------- |
| `external_id`            | X              | external id of the payment                |
| `product_external_id `   | X              | product external id which owns this payment  |   |
| `status`                 | X              | Status of the payment      |
| `amount`                 | X              | amount of the payment in pence. |
| `_links.self`            | X              | self GET link to the payment. |
| `_links.next`            | X              | next GET link |

## GET /v1/api/products/{productId}/payments

This endpoint retrieves list of payments that belongs to the specified product external id.

```
GET /v1/api/products/uier837y735n837475y3847534/payments
```  

### Response example

```
200 OK
Content-Type: application/json
    [
        {
            "external_id": "h6347634cwb67wii7b6ciueroytw",
            "next_url": "https://some.valid.url/paid",
            "product_external_id": "uier837y735n837475y3847534",
            "status": "CREATED",
            "amount" : 1050,
            "_links": [
                {
                    "rel": "self",
                    "method": "GET",
                    "href": "https://govukpay-products.cloudapps.digital/v1/api/payments/h6347634cwb67wii7b6ciueroytw"
                },
                {
                    "rel": "next",
                    "method": "GET",
                    "href": "https://some.valid.url/paid"
                }
            ]
        },
        {
            "external_id": "b3d007390f544a819eafe2b677652a40",
            "next_url": "www.example.org/paid",
            "product_external_id": "uier837y735n837475y3847534",
            "status": "CREATED",
            "amount" : 1050,
            "_links": [
                {
                    "rel": "self",
                    "method": "GET",
                    "href": "https://govukpay-products.cloudapps.digital/v1/api/payments/b3d007390f544a819eafe2b677652a40"
                },
                {
                    "rel": "next",
                    "method": "GET",
                    "href": "https://some.valid.url/paid"
                }       
            ]
        }
    ]
```
#### Response field description 
same as above(docs/api_specification.md#post-v1apipayments)

## POST /v1/api/products/{productId}/payments

This endpoint creates a new payment for a given product in pay-products.

### Request example

```
POST /v1/api/products/uier837y735n837475y3847534/payments
Content-Type: application/json
{
    "price" : 9090,
}
```

### Response example

```
200 OK
Content-Type: application/json
{
        "external_id": "h6347634cwb67wii7b6ciueroytw",
        "product_external_id": "uier837y735n837475y3847534",
        "status": "CREATED",
        "amount" : 9090,
        "_links": [
            {
                "rel": "self",
                "method": "GET",
                "href": "https://govukpay-products.cloudapps.digital/v1/api/payments/h6347634cwb67wii7b6ciueroytw"
            },
             {
                            "rel": "next",
                            "method": "GET",
                            "href": "https://some.valid.url/paid"
             }     
        ]
    }
```
#### Request body description

| Field                    | required | Description                                                      | Supported Values     |
| ------------------------ |:--------:| ---------------------------------------------------------------- |----------------------|
| `price`                  |          | Price override for the payment amount. If not present this will defaults to price of product.| |


#### Response field description 
| Field                    | always present | Description                                   |
| ------------------------ |:--------------:| --------------------------------------------- |
| `external_id`            | X              | external id of the payment                |
| `product_external_id `   | X              | product external id which owns this payment  |   
| `status`                 | X              | Status of the payment 
| `amount`                 | X              | amount of the payment in pence. |
| `_links.self`            | X              | self GET link to the payment |
| `_links.next`            | X              | next GET link |

## PATCH /v1/api/gateway-account/{gatewayAccountId}

This endpoint batch updates Service Names of Products with a given gatewayAccountId

### Request example

```
PATCH /v1/api/gateway-account/1234
Content-Type: application/json

{
    "op"    :     "replace",
    "path"  :     "service_name",
    "value" :     "A New Service Name"
}

```  
### Response example

```
200 OK -> if any matching gateway_account_id was found and path's value was replaced

202 ACCEPTED -> if no matching gateway_account_id was found

```

#### Request body description

| Field                    | required | Description                                                      | Supported Values     |
| ------------------------ |:--------:| ---------------------------------------------------------------- |----------------------|
| `op`                     | X        | the required operation, ie `replace`, `delete`, etc.             |                      |
| `path`                   | X        | the affected column name, ie `service_name`                      |                      |
| `value`                  | X        | the new value                                                    |                      |

