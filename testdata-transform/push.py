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
import requests


def create_submodel(host, json_payload, id):
    url = host + "/data/" + id
    print(url)
    payload = json.dumps(json_payload)
    headers = {
        'Content-Type': 'application/json'
    }
    response = requests.post(url=url, headers=headers, data=payload)
    print(response)


def create_digital_twin(host, json_payload):
    url = host + "/registry/shell-descriptors"
    print(url)
    payload = json.dumps(json_payload)
    headers = {
        'Content-Type': 'application/json'
    }
    response = requests.post(url=url, headers=headers, data=payload)
    print(response)


def create_edc_asset(url, digital_twin_id, digital_twin_submodel_id, api_key):
    payload = json.dumps({
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
    headers = {
        'X-Api-Key': api_key,
        'Content-Type': 'application/json'
    }
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response.text)


def create_edc_policy(url, edc_policy_id, digital_twin_id, digital_twin_submodel_id, api_key):
    payload = json.dumps({
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
    headers = {
        'X-Api-Key': api_key,
        'Content-Type': 'application/json'
    }
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response.text)


def create_edc_contract_definition(url, contract_id, edc_policy_id, api_key):
    payload = json.dumps({
        "id": contract_id,
        "accessPolicyId": edc_policy_id,
        "contractPolicyId": edc_policy_id,
        "criteria": []
    })
    headers = {
        'X-Api-Key': api_key,
        'Content-Type': 'application/json'
    }
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response.text)


if __name__ == "__main__":

    assemblyPart = "BPNL00000003AVTH/assemblyPartRelationship.json"
    serialPart = "BPNL00000003AVTH/serialPartTypization.json"
    aas = "BPNL00000003AVTH/aas.json"
    submodel_server1_url = "http://localhost:8194"
    aas_url = "http://localhost:4243"

    edc_asset_url = "http://localhost:8187/api/v1/data/assets"
    edc_policy_url = "http://localhost:8187/api/v1/data/policies"
    edc_contract_definition_url = "http://localhost:8187/api/v1/data/contractdefinitions"
    edc_api_key = '123456'

    f = open(assemblyPart, "r")
    data = json.load(f)
    for json_object in data:
        create_submodel(submodel_server1_url, json_object, json_object["catenaXId"])
        # print(json_object)

    f = open(serialPart, "r")
    data = json.load(f)
    for json_object in data:
        create_submodel(submodel_server1_url, json_object, json_object["catenaXId"])
        # print(json_object)

    f = open(aas, "r")
    data = json.load(f)
    for json_object in data:
        create_digital_twin(aas_url, json_object)
        # print(json_object)

    create_edc_asset(url=edc_asset_url, digital_twin_id="", digital_twin_submodel_id="", api_key=edc_api_key)
    create_edc_policy(url=edc_policy_url, edc_policy_id="", digital_twin_id="", digital_twin_submodel_id="",
                      api_key=edc_api_key)
    create_edc_contract_definition(url=edc_contract_definition_url, contract_id="", edc_policy_id="",
                                   api_key=edc_api_key)
