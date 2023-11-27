# ess-demo.py

## Prerequisites

- Python 3.10 or higher
- Requirements need to be installed
    ```shell
    pip install -r requirements.txt
    ```

## Usage

```shell
python ess-demo.py [ options ]
```

## Description

This script showcases the IRS ESS top-down investigation use-case.

The demo consists of four steps:

1. Request all digital twins of the provided Digital Twin Registry for the requested company.
2. Filter these Twins for the lifecycle asPlanned and collect their BPN and globalAssetId
3. Start an ESS Batch investigation with these twins
4. Wait for completion and print the result of the investigation

## Demo cases for IRS ESS INT environment

### Case 1 (incident and no issues in tree)

searchBPN: BPNL00ARBITRARY4  
incidentBPNS: BPNS00ARBITRARY6

### Case 2 (no incident and no issues in tree)

searchBPN: BPNL00ARBITRARY4  
incidentBPNS: BPNS00ARBITRARY8

### Case 3 (incident and not resolvable path in tree)

searchBPN: BPNL00ARBITRARY8  
incidentBPNS: BPNS0ARBITRARY10

### Case 4 (no incident and not resolvable path in tree)

searchBPN: BPNL00ARBITRARY8  
incidentBPNS: BPNS0ARBITRARY12

## Example usage

```shell
python ess-demo.py \
    --aas "https://registry.example/api/v3.0/shell-descriptors" \
    --ownBPN "BPNL0000000XYZ00" \
    --searchBPN "BPNL0000000XYZ11" \
    --incidentBPNS "BPNS0000000XYZ22" "BPNS0000000XYZ33" \
    --irs "https://irs.example" \
    --tokenurl "https://oauth2.example/auth/EXAMPLE/openid-connect/token" \
    --clientid "exampleuser" \
    --clientsecret "examplepass"
```

## Parameters

| Parameter      | Example                                                  | Description                                                        |
|----------------|----------------------------------------------------------|--------------------------------------------------------------------|
| -h, --help     |                                                          | show help message                                                  |
| --aas          | https://registry.example/api/v3.0/shell-descriptors      | AAS registry URL                                                   |
| --ownBPN       | BPNL0000000XYZ00                                         | BPN of the requesting Company                                      |
| --searchBPN    | BPNL0000000XYZ11                                         | BPN of the Company to search for                                   |
| --incidentBPNS | BPNS0000000XYZ22 BPNS0000000XYZ33                        | List of BPNS of the Companies where the incidents occurred         |
| --irs          | https://irs.example                                      | IRS base URL                                                       |
| --tokenurl     | https://oauth2.example/auth/EXAMPLE/openid-connect/token | OAuth2 token URL                                                   |
| --clientid     | exampleuser                                              | Client ID                                                          |
| --clientsecret | examplepass                                              | Client Secret                                                      |
| --batchsize    | 10                                                       | The batch size for a ESS investigation. Must be mod 10. Default 10 |
| --debug        |                                                          | debug logging (optional)                                           |

