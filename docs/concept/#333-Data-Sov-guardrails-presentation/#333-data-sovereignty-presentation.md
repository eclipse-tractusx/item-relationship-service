#  Data Sov guardrails presentation #333

| Key           | Value                                                                            |
|---------------|----------------------------------------------------------------------------------|
| Creation date | 01.02.2024                                                                       |
| Ticket Id     | [#333](https://github.com/eclipse-tractusx/item-relationship-service/issues/333) |    
| State         | WIP                                                                              | 

## Table of Contents

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

## Overview
New data sovereignty guardrails have been developed for release 24.05 DS, which are to be implemented by the product in release 24.5.

## Summary
The IRS acts as a data consumer. It is therefore necessary to analyse the new DS Guardrails requirements for data consumers.
Here it is important to fulfil these requirements against the current implementation of the IRS. 
If there are any deviations, these must be named, and concepts created for them.

## Problem Statement
Analyse and check new data sovereignty guardrails for release 24.05 against current implementation of IRS. 

## Requirements
- [ ] These Constraints are the base for a potential “auto-negotiation” process
  - [ ] Part of the required logic for such a negotiation process is already available
  - [ ] Application Providers must ensure a proper negotiation process
  - [ ] Do NOT accept Policies / Offers you or your organization doesn't understand or accept.

Core requirement for  data consumers. 
- [ ] Establish a legally binding framework agreement. 
  - [ ] Check whether Data Contract Offer matches Data Provider Offer.

  1. Data provider: Selection of a data offer from Catalog offer
  2. Data provider: 

Policy Store 
- [ ] As an admin, I have the option of storing usage policies in the Policy Store
  - [ ] As an admin, I have the option of modifying usage policies in the Policy Store
  - [ ] As an admin, I have the option of deleting usage policies in the Policy Store
  - [ ] Policies stored in the policy store must correspond to a specific format (Policy Definition).
    - This format is defined by the C-X Association. 
    - This format definition is contained in a set of rules in the Policy Repository (Policy Hub).

## Data Sovereignty Q-Gate Criteria for Release 24.05

### Data Provider (Applications FOSS & COTS incl. Broker)
Out of scope for IRS 

### Data Consumer (Applications FOSS & COTS incl. Broker)
- [x] **(DS_1.0.0)** The provider of the component implementing the DSP MUST ensure the consumption of data via the DSP protocol adhering to the released version of CX-0018 standard. For example, using the Eclipse Data Space Connector on Tractus X.
   - [x] **(DS_1.1.0)** The application MUST ensure that the consumer of the data is uniquely identifiable through its associated BPN-L.
   - [x] **(DS_1.2.0)** This BPN-L must be used by this application to identify the data.
  - [ ] **(DS_2.0.0)** The application MUST use the BPN-L of consumer of the data when consuming data via the DSP component (e.g. EDC or managed EDC).
  - [ ] **(DS_3.0.0)** A user of an application MUST be enforced to act within the constraints of the framework agreement by appropriate means (legal, technical,...).
  - [ ] **(DS_4.0.0)** To agree to one or multiple contract offers for a queried data asset, the selection of the appropriate offer MUST be done through the consuming application ensuring to follow company-defined rules and regulations for a user of that application.


#### DS_1.0.0
- [x] IRS [CX-0018 2.0.1](https://catena-x.net/fileadmin/user_upload/Standard-Bibliothek/Update_September23/CX-0018-EclipseDataConnector_EDC_-v.2.0.1.pdf) fulfills any asset consumption (aas and submodel) using the EDC connector.
  - [x] CX-0018 2.0.1 
    - [x] 1.4.1 Requirement: Identification of data exchange partners
      - [x] IRS uses the 'IRS Policy Store' to store usage policies 
      - [x] These policies contain constraints with defined 'RahmenbedingungsID'
      - [x] These usage policies are checked in case of any data consumption (notification exchange, aas consumption, submodel consumption) 
    - [x] 1.4.2 Requirement: Terms & Condition Signing
      - IRS validates the "Rahmenbedingungen" on each data consumption 
    - [x] 1.4.3 Requirement: Data Asset Creation
      - For Data consumer: **Out of scope**

#### DS_1.1.0 
_EDC consumer deployed in company context_
BPN-L is configured in the EDC consumer used by IRS instance deployed in company context. This BPNL is used to verify the Verifiable credentials provided by the (M)IdentityWallet user.

####  DS_1.2.0 
_EDC consumer deployed in company context_
BPN-L is configured in the EDC consumer used by IRS instance deployed in company context.  This BPNL is used to verify the Verifiable credentials provided by the (M)IdentityWallet user.

#### DS_2.0.0
_EDC consumer deployed in company context_
IRS uses the configured EDC consumer to consume any data asset. BPNL of company is configured in the EDC. 

### DS_3.0.0

#### Access policies 
Enforcement of access policies over the configured EDC consumer instance. 
The verification of the access policy for a data asset is ensured using the EDC for the concentration of data assets (shells and submodels).   
The EDC checks the consumer's authorisation for a data asset.

#### Usage policies
Enforcement of usage policy handling by irs.
Component to be used is the IRS Policy Store to provides a usage policies management.
IRS enforces to only consume assets where usage policy of BPNL matches.  
The IRS ensures that there is data consumption only if the usage policy matches and returns da initiating business application. If this usage policy matches, the data asset is accumulated via the EDC after successful contract fulfilment and made available to the business application.

Note:
There is no content or semantic check of the usage policy.
Usage policy existence and matching with provided usage policy of contract offer is checked by the IRS.
Assets are consumed only in case of a positive check whether the usage policy matches the usage policy of the offered ContractOffer.

### DS_4.0.0
Refer to DS_3.0.0. Policy Store and the associated usage policy check is fulfilling this requirement.

### Policy Hub 
Policy Hub provides api endpoints to receive usage policy template definition. Policy hub provides templates and restricted value ranges and defines allowed policies in the C-X environment for usage policies.
PolicyHub is used to store only policies which matches policy templates. 

### Assessment 
IRS current implementation matches the DS Guardrails for R24.5. 
Usage of EDC (CX-0018) for consumption of any data asset. 
Policy Store is used for checking if usage policy exists for BPNL offering desired asset. 
Policy Store is used for checking if usage policy matches policy of asset offered by a BPNL.

**Deviation:**
Actually, usage policies are not validated according to the policy specifications provided by the Policy Hub - this validation should be implemented for the Policy Store.

## Glossary

| Abbreviation | Name                       | Description                                                                                                                                                                                                                                         | 
|--------------|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DS           | Data Sovereignty           | Data sovereignty refers to the idea that data is subject to the laws and regulations of the country or region where it is located or originates. It asserts the rights of governments to control how data is managed and used within their borders. ||
| DSP          | Data Space Protocol        | Set of specifications designed to facilitate interoperable data sharing within a dataspace, currently governed by the IDSA                                                                                                                          |  
| AAS          | Asset Administration Shell | Asset Administration Shell (AAS) The Asset Administration Shell is a digital representation of an asset. It is a form of a digital twin.                                                                                                            |
| -            | Policy Hub                 | Policy Hub provides api endpoints to receive usage policy template definition. Policy hub provides templates and restricted value ranges and defines allowed policies in the C-X environment for usage policies.


## References
* [Swagger Policy Hub](https://policy-hub.dev.demo.catena-x.net/api/policy-hub/swagger/index.html)
  * [API Endpoint usage](https://github.com/eclipse-tractusx/policy-hub/blob/feature/request-docu/docs/developer/Technical-Documentation/requests/example-requests.md)dpoint usage](https://github.com/eclipse-tractusx/policy-hub/tree/release/v0.1.0-rc.4/docs/developer/Technical-Documentation/requests/example-requests.md)