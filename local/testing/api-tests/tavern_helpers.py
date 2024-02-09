from datetime import datetime

import os
from box import Box


def supplyChainImpacted_is_as_expected(response, expectedSupplyChainImpacted):
    submodels = response.json().get("submodels")
    print("submodels ", submodels)
    assert len(submodels) <= 1
    for i in submodels:
        assert 'supply_chain_impacted' in i.get('aspectType')
        assert expectedSupplyChainImpacted in i.get("payload").get('supplyChainImpacted')


def supplyChainFirstLevelBpn_is_as_expected(response, expectedBpnl):
    submodels = response.json().get("submodels")
    for i in submodels:
        assert expectedBpnl in i.get("payload").get("impactedSuppliersOnFirstTier").get("bpnl")


def supplyChainhops_is_as_expected(response, expectedHops):
    submodels = response.json().get("submodels")
    for i in submodels:
        assert expectedHops is i.get("payload").get("impactedSuppliersOnFirstTier").get("hops")


def errors_for_invalid_investigation_request_are_correct(response):
    print(response.json().get("messages"))
    error_list = response.json().get("messages")
    assert 'incidentBPNSs:must not be empty' in error_list
    assert 'key.globalAssetId:must not be blank' in error_list
    assert 'key.bpn:must not be blank' in error_list


def relationships_for_BPN_investigations_contains_several_childs(response):
    relationships = response.json().get("relationships")
    print("relationships: ", relationships)
    print("Länge: ", len(relationships))
    assert len(relationships) != 0


def ESS_job_parameter_are_as_requested(response):
    print("Check if ESS-job parameter are as requested:")
    parameter = response.json().get('job').get('parameter')
    print(parameter)
    assert parameter.get('bomLifecycle') == 'asPlanned'
    assert parameter.get('collectAspects') is True
    assert parameter.get('depth') == 1
    assert parameter.get('direction') == 'downward'
    assert parameter.get('lookupBPNs') is False
    #assert parameter.get('callbackUrl') == 'https://www.check123.com'
    aspects_list = parameter.get("aspects")
    assert 'PartSiteInformationAsPlanned' in aspects_list
    assert 'PartAsPlanned' in aspects_list


def tombstone_for_EssValidation_are_correct(response, expectedTombstone):
    error_list = response.json().get("tombstones")

    for i in error_list:
        print("Given tombstone: ", i)
        catenaXId = i.get("catenaXId")
        print("Given catenaXID: ", catenaXId)
        processingErrorStep = i.get("processingError").get("processStep")
        print("Processstep in ProcessingError: ", processingErrorStep)
        processingErrorDetail = i.get("processingError").get("errorDetail")
        print("ErrorMessage: ", processingErrorDetail)
        processingErrorLastAttempt = i.get("processingError").get("lastAttempt")
        print("LastAttempt: ", processingErrorLastAttempt)
        processingErrorRetryCounter = i.get("processingError").get("retryCounter")
        print("RetryCounter: ", processingErrorRetryCounter)
        assert 'EssValidation' in processingErrorStep
        assert expectedTombstone in processingErrorDetail
        assert processingErrorLastAttempt is not None
        assert 0 is processingErrorRetryCounter


############################## /\ ESS helpers /\ ##############################


def checkResponse(response):
    tombstones_are_not_empty(response)
    relationships_are_not_empty(response)
    submodels_are_not_empty(response)


def tombstones_are_empty(response):
    print(response.json().get("tombstones"))
    print("Check array is empty ", len(response.json().get("tombstones")))
    assert len(response.json().get("tombstones")) == 0


def tombstones_are_not_empty(response):
    print(response.json().get("tombstones"))
    print("Check if tombstones are not empty number:", len(response.json().get("tombstones")))
    assert len(response.json().get("tombstones")) != 0


def relationships_are_empty(response):
    print("Check if relationships are empty", len(response.json().get("relationships")))
    assert len(response.json().get("relationships")) == 0


def relationships_are_not_empty(response):
    print(response.json().get("relationships"))
    print("Check if relationships are not empty", len(response.json().get("relationships")))
    assert len(response.json().get("relationships")) != 0


def submodels_are_empty(response):
    print("Check if submodels are empty", len(response.json().get("submodels")))
    assert len(response.json().get("submodels")) == 0


def submodels_are_not_empty(response):
    print("Check if submodels are not empty", len(response.json().get("submodels")))
    assert len(response.json().get("submodels")) != 0


def errors_for_invalid_globalAssetId_are_correct(response):
    print(response.json().get("messages"))
    error_list = response.json().get("messages")
    assert 'key.globalAssetId:size must be between 45 and 45' in error_list
    assert 'key.globalAssetId:must match \"^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$\"' in error_list


def errors_for_invalid_depth_are_correct(response):
    print(response.json().get("messages"))
    error_list = response.json().get("messages")
    assert 'depth:must be greater than or equal to 1' in error_list


