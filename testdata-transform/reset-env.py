#!/usr/bin/python

import argparse
import requests

if __name__ == "__main__":
    # -a "https://irs-aas-registry.dev.demo.catena-x.net/registry/shell-descriptors"
    # -e "https://irs-provider-controlplane.dev.demo.catena-x.net/data/contractdefinitions"
    # -k '123456'
    parser = argparse.ArgumentParser(description="Script to delete testdata from CX Environment.",
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("-a", "--aas", type=str, help="aas url", required=True)
    parser.add_argument("-e", "--edc", type=str, help="edc url", required=True)
    parser.add_argument("-k", "--apikey", type=str, help="edc api key", required=True)

    args = parser.parse_args()
    config = vars(args)
    edc_api_key = config.get("apikey")
    registry_url = config.get("aas") + "/registry/shell-descriptors"
    edc_url = config.get("edc")

    controlplane_data_contracts = edc_url + "/data/contractdefinitions"
    controlplane_data_policies = edc_url + "/data/policydefinitions"
    controlplane_data_assets = edc_url + "/data/assets"
    headers = {
        'X-Api-Key': edc_api_key,
        'Content-Type': 'application/json'
    }
    response = requests.request(method="GET", url=registry_url+"?pageSize=50", headers=headers)
    response_json = response.json()
    items = response_json["items"]
    for item in items:
        global_asset_id = item["globalAssetId"]["value"][0]
        delete_response = requests.request(method="DELETE", url=registry_url+"/"+global_asset_id, headers=headers)
        print(delete_response)
        if delete_response.status_code > 205:
            print(global_asset_id)

    response = requests.request(method="GET", url=controlplane_data_contracts, headers=headers)
    values = response.json()
    for value in values:
        uid_ = value["id"]
        delete_response = requests.request(method="DELETE", url=controlplane_data_contracts+"/"+uid_, headers=headers)
        print(delete_response)
        if delete_response.status_code > 205:
            print(uid_)

    response = requests.request(method="GET", url=controlplane_data_policies, headers=headers)
    values = response.json()
    for value in values:
        uid_ = value["id"]
        delete_response = requests.request(method="DELETE", url=controlplane_data_policies+"/"+uid_, headers=headers)
        print(delete_response)
        if delete_response.status_code > 205:
            print(uid_)

    response = requests.request(method="GET", url=controlplane_data_assets, headers=headers)
    values = response.json()
    for value in values:
        uid_ = value["properties"]["asset:prop:id"]
        delete_response = requests.request(method="DELETE", url=controlplane_data_assets+"/"+str(uid_), headers=headers)
        print(delete_response)
        if delete_response.status_code > 205:
            print(uid_)

