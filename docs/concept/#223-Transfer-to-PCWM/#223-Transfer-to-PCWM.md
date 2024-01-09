# Transfer to Post-consortia-working-model (PCWM)

## How to migrate the Confluence pages to GitHub (Pages, Wiki, MD, issues)

Only a small portion of our Confluence documentation has to be migrated to GitHub, so copy-pasting the content manually is sufficient and the fastest solution.

Confluence pages which shall be moved to GitHub have to be marked as such. Everything else will not be moved.

## Test management and Test Reporting (Cucumber)

Cucumber Tests are were stored in and reported to Jira, using the Jira-Xray plugin. 

Feature files can be exported and directly stored in GitHub.

Test results have to be displayed directly in GitHub workflow / Pull Request. This can be done in two ways:

- using the free [Cucumber Report Service](https://cucumber.io/docs/cucumber/reporting/?lang=java#cucumber-reports-service), where the results will be published to a cloud hosted service
- Using the generated test report and upload it using a GitHub Action https://github.com/marketplace/actions/publish-test-results

## Replacing the CatenaX-NG environment

The CatenaX-NG ArgoCD environments were used to execute Integration- and E2E-Tests. After the end of the Consortia phase, they are no longer available and have to be replaced by an open solution.

The proposed solution is a helm chart with all required dependencies, including EDC Consumer, EDC Provider, MIW,
Semantic Hub, Discovery Service, Digital Twin Registry, OAuth Provider and IRS. Implementation of this chart is planned
in https://github.com/eclipse-tractusx/item-relationship-service/issues/312

The chart will then be used in a GitHub workflow to test the E2E Workflow. The already existing Cucumber Tests can be
used.

Furthermore, integration tests using mocks of the third-party systems should be extended so testing can be done as much as possible on the local machine or in GitHub workflows, without setting up the entire helm deployment. This is planned in https://github.com/eclipse-tractusx/item-relationship-service/issues/344

## Change of development workflow

PRs are to be opened directly to eclipse-tractusx/item-relationship-service. Eclipse committers can create branches directly in this project. External contributors can contribute by forking the eclipse-tractusx/item-relationship-service project and create pull requests from their fork.

Non-committers of the core IRS team can still use the catenax-ng/tx-item-relationship-service fork but pull requests still have to be opened directly to eclipse-tractusx/item-relationship-service.

Branch protection of catenax-ng/tx-item-relationship-service:main will be removed so synchronization with the upstream project can be done by one click.

