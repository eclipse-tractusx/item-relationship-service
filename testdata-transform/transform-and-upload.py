#!/usr/bin/python

#
#  Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
#
#  See the AUTHORS file(s) distributed with this work for additional
#  information regarding authorship.
#
#  See the LICENSE file(s) distributed with this work for
#  additional information regarding license terms.
#

import json
import os
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


if __name__ == "__main__":
    timestamp_start = time.time()
    filepath = "220513_CatenaX_Testdata_v1.1.json"

    submodel_server_1_folder = "BPNL00000003B0Q0"
    submodel_server_1_address = "http://localhost:8194"

    submodel_server_2_folder = "BPNL00000003AXS3"
    submodel_server_2_address = "http://localhost:8194"

    submodel_server_3_address = "http://localhost:8194"

    internal_control_plane_submodel_url = "http://provider-control-plane:8282"

    aas_url = "http://localhost:4243"

    edc_asset_url = "http://localhost:8187/api/v1/data/assets"
    edc_policy_url = "http://localhost:8187/api/v1/data/policies"
    edc_contract_definition_url = "http://localhost:8187/api/v1/data/contractdefinitions"
    edc_api_key = '123456'

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

                if submodel_descriptor["idShort"] == "assembly-part-relationship" or submodel_descriptor[
                        "idShort"] == "serial-part-typization":
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
                        print("Create assembly-part-relationship")
                        response = requests.request(method="POST",
                                                    url=create_submodel_url(submodel_url, digital_twin_submodel_id),
                                                    headers=headers, data=create_submodel_payload(assembly_part))
                        print(response)
                    elif submodel_descriptor["idShort"] == "serial-part-typization":
                        print("Create serial-part-typization")
                        response = requests.request(method="POST",
                                                    url=create_submodel_url(submodel_url, digital_twin_submodel_id),
                                                    headers=headers, data=create_submodel_payload(serial_part))
                        print(response)
                    # 3. Create edc asset
                    print("Create edc asset")
                    response = requests.request(method="POST", url=edc_asset_url, headers=headers_with_api_key,
                                                data=create_edc_asset_payload(digital_twin_id,
                                                                              digital_twin_submodel_id))
                    print(response)
                    # 4. Create edc policy
                    print("Create edc policy")
                    edc_policy_id = str(uuid.uuid4())
                    response = requests.request(method="POST", url=edc_policy_url, headers=headers_with_api_key,
                                                data=create_edc_policy_payload(edc_policy_id, digital_twin_id,
                                                                               digital_twin_submodel_id))
                    print(response)
                    # 5. Create edc contract definition
                    print("Create edc contreact definition")
                    response = requests.request(method="POST", url=edc_contract_definition_url,
                                                headers=headers_with_api_key,
                                                data=create_edc_contract_definition_payload(contract_id, edc_policy_id))
                    print(response)
                    contract_id = contract_id + 1
            # 6. Create digital twin
            response = requests.request(method="POST", url=create_digital_twin_payload_url(aas_url), headers=headers,
                                        data=create_digital_twin_payload(aas))
            print(response)
            print("%s / 2822" % contract_id)
    print(list_bpn)

    print("\n")

    dict_uuid_local_identifier = {}

    for tmp_bpn in list_bpn:
        folder_bpn = "%s" % tmp_bpn

        if not os.path.isdir(folder_bpn):
            os.mkdir(folder_bpn)

        ##########################
        # SERIAL PART TYPIZATION #
        if dict_serial_parts.get(tmp_bpn) is None:
            print("!!! ERROR: No parts for BPN %s" % tmp_bpn)
        else:
            print(tmp_bpn, len(dict_serial_parts[tmp_bpn]))

            f = open(folder_bpn + "/serialPartTypization.json", "w+")
            f.write("[\n")
            result = ",\n"
            json_list = []
            for tmp_json in dict_serial_parts[tmp_bpn]:
                json_list.append(json.dumps(tmp_json))
            f.write(result.join(json_list))
            f.write("\n]")
            f.close()

    print("\n")

    for tmp_bpn in list_bpn:
        folder_bpn = "%s" % tmp_bpn

        if not os.path.isdir(folder_bpn):
            os.mkdir(folder_bpn)

        ###############################
        # ASSEMBLY PARTS RELATIONSHIP #
        if dict_assembly_parts.get(tmp_bpn) is None:
            print("!!! ERROR: No assembly_part for BPN %s" % tmp_bpn)
        else:
            print(tmp_bpn, len(dict_assembly_parts[tmp_bpn]))

            f = open(folder_bpn + "/assemblyPartRelationship.json", "w+")
            f.write("[\n")
            result = ",\n"
            json_list = []
            for tmp_json in dict_assembly_parts[tmp_bpn]:
                json_list.append(json.dumps(tmp_json))
            f.write(result.join(json_list))
            f.write("\n]")
            f.close()

    print("\n")

    for tmp_bpn in list_bpn:
        folder_bpn = "%s" % tmp_bpn

        if not os.path.isdir(folder_bpn):
            os.mkdir(folder_bpn)

        ##############################
        # ASSET ADMINISTRATION SHELL #
        if dict_aas.get(tmp_bpn) is None:
            print("!!! ERROR: No aas for BPN %s" % tmp_bpn)
        else:
            print(tmp_bpn, len(dict_aas[tmp_bpn]))
            f = open(folder_bpn + "/aas.json", "w+")
            f.write("[\n")
            result = ",\n"
            json_list = []

            for tmp_json in dict_aas[tmp_bpn]:
                json_list.append(json.dumps(tmp_json))
            f.write(result.join(json_list))
            f.write("\n]")
            f.close()

    timestamp_end = time.time()
    print(timestamp_end - timestamp_start)
