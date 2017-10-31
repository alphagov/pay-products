# API Specification

## POST /v1/api/products

This endpoint creates a new product in pay-products.

### Request example

```
POST /v1/api/products
Authorization: Bearer API_TOKEN
Content-Type: application/json

{
    "gateway_account_id" :     "1234",
    "pay_api_token" :           "5meusgv5ufclsbvde78mdf35bfdhnhm1307euq94kcf0lcqcqrovbjovib",
    "name" :                    "A name for the product",
    "description" :             "Description of the product",
    "price" :                   1050,
    "return_url" :              "https://some.valid.url/"   
}
```

#### Request body description

| Field                    | required | Description                                                      | Supported Values     |
| ------------------------ |:--------:| ---------------------------------------------------------------- |----------------------|
| `gateway_account_id`     |    X     | gateway account id of the Gateway Account as identified by adminusers.  |   |
| `pay_api_token`          |    X     | valid api token for the gateway account of above service which this product takes payments for |  |
| `name`                   |    X     | Name of the product. This will be passed as the `name` when creating the charge | |
| `price`                  |    X     | Price for the product in pence. This will be passed as the  `amount` when creating charge    | |
| `description`            |          | Description of the product. This will be passed as the `description` when creating the charge | |
| `return_url`             |          | (https only) where to redirect to upon completion of a payment. If not provided, `pay-products` will generate a default url to itself when creating a charge | |

### Response example

```
201 OK
Content-Type: application/json
{
    "external_id": "874h5c87834659q345698495",
    "gateway_account_id" : "1234",
    "description":         "Description of the product",
    "price":               1050,
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
         "method" : "POST"
    }]
}
```

#### Response field description

| Field                    | always present | Description                                   |
| ------------------------ |:--------------:| --------------------------------------------- |
| `external_id`            | X              | external id of the new product                |
| `gateway_account_id `    | X              | gateway account id of the Gateway    as identified by adminusers.  |   |
| `description`            | X              | Description of the product |
| `price`                  | X              | Price for the product in pence      |
| `return_url`             |                | return url provided. _(not be available if it was not provided)_   |
| `_links.self`            | X              | self GET link to the product. |
| `_links.pay`             | X              | The link in `pay-products-ui` where a charge for this product will be generated and redirected to GOV.UK Pay |


## GET /v1/api/products/{productId}

This endpoint finds a product with the specified external product id

```
GET /v1/api/products/874h5c87834659q345698495
Authorization: Bearer API_TOKEN
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
}
```
#### Response field description 
same as above(docs/api_specification.md#post-v1apiproducts)


## GET /v1/api/products?gatewayAccountId={gatewayAccountId}

This endpoint retrieves list of products that belongs to the specified gateway account id.

```
GET /v1/api/products?gatewayAccountId=1234
Authorization: Bearer API_TOKEN
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

```
PATCH /v1/api/products/uier837y735n837475y3847534/disable
Authorization: Bearer API_TOKEN
```  

### Response example

```
204 OK
``` 


## GET /v1/api/payments/{paymentId}

This endpoint finds a payment with the specified external payment id

```
GET /v1/api/payments/h6347634cwb67wii7b6ciueroytw
Authorization: Bearer API_TOKEN
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
        "_links": [
            {
                "rel": "self",
                "method": "GET",
                "href": "https://govukpay-products.cloudapps.digital/v1/api/products/h6347634cwb67wii7b6ciueroytw"
            }
        ]
    }
```
#### Response field description 
| Field                    | always present | Description                                   |
| ------------------------ |:--------------:| --------------------------------------------- |
| `external_id`            | X              | external id of the payment                |
| `next_url`               | X              | Next URL provided|
| `product_external_id `   | X              | product external id which owns this payment  |   |
| `status`                 | X              | Status of the payment      |
| `_links.self`            | X              | self GET link to the payment. |

## GET /v1/api/products/{productId}/payments

This endpoint retrieves list of payments that belongs to the specified product external id.

```
GET /v1/api/products/uier837y735n837475y3847534/payments
Authorization: Bearer API_TOKEN
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
            "_links": [
                {
                    "rel": "self",
                    "method": "GET",
                    "href": "https://govukpay-products.cloudapps.digital/v1/api/products/h6347634cwb67wii7b6ciueroytw"
                }
            ]
        },
        {
            "external_id": "b3d007390f544a819eafe2b677652a40",
            "next_url": "www.example.org/paid",
            "product_external_id": "uier837y735n837475y3847534",
            "status": "CREATED",
            "_links": [
                {
                    "rel": "self",
                    "method": "GET",
                    "href": "https://govukpay-products.cloudapps.digital/v1/api/products/b3d007390f544a819eafe2b677652a40"
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
Authorization: Bearer API_TOKEN
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
        "_links": [
            {
                "rel": "self",
                "method": "GET",
                "href": "https://govukpay-products.cloudapps.digital/v1/api/products/h6347634cwb67wii7b6ciueroytw"
            }
        ]
    }
```
#### Response field description 
| Field                    | always present | Description                                   |
| ------------------------ |:--------------:| --------------------------------------------- |
| `external_id`            | X              | external id of the payment                |
| `next_url`               | X              | Next URL provided|
| `product_external_id `   | X              | product external id which owns this payment  |   |
| `status`                 | X              | Status of the payment      |
| `_links.self`            | X              | self GET link to the payment. |
