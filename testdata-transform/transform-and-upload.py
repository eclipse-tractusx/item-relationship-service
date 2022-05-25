#!/usr/bin/python

import json
import math
import time
import uuid
import requests
import argparse


def create_submodel_payload(json_payload):
    return json.dumps(json_payload)


def create_submodel_url(host, submodel_id):
    return host + "/data/" + submodel_id


def create_digital_twin_payload(json_payload):
    return json.dumps(json_payload)


def create_digital_twin_payload_url(host):
    return host + "/registry/shell-descriptors"


def create_edc_asset_payload(digital_twin_id, digital_twin_submodel_id):
    return json.dumps({
        "asset": {
            "properties": {
                "asset:prop:id": digital_twin_id + "-" + digital_twin_submodel_id,
                "asset:prop:name": "product description",
                "asset:prop:contenttype": "application/json",
                "asset:prop:policy-id": "use-eu"
            }
        },
        "dataAddress": {
            "properties": {
                "endpoint": "http://provider-backend-service:8080/data/" + digital_twin_submodel_id,
                "type": "HttpData"
            }
        }
    })


def create_edc_policy_payload(edc_policy_id, digital_twin_id, digital_twin_submodel_id):
    return json.dumps({
        "uid": edc_policy_id,
        "permissions": [
            {
                "target": digital_twin_id + "-" + digital_twin_submodel_id,
                "action": {
                    "type": "USE"
                },
                "edctype": "dataspaceconnector:permission"
            }
        ],
        "@type": {
            "@policytype": "set"
        }
    })


def create_edc_contract_definition_payload(contract_id, edc_policy_id):
    return json.dumps({
        "id": contract_id,
        "accessPolicyId": edc_policy_id,
        "contractPolicyId": edc_policy_id,
        "criteria": []
    })


def print_response(response_):
    if response_.status_code > 205:
        print(response_)


