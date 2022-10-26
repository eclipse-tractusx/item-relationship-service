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
    print(response.json().get("submodels"))
    print("Check if submodels are not empty", len(response.json().get("submodels")))
    assert len(response.json().get("submodels")) != 0


def errors_for_globalAssetId_are_correct(response):
    print(response.json().get("errors"))
    error_list = response.json().get("errors")
    assert 'globalAssetId:size must be between 45 and 45' in error_list
    assert 'globalAssetId:must match \"^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$\"' in error_list
