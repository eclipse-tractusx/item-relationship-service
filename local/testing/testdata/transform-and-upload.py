#!/usr/bin/python

import argparse
import json
import math
import time
import uuid
from copy import copy

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
                "asset:prop:contenttype": "application/json"
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
                "asset:prop:contenttype": "application/json"
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


def create_edc_contract_definition_payload(edc_policy_id_, asset_prop_id_):
    return json.dumps({
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


def check_url_args(submodel_server_upload_urls_, submodel_server_urls_, edc_upload_urls_, edc_urls_):
    nr_of_submodel_server_upload_urls = len(submodel_server_upload_urls_)
    nr_of_submodel_server_urls = len(submodel_server_urls_)
    if nr_of_submodel_server_upload_urls != nr_of_submodel_server_urls:
        raise Exception(
            f"Number and order of submodelserver upload URLs '{submodel_server_upload_urls_}' "
            f"has to match number and order Number and order of submodelserver URLs '{submodel_server_urls_}'")
    nr_of_edc_upload_urls = len(edc_upload_urls_)
    nr_of_edc_urls = len(edc_urls_)
    if nr_of_edc_upload_urls != nr_of_edc_urls:
        raise Exception(
            f"Number and order of edc upload URLs '{edc_upload_urls_}' has to match number and order of edc URLs "
            f"'{edc_urls_}'")
    if nr_of_submodel_server_urls != nr_of_edc_urls:
        raise Exception(
            f"Number and order of edc URLs '{edc_urls_}' has to match number and order of submodelserver URLS "
            f"'{submodel_server_urls_}'")


def create_policy(policy_, edc_upload_url_, edc_policy_path_, headers_, session_):
    url_ = edc_upload_url_ + edc_policy_path_
    response_ = session_.request(method="POST", url=url_ + "/request", headers=headers_, data=json.dumps({
        "filter": "id=" + policy_['id']
    }))
    if response_.status_code == 200 and response_.json():
        print(f"Policy {policy_['id']} already exists. Skipping creation.")
    else:
        response_ = session_.request(method="POST", url=url_, headers=headers_, data=json.dumps(policy_))
        print(response_)
        if response_.status_code > 205:
            print(response_.text)
        else:
            print(f"Successfully created policy {response_.json()['id']}.")


if __name__ == "__main__":
    timestamp_start = time.time()
    parser = argparse.ArgumentParser(description="Script to upload testdata into CX-Network.",
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("-f", "--file", type=str, help="Test data file location", required=True)
    parser.add_argument("-s", "--submodel", type=str, nargs="*", help="Submodel server display URLs", required=True)
    parser.add_argument("-su", "--submodelupload", type=str, nargs="*", help="Submodel server upload URLs",
                        required=False)
    parser.add_argument("-a", "--aas", type=str, help="aas url", required=True)
    parser.add_argument("-edc", "--edc", type=str, nargs="*", help="EDC provider control plane display URLs",
                        required=True)
    parser.add_argument("-eu", "--edcupload", type=str, nargs="*", help="EDC provider control plane upload URLs",
                        required=False)
    parser.add_argument("-k", "--apikey", type=str, help="EDC provider api key", required=True)
    parser.add_argument("-e", "--esr", type=str, help="ESR URL", required=False)
    parser.add_argument("--ess", help="Enable ESS data creation with invalid EDC URL", action='store_true',
                        required=False)
    parser.add_argument("--bpn", help="Faulty BPN which will create a non existing EDC endpoint", required=False)
    parser.add_argument("-p", "--policy", help="Default Policy which should be used for EDC contract definitions",
                        required=False)

    args = parser.parse_args()
    config = vars(args)

    filepath = config.get("file")
    submodel_server_urls = config.get("submodel")
    submodel_server_upload_urls = config.get("submodelupload")
    aas_url = config.get("aas")
    edc_urls = config.get("edc")
    edc_upload_urls = config.get("edcupload")
    edc_api_key = config.get("apikey")
    esr_url = config.get("esr")
    is_ess = config.get("ess")
    bpnl_fail = config.get("bpn")
    default_policy = config.get("policy")

    if submodel_server_upload_urls is None:
        submodel_server_upload_urls = submodel_server_urls
    if edc_upload_urls is None:
        edc_upload_urls = edc_urls

    if default_policy is None:
        default_policy = "default-policy"

    check_url_args(submodel_server_upload_urls, submodel_server_urls, edc_upload_urls, edc_urls)

    edc_asset_path = "/api/v1/management/assets"
    edc_policy_path = "/api/v1/management/policydefinitions"
    edc_contract_definition_path = "/api/v1/management/contractdefinitions"

    headers = {
        'Content-Type': 'application/json'
    }

    headers_with_api_key = {
        'X-Api-Key': edc_api_key,
        'Content-Type': 'application/json'
    }

    default_policy_definition = {
        "default": {
            "id": default_policy,
            "policy": {
                "prohibitions": [],
                "obligations": [],
                "permissions": [
                    {
                        "edctype": "dataspaceconnector:permission",
                        "action": {
                            "type": "USE"
                        }
                    }
                ]
            }
        }
    }

    # Opening JSON file
    f = open(filepath)
    data = json.load(f)
    f.close()
    testdata = data["https://catenax.io/schema/TestDataContainer/1.0.0"]
    policies = default_policy_definition
    if "policies" in data.keys():
        policies.update(data["policies"])

    contract_id = 1

    retries = Retry(total=5,
                    backoff_factor=0.1)
    session = requests.Session()
    session.mount('https://', HTTPAdapter(max_retries=retries))

    if policies:
        for policy in policies.keys():
            for url in edc_upload_urls:
                create_policy(policies[policy], url, edc_policy_path, headers_with_api_key, session)

    for tmp_data in testdata:
        catenax_id = tmp_data["catenaXId"]
        identification = uuid.uuid4().urn
        tmp_keys = tmp_data.keys()

        specific_asset_ids = [
        ]

        submodel_descriptors = []

        name_at_manufacturer = ""

        for tmp_key in tmp_keys:
            if "Batch" in tmp_key or "SerialPartTypization" in tmp_key:
                specific_asset_ids = copy(tmp_data[tmp_key][0]["localIdentifiers"])
                name_at_manufacturer = tmp_data[tmp_key][0]["partTypeInformation"]["nameAtManufacturer"].replace(" ",
                                                                                                                 "")
            if "PartAsPlanned" in tmp_key:
                name_at_manufacturer = tmp_data[tmp_key][0]["partTypeInformation"]["nameAtManufacturer"].replace(" ",
                                                                                                                 "")
                specific_asset_ids.append({
                    "value": tmp_data[tmp_key][0]["partTypeInformation"]["manufacturerPartId"],
                    "key": "manufacturerPartId"
                })
        print(name_at_manufacturer)

        specific_asset_ids.append({
            "key": "manufacturerId",
            "value": tmp_data["bpnl"]
        })

        esr = "urn:bamm:io.catenax.esr_certificates.esr_certificate_state_statistic:1.0.1#EsrCertificateStateStatistic"
        apr = "urn:bamm:io.catenax.assembly_part_relationship:1.1.1#AssemblyPartRelationship"
        if esr_url and apr in tmp_keys and "childParts" in tmp_data[apr][0] and tmp_data[apr][0]["childParts"]:
            tmp_data.update({esr: ""})

        policy_id = default_policy
        if "policy" in tmp_keys:
            policy_id = tmp_data["policy"]
            print("Policy: " + policy_id)

        for tmp_key in tmp_keys:
            if "PlainObject" not in tmp_key and "catenaXId" not in tmp_key and "bpn" not in tmp_key and "policy" not in tmp_key:
                # Prepare submodel endpoint address
                submodel_url = submodel_server_urls[contract_id % len(submodel_server_urls)]
                submodel_upload_url = submodel_server_upload_urls[contract_id % len(submodel_server_upload_urls)]
                edc_url = edc_urls[contract_id % len(edc_urls)]
                edc_upload_url = edc_upload_urls[contract_id % len(edc_upload_urls)]

                submodel_name = tmp_key[tmp_key.index("#") + 1: len(tmp_key)]
                submodel_identification = uuid.uuid4().urn
                semantic_id = tmp_key

                if is_ess and tmp_data["bpnl"] in bpnl_fail:
                    endpoint_address = "http://idonotexist/" + catenax_id + "-" + submodel_identification + "/submodel?content=value&extent=withBlobValue"
                elif submodel_name == "EsrCertificateStateStatistic" and esr_url is not None:
                    endpoint_address = esr_url + "/" + catenax_id + "/asBuilt/ISO14001/submodel"
                else:
                    endpoint_address = edc_url + "/" + catenax_id + \
                                       "-" + submodel_identification + "/submodel?content=value&extent=withBlobValue"

                descriptor = create_submodel_descriptor(submodel_name, submodel_identification, semantic_id,
                                                        endpoint_address)
                submodel_descriptors.append(json.loads(descriptor))

                edc_policy_id = str(uuid.uuid4())
                asset_prop_id = catenax_id + "-" + submodel_identification

                print("Create submodel on submodel server")
                if tmp_data[tmp_key] != "":
                    payload = create_submodel_payload(tmp_data[tmp_key][0])
                    response = session.request(method="POST",
                                               url=create_submodel_url(submodel_upload_url, submodel_identification),
                                               headers=headers, data=payload)
                    print_response(response)

                print("Create edc asset")
                if submodel_name == "EsrCertificateStateStatistic" and esr_url is not None:
                    payload = create_esr_edc_asset_payload(esr_url, asset_prop_id, catenax_id)
                else:
                    payload = create_edc_asset_payload(submodel_url, asset_prop_id, submodel_identification)
                response = session.request(method="POST", url=edc_upload_url + edc_asset_path,
                                           headers=headers_with_api_key,
                                           data=payload)
                print_response(response)

                print("Create edc contract definition")
                payload = create_edc_contract_definition_payload(policy_id, asset_prop_id)
                response = session.request(method="POST", url=edc_upload_url + edc_contract_definition_path,
                                           headers=headers_with_api_key,
                                           data=payload)
                print_response(response)
                contract_id = contract_id + 1

        if submodel_descriptors:
            print("Create aas shell")
            payload = create_aas_shell(catenax_id, name_at_manufacturer, identification, specific_asset_ids,
                                       submodel_descriptors)
            response = session.request(method="POST", url=create_digital_twin_payload_url(aas_url),
                                       headers=headers,
                                       data=payload)
            print_response(response)

    timestamp_end = time.time()
    duration = timestamp_end - timestamp_start
    print(f"Test data upload completed in {math.ceil(duration)} Seconds")