def submodelDescriptors_in_shells_are_empty(response):
    shells = response.json().get("shells")
    print("shells ", shells)
    for i in shells:
        assert len(i.get("payload").get("submodelDescriptors")) == 0


def aspects_in_job_parameter_are_empty(response):
    parameter = response.json().get('job').get('parameter')
    print(parameter)
    assert parameter.get('collectAspects') is True
    assert len(parameter.get("aspects")) == 0


def errors_for_unknown_globalAssetId_are_correct(response):
    error_list = response.json().get("tombstones")

    for i in error_list:
        print("Given tombstone: ", i)
        catenaXId = i.get("catenaXId")
        print("Given catenaXID: ", catenaXId)
        processingErrorStep = i.get("processingError").get("processStep")
        print("Processstep in ProcessingError: ", processingErrorStep)
        processingErrorDetail = i.get("processingError").get("errorDetail")
        print("ErrorMessage: ", processingErrorDetail)
        processingErrorLastAttempt = i.get("processingError").get("lastAttempt")
        print("LastAttempt: ", processingErrorLastAttempt)
        processingErrorRetryCounter = i.get("processingError").get("retryCounter")
        print("RetryCounter: ", processingErrorRetryCounter)
        assert 'urn:uuid:cce14502-958a-42e1-8bb7-f4f41aaaaaaa' in catenaXId
        assert 'DigitalTwinRequest' in processingErrorStep
        #assert 'Shell for identifier urn:uuid:cce14502-958a-42e1-8bb7-f4f41aaaaaaa not found' in processingErrorDetail ##commented out since this error message is not possible currently after DTR changes
        assert processingErrorLastAttempt is not None
        assert 3 is processingErrorRetryCounter


def status_of_jobs_are_as_expected(response, expected_status):
    job_list = response.json().get("content")
    print("Job Liste:  ", job_list)
    for i in job_list:
        actual_status = i.get("state")
        print(f"Asserting expected status '{expected_status}' to be equal to actual status '{actual_status}'")
        assert expected_status in actual_status


def status_of_all_jobs_are_given(response):
    job_list = response.json().get("content")
    print("Job Liste:  ", job_list)
    for i in job_list:
        actual_status = i.get("state")
        assert any(
            ["COMPLETED" in actual_status, "ERROR" in actual_status, "INITIAL" in actual_status, "CANCELED" in actual_status, "RUNNING" in actual_status]
        )


def errors_for_unknown_requested_globalAssetId_are_correct(response):
    print(response.json().get("messages"))
    error_list = response.json().get("messages")
    assert 'No job exists with id bc1b4f4f-aa00-4296-8738-e7913c95f2d9' in error_list


