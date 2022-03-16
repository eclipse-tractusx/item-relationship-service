# Item Relationship Service

## How to run

The two following subsections provide instructions for running either only the infrastructure on docker-compose and the application in the IDE, or for running the full stack (including the application) in docker-compose.

### Docker-compose + IDE

* Start the necessary infrastructure by running `docker-compose up`

* Start the application from your favorite IDE

### Docker-compose full stack

* (Optional) Copy the file `.env.example` to `.env` and provide your Application Insights connection string.

* Run `docker-compose --profile irs up`

### Docker-compose debug profile

* (Optional) Copy the file `.env.example` to `.env` and provide your Application Insights connection string.
* Run `docker-compose --profile debug up`
* This will start additional containers:
  * [`kafkacat`](https://docs.confluent.io/platform/current/app-development/kafkacat-usage.html), a generic kafka command-line client, used here to output messages to the Docker-compose output stream as they are sent to the broker
  * [`kafka-ui`](https://github.com/provectus/kafka-ui), a UI interface to manage local kafka cluster, which can be used for similar purposes through a Web UI. `kafka-ui` is available at http://localhost:9090/.
  * [Prometheus](https://prometheus.io/docs/introduction/overview/), a server to collect and query metrics. Prometheus is available at http://localhost:9091/.

## Work with sample data

* Retrieve one sample preloaded BOM:

```bash
curl -X GET "http://localhost:8080/api/v0.1/vins/BMWOVCDI21L5DYEUU/partsTree?view=AS_BUILT"
```

## Inspect data

### Get persisted data

```bash
docker exec -it db bash -c 'psql -U $POSTGRES_USER postgres'
```

```sql
select * from part_relationship;
```

## Swagger UI

### IRS API

- Swagger UI: http://localhost:8080/api/swagger-ui
- API docs: http://localhost:8080/api/api-docs
- API docs in yaml:  http://localhost:8080/api/api-docs.yaml

## Terraform

See [Terraform deployment](terraform).

## Deploy to DEV

The new version of the application is deployed to DEV on merge to main through the `IRS Deploy` workflow.
The workflow builds a new image, pushes it to ACR and deploys it to Kubernetes. If you make changes to Terraform, these changes will be applied as the workflow runs `terraform apply`.
If you want to make sure the IRS deployment will work well with your changes, you can run the `IRS Deploy` workflow manually on your branch. Note that other PRs merged to main will cause Terraform to potentially roll back those changes.

## Deploy to INT

A deployment to the INT environment can be triggered manually with the `IRS Deploy` workflow as well by overriding the default target environment parameter to "int". INT environment deployments should be coordinated with the consumers of the IRS systems.

## Load Test Data

Test data can be loaded using `IRS Load Test Data` workflow. This workflow is triggered manually. It checks out the json files stored in
[test-data folder](./coreservices/partsrelationshipservice/cd/test-data), converts the data into sql queries and inserts the data into IRS database.
Before inserting all records with oneIds from json files are deleted from the database.

## Smoke tests

[Smoke tests](integration-tests/src/test/java/net/catenax/irs/smoketest) are running against the application and the consumer connectors in the IRS Smoke tests pipeline.
To run the tests against locally running application provide `baseURI` (IRS API base URI, by default http://localhost:8080) `brokerProxyBaseURI` (Broker Proxy API base URI, by default: http://localhost:8081). If only `baseURI` is provided, `brokerProxyBaseURI` has the same value as `baseURI`.
If you want to run it against connector you need to add the following VM options:
`-DbaseURI=<consumer-artifact-uri> -DbrokerProxyBaseURI=<broker-proxy-uri> -Dusername=<username-to-access-consumer> -Dpassword=<password>`

## System tests

[System tests](integration-tests/src/test/java/net/catenax/irs/systemtest) are running against multiple IRS deployments and reconstructing a parts tree from multiple partial trees.
To run the tests, download the file artifact `dataspace-deployments.json` from the latest IRS Deploy GitHub Actions run into the `dev/local` folder.

## Licenses
Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
