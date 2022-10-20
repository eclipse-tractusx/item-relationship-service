# testing_utils.py
def array_is_empty(response):
    """check array is empty
    """
    # assert len(response.json().get("tombstones")) != 0

    try:
        assert len(response.json().get("tombstones")) == 0
    except TypeError as e:
        raise (
            "Tried to match a pykwalify schema against a non-json response"
        ) from e