def check_timestamps_for_completed_jobs(response):
    print(f"createdOn: {response.json().get('job').get('createdOn')}")
    print(f"startedOn: {response.json().get('job').get('startedOn')}")
    print(f"lastModifiedOn: {response.json().get('job').get('lastModifiedOn')}")
    print(f"completedOn: {response.json().get('job').get('completedOn')}")

    created_on_timestamp = datetime.strptime(response.json().get('job').get('createdOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    started_on_timestamp = datetime.strptime(response.json().get('job').get('startedOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    last_modified_on_timestamp = datetime.strptime(response.json().get('job').get('lastModifiedOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    job_completed_timestamp = datetime.strptime(response.json().get('job').get('completedOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    assert started_on_timestamp > created_on_timestamp
    assert last_modified_on_timestamp > started_on_timestamp
    assert job_completed_timestamp >= last_modified_on_timestamp


def check_timestamps_for_not_completed_jobs(response):
    print(f"createdOn: {response.json().get('job').get('createdOn')}")
    print(f"startedOn: {response.json().get('job').get('startedOn')}")
    print(f"lastModifiedOn: {response.json().get('job').get('lastModifiedOn')}")

    created_on_timestamp = datetime.strptime(response.json().get('job').get('createdOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    started_on_timestamp = datetime.strptime(response.json().get('job').get('startedOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    last_modified_on_timestamp = datetime.strptime(response.json().get('job').get('lastModifiedOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    assert started_on_timestamp > created_on_timestamp
    assert last_modified_on_timestamp > started_on_timestamp


def check_startedOn_timestamp_exists(response):
    response_list = response.json().get("content")
    for i in response_list:
        startedOn = i.get("startedOn")
        print("Check if startedOn timestamp is existing.")
        assert startedOn is not None


def check_completedOn_timestamp_not_exists(response):
    response_list = response.json().get("content")
    for i in response_list:
        completedOn = i.get("completedOn")
        print("Check if completedOn timestamp is missing.")
        assert completedOn is None


def check_completedOn_timestamp_exists(response):
    response_list = response.json().get("content")
    for i in response_list:
        completedOn = i.get("completedOn")
        print("Check if completedOn timestamp is existing.")
        assert completedOn is not None


def check_startedOn_is_smaller_than_completedOn(response):
    response_list = response.json().get("content")
    for i in response_list:
        completedOn_timestamp = datetime.strptime(i.get("completedOn")[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
        startedOn_timestamp = datetime.strptime(i.get("startedOn")[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
        print(f"Check if startedOn timestamp '{startedOn_timestamp}' is smaller than completedOn timestamp '{completedOn_timestamp}'")
        assert startedOn_timestamp < completedOn_timestamp


def check_pagination_details_exists(response):
    response_list = response.json()
    assert response_list.get("pageNumber") == 0
    assert response_list.get("pageCount") > 0
    assert response_list.get("pageSize") == 20
    assert response_list.get("totalElements") > 0


def check_pagination_is_requested_correctly(response):
    response_list = response.json()
    assert response_list.get("pageNumber") == 1
    assert response_list.get("pageCount") > 0
    assert response_list.get("pageSize") == 3
    assert response_list.get("totalElements") > 0
    assert len(response.json().get("content")) == 3


def bpns_are_empty(response):
    print(response.json().get("bpns"))
    print("Check if bpns are empty", len(response.json().get("bpns")))
    assert len(response.json().get("bpns")) == 0


def bpns_are_not_empty(response):
    print(response.json().get("bpns"))
    print("Check if bpns are not empty number:", len(response.json().get("bpns")))
    assert len(response.json().get("bpns")) != 0


def summary_for_bpns_is_given(response):
    print("Check if summary for bpns is given:")
    bpnLookups = response.json().get('job').get('summary').get('bpnLookups')
    print(bpnLookups)
    print("completed:  ", bpnLookups.get('completed'))
    assert bpnLookups.get('completed') != 0
    assert bpnLookups.get('failed') == 0


def errors_for_invalid_batchSize_are_correct(response):
    print(response.json().get("messages"))
    error_list = response.json().get("messages")
    assert 'batchSize:Batch size value must be mod 10 compliant' in error_list
    assert 'batchSize:must be greater than or equal to 10' in error_list


def order_informations_for_batchprocessing_are_given(response, amount_batches):
    print(response.json())

    assert response.json().get("orderId") is not None
    assert response.json().get("state") == 'INITIALIZED'
    assert response.json().get("batchChecksum") == amount_batches

    batches_list = response.json().get("batches")
    assert len(batches_list) == amount_batches
    for batches in batches_list:
        assert batches.get("batchId") is not None
        assert batches.get("batchNumber") is not None
        assert batches.get("jobsInBatchChecksum") is not None
        assert ('https://irs.dev.demo.catena-x.net/irs/orders' in batches.get("batchUrl")) or ('https://irs.int.demo.catena-x.net/irs/orders' in batches.get("batchUrl"))
        assert batches.get("batchProcessingState") == 'INITIALIZED'
        assert batches.get("errors") is None


def getBatchId(response, batchId_number):
    batches_list = response.json().get("batches")
    batchId_list = []
    for batches in batches_list:
        batchId_list.append(batches.get("batchId"))
    return Box({"batch_id": batchId_list[batchId_number]})


def check_batches_are_canceled_correctly(response):
    jobs_list = response.json().get("jobs")
    for jobs in jobs_list:
        assert jobs.get("state") == 'CANCELED'


def job_parameter_are_as_requested(response):
    print("Check if job parameter are as requested:")
    parameter = response.json().get('job').get('parameter')
    print(parameter)
    assert parameter.get('bomLifecycle') == 'asPlanned'
    assert parameter.get('collectAspects') is True
    assert parameter.get('depth') == 2
    assert parameter.get('direction') == 'downward'
    assert parameter.get('lookupBPNs') is True
    assert parameter.get('callbackUrl') == 'https://www.check123.com'
    aspects_list = parameter.get("aspects")
    assert 'SerialPart' in aspects_list
    assert 'PartAsPlanned' in aspects_list


def create_api_key():
    api_key = os.getenv('ADMIN_USER_API_KEY')

    return {"X-API-KEY": api_key}


def create_api_key_ess():
    api_key = os.getenv('ADMIN_USER_API_KEY_ESS')

    return {"X-API-KEY": api_key}


def contractAgreementId_in_shells_existing(response):
    shells = response.json().get("shells")
    print("shells ", shells)
    assert len(shells) >= 1
    for i in shells:
        assert i.get("contractAgreementId") is not None


def contractAgreementId_in_submodels_existing(response):
    submodels = response.json().get("submodels")
    print("submodels ", submodels)
    assert len(submodels) >= 1
    for i in submodels:
        assert i.get("contractAgreementId") is not None


def contractAgreementId_in_shells_not_existing(response):
    shells = response.json().get("shells")
    print("shells ", shells)
    assert len(shells) >= 1
    for i in shells:
        assert i.get("contractAgreementId") is None


def contractAgreementId_in_submodels_not_existing(response):
    submodels = response.json().get("submodels")
    print("submodels ", submodels)
    assert len(submodels) >= 1
    for i in submodels:
        assert i.get("contractAgreementId") is None
