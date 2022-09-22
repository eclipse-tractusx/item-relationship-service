#!/usr/bin/python

import argparse
import json
import math
import time
import uuid

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


def print_response(response_):
    print(response_)
    if response_.status_code > 205:
        print(response_.text)


if __name__ == "__main__":
    timestamp_start = time.time()
    # -f smallTestdata.json -s1 "http://localhost:8194" -s2 "http://localhost:8194" -s3 "http://localhost:8194"
    # -i "http://provider-control-plane:8282" -a "http://localhost:4243" -e "http://localhost:8187" -k '123456'
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

    headers = {
        'Content-Type': 'application/json'
    }

    headers_with_api_key = {
        'X-Api-Key': edc_api_key,
        'Content-Type': 'application/json'
    }

    error_no_bpn_found_ = "!!! ERROR: NO BPN FOUND !!!"

    # Opening JSON file
    f = open(filepath)
    data = json.load(f)
    f.close()

    testdata = data["https://catenax.io/schema/TestDataContainer/1.0.0"]

    dict_cxId_bpn = {}

    list_bpn = []
    dict_serial_parts = {}
    dict_assembly_parts = {}
    dict_aas = {}
    dict_batch = {}
    contract_id = 1

    for tmp_data in testdata:
        tmp_keys = tmp_data.keys()
        aas = None
        part_bpn = None
        serial_part = None
        assembly_part = None
        batch = None

        for tmp_key in tmp_keys:
            if tmp_key == "https://catenax.io/schema/SerialPartTypization/1.0.0":
                serial_part = tmp_data[tmp_key][0]

                part_bpn = ""
                for local_identifier in serial_part["localIdentifiers"]:
                    if local_identifier["key"] == "ManufacturerID":
                        part_bpn = local_identifier["value"]

                if part_bpn == "":
                    print(error_no_bpn_found_)

                dict_cxId_bpn[serial_part["catenaXId"]] = part_bpn

                if dict_serial_parts.get(part_bpn) is None:
                    dict_serial_parts[part_bpn] = []

                dict_serial_parts[part_bpn].append(serial_part)

                if part_bpn not in list_bpn:
                    list_bpn.append(part_bpn)

            elif tmp_key == "https://catenax.io/schema/AssemblyPartRelationship/1.0.0":
                assembly_part = tmp_data[tmp_key][0]

                part_bpn = ""
                if dict_cxId_bpn.get(assembly_part["catenaXId"]) is not None:
                    part_bpn = dict_cxId_bpn[assembly_part["catenaXId"]]

                if part_bpn == "":
                    print(error_no_bpn_found_)

                if dict_assembly_parts.get(part_bpn) is None:
                    dict_assembly_parts[part_bpn] = []

                dict_assembly_parts[part_bpn].append(assembly_part)

            elif tmp_key == "https://catenax.io/schema/Batch/1.0.0":
                batch = tmp_data[tmp_key][0]

                part_bpn = ""
                if dict_cxId_bpn.get(batch["catenaXId"]) is not None:
                    part_bpn = dict_cxId_bpn[batch["catenaXId"]]

                if part_bpn == "":
                    print(error_no_bpn_found_)

                if dict_batch.get(part_bpn) is None:
                    dict_batch[part_bpn] = []

                dict_batch[part_bpn].append(batch)

            elif tmp_key == "https://catenax.io/schema/AAS/3.0":
                aas = tmp_data[tmp_key][0]

                part_bpn = ""
                if dict_cxId_bpn.get(aas.get("globalAssetId").get("value")[0]) is not None:
                    part_bpn = dict_cxId_bpn[aas.get("globalAssetId").get("value")[0]]

                if part_bpn == "":
                    print(error_no_bpn_found_)

                # Transform semanticId to match the current model on CX-INT DT-Registry
                for submodel_descriptor in aas["submodelDescriptors"]:
                    value = submodel_descriptor["semanticId"][0]["value"]
                    data = {"value": [value]}
                    submodel_descriptor["semanticId"] = data

                if dict_aas.get(part_bpn) is None:
                    dict_aas[part_bpn] = []

                dict_aas[part_bpn].append(aas)

        if aas and part_bpn and (serial_part or assembly_part) is not None:
            digital_twin_id = aas.get("globalAssetId")["value"][0]
            aas["identification"] = digital_twin_id

            for submodel_descriptor in aas.get("submodelDescriptors"):
                digital_twin_submodel_id = uuid.uuid4().urn

                if submodel_descriptor["idShort"] == "assembly-part-relationship" \
                        or submodel_descriptor["idShort"] == "serial-part-typization" \
                        or submodel_descriptor["idShort"] == "batch":
                    # 1. Prepare submodel endpoint address
                    endpoint = submodel_descriptor["endpoints"][0]
                    address = endpoint["protocolInformation"]["endpointAddress"]
                    if part_bpn == submodel_server_1_folder:
                        submodel_url = submodel_server_1_address
                    elif part_bpn == submodel_server_2_folder:
                        submodel_url = submodel_server_2_address
                    else:
                        submodel_url = submodel_server_3_address

                    generated_address = public_control_plane_url + "/" + digital_twin_id + \
                        "-" + digital_twin_submodel_id + "/submodel?content=value&extent=withBlobValue"
                    endpoint["protocolInformation"]["endpointAddress"] = generated_address
                    submodel_descriptor["identification"] = digital_twin_submodel_id

                    # 2. Create submodel on submodel server
                    if submodel_descriptor["idShort"] == "assembly-part-relationship":
                        # Create assembly-part-relationship
                        payload = create_submodel_payload(assembly_part)
                        # print(payload)
                        response = requests.request(method="POST",
                                                    url=create_submodel_url(submodel_url, digital_twin_submodel_id),
                                                    headers=headers, data=payload)
                        print_response(response)
                    elif submodel_descriptor["idShort"] == "serial-part-typization":
                        # Create serial-part-typization
                        payload = create_submodel_payload(serial_part)
                        # print(payload)
                        response = requests.request(method="POST",
                                                    url=create_submodel_url(submodel_url, digital_twin_submodel_id),
                                                    headers=headers, data=payload)
                        print_response(response)
                    elif submodel_descriptor["idShort"] == "batch":
                        # Create batch
                        payload = create_submodel_payload(batch)
                        # print(payload)
                        response = requests.request(method="POST",
                                                    url=create_submodel_url(submodel_url, digital_twin_submodel_id),
                                                    headers=headers, data=payload)
                        print_response(response)
                    edc_policy_id = str(uuid.uuid4())

                    asset_prop_id = digital_twin_id + "-" + digital_twin_submodel_id

                    # 3. Create edc asset
                    payload = create_edc_asset_payload(submodel_url, asset_prop_id, digital_twin_submodel_id)
                    # print(payload)
                    response = requests.request(method="POST", url=edc_asset_url, headers=headers_with_api_key,
                                                data=payload)
                    print_response(response)
                    # 4. Create edc policy
                    payload = create_edc_policy_payload(contract_id, asset_prop_id)
                    # print(payload)
                    response = requests.request(method="POST", url=edc_policy_url, headers=headers_with_api_key,
                                                data=payload)
                    print_response(response)
                    # 5. Create edc contract definition
                    payload = create_edc_contract_definition_payload(contract_id, contract_id, asset_prop_id)
                    # print(payload)
                    response = requests.request(method="POST", url=edc_contract_definition_url,
                                                headers=headers_with_api_key,
                                                data=payload)
                    print_response(response)
                    contract_id = contract_id + 1
            payload = create_digital_twin_payload(aas)
            # print(payload)
            response = requests.request(method="POST", url=create_digital_twin_payload_url(aas_url),
                                        headers=headers,
                                        data=payload)
            print_response(response)

    timestamp_end = time.time()
    duration = timestamp_end - timestamp_start
    print("Duration: %s Seconds" % math.ceil(duration))
