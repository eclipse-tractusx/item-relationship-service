#  Copyright (c) 2021,2022,2023
#        2022: ZF Friedrichshafen AG
#        2022: ISTOS GmbH
#        2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
#        2022,2023: BOSCH AG
#  Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
#
#  See the NOTICE file(s) distributed with this work for additional
#  information regarding copyright ownership.
#
#  This program and the accompanying materials are made available under the
#  terms of the Apache License, Version 2.0 which is available at
#  https://www.apache.org/licenses/LICENSE-2.0.
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations
#  under the License.
#
#  SPDX-License-Identifier: Apache-2.0

import json
import logging
import time
from types import SimpleNamespace

import requests
from requests.adapters import HTTPAdapter, Retry
from requests.auth import HTTPBasicAuth
from requests_oauthlib import OAuth2Session
from oauthlib.oauth2 import BackendApplicationClient


def get_semantic_ids_from_twin(digital_twin_):
    semantic_ids_ = []
    for submodel_descriptor in digital_twin_.submodelDescriptors:
        keys = submodel_descriptor.semanticId.keys
        for key in keys:
            semantic_ids_.append(key.value)
    return semantic_ids_


def get_bpn_from_twin(digital_twin_):
    bpn_ = None
    for specific_asset_id in digital_twin_.specificAssetIds:
        if "manufacturerId" == specific_asset_id.name:
            bpn_ = specific_asset_id.value
    return bpn_


def fetch_all_registry_data(registry_url_, bpn_):
    twins_ = []
    cursor = None
    while True:
        params = {'cursor': cursor} if cursor else None
        registry_response = fetch_registry_data(registry_url_, bpn_, params)

        if not registry_response:
            logging.info(f"No registry response: '{registry_response}'")
            break

        result = registry_response.result
        logging.info(f"Adding {len(result)} twins to result list.")
        twins_.extend(result)

        if hasattr(registry_response.paging_metadata, 'cursor'):
            cursor = registry_response.paging_metadata.cursor
            logging.info(f"Setting cursor: '{cursor}'")
        else:
            logging.info("No cursor found.")
            break
    logging.info(f"Returning {len(twins_)} twins.")
    return twins_


def fetch_registry_data(registry_url_, bpn_, params_=None):
    headers_ = {"Edc-Bpn": bpn_}
    response_ = session.get(registry_url_, headers=headers_, params=params_)
    if response_.status_code == 200:
        registry_response = json.loads(response_.text, object_hook=lambda d: SimpleNamespace(**d))
        return registry_response
    else:
        logging.error(f"Failed to fetch registry data. Status code: {response_.status_code}")
        return None


def filter_for_as_planned_and_bpn(search_bpn_):
    filtered_twins_ = []
    for twin in digital_twins:
        semantic_ids = get_semantic_ids_from_twin(twin)
        if any("AsPlanned" in x for x in semantic_ids):
            global_asset_id = twin.globalAssetId
            bpn = get_bpn_from_twin(twin)
            if bpn in search_bpn_:
                logging.info(global_asset_id)
                logging.info(bpn)
                x = {
                    "globalAssetId": global_asset_id,
                    "bpn": bpn
                }
                filtered_twins_.append(x)
    return filtered_twins_


def poll_batch_job(batch_url, token_):
    header_ = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token_['access_token']}"
    }
    while True:
        try:
            response_ = oauth.get(batch_url, headers=header_)
            response_json = response_.json()
            logging.info(response_json)

            state = response_json.get("state")
            if state in ("COMPLETED", "ERROR", "CANCELED"):
                logging.info(f"Batch completed in state '{state}'")
                return response_json

            time.sleep(5)
        except requests.exceptions.RequestException as e:
            logging.error(f"Error: {e}")
            break


def get_oauth_token(token_url_, client_id_, client_secret_):
    client = BackendApplicationClient(client_id=client_id_)
    oauth_ = OAuth2Session(client=client)
    token_ = oauth_.fetch_token(token_url=token_url_, auth=HTTPBasicAuth(client_id_, client_secret_))["access_token"]
    return oauth_, token_


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, format='%(asctime)s [%(levelname)-5.5s] %(message)s')
    retries = Retry(total=3,
                    backoff_factor=0.1)
    session = requests.Session()
    session.mount('https://', HTTPAdapter(max_retries=retries))

    # TODO Args: -debug for "debug" instead of "info" logs
    # TODO params for registry url, requestor BPN, filter BPN

    # TODO eval, if it makes sense to filter via lookup/shells endpoint or filter by getting the entire list of twins

    # Fetch all digital Twins from the DTR
    digital_twins = fetch_all_registry_data(registry_url, own_BPN)
    # print(digital_twins)

    # Filter for bomLifecycle "asPlanned" and the provided BPN
    filtered_twins = filter_for_as_planned_and_bpn(search_BPN)

    logging.info(f"Found {len(filtered_twins)} twin(s) after filtering.")
    logging.info(filtered_twins)

    # Start IRS batch job

    oauth, token = get_oauth_token(oauth_url, client_id, client_secret)
    logging.info(token)

    # Start IRS batch job
    payload = {
        "batchSize": 10,
        "batchStrategy": "PRESERVE_BATCH_JOB_ORDER",
        "incidentBPNSs": [incident_BPN],
        "keys": filtered_twins
    }

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    logging.info(payload)
    response = oauth.post(url=irs_ess_url, json=payload, headers=headers)
    logging.info(response.json())

    if response.status_code != 201:
        logging.error(f"Failed to start IRS Batch Job. Status code: {response.status_code}")
        raise Exception("Failed to start IRS Batch job")
    else:
        batch_id = response.json().get("id")

    irs_batch_url = f"{irs_ess_url}/{batch_id}"
    completed_batch = poll_batch_job(irs_batch_url, token)
    for batch in completed_batch:
        logging.info(batch.get("id"))
        logging.info(batch.get("state"))
        url = batch.get("batchUrl")
        logging.info(url)
        response = oauth.get(url, headers=headers)
        print(response.text)

    # TODO better formatted batch polling
    # TODO Get jobs from batch
    # TODO Show result