if __name__ == "__main__":
    timestamp_start = time.time()
    # -f smallTestdata.json -s1 "http://localhost:8194" -s2 "http://localhost:8194" -s3 "http://localhost:8194" -i "http://provider-control-plane:8282" -a "http://localhost:4243" -e "http://localhost:8187" -k '123456'
    parser = argparse.ArgumentParser(description="Script to upload testdata into CX-Network.",
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("-f", "--file", type=str, help="Test data file location", required=True)
    parser.add_argument("-s1", "--submodel1", type=str, help="url of submodel server 1", required=True)
    parser.add_argument("-s2", "--submodel2", type=str, help="url of submodel server 2", required=True)
    parser.add_argument("-s3", "--submodel3", type=str, help="url of submodel server 3", required=True)
    parser.add_argument("-i", "--internal", type=str, help="internal control plane submodel url", required=True)
    parser.add_argument("-a", "--aas", type=str, help="aas url", required=True)
    parser.add_argument("-e", "--edc", type=str, help="edc url", required=True)
    parser.add_argument("-k", "--apikey", type=str, help="edc api key", required=True)

    args = parser.parse_args()
    config = vars(args)

    filepath = config.get("file")
    submodel_server_1_address = config.get("submodel1")
    submodel_server_2_address = config.get("submodel2")
    submodel_server_3_address = config.get("submodel3")
    internal_control_plane_submodel_url = config.get("internal")
    aas_url = config.get("aas")
    edc_url = config.get("edc")
    edc_api_key = config.get("apikey")

    submodel_server_1_folder = "BPNL00000003B0Q0"
    submodel_server_2_folder = "BPNL00000003AXS3"

    edc_asset_url = "%s/api/v1/data/assets" % edc_url
    edc_policy_url = "%s/api/v1/data/policies" % edc_url
    edc_contract_definition_url = "%s/api/v1/data/contractdefinitions" % edc_url

    replaceURL = "http://provider.connector:port"

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

    dict_cxId_bpn = {}

    list_bpn = []
    dict_serial_parts = {}
    dict_assembly_parts = {}
    dict_aas = {}
    contract_id = 1
    iteration = 1

    for tmp_data in testdata:
        tmp_keys = tmp_data.keys()
        aas = None
        part_bpn = None
        serial_part = None
        assembly_part = None

        for tmp_key in tmp_keys:
            if tmp_key == "https://catenax.io/schema/SerialPartTypization/1.0.0":
                serial_part = tmp_data[tmp_key][0]

                part_bpn = ""
                for local_identifier in serial_part["localIdentifiers"]:
                    if local_identifier["key"] == "ManufacturerID":
                        part_bpn = local_identifier["value"]

                if part_bpn == "":
                    print("!!! ERROR: NO BPN FOUND !!!")

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
                    print("!!! ERROR: NO BPN FOUND !!!")

                if dict_assembly_parts.get(part_bpn) is None:
                    dict_assembly_parts[part_bpn] = []

                dict_assembly_parts[part_bpn].append(assembly_part)

            elif tmp_key == "https://catenax.io/schema/AAS/3.0":
                aas = tmp_data[tmp_key][0]

                part_bpn = ""
                if dict_cxId_bpn.get(aas.get("globalAssetId").get("value")[0]) is not None:
                    part_bpn = dict_cxId_bpn[aas.get("globalAssetId").get("value")[0]]

                if part_bpn == "":
                    print("!!! ERROR: NO BPN FOUND !!!")

                for submodel_descriptor in aas["submodelDescriptors"]:
                    value = submodel_descriptor["semanticId"][0]["value"]
                    data = {"value": [value]}
                    submodel_descriptor["semanticId"] = data

                if dict_aas.get(part_bpn) is None:
                    dict_aas[part_bpn] = []

                dict_aas[part_bpn].append(aas)

        if aas and part_bpn and serial_part and assembly_part is not None:
            digital_twin_id = aas.get("globalAssetId")["value"][0]
            aas["identification"] = digital_twin_id

            for submodel_descriptor in aas.get("submodelDescriptors"):
                digital_twin_submodel_id = uuid.uuid4().urn

                if submodel_descriptor["idShort"] == "assembly-part-relationship" or submodel_descriptor["idShort"] == "serial-part-typization":
                    # 1. Prepare submodel endpoint address
                    endpoint = submodel_descriptor["endpoints"][0]
                    address = endpoint["protocolInformation"]["endpointAddress"]
                    if part_bpn == submodel_server_1_folder:
                        submodel_url = submodel_server_1_address
                    elif part_bpn == submodel_server_2_folder:
                        submodel_url = submodel_server_2_address
                    else:
                        submodel_url = submodel_server_3_address

                    generated_address = internal_control_plane_submodel_url + "/" + part_bpn + "/" + digital_twin_id + \
                                        "-" + digital_twin_submodel_id + "/submodel?content=value&extent=withBlobValue"
                    endpoint["protocolInformation"]["endpointAddress"] = generated_address
                    submodel_descriptor["identification"] = digital_twin_submodel_id

                    # 2. Create submodel on submodel server
                    if submodel_descriptor["idShort"] == "assembly-part-relationship":
                        # Create assembly-part-relationship
                        response = requests.request(method="POST",
                                                    url=create_submodel_url(submodel_url, digital_twin_submodel_id),
                                                    headers=headers, data=create_submodel_payload(assembly_part))
                        print_response(response)
                    elif submodel_descriptor["idShort"] == "serial-part-typization":
                        # Create serial-part-typization
                        response = requests.request(method="POST",
                                                    url=create_submodel_url(submodel_url, digital_twin_submodel_id),
                                                    headers=headers, data=create_submodel_payload(serial_part))
                        print_response(response)
                    # 3. Create edc asset
                    response = requests.request(method="POST", url=edc_asset_url, headers=headers_with_api_key,
                                                data=create_edc_asset_payload(digital_twin_id,
                                                                              digital_twin_submodel_id))
                    print_response(response)
                    # 4. Create edc policy
                    edc_policy_id = str(uuid.uuid4())
                    response = requests.request(method="POST", url=edc_policy_url, headers=headers_with_api_key,
                                                data=create_edc_policy_payload(edc_policy_id, digital_twin_id,
                                                                               digital_twin_submodel_id))
                    print_response(response)
                    # 5. Create edc contract definition
                    response = requests.request(method="POST", url=edc_contract_definition_url,
                                                headers=headers_with_api_key,
                                                data=create_edc_contract_definition_payload(contract_id, edc_policy_id))
                    print_response(response)
                    contract_id = contract_id + 1
                else:
                    print("No Submodel for AssemblyPartRelationship or SerialPartTypization exists in Data Set.")
            response = requests.request(method="POST", url=create_digital_twin_payload_url(aas_url),
                                        headers=headers,
                                        data=create_digital_twin_payload(aas))
            print_response(response)
            print("%s / 1492" % iteration)
            iteration += 1

    timestamp_end = time.time()
    duration = timestamp_end - timestamp_start
    print("\nDuration: %s Seconds" % math.ceil(duration))
