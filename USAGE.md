[[Back to main IRS README](README.md)]

## IRS Usage

### Sample Calls

#### IRS

Start a job for a globalAssetId:

```bash
curl -X 'POST' \
  'http://localhost:8080/irs/jobs' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token_value>' \
  -d '{
  "aspects": [
    "SerialPart"
  ],
  "key": {
	  "globalAssetId": "urn:uuid:00000000-0000-0000-0000-000000000000",
	  "bpn": "BPNL000000000000"
  }
  "bomLifecycle": "asBuilt",
  "depth": 1,
  "direction": "downward",
  "collectAspects": true,
  "callbackUrl": "https://hostname.com/callback?id={id}&state={state}"
}'
```

Retrieve the job results by using the jobId returned by the previous call:

```bash
curl -X 'GET' 'http://localhost:8080/irs/jobs/<jobID>' -H 'accept: application/json' -H 'Authorization: Bearer <token_value>'
```

#### Environmental and Social Standards (ESS)

Start an ESS investigation for a globalAssetId and Incident BPNS.

```bash
curl -X 'POST' \
  'http://localhost:8080/ess/bpn/investigations' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer <token_value>' \
  -d '{
	"key": {
		"globalAssetId": "urn:uuid:3d61ada2-1a50-42a0-b411-40a932dd56cc",
		"bpn": "BPNL00ARBITRARY6"
	},
	"incidentBPNSs": [
		"BPNS00ARBITRARY7"
	],
	"bomLifecycle": "asPlanned"
    }'
```

Retrieve the investigation results by using the jobId returned by the previous call:

```bash
curl -X 'GET' 'http://localhost:8080/ess/bpn/investigations/<jobID>' -H 'accept: application/json' -H 'Authorization: Bearer <token_value>'
```

### IRS Request Collection

Besides the examples above, you can find more examples in the [REST request collection for Insomnia](local/testing/request-collection/IRS_Request_Collection.json).
Please see the [corresponding README](local/testing/request-collection/README.md) for more information.
