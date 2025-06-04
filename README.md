# pay-products

Products integrating with GOV.UK Pay in Java(Dropwizard)

This microservice manages products (things that a Government Service would like to take payments for).

These products are integrated with GOV.UK Pay so that a user can make payments using a provided URL. 

## API Specification

The [API Specification](openapi/products_spec.yaml) provides more detail on the paths and operations including examples.

[View the API specification for pay-products in Swagger Editor](https://editor.swagger.io/?url=https://raw.githubusercontent.com/alphagov/pay-products/master/openapi/products_spec.yaml).

## Environment Variables

| Variable                                  | Description                                                                                                               |
|-------------------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| `ADMIN_PORT`                              | The port number to listen for Dropwizard admin requests on. Defaults to `8081`.                                           |
| `BIND_HOST`                               | The IP address for the application to bind to. Defaults to `127.0.0.1`.                                                   |
| `BASE_URL`                                | The base URL of the [products-ui](https://github.com/alphagov/pay-products-ui) microservice.                              |
| `DB_HOST`                                 | The hostname of the database server. Defaults to `products.db.pymnt.localdomain`                                          |
| `DB_PASSWORD`                             | The password for the `DB_USER` user.                                                                                      |
| `DB_SSL_OPTION`                           | To turn TLS on this value must be set as `ssl=true`. Otherwise must be empty.                                             |
| `DB_USER`                                 | The username to log into the database as.                                                                                 |
| `JAVA_HOME`                               | The location of the JRE. Set to `/opt/java/openjdk` in the `Dockerfile`.                                                  |
| `JAVA_OPTS`                               | Commandline arguments to pass to the java runtime. Optional.                                                              |
| `JPA_LOG_LEVEL`                           | The logging level to set for JPA. Defaults to `WARNING`.                                                                  |
| `JPA_SQL_LOG_LEVEL`                       | The logging level to set for JPA SQL logging. Defaults to `WARNING`.                                                      |
| `PORT`                                    | The port number to listen for requests on. Defaults to `8080`.                                                            |
| `PRODUCTSUI_CONFIRMATION_URL`             | The URL of the confirmation page endpoint in the [products-ui](https://github.com/alphagov/pay-products-ui) microservice. |
| `PRODUCTSUI_PAY_URL`                      | The URL of the `pay` endpoint in the [products-ui](https://github.com/alphagov/pay-products-ui) microservice.             |
| `PRODUCTS_FRIENDLY_BASE_URI`              | The URL of the products endpoint in the [products-ui](https://github.com/alphagov/pay-products-ui) microservice.          |
| `PUBLICAPI_URL`                           | The URL to the [publicapi](https://github.com/alphagov/pay-publicapi) microservice                                        |
| `EMAIL_ADDRESS_FOR_REPLACING_API_TOKENS`  | The email address used in a request to Public Auth application for generating an API token.                               |
| `RUN_APP`                                 | Set to `true` to run the application. Defaults to `true`.                                                                 |
| `RUN_MIGRATION`                           | Set to `true` to run a database migration. Defaults to `false`.                                                           |
| `SECURE_RETURN_URLS`                      | Set to `false` to allow non-HTTPS URLs for the `return_url` field of a product. Defaults to `true`.                       |
| `EXPUNGE_HISTORICAL_DATA_ENABLED`         | Set to `true` to enable deletion of payments. Defaults to `false`.                                                        |
| `EXPUNGE_DATA_OLDER_THAN_DAYS`            | Minimum age of transactions in days that need to be redacted/expunged. Defaults to 2555 (7 years).                        |
| `EXPUNGE_NO_OF_PAYMENTS_PER_TASK_RUN` | Number of payments to delete per task run.                                                                                |

## Vulnerability Disclosure

GOV.UK Pay aims to stay secure for everyone. If you are a security researcher and have discovered a security vulnerability in this code, we appreciate your help in disclosing it to us in a responsible manner. Please refer to our [vulnerability disclosure policy](https://www.gov.uk/help/report-vulnerability) and our [security.txt](https://vdp.cabinetoffice.gov.uk/.well-known/security.txt) file for details.
