# testing_utils.py

def supplyChainImpacted_is_correct_in_submodels_for_valid_ID(response):
    submodels = response.json().get("submodels")
    print("submodels ", submodels)
    assert len(submodels) <= 1
    for i in submodels:
        assert 'supply_chain_impacted' in i.get('aspectType')
        assert 'Yes' in i.get("payload").get('supplyChainImpacted')


def supplyChainImpacted_is_correct_in_submodels_for_valid_ID_in_unknown_BPN(response):
    submodels = response.json().get("submodels")
    print("submodels ", submodels)
    assert len(submodels) <= 1
    for i in submodels:
        assert 'supply_chain_impacted' in i.get('aspectType')
        assert 'No' in i.get("payload").get('supplyChainImpacted')


def supplyChainImpacted_is_correct_in_submodels_for_unknown_ID(response):
    submodels = response.json().get("submodels")
    print("submodels ", submodels)
    assert len(submodels) <= 1
    for i in submodels:
        assert 'supply_chain_impacted' in i.get('aspectType')
        assert 'Unknown' in i.get("payload").get('supplyChainImpacted')


def errors_for_invalid_investigation_request_are_correct(response):
    print(response.json().get("messages"))
    error_list = response.json().get("messages")
    assert 'incidentBpns:must not be empty' in error_list
    assert 'globalAssetId:must not be blank' in error_list


def relationships_for_BPN_investigations_contains_several_childs(response):
    relationships = response.json().get("relationships")
    print("relationships: ", relationships)
    print("Länge: ", len(relationships))
    assert len(relationships) > 1