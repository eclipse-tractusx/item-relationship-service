#!/usr/bin/python

import requests

if __name__ == "__main__":
    registry_url = "https://irs-aas-registry.dev.demo.catena-x.net/registry/shell-descriptors"
    controlplane_data_contracts = "https://irs-provider-controlplane.dev.demo.catena-x.net/data/contractdefinitions"
    controlplane_data_policies = "https://irs-provider-controlplane.dev.demo.catena-x.net/data/policydefinitions"
    controlplane_data_assets = "https://irs-provider-controlplane.dev.demo.catena-x.net/data/assets"
    edc_api_key = '123456'
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
        print(global_asset_id)
        print(delete_response)
        print(delete_response.text)

    response = requests.request(method="GET", url=controlplane_data_contracts, headers=headers)
    values = response.json()
    for value in values:
        uid_ = value["id"]
        delete_response = requests.request(method="DELETE", url=controlplane_data_contracts+"/"+uid_, headers=headers)
        print(uid_)
        print(delete_response)

    response = requests.request(method="GET", url=controlplane_data_policies, headers=headers)
    values = response.json()
    for value in values:
        uid_ = value["uid"]
        delete_response = requests.request(method="DELETE", url=controlplane_data_policies+"/"+uid_, headers=headers)
        print(uid_)
        print(delete_response)

    response = requests.request(method="GET", url=controlplane_data_assets, headers=headers)
    values = response.json()
    for value in values:
        uid_ = value["properties"]["asset:prop:id"]
        delete_response = requests.request(method="DELETE", url=controlplane_data_assets+"/"+str(uid_), headers=headers)
        print(uid_)
        print(delete_response)

