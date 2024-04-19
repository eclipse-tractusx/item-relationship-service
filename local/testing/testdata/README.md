[[Back to main IRS README](../../../README.md)]

# transform-and-upload.py

## Prerequisites

Python 3.10 or higher has to be installed.

## Usage

```shell
python transform-and-upload.py [ options ]
```

## Description

This script uses a test data file, extracts the data. The data is then distributed to the corresponding system.  
Aspect models are uploaded to the submodel server specified with `-s`. Each aspect model is assigned a random uuid.

This script supports custom EDC policies. The policies can be specified by adding a `policy` field can be added to the
beginning of the testdata file, where the desired policies can be defined.

Each aspect model will be registered within the EDC. For that, an asset and contract definition will be created.

The options `-s`, `-a` and `-edc` also have the option to provide special upload URLs `-su`, `-au` and `-eu`. These are
mostly used for a local
kubernetes deployment, where the services are forwarded to localhost, but have different URLs inside the cluster.  
If the upload URLs are not specified, the URLs of `-s`, `-a` and `-edc` are also used for uploading the data.

It is possible to use more than one URL for `-s`, `-a` and `-edc`. To do that, simply add a list to the parameter by
separating each URL by a space. When using multiple URLs, the testdata will be evenly distributed to the URLs.

The number of URLs provided via `-s`, `-a` and `-edc` have to be the same!  
Note, that the order of the specified URLs is important.

Example for multiple providers:
```shell
python3 transform-and-upload.py \
-f /path/to/testdata.json \
-s https://submodel.server1 https://submodel.server2 https://submodel.server3 \
-a https://registry.server1 https://registry.server2 https://registry.server3 \
-edc https://edc.controlplane1 https://edc.controlplane2 https://edc.controlplane3 \
-k edc-api-key
```

### Policies

If you want to include different EDC usage policies, you can add them to the beginning of the testdata file.

An example policy can look like this:

```json
{
  "policies": {
    "ID 3.1 Trace": {
       "@context": {
          "odrl": "http://www.w3.org/ns/odrl/2/"
       },
       "@type": "PolicyDefinitionRequestDto",
       "@id": "id-3.1-trace",
       "policy": {
          "@type": "Policy",
          "odrl:permission": [
             {
                "odrl:action": "USE",
                "odrl:constraint": {
                   "@type": "AtomicConstraint",
                   "odrl:or": [
                      {
                         "@type": "Constraint",
                         "odrl:leftOperand": "idsc:PURPOSE",
                         "odrl:operator": "EQ",
                         "odrl:rightOperand": "ID 3.1 Trace"
                      }
                   ]
                }
             }
          ]
       }
    }
  },
  "https://catenax.io/schema/TestDataContainer/1.0.0": [
    ...
  ]
}
```

There are two options to apply the policy.

1. Use the parameter `-p id-3.1-trace` to apply this policy to every asset
2. Add a field `policy: "id-3.1-trace"` to the top level of each asset you want to use this policy. Every other asset
   will get the default, empty policy.

It is also possible to define more than one policy at the beginning and then use the `"policy": "<id>"` field to apply a
different policy to each asset.

## Parameters

If not mentioned otherwise, all parameters are mandatory for the script to work.

**-f, --file <file>**  
Path to the test data file.

**-s, --submodel <[URL ...] >**  
A space-separated list of submodel server URLs. These URLs will be used to register EDC assets. If `-su` is not
specified, these URLs will be also used for upload.

**-su, --submodelupload <[URL ...] >**  
(Optional) If specified, these URLs will be used to upload the assets to.

**-a, --aas <AAS>**  
These URLs will be used to create the EDC asset for the decentralized digital twin registry. If `-au` is not specified,
these URLs will be also used for upload of the digital twins.

**-au, --aasupload <AASUPLOAD>**  
(Optional) If specified, these URLs will be used to upload the digital twins and the `-a` URLs will only be used for EDC
asset creation.

**-edc, --edc <[URL ...] >**  
These URLs will be referenced in the digital twin registry. If `-eu` is not specified, these URLs will be also used for
creation and upload of EDC assets, policies and contract-definitions.

**-eu, --edcupload <[URL ...] >**  
(Optional) If specified, these URLs will be used to upload assets, policies and contract-definitions.

**-k, --apikey <APIKEY>**  
API key for the EDCs specified with `-edc` or `-eu`.

**-e, --esr <URL>**  
(Optional) This is used for the IRS use case Environmental and Social Responsibility. The provided URL will be used, to
register the `EsrCertificateStateStatistic` Aspect Model for the recursive IRS approach.

**--ess**  
(Optional) Enable ESS data creation with invalid EDC URL. This can be used in combination with `--bpn`. Every digital
twin with this BPN will be created with an invalid endpointAddress.

**--bpn <BPN>**  
(Optional) Faulty BPN which will create a non-existing EDC endpoint in the digital twin.

**-p, --policy <POLICY>**  
(Optional) Default EDC Policy which should be used for EDC contract definitions. Define this policy at the beginning of
the testdata file and pass the id with this parameter. If nothing is provided, the default policy will be empty with the
id "default-policy".

**-bpns, --bpns <[BPN ...] >**  
(Optional) A list of BPNs Filter upload to upload only specific BPNs

**-d, --dataplane <[URL ...] >**  
(Optional) If specified, these public dataplane URLs will be displayed in the digital twin in attribute 'href'

**--aas3 <[BPN ...] >**  
(Optional) Flag to create AAS assets according to version 3.0.1

**--allowedBPNs <[BPN ...] >**  
(Optional) (Required for --aas3) A list of allowed BPNs which will be added to the specificAssetIds when creating DTR assets

**-h, --help**  
Usage help. This lists all current command line options with a short description.
