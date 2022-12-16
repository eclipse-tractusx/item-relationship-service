# testing_utils.py
from datetime import datetime


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
    print(response.json().get("submodels"))
    print("Check if submodels are empty", len(response.json().get("submodels")))
    assert len(response.json().get("submodels")) == 0


def submodels_are_not_empty(response):
    print(response)
    print(response.json().get("submodels"))
    print("Check if submodels are not empty", len(response.json().get("submodels")))
    assert len(response.json().get("submodels")) != 0


def errors_for_invalid_globalAssetId_are_correct(response):
    print(response.json().get("errors"))
    error_list = response.json().get("errors")
    assert 'globalAssetId:size must be between 45 and 45' in error_list
    assert 'globalAssetId:must match \"^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$\"' in error_list


def errors_for_unknown_globalAssetId_are_correct(response):
    error_list = response.json().get("tombstones")
    print(error_list)

    for i in error_list:
        print("Given tombstone: -- ", i, " --")
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
        assert '404 : "{"error":{"message":"Shell for identifier urn:uuid:cce14502-958a-42e1-8bb7-f4f41aaaaaaa not found","path":"/registry/shell-descriptors/urn:uuid:cce14502-958a-42e1-8bb7-f4f41aaaaaaa","details":{}}}"' in processingErrorDetail
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
    print(response.json().get("errors"))
    error_list = response.json().get("errors")
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
    assert job_completed_timestamp > last_modified_on_timestamp


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