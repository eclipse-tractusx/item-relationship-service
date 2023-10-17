# \[Concept\] \[TRI-1604\] EDC EDR Token negotiation and usage

## Glossary

| Abbreviation | Name                         |
|--------------|------------------------------|
| EDR          | EndpointDataReference        |
| EDR-Storage  | EndpointDataReferenceStorage |

## Existing flow - single use token

The existing flow of token negotiation and usage only allows to use a negotiated contract and the resulting EDR (
Endpoint Data Reference) Token to be used once.
After the token is consumed, it is removed from the EDR-Storage (EndpointDataReferenceStorage).

```mermaid
sequenceDiagram
    autonumber
    participant EdcSubmodelClient
    participant ContractNegotiationService
    participant EndpointDataReferenceStorage
    participant EdcCallbackController
    participant EdcDataPlaneClient
    EdcSubmodelClient ->> ContractNegotiationService: Negotiate new EDR Token
    ContractNegotiationService -->> EdcCallbackController: EDC flow
    EdcCallbackController ->> EndpointDataReferenceStorage: Store EDR token by contract agreement id after EDC callback
    loop While EDR Token is not present
        EdcSubmodelClient ->> EndpointDataReferenceStorage: Poll for EDR Token
    end
    EndpointDataReferenceStorage ->> EdcSubmodelClient: Return EDR Token
    EdcSubmodelClient ->> EdcDataPlaneClient: Get data(EDR Token, Dataplane URL)
    EdcDataPlaneClient ->> EdcSubmodelClient: Return data
```

## New flow - reuse token

To increase performance for assets with the same EDC contract, IRS should be able to reuse an existing EDR token.

To make this possible, two things have to be adjusted.

1. IRS has to check first, if an EDR Token for the requested EDC Asset ID is available in EDR-Storage
2. EDR Tokens have to be stored in the EDR-Storage with the EDC Asset ID as key instead of contract agreement id

```mermaid
sequenceDiagram
    autonumber
    participant EdcSubmodelClient
    participant ContractNegotiationService
    participant EndpointDataReferenceStorage
    participant EdcCallbackController
    participant EdcDataPlaneClient
    EdcSubmodelClient ->> EndpointDataReferenceStorage: Get EDR Token for EDC asset id
    EndpointDataReferenceStorage ->> EdcSubmodelClient: Return Optional<EDR Token>
    alt Token is present and not expired
        EdcSubmodelClient ->> EdcSubmodelClient: Optional.get
    else
        alt Token is expired
            EdcSubmodelClient ->> ContractNegotiationService: Renew EDR Token based on existing Token
        else Token is not present
            EdcSubmodelClient ->> ContractNegotiationService: Negotiate new EDR Token
        end
        ContractNegotiationService -->> EdcCallbackController: EDC flow
        EdcCallbackController ->> EndpointDataReferenceStorage: Store EDR token by EDC asset id after EDC callback
        loop While EDR Token is not present
            EdcSubmodelClient ->> EndpointDataReferenceStorage: Poll for EDR Token
        end
        EndpointDataReferenceStorage ->> EdcSubmodelClient: Return EDR Token
    end
    EdcSubmodelClient ->> EdcDataPlaneClient: Get data(EDR Token, Dataplane URL)
    EdcDataPlaneClient ->> EdcSubmodelClient: Return data
```

### Case: Token already present in EDR-Storage

```mermaid
flowchart LR
    A[EDR Token] --> B[extract authCode]
    B --> C[decode JWT]
    C --> D[get expiry date 'exp']
    D --> E{isAvailable}
    E -->|Yes| F{isValid}
    E -->|No| H[Request new Token]
    F -->|Yes| G{isExpired}
    F -->|No| H
    G -->|Yes| H
    G -->|No| I[Reuse Token]
```

### Case: Token is expired

```mermaid
sequenceDiagram
    autonumber
    participant EdcSubmodelClient
    participant ContractNegotiationService
    participant EndpointDataReferenceStorage
    participant EdcCallbackController
    EdcSubmodelClient ->> EdcSubmodelClient: extract contract agreement ID 'cid' from Token authCode
    EdcSubmodelClient ->> ContractNegotiationService: start transfer process(EDC asset ID, cid, connector URL, connector ID)
    ContractNegotiationService -->> EdcCallbackController: EDC flow
    EdcCallbackController ->> EndpointDataReferenceStorage: Store EDR token by EDC asset id after EDC callback
    loop While EDR Token is not present
        EdcSubmodelClient ->> EndpointDataReferenceStorage: Poll for EDR Token
    end
    EndpointDataReferenceStorage ->> EdcSubmodelClient: Return EDR Token
```
