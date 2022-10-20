#!/usr/bin/python

import argparse
import json
import math
import time
import uuid

import requests
from requests.adapters import HTTPAdapter, Retry


def create_submodel_payload(json_payload):
    return json.dumps(json_payload)


def create_submodel_url(host, submodel_id):
    return host + "/data/" + submodel_id


def create_digital_twin_payload(json_payload):
    return json.dumps(json_payload)


def create_digital_twin_payload_url(host):
    return host + "/registry/shell-descriptors"


def create_edc_asset_payload(submodel_url_, asset_prop_id_, digital_twin_submodel_id_):
    return json.dumps({
        "asset": {
            "properties": {
                "asset:prop:id": asset_prop_id_,
                "asset:prop:description": "product description",
                "asset:prop:contenttype": "application/json",
                "asset:prop:policy-id": "use-eu"
            }
        },
        "dataAddress": {
            "properties": {
                "baseUrl": submodel_url_ + "/data/" + digital_twin_submodel_id_,
                "type": "HttpData",
                "proxyBody": True,
                "proxyMethod": True
            }
        }
    })


def create_esr_edc_asset_payload(esr_url_, asset_prop_id_, digital_twin_id_):
    return json.dumps({
        "asset": {
            "properties": {
                "asset:prop:id": asset_prop_id_,
                "asset:prop:description": "product description",
                "asset:prop:contenttype": "application/json",
                "asset:prop:policy-id": "use-eu"
            }
        },
        "dataAddress": {
            "properties": {
                "baseUrl": esr_url_ + "/" + digital_twin_id_ + "/asBuilt/ISO14001/submodel",
                "type": "HttpData",
                "proxyBody": True,
                "proxyMethod": True
            }
        }
    })


def create_edc_policy_payload(edc_policy_id_, asset_prop_id_):
    return json.dumps({
        "id": edc_policy_id_,
        "policy": {
            "prohibitions": [],
            "obligations": [],
            "permissions": [
                {
                    "edctype": "dataspaceconnector:permission",
                    "action": {
                        "type": "USE"
                    },
                    "target": asset_prop_id_
                }
            ]
        }
    })


def create_edc_contract_definition_payload(contract_id_, edc_policy_id_, asset_prop_id_):
    return json.dumps({
        "id": contract_id_,
        "criteria": [
            {
                "operandLeft": "asset:prop:id",
                "operator": "=",
                "operandRight": asset_prop_id_
            }
        ],
        "accessPolicyId": edc_policy_id_,
        "contractPolicyId": edc_policy_id_
    })


def create_aas_shell(global_asset_id_, id_short_, identification_, specific_asset_id_, submodel_descriptors_):
    return json.dumps({
        "description": [],
        "globalAssetId": {
            "value": [
                global_asset_id_
            ]
        },
        "idShort": id_short_,
        "identification": identification_,
        "specificAssetIds": specific_asset_id_,
        "submodelDescriptors": submodel_descriptors_
    })


def create_submodel_descriptor(id_short_, identification_, semantic_id_, endpoint_address_):
    return json.dumps(
        {
            "description": [],
            "idShort": id_short_,
            "identification": identification_,
            "semanticId": {
                "value": [
                    semantic_id_
                ]
            },
            "endpoints": [
                {
                    "interface": "HTTP",
                    "protocolInformation": {
                        "endpointAddress": endpoint_address_,
                        "endpointProtocol": "HTTPS",
                        "endpointProtocolVersion": "1.0"
                    }
                }
            ]
        }
    )


def print_response(response_):
    print(response_)
    if response_.status_code > 205:
        print(response_.text)


