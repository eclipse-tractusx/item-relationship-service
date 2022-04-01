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
  * [Prometheus](https://prometheus.io/docs/introduction/overview/), a server to collect and query metrics. Prometheus is available at http://localhost:9091/.

## Work with sample data

* Retrieve one sample preloaded BOM:

```bash
curl -X GET "http://localhost:8080/api/v0.1/vins/BMWOVCDI21L5DYEUU/partsTree?view=AS_BUILT"
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

## Smoke tests

[Smoke tests](integration-tests/src/test/java/net/catenax/irs/smoketest) are running against the application and the consumer connectors in the IRS Smoke tests pipeline.
To run the tests against locally running application provide `baseURI` (IRS API base URI, by default http://localhost:8080).
If you want to run it against connector you need to add the following VM options:
`-DbaseURI=<consumer-artifact-uri> -Dusername=<username-to-access-consumer> -Dpassword=<password>`

## System tests

[System tests](integration-tests/src/test/java/net/catenax/irs/systemtest) are running against multiple IRS deployments and reconstructing a parts tree from multiple partial trees.
To run the tests, download the file artifact `dataspace-deployments.json` from the latest IRS Deploy GitHub Actions run into the `dev/local` folder.

## Commit messages
The commit messages have to match a pattern in the form of:  
< type >(optional scope):[<Ticket_ID>] < description >

Example:  
chore(api):[TRI-123] some text

Detailed pattern can be found here: [commit-msg](dev/commit-msg)

###Installation
```shell
cp dev/commit-msg .git/hooks/commit-msg && chmod 500 .git/hooks/commit-msg
```

For further information please see https://github.com/hazcod/semantic-commit-hook

## Licenses
Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0)
