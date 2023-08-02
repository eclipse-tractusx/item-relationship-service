---
id: Adoption View Data Chain Kit
title: Adoption View
description: 'Data Chain Kit'
sidebar_position: 1
---

![Datachain kit banner](@site/static/img/DataChainKitIcon.png)

### Data Chain Kit

## Vision & Mission

### Vision

DataChainKit brings valuable data chain information to your use-cases and services through connected data that can help Business Owner and Catena-X participants to be up to date and prepared. It's easy to use the DataChainKit with an Open Source Software package, which can easily deployed via HELM or docker-compose. The DataChainKit enables to apply business logic along a distributed data chains, for example aggregation of certificates along the value chain. Also ad-hoc provisioning of continuous data chains across company boundaries for empowerment of use cases Circular Economy, Traceability, Quality and the European supply chain act.

(#GreenIT#DataSovereignty#Interoperability#ConnectedData)

### Mission

The Data Chain KIT provides reference implementation as functional federated component to handle ad-hoc data chains across n-tiers within the Catena-X network. To realize these data chains, the Data Chain Kit relies on data models of the Traceability use case and provides the federated data chains to customers or applications. Furthermore, the target picture of the IRS includes the enablement of new business areas by means of data chains along the value chain in the automotive industry.

All described specifications in the KIT are based on Catena-X standards and refer to other Catena-X KITs like the Connector KIT (EDC) and Digital Twin Registry to ensure interoperability and data sovereignty according to IDSA and Gaia-X principles.

### Customer Journey

## Business Value

* Application and Service provider can reduce integrate or migrate due to one API specification
* The IRS Iterative API enables an easy interface for complex network tasks
* Potential to scale and optimize network traffic
* The IRS Iterative API is providing one Endpoint to access and collect widely distributed data
* The Data Chain Kit enables interoperability for Data Chains along the value chain to extend

## Use Case

## Tutorials

## Logic & Schema

## Business Process

The DataChainKit acts as a middleware between consumers and manufacturers. This section describes the Business Process of DataChainKit. Who are its users, and with which other systems does it interact with.

### Consumer

The DataChainKit by IRS API is being consumed by the dismantler dashboard and other parties which are part of the Catena-X network. They need to provide valid credentials issued by the Catena-X IAM. Additionally, they must provide a base global asset identifier to retrieve information for as well as configuration details for the view on that information.

### Catena-X network

The IRS retrieves data from the Catena-X network (using the necessary infrastructure, see Technical Context), aggregates it and provides it back to the consumers. This connection is mandatory. If the Catena-X services are unavailable, the IRS cannot perform any work.

As of now, the IRS uses its own IAM credentials to gather the required data. This might be changed to use the consumer credentials in the future.

## Standards

Our relevant standards can be downloaded from the official [Catena-X Standard Library](https://catena-x.net/de/standard-library)

- [CX - 0005 Item Relationship Service API](https://catena-x.net/fileadmin/user_upload/Standard-Bibliothek/Archiv/Update_Juli_23_R_3.2/CX-0005-ItemRelationshipServiceApi-v1.1.1.pdf)
- [CX - 0045 Aspect Model Template Data Chain](https://catena-x.net/fileadmin/user_upload/Standard-Bibliothek/Archiv/Update_Juli_23_R_3.2/CX-0045-AspectModelDataChainTemplate-v1.1.1.pdf)

## Why Data Chain Kit

What is in for you to use the Data Chain Kit. On what is it built on.

![Why use Datachain Kit](@site/static/img/why_data_chain-minified.png)

## IRS Iterative

The IRS iterative iterates through the different digital twin aspects, which are representing a relationship. For Release 1 this is the AssemblyPartRelationship aspect, which connects serialized parts. This service can access the digital twins for which a EDC policy and and data contract exists. In this case the consumer needs a contract which each participant of the data chain.

![IRS iterative diagram](@site/static/img/irs-minified.svg)

The following general conditions apply:
-access control through policies and contracts is done by the EDC
-direct data exchange between supply-chain partners
-Catena-X partners of the accessible value chain are known to the data-consumer
