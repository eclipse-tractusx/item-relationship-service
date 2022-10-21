# testing_utils.py

def checkResponse(response):
    tombstones_are_not_empty(response)
    relationships_are_not_empty(response)
    submodels_are_not_empty(response)


def tombstones_are_empty(response):
    print (response.json().get("tombstones"));
    print ("Check array is empty ", len(response.json().get("tombstones")))
    assert len(response.json().get("tombstones")) == 0
    


def tombstones_are_not_empty(response):

    print (response.json().get("tombstones"));
    print ("Check if tombstones are not empty number:", len(response.json().get("tombstones")))
    assert len(response.json().get("tombstones")) != 0

    # try:
    #     assert len(response.json().get("tombstones")) == 0
    # except TypeError as e:
    #     raise (
    #         "Tried to match a pykwalify schema against a non-json response"
    #     ) from e


def relationships_are_empty(response):

    print (response.json().get("relationships"));
    print ("Check if relationships are empty", len(response.json().get("relationships")))
    assert len(response.json().get("relationships")) == 0


def relationships_are_not_empty(response):

    print (response.json().get("relationships"));
    print ("Check if relationships are not empty", len(response.json().get("relationships")))
    assert len(response.json().get("relationships")) != 0


def submodels_are_empty(response):

    assert len(response.json().get("submodels")) == 0


def submodels_are_not_empty(response):

    assert len(response.json().get("submodels")) != 0