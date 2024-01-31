# Concept #322 Provisioning of contract agreement id for assets

| Key           | Value                                                                                                                                               |
|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| Creation date | 11.01.2024                                                                                                                                          |
| Issue Id      | https://github.com/eclipse-tractusx/item-relationship-service/issues/322   https://github.com/eclipse-tractusx/item-relationship-service/issues/370 |    
| State         | DRAFT                                                                                                                                               | 

# Table of Contents

1. [Overview](#overview)
2. [Summary](#summary)
3. [Problem Statement](#problem-statement)
4. [Requirements](#requirements)
5. [NFR](#nfr)
6. [Out of scope](#out-of-scope)
7. [Assumptions](#assumptions)
8. [Concept](#concept)
9. [Glossary](#glossary)
10. [References](#references)

# Overview

The exchange of assets via the EDC takes place after a successful contract negotiation in which it is checked whether
the consumer has authorized access to the data asset.
This access is automatically checked by the EDC via so-called AccessPolicies. The consumer is only granted access to the
data after a successful check.
During contract negotiation the edc stores audits to the artefacts edc:ContractAgreement and edc:ContractNegotiation.
These audit information can be requested over the edc management API.
To request the mentioned artefacts over the management API the ContractAgreementDto:@id is required.
This specific id must therefore be stored and linked for the exchanged asset in order to be able to determine the
corresponding contract agreement later on.

# Summary

This feature is responsible for collecting the CIDs of EDC contract negotiations regarding during in the IRS processing.
The CIDs are provided in the IRS JobResponse document for collected assets.
For shells and submodels CIDs are collected and provided.
In case of a usage policy mismatch. The irs policy store does not provide the usage policy of the asset. This
circumstance is reported in a tombstone which contains the policy of the asset.

# Problem Statement

1. The ContractAgreementDto:@id is currently not delivered via the IRS response, so business apps that use the IRS
   cannot access the corresponding ContractAggreement under which the assets, delivered by the IRS, were exchanged.
2. The EDC management API provides an endpoint to query contract agreements by assetId. It is not possible to determine which ContractAgreement belongs to which negotiation in case of multiple negotiations for the same asset
3. Business apps must make the contract agreements under which the assets were exchanged available for audit purposes.

# Requirements

1. [ ] Provisioning of ContractAgreementDto:@id via IRS JobReponse for AAS retrieved via EDC.
2. [ ] Provisioning of ContractAgreementDto:@id via IRS JobReponse for submodels retrieved via EDC.
3. [ ] API parameter SHOULD control whether ContractAgreementDto:@id should be returned via the IRSJobResponse

# NFR

# Out of scope

# Assumptions

- [x] Business app (Trace-X) has access to the management API of the IRS EDC Consumer.
- [x] EDC Management API of EDC Consumer is configured as a parameter in Business app (Trace-X). IRS does not provide
  API url to EDC Management API via JobResponse.
- [x] In case there negotiation is not executed because of policy mismatch, the catalog offer policy of will be
  collected inside IRS flow and provided to irs requestor.

# Concept

## API Extensions

## APIs

- POST /irs/jobs
- POST /irs/orders
- POST /irs/ess/orders
- POST /ess/bpn/investigation

- parameter name: auditContractNegotiation
- parameter value: boolean value
- parameter default: true
- parameter description: enables and disables auditing including provisioning of ContractAgreementId inside submodels
  and shells objects
- parameter impacts: enables collection of ContractAgreementId in job processing and provides this information in
  SubmodelContainers and ShellContainers or policy from CatalogOffer in Tombstone in case of policy mismatch

## 1. Case 1: Successful contract negotiation

IRS proceed EDC contract negotiation succeeds.
IRS transfers assets and collects contractAgreementId for asset

### Receiving EndpointDataReference / EDR token for Catalog Entry

````mermaid

sequenceDiagram
    %%{init: {'theme': 'dark', 'themeVariables': { 'fontSize': '15px'}}}%%
    autonumber
    SubmodelDelegate ->> EdcSubmodelFacade : getEndpointReferenceForAsset
    EdcSubmodelFacade ->> EdcSubmodelClientImpl: getEndpointReferenceForAsset
    note left of EdcSubmodelClientImpl: Get EDR token to call endpoint reference 
    EdcSubmodelClientImpl ->> EDCCatalogFacade: fetchCatalogByFilter key/value 
    EDCCatalogFacade ->> EdcControlPlaneClient: getCatalogWithFilter key/value
    EdcControlPlaneClient ->> EDC Consumer ControlPlane : GET catalog with filter <br /> [POST /management/v2/catalog/request]
    EDC Consumer ControlPlane ->> EdcControlPlaneClient : List<CatalogItems>
    EdcControlPlaneClient -->> EDCCatalogFacade : List<CatalogItems>
    EDCCatalogFacade -->> EdcSubmodelClientImpl : List<CatalogItems>
    EdcSubmodelClientImpl --> ContractNegotiationService : do negotiation for List<CatalogItems>
    ContractNegotiationService --> ContractNegotiationService : startNewNegotiation
    ContractNegotiationService --> PolicyCheckerService : isValid 
    alt is valid
        ContractNegotiationService -->> EdcSubmodelClientImpl : return NegotiationResponse
        EdcSubmodelClientImpl ->> EdcSubmodelClientImpl : getContractAgreementId from  NegotiationResponse
        EdcSubmodelClientImpl ->> AsyncPollingService : retrieveEndpointReference
        AsyncPollingService ->>  EdcSubmodelClientImpl : EndpointDataReference
        note left of EdcSubmodelClientImpl : received EDR Token to receive assets
        EdcSubmodelClientImpl -->> EdcSubmodelFacade : EndpointDataReference
        EdcSubmodelFacade -->> SubmodelDelegate : EndpointDataReference
    else is not valid
        ContractNegotiationService ->> ContractNegotiationService : throw UsagePolicyException
        note right of ContractNegotiationService : UsagePolicyException MUST cover the policy of catalog item  this is relevant to create tombstone afterwards with policy
        SubmodelDelegate --> ItemContainer: create tombstone with policy payload
    end
````

### Receiving Submodel Payload

````mermaid

sequenceDiagram
    %%{init: {'theme': 'dark', 'themeVariables': { 'fontSize': '15px'}}}%%
    autonumber
    IRS ->> SubmodelDelegate : process 
    SubmodelDelegate ->> EdcSubmodelFacade : getSubmodels
    EdcSubmodelFacade ->> EdcSubmodelClientImpl : getSubmodelRawPayload (onnectorEndpoint, submodelDataplaneUrl, assetId)
    EdcSubmodelClientImpl ->> EdcSubmodelClientImpl : getEndpointDataReference EndpointDataReference
    EdcSubmodelClientImpl ->> EdcSubmodelClientImpl: storageId a
    EdcSubmodelClientImpl  ->>   AsyncPollingService : createJob 
    AsyncPollingService -->>  EdcSubmodelClientImpl: submodel payload 
    EdcSubmodelClientImpl -->> EdcSubmodelFacade : submodel payload
    EdcSubmodelFacade -->> SubmodelDelegate: submodel payload
    SubmodelDelegate -->> IRS: write contractAgreementId to JobReponse submodels[]
````

### Receiving AAS Payload

````mermaid

sequenceDiagram
    %%{init: {'theme': 'dark', 'themeVariables': { 'fontSize': '15px'}}}%%
    autonumber
    IRS ->> DigitalTwinDelegate : process
    DigitalTwinDelegate ->> DigitalTwinRegistryService : fetchShells
    DigitalTwinRegistryService ->> DecentralDigitalTwinRegistryService : fetchShellDescriptors
    loop DigitalTwinRegistryKey
        DigitalTwinRegistryService ->> DecentralDigitalTwinRegistryService : fetchShellDescriptor
        DigitalTwinRegistryService ->> DecentralDigitalTwinRegistryService : AASContainer with AssetAdministrationShellDescriptor + CID
    end 
    DecentralDigitalTwinRegistryService -->> DigitalTwinRegistryService : AASContainer with AssetAdministrationShellDescriptor + CID
    DigitalTwinRegistryService -->> IRS :  write contractAgreementId to JobReponse shells array
    
````   

````mermaid

sequenceDiagram
    %%{init: {'theme': 'dark', 'themeVariables': { 'fontSize': '15px'}}}%%
    autonumber
    IRS ->> SubmodelDelegate: 
    SubmodelDelegate ->> SubmodelDelegate : requestSubmodelAsString 
    SubmodelDelegate ->>  AbstractDelegate : requestSubmodelAsString
    alt dspEndpoint.isPresent()
        AbstractDelegate ->> SubmodelFacade : getSubmodelRawPayload
    else
        AbstractDelegate ->> AbstractDelegate: getSubmodel
        loop endpoints 
            AbstractDelegate ->> SubmodelFacade : getSubmodelRawPayload
        end
    end
    SubmodelFacade ->> EdcSubmodelClientImpl : getSubmodelRawPayload
    EdcSubmodelClientImpl ->> EdcSubmodelClientImpl : getEndpointDataReference
    EdcSubmodelClientImpl -->> EdcSubmodelClientImpl : EndpointDataReference
    EdcSubmodelClientImpl ->> PollingService : createJob 
    PollingService -->> EdcSubmodelClientImpl : SubmodelContainer contains SubmodelPayload and CID
    EdcSubmodelClientImpl -->> SubmodelFacade :  SubmodelContainer contains SubmodelPayload and CID
    SubmodelFacade -->> AbstractDelegate : SubmodelContainer contains SubmodelPayload and CID
    AbstractDelegate -->> SubmodelDelegate : SubmodelContainer contains SubmodelPayload and CID
    SubmodelDelegate --> IRS : write contractAgreementÍd to submodel inside JobReponse
````   

## 2. Case 2: IRS proceed EDC contract negotiation fails because of internal EDC error

### Case 2.1: GET contract negotiation return 404 and "type": "ObjectNotFound",

````
404
[
  {
    "message": "Object of type ContractNegotiation with ID=f9600523-f8e4-42b3-b388-485370b4f8f4 was not found",
    "type": "ObjectNotFound",
    "path": null,
    "invalidValue": null
  }
]
````

## 3. Case 3: IRS revokes asset transfers cause by not matching usage policy

Usage policy is checked before contract negotiation. in case policy mismatch, no negotiation will be started.

````mermaid

sequenceDiagram
    %%{init: {'theme': 'dark', 'themeVariables': { 'fontSize': '15px'}}}%%
    autonumber
    ContractNegotiationService --> ContractNegotiationService : startNewNegotiation
    ContractNegotiationService --> PolicyCheckerService : isValid
alt is valid
    ContractNegotiationService -->> EdcSubmodelClientImpl : return NegotiationResponse
    EdcSubmodelClientImpl ->> EdcSubmodelClientImpl : getContractAgreementId from  NegotiationResponse
    EdcSubmodelClientImpl ->> AsyncPollingService : retrieveEndpointReference
    AsyncPollingService ->>  EdcSubmodelClientImpl : EndpointDataReference
note left of EdcSubmodelClientImpl : received EDR Token to receive assets
    EdcSubmodelClientImpl -->> EdcSubmodelFacade : EndpointDataReference
    EdcSubmodelFacade -->> SubmodelDelegate : EndpointDataReference
else is not valid
    ContractNegotiationService ->> ContractNegotiationService : throw UsagePolicyException
    note right of ContractNegotiationService : UsagePolicyException MUST cover the policy of catalog item  this is relevant to create tombstone afterwards with policy
    SubmodelDelegate --> ItemContainer: create tombstone with policy payload
end


````

## EDC Management API 

The EDC Management API is provided by EDC consumer. In this case the IRS configured EDC provider logs the required
contract aggreements and provides the API to request contract agreements and contract negotations for given contract
agreement @id

Source: https://app.swaggerhub.com/apis/eclipse-tractusx-bot/tractusx-edc/0.5.3#/Contract%20Agreement/getNegotiationByAgreementId

```
GET /v2/contractagreements/{id} 
    Gets an contract agreement with the given ID
GET /v2/contractagreements/{id}/negotiation
    Gets a contract negotiation with the given contract agreement ID
```

## Mapping of contract ContractAgreementId in theJobResponse

### Add ContractAgreementId to shells (AAS) 

The structure of the shells is extended in the same way as the submodels[] structure.
The meta information, which is currently the "contractAgreementId", is written to the shells object. The actual payload
of the shell is written to the payload object.
This follows a uniform structure, as can also be found in the submodel object.

```json 
"shells": [
  {
    "contractAgreementId": "",
    "payload": {
      {
        "administration": null,
        ...
        "globalAssetId": "urn:uuid:a1fa0f85-697d-4c9d-982f-2501af8e8636",
        "idShort": "VehicleCombustionA",
        "id": "urn:uuid:56ee00a5-ca0f-4366-a00d-193e08e74995",
        "specificAssetIds": [
          {}
        ],
        "submodelDescriptors": [
          {}
        ]
      }
    }
  },
  {
    "contractAgreementId": "",
    "payload": {
      // aas shell payload
    }				
  }
]
```

### Add ContractAgreementId to relationships

In the first version, the ContractAgreementId is not mapped in the "relationship".
If this information is required, the requester can register traversal aspect "SingleLevel*" in api parameter "aspects[]"
in request POST /irs/jobs. This collects the submodel which include the required ContractAgreementId.

````json
"aspects": [
  "SingleLevelBomAsBuilt"
]
````

### Add ContractAgreementId to submodels 

#### Option 1: Provide contractAgreementId for each submodel:

Decision: **Option 1 will be implemented.** Option 2 is too invasive change to JobResponse is required now.

Impact:

- Redundant information in case multiple submodels were received for the same contractAgreementId.
- JobReponse size is already critical and extends by ~100byte multiples with every submodel and aas shell stored in the
  JobReponse

```json 
"submodels": [
  {
    "identification": "urn:uuid:f9b6f066-c4de-4bed-b531-2a1cad7bd173",
    "aspectType": "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt",
    "contractAgreementId": "<contractAgreementId>",
    "payload": {
      <... submodel payload ...>
    }
  }
]
```

#### Option 2: Provide submodel for contractAgreementId

Impact:

1. High invasive changes to the code, api, JobResponse and documentation.
2. This variant complicates the optional (activatable/deactivatable) collection of ContractAggreementIds as these are an
   essential part of the response structure.

```json 
"contractAggreements": [
  "contractAggreement": {
    "contractAgreementId": "<contractAgreementId>",
    "submodels": [
       {
         "identification": "<identification>>",
         "aspectType": "<aspectType>",
         "payload": {
           <... submodel payload ...>
         }
       }
    ] 
  }
]
```

### Add Tombstone in case of not matching policy

Tombstone is extended with policy payload when policy is not matched and contract negotiation is not conducted.
The requestor gets the insight which policy does not match and has the opportunity to add the specific policy to the IRS
policy store in order to receice further on assets with the specific policy.

- [ ] Tombstone is extended with policy payload in case policy is invalid AND auditContractNegotiation api parameter is
  enabled.
- [ ] Policy is extended in case of an UsagePolicyValidation  

`````json 
"tombstones": [
  {
    "catenaXId": "urn:uuid:6ce41c0b-c84a-46b0-b4c4-78fce958663d",
    "endpointURL": null,
    "policy": {
      "odrl:hasPolicy": {
      "@id": "ZDgzZjhjY2EtMGY2MC00OWMzLWJjNDYtMWE0OTY2MDdlMzhj:cmVnaXN0cnktYXNzZXQ=:Y2IxNzI5MjUtYzUyNS00NmJiLWFiZWQtMDVhMTdkNTFiZTg0",
      "@type": "odrl:Set",
      "odrl:permission": {
        "odrl:target": "registry-asset",
        "odrl:action": {
          "odrl:type": "USE"
        }
      },
      "odrl:constraint": {
        "odrl:or": {
          "odrl:leftOperand": "PURPOSE",
          "odrl:operator": {
            "@id": "odrl:eq"
          },
          "odrl:rightOperand": "ID 3.0 Trace"
        }
      }
      }
    },
    "processingError": {
       "processStep": "UsagePolicyValidation", 
       "errorDetail": "Consumption of asset (itemId) is not permitted as the required catalog offer policies do not comply with defined IRS policies.",
       "lastAttempt": "2024-01-17T09:02:36.648055745Z",
       "retryCounter": 3
    }
  }
],
`````

# Glossary

| Abbreviation           | Name                     | Description                               |  
|------------------------|--------------------------|-------------------------------------------|
| edc:ContractAgreement  | edc:ContractAgreement    | ContractAgreement of negotation in EDC    |
| edc:ContractNegotation | edc:ContractNegotation   | ContractNegotation of negotation in EDC   |
| AAS                    | AssetAdministrationShell | The Shell object dtored in  dDTR registry | 
| CID                    | contractAgreementId      | Unique Id of an contract aggrement        |

# References

https://app.swaggerhub.com/apis/eclipse-tractusx-bot/tractusx-edc/0.5.3#/Contract%20Agreement/getAgreementById
https://github.com/eclipse-tractusx/tractusx-edc/blob/main/docs/kit/Development%20View/01_MGMT_API_Walkthrough/02_policies.md
https://eclipse-edc.github.io/docs/#/submodule/Connector/docs/developer/contracts