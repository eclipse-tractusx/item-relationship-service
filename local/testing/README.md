[[Back to main README](../../README.md)]

# REST Request Collection for Insomnia

A brief description of how to use the [Insomnia](https://insomnia.rest/) request collection with environments.


## Import a Request Collection

You can use the import functionality of [Insomnia](https://insomnia.rest/) (_Create from File_ > _Select File_). 
Import the file ([IRS_Request-Collection.json](IRS_Request_Collection.json)) as a Request Collection. 

## Export a Request Collection

To export the request collection you can click the _"Export Data"_ button under _Application > Preferences > Data_. 
There only the collection should be selected for the export.

#### Important Note 

It is important not to export environments used in the collection, 
since these are usually exported automatically with the collection. 
To prevent this, each environment must be created as a **_private_** environment.

## Import an Environment

To import one of the environments open the desired environment file and copy the whole content. 
Create a new **_private_** Environment for your collection and paste it in the created file.
This is important so that it is not included if you need to export the collection.

## Export an Environment

If an environment has been changed to update it for the project, 
it is suggested to replace or add the content of the original environment file.

## Reason for this Procedure

If you want to save environments and the request collection separately from each other (as this project currently does),
the Insomnia Exporter currently does not offer the possibility to export them separately from each other. 
Therefore, this procedure is suggested.

## Setup

Once the collection and the desired environment(s) have been imported, the environments must be configured:
dd credentials (_Client_ID_, _Client_Secret_) to automatically fetch and use an OAuth2-Access token before each request. 
Additionally, it is possible to generate one manually by selecting a request and clicking on _"Fetch Token"_.

Some requests consist of prompts which you need to provide data. 
The last input will be remembered and filled in advance for the next request.
