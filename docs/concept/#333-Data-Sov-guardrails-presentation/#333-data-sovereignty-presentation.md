# \[Concept\] \[#ID#\] Summary

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
If there are any deviations, these must be named and concepts created for them.

## Problem Statement
Analyse and check new data sovereignty guardrails for release 24.05 against current implementation of IRS. 

## Requirements
- [ ] These Constraints are the base for a potential “auto-negotiation” process
- [ ] Part of the required logic for such a negotiation process is already available
- [ ] Application Providers must ensure a proper negotiation process

Core requirement for  data consumers 
- [ ] Establish a legally binding framework agreement 
- [ ] Check whether Data Contract Offer matches Data Provider Offer

1. Data provider: Selection of a data offer from Catalog offer
2. Data provider: 

Policy Store 
- [ ] As an admin, I have the option of storing usage policies in the Policy Store
- [ ] As an admin, I have the option of modifying usage policies in the Policy Store
- [ ] As an admin, I have the option of deleting usage policies in the Policy Store
- [ ] Policies stored in the policy store must correspond to a specific format (Policy Definition).
  - This format is defined by the C-X Association. 
  - This format definition is contained in a set of rules in the Policy Repository (Policy Hub).
- [ ] 

## Data Sovereignty Q-Gate Criteria for Release 24.05

### Data Provider (Applications FOSS & COTS incl. Broker)
Out of scope for IRS 

### Data Consumer (Applications FOSS & COTS incl. Broker)
- [ ] **(DS_1.0.0)** The provider of the component implementing the DSP MUST ensure the consumption of data via the DSP protocol adhering to the released version of CX-0018 standard. For example, using the Eclipse Data Space Connector on Tractus X.
   - [ ] **(DS_1.1.0)** The application MUST ensure that the consumer of the data is uniquely identifiable through its associated BPN-L.
   - [ ] **(DS_1.2.0)** This BPN-L must be used by this application to identify the data.
- [ ] **(DS_2.0.0)** The application MUST use the BPN-L of consumer of the data when consuming data via the DSP component (e.g. EDC or managed EDC).
- [ ] **(DS_3.0.0)** A user of an application MUST be enforced to act within the constraints of the framework agreement by appropriate means (legal, technical,...).
- [ ] **(DS_4.0.0)** To agree to one or multiple contract offers for a queried data asset, the selection of the appropriate offer MUST be done through the consuming application ensuring to follow company-defined rules and regulations for a user of that application.


#### DS_1.0.0
- [ ] IRS [CX-0018 2.0.1](https://catena-x.net/fileadmin/user_upload/Standard-Bibliothek/Update_September23/CX-0018-EclipseDataConnector_EDC_-v.2.0.1.pdf) fulfills any asset consumption (aas and submodel) using the EDC connector.
  - [ ] CX-0018 2.0.1
    - [ ] 1.4.1 Requirement: Identification of data exchange partners
      - [ ] IRS uses the 'IRS Policy Store' to store policies 
      - [ ] These policies contains Usage policies with defined 'RahmenbedingungsID'
      - [ ] These usage policies are checked in case of any data consumption (notification exchange, aas consumption, submodel consumption) 
    - [ ] 1.4.2 Requirement: Terms & Condition Signing
      - IRS validates the "Rahmenbedingungen" on each data consumption 
    - [ ] 1.4.3 Requirement: Data Asset Creation
      - Data consumer: **Out of scope**

| QG_ID        | Assessment                                                                                                                                                                                                                      | Evidence | 
|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
|  | | TODO     | 
| **DS_1.1.0** | TODO                                                                                                                                                                                                                            | TODO     | 
| **DS_1.2.0** | TODO                                                                                                                                                                                                                            | TODO     | 
| **DS_2.0.0** | TODO                                                                                                                                                                                                                            | TODO     | 
| **DS_3.0.0** | TODO                                                                                                                                                                                                                            | TODO     | 
| **DS_4.0.0** | TODO                                                                                                                                                                                                                            | TODO     | 





## NFR

## Out of scope

## Assumptions

## Concept

## Glossary

| Abbreviation      | Name             | Description | 
|-------------------|------------------|-------------|
| DS                | Data Sovereignty | TODO        | 
| Policy Definition | TODO             |
 | DSP               | TODO             |    
| AAS               | Asset Administration Shell | TODO        |

## References