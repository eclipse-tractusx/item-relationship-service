# Usage 
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
export GLOBAL_ASSET_ID=urn:uuid:a4a2ba57-1c50-48ad-8981-7a0ef032146b
# cannot put secrets here, please set it manually
export OAUTH2_HOST=""
export OAUTH2_CLIENT_ID=
export OAUTH2_CLIENT_SECRET=
```
* Execute command 
```console
  source variable.env
  py -m pytest irs-api-tests.tavern.yaml
```



