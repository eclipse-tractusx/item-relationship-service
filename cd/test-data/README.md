# How to load test data into PRS

1. Download latest test data json files by running `./download-test-data-from-tgd.sh <env>` replacing env with the environment (int or dev)
2. Check json files for correctness.
3. If needed manipulate aspect URLS as described in the section below. Check json files for correctness.
4. Commit the files and load test data using the "PRS Load Test Data" workflow

## Update PartAspectUpdate.json with correct consumer URLS

Aspect urls as returned by the Test Data Generator point to the TDG itself. In order to manipulate these to use connector URLs instead you can use the following script.

```bash
python3 update_aspects_url.py <part-aspect-update-json-file-path> \
<aspect-url-mapping> \
<output-file> \
<default-aspect-url> # URL that should be used if the oneIdManufacturer is not mapped with a URL.
```

For example for dev:

```bash
python3 update_aspects_url.py test-data/dev/PartAspectUpdate.json \
test-data/dev/aspect_urls_by_oneidmanufacturer.json \
test-data/dev/PartAspectUpdate.json \
http://tdmgeneratordev.germanywestcentral.azurecontainer.io:8080
```
