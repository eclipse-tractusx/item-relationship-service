import uuid
import requests
import json


def create_asset(edc_policy_id, submodel_id, digital_twin_id, contract_id, submodel_object, aas_shell,
                 submodel_server_data_url, edc_api_key, digital_twin_url):
    dt_response = requests.request("GET", digital_twin_url)
    # print(response1.status_code)
    if dt_response.status_code == 200:
        print("Successful")
    elif dt_response.status_code == 404:
        print("Not Found, creating new")
        aas_shell.get

    url = (submodel_server_data_url + "/%s") % submodel_id
    payload = json.dumps(submodel_object)
    headers = {
        # 'Authorization': 'Basic c29tZXVzZXI6c29tZXBhc3N3b3Jk',
        'Content-Type': 'application/json'
    }
    print(url)
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response)

    url = "http://localhost:8187/api/v1/data/assets"
    payload = json.dumps({
        "asset": {
            "properties": {
                "asset:prop:id": ((digital_twin_id + "-%s") % submodel_id),
                "asset:prop:name": "product description",
                "asset:prop:contenttype": "application/json",
                "asset:prop:policy-id": "use-eu"
            }
        },
        "dataAddress": {
            "properties": {
                "endpoint": ("http://provider-backend-service:8080/data/%s" % submodel_id),
                "type": "HttpData"
            }
        }
    })
    headers = {
        'X-Api-Key': edc_api_key,
        'Content-Type': 'application/json'
        # 'Authorization': 'Basic c29tZXVzZXI6c29tZXBhc3N3b3Jk'
    }
    print(url)
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response)

    url = "http://localhost:8187/api/v1/data/policies"
    payload = json.dumps({
        "uid": edc_policy_id,
        "permissions": [
            {
                "target": ((digital_twin_id + "-%s") % submodel_id),
                "action": {
                    "type": "USE"
                },
                "edctype": "dataspaceconnector:permission"
            }
        ],
        "@type": {
            "@policytype": "set"
        }
    })
    headers = {
        'X-Api-Key': edc_api_key,
        'Content-Type': 'application/json'
        # 'Authorization': 'Basic c29tZXVzZXI6c29tZXBhc3N3b3Jk'
    }
    print(url)
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response)

    url = "http://localhost:8187/api/v1/data/contractdefinitions"
    payload = json.dumps({
        "id": ("%s" % contract_id),
        "accessPolicyId": edc_policy_id,
        "contractPolicyId": edc_policy_id,
        "criteria": []
    })
    headers = {
        'X-Api-Key': edc_api_key,
        'Content-Type': 'application/json'
        # 'Authorization': 'Basic c29tZXVzZXI6c29tZXBhc3N3b3Jk'
    }
    print(url)
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response)

    url = "http://localhost:4243/registry/shell-descriptors"
    payload = json.dumps(aas_shell)
    headers = {
        # 'Authorization': 'Basic c29tZXVzZXI6c29tZXBhc3N3b3Jk',
        'Content-Type': 'application/json'
    }
    print(url)
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response)


