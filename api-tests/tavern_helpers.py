# testing_utils.py
def tombstones_are_empty(response):
    """check array is empty"""
    assert len(response.json().get("tombstones")) == 0


def tombstones_are_not_empty(response):
    """check array is empty"""
    assert len(response.json().get("tombstones")) != 0

    # try:
    #     assert len(response.json().get("tombstones")) == 0
    # except TypeError as e:
    #     raise (
    #         "Tried to match a pykwalify schema against a non-json response"
    #     ) from e


def relationships_are_empty(response):
    """check array is empty
    """
    assert len(response.json().get("relationships")) == 0


def relationships_are_not_empty(response):
    """check array is empty
    """
    assert len(response.json().get("relationships")) != 0


def submodels_are_empty(response):
    """check array is empty
    """
    assert len(response.json().get("submodels")) == 0


def submodels_are_not_empty(response):
    """check array is empty
    """
    assert len(response.json().get("submodels")) != 0