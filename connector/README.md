# Provider & Consumer Connector

## Git token

Create a [personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token) in GitHub, limited to **read:packages** scope.

Configure the following GitHub *repository secrets*:

- PRS_EDC_PKG_USERNAME (your_github_username)
- PRS_EDC_PKG_PASSWORD (your_github_pat_token)

## Local build

Copy `settings.xml` into your `~/.m2/` folder (or merge it with a file already there), and replace the environment variable references with the following:

```xml
<username>your_github_username</username>
<password>your_github_pat_token</password>
```

## Docker build

See `run-integration-test.sh` file.

## Running tests

Download certificate for the PRS Connector Consumer to communicate with its Key Vault to local filesystem. In the `cd/terraform-identities` directory run:

```sh
terraform init
az keyvault secret download --file ../../dev/local/cert.pfx --vault-name "$(terraform output -raw vault_name)" --name "$(terraform output -raw prs_connector_consumer_cert_name)" --encoding base64
```

Set environment variables for GitHub access:

```bash
export PRS_EDC_PKG_USERNAME=your_github_username
export PRS_EDC_PKG_PASSWORD=your_github_pat_token
```
Make sure you have configured the env properties. It can be done by creating .env file and copying content of the .env.example file into it.
Run integration tests:

```bash
./run-integration-test.sh
```

### Debugging connectors from IDE

Download certificate for the PRS Connector Consumer as explained in the previous section.

Create empty file for the Provider Connector filesystem vault (FsVault):

```bash
touch ../dev/local/dataspaceconnector-vault.properties
```

Import run configurations in the `dev/ide` folder into your IDE and use these to debug the consumer and provider connectors. As default the provider connector will try to use a local PRS running at `localhost:8080`, make sure to previously start the PRS api at that port as well.

Send a request to the local consumer connector by issuing:
```bash
curl -f -X POST http://localhost:9191/api/v0.1/retrievePartsTree -H "Content-type:application/json" -d '{"byObjectIdRequest": {"oneIDManufacturer": "BMW MUC", "objectIDManufacturer": "YS3DD78N4X7055320", "view": "AS_BUILT", "aspect": "MATERIAL", "depth": 2}}'
```

## Prometheus endpoint

- Download latest jar from https://github.com/prometheus/jmx_exporter/releases

- Config is available at location cd/jmx_prometheus_config.yml

- Attach jmx prometheus jar as a java agent to running process.

```bash
-javaagent:./jmx_prometheus_javaagent-<version>.jar=<port>:cd/jmx_prometheus_config.yml
```

- Metric endpoint will be available on http://localhost:<port>/metrics
