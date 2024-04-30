# Tavern API Tests

This folder contains [Tavern](https://tavern.readthedocs.io) API tests for the IRS.

## Usage 

1. Install python
2. Install pytest: ```py -m pip install pytest```
3. Install tavern: ```py -m pip install tavern```
4. Create variable.env in directory api-tests
5. Copy content to variable.env
```
# this is temporary file for local development to setup environments variable such a secrets and URLs
# to run it just execute in console:
# source variable.env
# before running tests
export IRS_HOST="http://localhost:8080"
export IRS_ESS_HOST="https://localhost:8080"
export GLOBAL_ASSET_ID_AS_PLANNED=urn:uuid:0733946c-59c6-41ae-9570-cb43a6e4c79e
export BPN_AS_PLANNED=BPNL00000003AYRE
export GLOBAL_ASSET_ID_AS_BUILT=urn:uuid:6d505432-8b31-4966-9514-4b753372683f
export BPN_AS_BUILT=BPNL00000003AVTH
# cannot put secrets here, please set it manually
export REGULAR_USER_API_KEY=
export ADMIN_USER_API_KEY=
export REGULAR_USER_API_KEY_ESS=
export ADMIN_USER_API_KEY_ESS=
```
* Execute command 

- Note: Execute command from irs root directory ..\item-relationship-service

```console
  source local/testing/api-tests/variable.env && python -m pytest local/testing/api-tests/irs-api-tests.tavern.yaml
```



   2. To run a single test:
        ```console
          py -m pytest local/testing/api-tests/irs-api-tests.tavern.yaml::"<test name>"
          
          # for example:      
          py -m pytest local/testing/api-tests/irs-api-tests.tavern.yaml::"Make sure job with submodels process with status COMPLETED with asPlanned-id"
        ```
