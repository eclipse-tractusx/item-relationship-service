[[Back to main README](../../README.md)]

# README

A brief description of how to use the [insomnia](https://insomnia.rest/) request collection with environments.


## Import a Request Collection
You can use the import functionality of insomnia (Create from File > select File). Import the file (IRS_Request-Collection.json) as a Request Collection. 

## Export a Request Collection
To export the request collection you can click the "Export Data" button under Application > Preferences > Data. There only the collection should be selected for the export.

#### Note 
It is important not to export environments used in the collection, since these are usually exported automatically with the collection. To prevent this, each environment must be created as a private environment.

## Import an Environment
To import one of the environments open the desired environment file and copy the whole content. Create a new **private** Environment (to make sure that it is not included if you need to export the collection) for your collection and paste it in the created file.

## Export an Environment
If an environment has been changed to update it for the project, it is suggested to replace or add the content of the original environment file.

## Reason for this procedure
If you want to save environments and the request collection separately from each other (as this project currently does), the Insomnia Exporter currently does not offer the possibility to export them separately from each other. Therefore, this procedure is suggested.

## Setup
Once the collection and the desired environment(s) have been imported, the environments must be configured. Add credentials (Client_ID, Client_Secret) to automatically fetch and use an OAuth2-Access token before each request. Additionally, it is possible to generate one manually by selecting a request and clicking on "Fetch Token".

Some requests consist of prompts which you need to provide data. The last input will be remembered and filled in advance for the next request.