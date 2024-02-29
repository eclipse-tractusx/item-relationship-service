
[[Back to main IRS README](README.md)]

## Tests

### Unit Tests

Unit tests are executed automatically during the GitHub workflow
[IRS build](.github/workflows/irs-build.yml).

**Test Reports:**
Currently, no test reports are published.
Failing tests result in a failing build and are reported there.

**Test Coverage:**
The test coverage can be viewed in detail on [sonarcloud.io](https://sonarcloud.io/summary/new_code?id=eclipse-tractusx_item-relationship-service).
See the badge at the top of this README for the current test coverage percentage.
For more information on test coverage see the architecture documentation in the section
[Build, Test, Deploy](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/full.html#_build_test_deploy).

**Testing Frameworks and Libaries:**
The IRS project uses [JUnit](https://junit.org/) with [AssertJ](https://github.com/assertj/assertj)
and [Mockito](https://site.mockito.org/) for unit testing and the testing capabilities of the
[Spring Boot Framework](https://spring.io/projects/spring-boot) are utilized.

Furthermore, the following libraries are utilized:
- [Awaitility](http://www.awaitility.org/) for expressing expectations of asynchronous code
  in a concise and easy to read manner.
- [DataFaker](https://www.datafaker.net/) for creating fake data.


### Integration Tests

Besides the Spring Boot features testing features the following frameworks and tools are used for integation testing:
- [Testcontainers](https://java.testcontainers.org/) for bootstrapping integration tests with real services wrapped in Docker containers
- [Wiremock](https://wiremock.org/) for building mock APIs in order to simulate dependencies

The Wiremock tests are intended to cover the IRS flow and communication
without the need of a running environment that includes all dependencies.
Wiremock Tests and their corresponding utilities are marked by the suffix `WiremockTest` respectively `WiremockSupport`.


### Smoke Tests

See `irs-integration-tests/src/test/java/org/eclipse/tractusx/irs/smoketest/ItemGraphSmokeTest.java`.
The smoke test is executed via the GitHub workflow  [IRS integration tests](.github/workflows/int-test-automation.yml).


### Regression Tests

We use both [Tavern](https://tavern.readthedocs.io) and [Cucumber](https://cucumber.io/) for regression testing.


#### Tavern Tests

For more information about [Tavern](https://tavern.readthedocs.io) API tests for the IRS see
[Tavern API Tests README](local/testing/api-tests/README.md) under `local/testing/api-tests`, please.


#### Cucumber Tests

There are also some [Cucumber](https://cucumber.io/) that verify the response bodies in more detail.
See the module `irs-cucumber-tests` and the [Cucumber Tests README](irs-cucumber-tests/README.md) for more information.

Please note that there will be changes concerning test execution due to the
[transfer to Post-consortia-working-model (PCWM)](docs/concept/%23223-Transfer-to-PCWM/%23223-Transfer-to-PCWM.md).


### Load Tests

We use [Gatling](https://gatling.io/) for load testing.
IRS load tests can be triggered manually via GitHub workflow [IRS Load Test](.github/workflows/irs-load-test.yaml).
Please see [IRS Load Tests README](irs-load-tests/README.md) for more information.

### Stress Tests

Currently, there aren't any stress tests.
