# testing_utils.py

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
            ["COMPLETED" in actual_status, "ERROR" in actual_status, "INITIAL" in actual_status, "CANCELED" in actual_status]
        )


def errors_for_unknown_requested_globalAssetId_are_correct(response):
    print(response.json().get("errors"))
    error_list = response.json().get("errors")
    assert 'No job exists with id bc1b4f4f-aa00-4296-8738-e7913c95f2d9' in error_list


def check_createdOn_bigger_startedOn(createdOn, startedOn):
    print(f"createdOn: {createdOn}")
    print(f"startedOn: {startedOn}")
    assert startedOn > createdOn

