#!/usr/bin/python

import argparse
import json
import math
import time
import uuid
import re

import requests


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
                "baseUrl": esr_url_ + digital_twin_id_ + "/asBuilt/ISO14001/submodel",
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
    parser.add_argument("-c", "--controlplane", type=str, help="Public provider control plane url", required=True)
    parser.add_argument("-a", "--aas", type=str, help="aas url", required=True)
    parser.add_argument("-e", "--edc", type=str, help="edc url", required=True)
    parser.add_argument("-k", "--apikey", type=str, help="edc api key", required=True)

    args = parser.parse_args()
    config = vars(args)

    filepath = config.get("file")
    submodel_server_1_address = config.get("submodel1")
    submodel_server_2_address = config.get("submodel2")
    submodel_server_3_address = config.get("submodel3")
    public_control_plane_url = config.get("controlplane")
    aas_url = config.get("aas")
    edc_url = config.get("edc")
    edc_api_key = config.get("apikey")

    submodel_server_1_folder = "BPNL00000003B0Q0"
    submodel_server_2_folder = "BPNL00000003AXS3"

    edc_asset_url = "%s/data/assets" % edc_url
    edc_policy_url = "%s/data/policydefinitions" % edc_url
    edc_contract_definition_url = "%s/data/contractdefinitions" % edc_url

    esr_url = "https://irs-esr.dev.demo.catena-x.net/esr/esr-statistics/"

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

    semantic_dict = {
        "SerialPartTypization": "urn:bamm:io.catenax.serial_part_typization:1.1.0#SerialPartTypization",
        "AssemblyPartRelationship": "urn:bamm:io.catenax.assembly_part_relationship:1.1.0#AssemblyPartRelationship",
        "MaterialForRecycling": "urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling",
        "CertificateOfDestruction": "urn:bamm:io.catenax.certificate_of_destruction:1.0.0#CertificateOfDestruction",
        "VehicleProductDescription": "urn:bamm:io.catenax.vehicle.product_description:1.0.1#ProductDescription",
        "BatteryProductDescription": "urn:bamm:io.catenax.battery.product_description:1.0.1#ProductDescription",
        "ReturnRequest": "urn:bamm:io.catenax.return_request:1.0.0#ReturnRequest",
        "PhysicalDimension": "urn:bamm:io.catenax.physical_dimension:1.0.0#PhysicalDimension",
        "batch": "urn:bamm:io.catenax.batch:1.0.0#Batch"
    }

    contract_id = 1

    for tmp_data in testdata:
        catenax_id = tmp_data["catenaXId"]
        identification = uuid.uuid4().urn
        tmp_keys = tmp_data.keys()

        specific_asset_ids = [
            {
                "value": "",
                "key": "manufacturerId"
            },
            {
                "value": "",
                "key": "manufacturerPartId"
            },
            {
                "value": "",
                "key": "partInstanceId"
            }
        ]

        submodel_descriptors = []

        for tmp_key in tmp_keys:
            if tmp_key in "https://catenax.io/schema/batch/1.0.0" \
                    or tmp_key in "https://catenax.io/schema/SerialPartTypization/1.0.0":
                specific_asset_ids = tmp_data[tmp_key][0]["localIdentifiers"]

        part_bpn = ""

        for specific_asset_id in specific_asset_ids:
            if specific_asset_id.get("key") == "manufacturerId":
                part_bpn = specific_asset_id.get("value")

        for tmp_key in tmp_keys:
            if tmp_key not in ("PlainObject", "catenaXId"):
                submodel_name = re.sub('/[0-9].[0-9].[0-9]', '', tmp_key.replace("https://catenax.io/schema/", ""))
                submodel_identification = uuid.uuid4().urn
                semantic_id = semantic_dict.get(submodel_name)
                endpoint_address = public_control_plane_url + "/" + catenax_id + \
                    "-" + submodel_identification + "/submodel?content=value&extent=withBlobValue"
                descriptor = create_submodel_descriptor(submodel_name, submodel_identification, semantic_id,
                                                        endpoint_address)
                submodel_descriptors.append(json.loads(descriptor))

                # 1. Prepare submodel endpoint address
                if part_bpn == submodel_server_1_folder:
                    submodel_url = submodel_server_1_address
                elif part_bpn == submodel_server_2_folder:
                    submodel_url = submodel_server_2_address
                else:
                    submodel_url = submodel_server_3_address

                # 2. Create submodel on submodel server
                payload = create_submodel_payload(tmp_data[tmp_key][0])
                print(payload)
                # response = requests.request(method="POST",
                #                             url=create_submodel_url(submodel_url, submodel_identification),
                #                             headers=headers, data=payload)
                # print_response(response)

                edc_policy_id = str(uuid.uuid4())

                asset_prop_id = catenax_id + "-" + submodel_identification

                payload = create_edc_asset_payload(submodel_url, asset_prop_id, submodel_identification)
                print(payload)
                # response = requests.request(method="POST", url=edc_asset_url, headers=headers_with_api_key,
                #                             data=payload)
                # print_response(response)
                # 4. Create edc policy
                payload = create_edc_policy_payload(contract_id, asset_prop_id)
                print(payload)
                # response = requests.request(method="POST", url=edc_policy_url, headers=headers_with_api_key,
                #                             data=payload)
                # print_response(response)
                # 5. Create edc contract definition
                payload = create_edc_contract_definition_payload(contract_id, contract_id, asset_prop_id)
                print(payload)
                # response = requests.request(method="POST", url=edc_contract_definition_url,
                #                             headers=headers_with_api_key,
                #                             data=payload)
                # print_response(response)
                contract_id = contract_id + 1

        # create esr assets
        # payload = create_esr_edc_asset_payload(esr_url, asset_prop_id, digital_twin_id)

        payload = create_aas_shell(catenax_id, "", identification, specific_asset_ids, submodel_descriptors)
        print(payload)

        # payload = create_digital_twin_payload(shell)
        # print(payload)
        # response = requests.request(method="POST", url=create_digital_twin_payload_url(aas_url),
        #                             headers=headers,
        #                             data=payload)
        # print_response(response)

    timestamp_end = time.time()
    duration = timestamp_end - timestamp_start
    print("Duration: %s Seconds" % math.ceil(duration))
