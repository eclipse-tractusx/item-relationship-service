# Get started with DataChainKit

DataChainKit brings valuable data chain information to apps and services through a wide range of data-chains that can help you to be up-to-date and prepared. It's easy to use the DataChainKit with an Open Source Software package, which can easily be deployed via helm or docker-compose.

----

# DataChainKit Elements

## API Description
For each App or Service Provider can use this documentation to implement their own instance to participate within the Catena-X
- 📄 [IRS REST Api Documentation](http://irs.dev.demo.catena-x.net/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config)
- 📄 [IRS Specification](https://confluence.catena-x.net/x/NLGAAQ) (only for consortia members accessible)

## Item Relationship Service
The Item Relationship Service (IRS) is the first reference application of the DataChainKit. It is built with the Java Spring framework. It is dockerized and can easily be deployed via helm or docker compose on infrastructure you choose.

### Requirements
The IRS needs to be deployed with the following components in the same environment: 
- [Eclipse Dataspace Connector](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector)
- AAS Wrapper 

The Service interacts with the Catena-X [Digital Twin Registry](https://catena-x.net/de/angebote/digitaler-zwilling) and is built upon the Catena-X Semantic Models which are stored in the [Semantic Hub](https://catena-x.net/de/angebote/digitaler-zwilling). 

### Tools and documentation

- 🖥 Item Relationship Service Video
- 📄 [Item Relationship Service Product Page](https://catena-x.net/en/angebote/item-relationship-service)
- 📄 [Item Relationship Service Documentation](https://github.com/catenax-ng/product-item-relationship-service/blob/main/README.md)
- 📄 [IRS REST Api Documentation](http://irs.dev.demo.catena-x.net/api/swagger-ui/index.html?configUrl=/api/api-docs/swagger-config)
- 📄 Catena-X Kit's

