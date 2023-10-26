# \[TRI-1576\] \[CONCEPT\] Handling of assets provided by multiple Digital Twin Registries

## Glossary

| Abbreviation | Name                        |
|--------------|-----------------------------|
| EDC          | Eclipse Dataspace Connector |
| DTR          | Digital Twin Registry       |
| DT           | Digital Twin                |

## Scenarios

IRS is not able to validate if a certain data offer is correct or outdated. IRS will take the first responding offer
which is available.

### EDC with multiple DTRs

IRS queries all DTRs for the globalAssetId and will take the first result it gets.  
If none of the DTRs return a result, IRS will create a tombstone.

````mermaid
sequenceDiagram
    IRS ->> Discovery Service: Get EDCs for BPN
    Discovery Service ->> IRS: Return list of 1 EDC
    IRS ->> EDC Provider: Query for DTR contract offer
    EDC Provider ->> IRS: 2 DTR contract offers
    par Query DTR 1
        IRS ->> EDC Provider: Negotiate contract
        IRS ->> DTR 1: Query for DT
        DTR 1 ->> IRS: no DT
    and Query DTR 2
        IRS ->> EDC Provider: Negotiate contract
        IRS ->> DTR 2: Query for DT
        DTR 2 ->> IRS: DT
    end
````

### Multiple EDCs with one DTR

IRS starts a contract negotiation for all registry contract offers in parallel and queries the DTRs for all successful
negotiations.  
The first registry which responds with a result will be the one used by IRS.

````mermaid
sequenceDiagram
    IRS ->> Discovery Service: Get EDCs for BPN
    Discovery Service ->> IRS: Return list of 3 EDCs
    par Catalog Request to EDC 1
        IRS ->> EDC Provider 1: Query for DTR contract offer
        EDC Provider 1 ->> IRS: No offer
    and Catalog Request to EDC 2
        IRS ->> EDC Provider 2: Query for DTR contract offer
        EDC Provider 2 ->> IRS: No offer
    and Catalog Request to EDC 3
        IRS ->> EDC Provider 3: Query for DTR contract offer
        EDC Provider 3 ->> IRS: DTR contract offer
        IRS -> EDC Provider 3: Negotiate contract
        IRS ->> DTR: Query for DT
        DTR ->> IRS: DT
    end
````

### One EDC with one DTR

Only one EDC found for BPN and the catalog only contains one offer for the DTR.  
IRS will use this registry and will create a tombstone if no DT could be found for the globalAssetId.

````mermaid
sequenceDiagram
    IRS ->> Discovery Service: Get EDCs for BPN
    Discovery Service ->> IRS: Return list of 1 EDC
    IRS ->> EDC Provider 3: Query for DTR contract offer
    EDC Provider 3 ->> IRS: DTR contract offer
    IRS -> EDC Provider 3: Negotiate contract
    IRS ->> DTR: Query for DT
    DTR ->> IRS: DT
````

### Multiple EDCs with multiple DTRs

IRS starts a contract negotiation for all the registry offers.

````mermaid
sequenceDiagram
    IRS ->> Discovery Service: Get EDCs for BPN
    Discovery Service ->> IRS: Return list of 3 EDCs
    par Catalog Request to EDC 1
        IRS ->> EDC Provider 1: Query for DTR contract offer
        EDC Provider 1 ->> IRS: No offer
    and Catalog Request to EDC 2
        IRS ->> EDC Provider 2: Query for DTR contract offer
        EDC Provider 2 ->> IRS: DTR contract offer
        IRS -> EDC Provider 2: Negotiate contract
        IRS ->> DTR: Query for DT
        DTR ->> IRS: DT
    and Catalog Request to EDC 3
        IRS ->> EDC Provider 3: Query for DTR contract offer
        EDC Provider 3 ->> IRS: DTR contract offer
        IRS -> EDC Provider 3: Negotiate contract
        IRS ->> DTR: Query for DT
        DTR ->> IRS: No DT
    end
````

### Multiple EDCs with no DTRs

IRS starts a contract negotiation for all the registry offers and creates a tombstone since no DTR could be discovered.

````mermaid
sequenceDiagram
    IRS ->> Discovery Service: Get EDCs for BPN
    Discovery Service ->> IRS: Return list of 3 EDCs
    par Catalog Request to EDC 1
        IRS ->> EDC Provider 1: Query for DTR contract offer
        EDC Provider 1 ->> IRS: No offer
    and Catalog Request to EDC 2
        IRS ->> EDC Provider 2: Query for DTR contract offer
        EDC Provider 2 ->> IRS: No offer
    and Catalog Request to EDC 3
        IRS ->> EDC Provider 3: Query for DTR contract offer
        EDC Provider 3 ->> IRS: No offer
    end
    IRS ->> IRS: Tombstone
````

## Special Scenarios

### Same DT in multiple DTRs

IRS will use all registries to query for the globalAssetId and takes the first result which is returned.  
If no DT could be found in any of the DTRs, IRS will create a tombstone.

### Multiple DTs (with the same globalAssetId) in one DTRs

IRS uses the /query endpoint of the DTR to get the DT id based on the globalAssetId. If more than one id is present for
a globalAssetId, IRS will use the first of the list.

````mermaid
sequenceDiagram
    IRS ->> DTR: /query for globalAssetId
    DTR ->> IRS: return list of two results
    IRS ->> IRS: use first
````

## To be clarified

1. How should the procedure look like for Notifications?