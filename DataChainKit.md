# Get started with DataChainKit

DataChainKit brings valuable data chain information to apps and services through a wide range of data-chains that can help you to be up-to-date and prepared. It's easy to use the DataChainKit with an Open Source Software package, which can easily be deployed via helm or docker-compose.

----

# DataChainKit Elements

## API Description
For each App or Service Provider can use this documentation to implement their own instance to participate within the Catena-X
- 📄 [IRS REST API Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/api-specification/api-specification.html)
- 📄 [IRS Specification](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42)

## Item Relationship Service
The Item Relationship Service (IRS) is the first reference application of the DataChainKit. It is built with the Java Spring framework. It is dockerized and can easily be deployed via helm or docker compose on infrastructure you choose.

### Requirements
The IRS needs to be deployed with the following components in the same environment: 
- [Eclipse Dataspace Connector](https://github.com/eclipse-tractusx/tractusx-edc)

The Service interacts with the Catena-X [Digital Twin Registry](https://github.com/eclipse-tractusx/sldt-digital-twin-registry) and is built upon the Catena-X Semantic Models which are stored in the [Semantic Hub](https://github.com/eclipse-tractusx/sldt-semantic-hub). 

### Tools and documentation

- 🖥 Item Relationship Service Video
- 📄 [Item Relationship Service Product Page](https://catena-x.net/en/offers-standards/item-relationship-service)
- 📄 [Item Relationship Service Documentation](https://github.com/eclipse-tractusx/item-relationship-service/blob/main/README.md)
- 📄 [IRS REST API Documentation](https://eclipse-tractusx.github.io/item-relationship-service/docs/api-specification/api-specification.html)
- 📄 Catena-X Kit's

## NOTICE

This work is licensed under the [CC-BY-4.0](https://creativecommons.org/licenses/by/4.0/legalcode).

- SPDX-License-Identifier: CC-BY-4.0
- SPDX-FileCopyrightText: 2022 Contributors to the Eclipse Foundation
- Source URL: https://github.com/eclipse-tractusx/item-relationship-service