if __name__ == "__main__":
    submodel_server_data_url = "http://localhost:8194/data"
    digital_twin_url = "http://localhost:4243/registry/shell-descriptors/"

    submodel1 = {
        "performanceIndicator": {
            "electricCapacityMin": 1.7976931348623155e+308,
            "electricCapacityMax": 1.7976931348623155e+308
        },
        "minimalStateOfHealth": {
            "minimalStateOfHealthValue": 1.7976931348623155e+308,
            "specificatorId": "eOMtThyhVNLWUZNRcBaQKxI",
            "minimalStateOfHealthPhase": "as specified by OEM"
        },
        "type": "HVB",
        "currentStateOfHealth": [
            {
                "currentStateOfHealthPhase": "as specified by OEM",
                "currentStateOfHealthTimestamp": "2",
                "currentStateOfHealthValue": 1.7976931348623155e+308
            }
        ]
    }
    submodel2 = {
        "localIdentifiers": [
            {
                "value": "BPNL00000003B0Q0",
                "key": "ManufacturerID"
            },
            {
                "value": "01697F7-65",
                "key": "ManufacturerPartID"
            },
            {
                "value": "NO-947880349904267845729159",
                "key": "PartInstanceID"
            }
        ],
        "manufacturingInformation": {
            "date": "2022-02-04T14:48:54",
            "country": "DEU"
        },
        "catenaXId": "urn:uuid:16bb1a7e-8ed8-48ca-a839-5f38b704fcae",
        "partTypeInformation": {
            "manufacturerPartID": "01697F7-65",
            "customerPartId": "01697F7-65",
            "classification": "component",
            "nameAtManufacturer": "Engineering Plastics",
            "nameAtCustomer": "Engineering Plastics"
        }
    }
    submodel3 = {
        "catenaXId": "urn:uuid:36dc5dc3-9829-43c3-8839-dd84ea9fa3aa",
        "childParts": [
            {
                "quantity": {
                    "quantityNumber": 1,
                    "measurementUnit": {
                        "datatypeURI": "urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece",
                        "lexicalValue": "piece"
                    }
                },
                "lifecycleContext": "AsBuilt",
                "assembledOn": "2022-02-03T14:48:54.709Z",
                "lastModifiedOn": "2022-02-03T14:48:54.709Z",
                "childCatenaXId": "urn:uuid:e3da4ef8-09c2-4a70-a244-d5f8a49d6793"
            },
            {
                "quantity": {
                    "quantityNumber": 1,
                    "measurementUnit": {
                        "datatypeURI": "urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece",
                        "lexicalValue": "piece"
                    }
                },
                "lifecycleContext": "AsBuilt",
                "assembledOn": "2022-02-03T14:48:54.709Z",
                "lastModifiedOn": "2022-02-03T14:48:54.709Z",
                "childCatenaXId": "urn:uuid:cfad2909-2c86-4fc5-b1e0-69a6dac21cb1"
            },
            {
                "quantity": {
                    "quantityNumber": 1,
                    "measurementUnit": {
                        "datatypeURI": "urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece",
                        "lexicalValue": "piece"
                    }
                },
                "lifecycleContext": "AsBuilt",
                "assembledOn": "2022-02-03T14:48:54.709Z",
                "lastModifiedOn": "2022-02-03T14:48:54.709Z",
                "childCatenaXId": "urn:uuid:cdd67044-dc73-471a-998a-3fea5508450a"
            }
        ]
    }
    aas_shell = {
        "identification": "urn:uuid:fef9693c-6705-4cf8-bb67-1fef4e97107f",
        "idShort": "transmission.asm",
        "specificAssetIds": [
            {
                "value": "NO-006580393350168576734549",
                "key": "http://pwc.t-systems.com/datamodel/common"
            },
            {
                "value": "1O222E8-43",
                "key": "urn:VR:wt.part.WTPart#"
            }
        ],
        "description": [
            {
                "language": "en",
                "text": "Transmission"
            }
        ],
        "submodelDescriptors": [
            {
                "semanticId": [
                    {
                        "value": "urn:bamm:com.catenax.physical_dimension:1.0.0"
                    }
                ],
                "endpoints": [
                    {
                        "interface": "https://BPNL00000003B2OM.connector",
                        "protocolInformation": {
                            "endpointAddress": "http://provider.connector:port/BPNL00000003B2OM/urn:uuid:e3da4ef8-09c2-4a70-a244-d5f8a49d6793-urn:uuid:bd97cbd-737a-7dc8-8e59-7443b76c/submodel?content=value&extent=WithBLOBValue",
                            "endpointProtocolVersion": "1.0RC02",
                            "endpointProtocol": "AAS/SUBMODEL"
                        }
                    }
                ],
                "identification": "physicalDimension",
                "idShort": "physical-dimension"
            },
            {
                "semanticId": [
                    {
                        "value": "urn:bamm:com.catenax.material_for_recycling:1.0.0"
                    }
                ],
                "endpoints": [
                    {
                        "interface": "https://BPNL00000003B2OM.connector",
                        "protocolInformation": {
                            "endpointAddress": "http://provider.connector:port/BPNL00000003B2OM/urn:uuid:e3da4ef8-09c2-4a70-a244-d5f8a49d6793-urn:uuid:4007a84-83b8-64a4-fff9-04bbc24d/submodel?content=value&extent=WithBLOBValue",
                            "endpointProtocolVersion": "1.0RC02",
                            "endpointProtocol": "AAS/SUBMODEL"
                        }
                    }
                ],
                "identification": "materialForRecycling",
                "idShort": "material-for-recycling"
            },
            {
                "semanticId": [
                    {
                        "value": "urn:bamm:com.catenax.assembly_part_relationship:1.0.0"
                    }
                ],
                "endpoints": [
                    {
                        "interface": "https://BPNL00000003B2OM.connector",
                        "protocolInformation": {
                            "endpointAddress": "http://provider.connector:port/BPNL00000003B2OM/urn:uuid:e3da4ef8-09c2-4a70-a244-d5f8a49d6793-urn:uuid:23685ed-75cc-2a40-3a13-792a8bb2/submodel?content=value&extent=WithBLOBValue",
                            "endpointProtocolVersion": "1.0RC02",
                            "endpointProtocol": "AAS/SUBMODEL"
                        }
                    }
                ],
                "identification": "assemblyPartRelationship",
                "idShort": "assembly-part-relationship"
            }
        ],
        "globalAssetId": {
            "value": [
                "urn:uuid:e3da4ef8-09c2-4a70-a244-d5f8a49d6793"
            ]
        }
    }
    submodel4_1 = {
        "localIdentifiers": [
            {
                "value": "BPNL00000003B2OM",
                "key": "ManufacturerID"
            },
            {
                "value": "1O222E8-43",
                "key": "ManufacturerPartID"
            },
            {
                "value": "NO-006580393350168576734549",
                "key": "PartInstanceID"
            }
        ],
        "manufacturingInformation": {
            "date": "2022-02-04T14:48:54",
            "country": "DEU"
        },
        "catenaXId": "urn:uuid:e3da4ef8-09c2-4a70-a244-d5f8a49d6793",
        "partTypeInformation": {
            "manufacturerPartID": "1O222E8-43",
            "customerPartId": "1O222E8-43",
            "classification": "component",
            "nameAtManufacturer": "Transmission",
            "nameAtCustomer": "Transmission"
        }
    }
    submodel4_2 = {
        "catenaXId": "urn:uuid:e3da4ef8-09c2-4a70-a244-d5f8a49d6793",
        "childParts": [
            {
                "quantity": {
                    "quantityNumber": "0.2014",
                    "measurementUnit": {
                        "datatypeURI": "urn:bamm:io.openmanufacturing:meta-model:1.0.0#kilogram",
                        "lexicalValue": "kilogram"
                    }
                },
                "lifecycleContext": "AsBuilt",
                "assembledOn": "2022-02-03T14:48:54.709Z",
                "lastModifiedOn": "2022-02-03T14:48:54.709Z",
                "childCatenaXId": "urn:uuid:26125556-104e-4235-99a2-02a925f6bed4"
            },
            {
                "quantity": {
                    "quantityNumber": 1,
                    "measurementUnit": {
                        "datatypeURI": "urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece",
                        "lexicalValue": "piece"
                    }
                },
                "lifecycleContext": "AsBuilt",
                "assembledOn": "2022-02-03T14:48:54.709Z",
                "lastModifiedOn": "2022-02-03T14:48:54.709Z",
                "childCatenaXId": "urn:uuid:5ae2f54f-0774-4a0a-8640-d083adbf55d5"
            },
            {
                "quantity": {
                    "quantityNumber": "0.2341",
                    "measurementUnit": {
                        "datatypeURI": "urn:bamm:io.openmanufacturing:meta-model:1.0.0#kilogram",
                        "lexicalValue": "kilogram"
                    }
                },
                "lifecycleContext": "AsBuilt",
                "assembledOn": "2022-02-03T14:48:54.709Z",
                "lastModifiedOn": "2022-02-03T14:48:54.709Z",
                "childCatenaXId": "urn:uuid:27d500af-6df3-4e6d-82fa-d33461c167ee"
            }
        ]
    }

    create_asset(edc_policy_id=str(uuid.uuid4()),
                 submodel_id=uuid.uuid4().urn,
                 digital_twin_id="urn:uuid:365e6fbe-bb34-11ec-8422-0242ac120002", contract_id=str(uuid.uuid4()),
                 submodel_object=submodel1, aas_shell=aas_shell, submodel_server_data_url=submodel_server_data_url,
                 edc_api_key='123456', digital_twin_url=digital_twin_url)

    create_asset(edc_policy_id=str(uuid.uuid4()),
                 submodel_id=uuid.uuid4().urn,
                 digital_twin_id=submodel2.get("catenaXId"), contract_id=str(uuid.uuid4()),
                 submodel_object=submodel2, aas_shell=aas_shell, submodel_server_data_url=submodel_server_data_url,
                 edc_api_key='123456', digital_twin_url=digital_twin_url)

    create_asset(edc_policy_id=str(uuid.uuid4()),
                 submodel_id=uuid.uuid4().urn,
                 digital_twin_id=submodel3.get("catenaXId"), contract_id=str(uuid.uuid4()),
                 submodel_object=submodel3, aas_shell=aas_shell, submodel_server_data_url=submodel_server_data_url,
                 edc_api_key='123456',
                 digital_twin_url=digital_twin_url)

    create_asset(edc_policy_id=str(uuid.uuid4()),
                 submodel_id=uuid.uuid4().urn,
                 digital_twin_id=aas_shell.get("identification"), contract_id=str(uuid.uuid4()),
                 submodel_object=submodel4_1, aas_shell=aas_shell,
                 submodel_server_data_url=submodel_server_data_url, edc_api_key='123456',
                 digital_twin_url=digital_twin_url)

    create_asset(edc_policy_id=str(uuid.uuid4()),
                 submodel_id=uuid.uuid4().urn,
                 digital_twin_id=aas_shell.get("identification"), contract_id=str(uuid.uuid4()),
                 submodel_object=submodel4_2, aas_shell=aas_shell,
                 submodel_server_data_url=submodel_server_data_url, edc_api_key='123456',
                 digital_twin_url=digital_twin_url)
