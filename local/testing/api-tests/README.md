# Tavern API Tests

This folder contains [Tavern](https://tavern.readthedocs.io) API tests for the IRS.

## Usage 

* Install python
* Install pytest: ```py -m pip install pytest```
* Install tavern: ```py -m pip install tavern```
* Create variable.env in directory api-tests
* Copy content to variable.env
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
```console
  source variable.env
  py -m pytest irs-api-tests.tavern.yaml
```