if __name__ == "__main__":
    timestamp_start = time.time()
    # -f smallTestdata.json -s1 "http://localhost:8194" -s2 "http://localhost:8194" -s3 "http://localhost:8194"
    # -p "http://provider-control-plane:8282" -a "http://localhost:4243" -e "http://localhost:8187" -k '123456'
    parser = argparse.ArgumentParser(description="Script to upload testdata into CX-Network.",
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("-f", "--file", type=str, help="Test data file location", required=True)
    parser.add_argument("-s1", "--submodel1", type=str, help="url of submodel server 1", required=True)
    parser.add_argument("-s2", "--submodel2", type=str, help="url of submodel server 2", required=True)
    parser.add_argument("-s3", "--submodel3", type=str, help="url of submodel server 3", required=True)
    parser.add_argument("-a", "--aas", type=str, help="aas url", required=True)
    parser.add_argument("-e1", "--edc1", type=str, help="Public EDC provider control plane url 1", required=True)
    parser.add_argument("-e2", "--edc2", type=str, help="Public EDC provider control plane url 2", required=True)
    parser.add_argument("-e3", "--edc3", type=str, help="Public EDC provider control plane url 3", required=True)
    parser.add_argument("-k", "--apikey", type=str, help="edc api key", required=True)
    parser.add_argument("-e", "--esr", type=str, help="esr url", required=False)

    args = parser.parse_args()
    config = vars(args)

    filepath = config.get("file")
    submodel_server_1_address = config.get("submodel1")
    submodel_server_2_address = config.get("submodel2")
    submodel_server_3_address = config.get("submodel3")
    aas_url = config.get("aas")
    edc_url1 = config.get("edc1")
    edc_url2 = config.get("edc2")
    edc_url3 = config.get("edc3")
    edc_api_key = config.get("apikey")
    esr_url = config.get("esr")

    submodel_server_1_bpn = "BPNL00000003B0Q0"
    submodel_server_2_bpn = "BPNL00000003AYRE"

    edc_asset_path = "/data/assets"
    edc_policy_path = "/data/policydefinitions"
    edc_contract_definition_path = "/data/contractdefinitions"

    headers = {
        'Content-Type': 'application/json'
    }

    headers_with_api_key = {
        'X-Api-Key': edc_api_key,
        'Content-Type': 'application/json'
    }

    # Opening JSON file
    f = open(filepath)
    data = json.load(f)
    f.close()
    testdata = data["https://catenax.io/schema/TestDataContainer/1.0.0"]

    contract_id = 1

    retries = Retry(total=5,
                    backoff_factor=0.1)

    session = requests.Session()
    session.mount('https://', HTTPAdapter(max_retries=retries))

    statistic_dict = {
        "provider1": 0,
        "provider2": 0,
        "provider3": 0
    }

    for tmp_data in testdata:
        catenax_id = tmp_data["catenaXId"]
        identification = uuid.uuid4().urn
        tmp_keys = tmp_data.keys()

        specific_asset_ids = [
        ]

        submodel_descriptors = []

        name_at_manufacturer = ""

        for tmp_key in tmp_keys:
            if tmp_key in "urn:bamm:io.catenax.batch:1.0.0#Batch" \
                    or tmp_key in "urn:bamm:io.catenax.serial_part_typization:1.1.0#SerialPartTypization":
                specific_asset_ids = tmp_data[tmp_key][0]["localIdentifiers"]
                name_at_manufacturer = tmp_data[tmp_key][0]["partTypeInformation"]["nameAtManufacturer"] \
                    .replace(" ", "")
            if tmp_key in "urn:bamm:io.catenax.part_as_planned:1.0.0#PartAsPlanned":
                name_at_manufacturer = tmp_data[tmp_key][0]["partTypeInformation"]["nameAtManufacturer"] \
                    .replace(" ", "")
                specific_asset_ids.append({
                    "value": tmp_data[tmp_key][0]["partTypeInformation"]["manufacturerPartId"],
                    "key": "manufacturerPartId"
                })
            if tmp_key in "urn:bamm:io.catenax.part_site_information:1.0.0#PartSiteInformation":
                specific_asset_ids.append({
                    "value": tmp_data[tmp_key][0]["sites"][0]["externalSiteIdentifier"][0]["organization"],
                    "key": "manufacturerId"
                })
        print(name_at_manufacturer)

        part_bpn = ""
        for specific_asset_id in specific_asset_ids:
            if specific_asset_id.get("key") == "manufacturerId":
                part_bpn = specific_asset_id.get("value")

        asr = "urn:bamm:io.catenax.assembly_part_relationship:1.1.0#AssemblyPartRelationship"

        if esr_url and asr in tmp_keys and "childParts" in tmp_data[asr][0] and tmp_data[asr][0]["childParts"]:
            tmp_data.update({"urn:bamm:io.catenax.esr_certificates.esr_certificate_state_statistic:1.0.1#EsrCertificateStateStatistic": ""})

        for tmp_key in tmp_keys:
            if tmp_key not in ("PlainObject", "catenaXId",
                               "urn:bamm:io.catenax.physical_dimension:1.0.0#PhysicalDimension",
                               "urn:bamm:io.catenax.battery.product_description:1.0.1#ProductDescription/1.0.1"):
                # 1. Prepare submodel endpoint address
                if contract_id % 3 == 0:
                    submodel_url = submodel_server_1_address
                    edc_url = edc_url1
                    statistic_dict.update({"provider1": statistic_dict["provider1"] + 1})
                elif contract_id % 3 == 1:
                    submodel_url = submodel_server_2_address
                    edc_url = edc_url2
                    statistic_dict.update({"provider2": statistic_dict["provider2"] + 1})
                else:
                    submodel_url = submodel_server_3_address
                    edc_url = edc_url3
                    statistic_dict.update({"provider3": statistic_dict["provider3"] + 1})

                submodel_name = tmp_key[tmp_key.index("#") + 1: len(tmp_key)]
                submodel_identification = uuid.uuid4().urn
                semantic_id = tmp_key
                if submodel_name == "EsrCertificateStateStatistic" and esr_url is not None:
                    endpoint_address = esr_url + "/" + catenax_id + "/asBuilt/ISO14001/submodel"
                else:
                    endpoint_address = edc_url + "/" + catenax_id + \
                                       "-" + submodel_identification + "/submodel?content=value&extent=withBlobValue"
                descriptor = create_submodel_descriptor(submodel_name, submodel_identification, semantic_id,
                                                        endpoint_address)
                submodel_descriptors.append(json.loads(descriptor))

                edc_policy_id = str(uuid.uuid4())
                asset_prop_id = catenax_id + "-" + submodel_identification

                # 2. Create submodel on submodel server
                if tmp_data[tmp_key] != "":
                    payload = create_submodel_payload(tmp_data[tmp_key][0])
                    response = session.request(method="POST",
                                               url=create_submodel_url(submodel_url, submodel_identification),
                                               headers=headers, data=payload)
                    print_response(response)

                # 3. Create edc asset
                if submodel_name == "EsrCertificateStateStatistic" and esr_url is not None:
                    payload = create_esr_edc_asset_payload(esr_url, asset_prop_id, catenax_id)
                else:
                    payload = create_edc_asset_payload(submodel_url, asset_prop_id, submodel_identification)
                response = session.request(method="POST", url=edc_url + edc_asset_path, headers=headers_with_api_key,
                                           data=payload)
                print_response(response)

                # 4. Create edc policy
                payload = create_edc_policy_payload(contract_id, asset_prop_id)
                response = session.request(method="POST", url=edc_url + edc_policy_path, headers=headers_with_api_key,
                                           data=payload)
                print_response(response)
                # 5. Create edc contract definition
                payload = create_edc_contract_definition_payload(contract_id, contract_id, asset_prop_id)
                response = session.request(method="POST", url=edc_url + edc_contract_definition_path,
                                           headers=headers_with_api_key,
                                           data=payload)
                print_response(response)
                contract_id = contract_id + 1

        # Create aas shell
        if submodel_descriptors:
            payload = create_aas_shell(catenax_id, name_at_manufacturer, identification, specific_asset_ids,
                                       submodel_descriptors)
            response = session.request(method="POST", url=create_digital_twin_payload_url(aas_url),
                                       headers=headers,
                                       data=payload)
            print_response(response)

    timestamp_end = time.time()
    duration = timestamp_end - timestamp_start
    print("Duration: %s Seconds" % math.ceil(duration))
    print(statistic_dict)
