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
    print(response.json().get("relationships"))
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


def errors_for_globalAssetId_are_correct(response):
    print(response.json().get("errors"))
    error_list = response.json().get("errors")
    assert 'globalAssetId:size must be between 45 and 45' in error_list
    assert 'globalAssetId:must match \"^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$\"' in error_list


def status_of_jobs_are_as_expected(response, expected_status):
    print(response.json())
    for i in response.json():
        actual_status = i.get("status")
        print(f"Asserting expected status '{expected_status}' to be equal to actual status '{actual_status}'")
        assert expected_status in actual_status


def status_of_all_jobs_are_given(response):
    print(response.json())
    for i in response.json():
        actual_status = i.get("status")
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
    print(f"jobCompleted: {response.json().get('job').get('jobCompleted')}")

    created_on_timestamp = datetime.strptime(response.json().get('job').get('createdOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    started_on_timestamp = datetime.strptime(response.json().get('job').get('startedOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    last_modified_on_timestamp = datetime.strptime(response.json().get('job').get('lastModifiedOn')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    job_completed_timestamp = datetime.strptime(response.json().get('job').get('jobCompleted')[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
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
    for i in response.json():
        startedOn = i.get("startedOn")
    print("Check if startedOn timestamp is existing.")
    assert startedOn is not None


def check_jobCompleted_timestamp_not_exists(response):
    for i in response.json():
        jobCompleted = i.get("jobCompleted")
    print("Check if jobCompleted timestamp is missing.")
    assert jobCompleted is None


def check_jobCompleted_timestamp_exists(response):
    for i in response.json():
        jobCompleted = i.get("jobCompleted")
    print("Check if jobCompleted timestamp is existing.")
    assert jobCompleted is not None



def check_startedOn_is_smaller_than_jobCompleted(response):
    for i in response.json():
        jobCompleted = i.get("jobCompleted")
        startedOn = i.get("startedOn")

        jobCompleted_timestamp = datetime.strptime(i.get("jobCompleted")[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
        startedOn_timestamp = datetime.strptime(i.get("createdOn")[:26], '%Y-%m-%dT%H:%M:%S.%f').timestamp()
    print(f"Check if startedOn timestamp '{startedOn}' is smaller than jobCompleted timestamp '{jobCompleted}'")
    assert startedOn_timestamp > jobCompleted_timestamp
