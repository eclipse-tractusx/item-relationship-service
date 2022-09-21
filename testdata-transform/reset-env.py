#!/usr/bin/python

import argparse
import requests
from requests.adapters import HTTPAdapter, Retry


def delete_shells(url_, headers_):
    while session.request(method="GET", url=url_ + "?pageSize=50", headers=headers_).json()["items"]:
        response = session.request(method="GET", url=url_ + "?pageSize=50", headers=headers_)
        response_json = response.json()
        items = response_json["items"]
        for item in items:
            global_asset_id = item["identification"]
            delete_response = session.request(method="DELETE", url=url_ + "/" + global_asset_id, headers=headers_)
            print(delete_response)
            if delete_response.status_code > 205:
                print(global_asset_id)


def delete_(url_, headers_):
    response = session.request(method="GET", url=url_, headers=headers_)
    values = response.json()
    for value in values:
        uid_ = value["id"]
        delete_response = session.request(method="DELETE", url=url_ + "/" + uid_,
                                          headers=headers_)
        print(delete_response)
        if delete_response.status_code > 205:
            print(uid_)


def delete_contracts(url_, headers_):
    while session.request(method="GET", url=url_, headers=headers_).json():
        delete_(url_, headers_)


def delete_policies_and_assets(policy_url_, asset_url_, headers_):
    while session.request(method="GET", url=policy_url_, headers=headers_).json():
        delete_(policy_url_, headers_)

        response = session.request(method="GET", url=asset_url_, headers=headers_)
        values = response.json()
        for value in values:
            uid_ = value["properties"]["asset:prop:id"]
            delete_response = session.request(method="DELETE", url=asset_url_ + "/" + str(uid_),
                                              headers=headers_)
            print(delete_response)
            if delete_response.status_code > 205:
                print(uid_)


if __name__ == "__main__":
    # -a "https://irs-aas-registry.dev.demo.catena-x.net/registry/shell-descriptors"
    # -e1 "https://irs-provider-controlplane.dev.demo.catena-x.net/data/contractdefinitions"
    # -e2 "https://irs-provider-controlplane2.dev.demo.catena-x.net/data/contractdefinitions"
    # -e3 "https://irs-provider-controlplane3.dev.demo.catena-x.net/data/contractdefinitions"
    # -k '123456'
    parser = argparse.ArgumentParser(description="Script to delete testdata from CX Environment.",
                                     formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument("-a", "--aas", type=str, help="aas url", required=True)
    parser.add_argument("-e1", "--edc1", type=str, help="Public EDC provider control plane url 1", required=True)
    parser.add_argument("-e2", "--edc2", type=str, help="Public EDC provider control plane url 2", required=True)
    parser.add_argument("-e3", "--edc3", type=str, help="Public EDC provider control plane url 3", required=True)
    parser.add_argument("-k", "--apikey", type=str, help="edc api key", required=True)

    args = parser.parse_args()
    config = vars(args)
    edc_api_key = config.get("apikey")
    registry_url = config.get("aas") + "/registry/shell-descriptors"
    edc_urls = [config.get("edc1"), config.get("edc2"), config.get("edc3")]

    headers = {
        'X-Api-Key': edc_api_key,
        'Content-Type': 'application/json'
    }

    retries = Retry(total=5,
                    backoff_factor=0.1)
    session = requests.Session()
    session.mount('https://', HTTPAdapter(max_retries=retries))

    delete_shells(registry_url, headers)

    for edc_url in edc_urls:
        controlplane_data_contracts = edc_url + "/data/contractdefinitions"
        controlplane_data_policies = edc_url + "/data/policydefinitions"
        controlplane_data_assets = edc_url + "/data/assets"

        delete_contracts(controlplane_data_contracts, headers)

        delete_policies_and_assets(controlplane_data_policies, controlplane_data_assets, headers)
