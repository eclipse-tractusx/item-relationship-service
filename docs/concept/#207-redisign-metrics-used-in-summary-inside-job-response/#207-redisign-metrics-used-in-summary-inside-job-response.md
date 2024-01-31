# \[Concept\] \[#ID#\] Summary

| Key           | Value                                                                         |
|---------------|-------------------------------------------------------------------------------|
| Creation date | 31.01.2024                                                                    |
| Ticket Id     | #207 https://github.com/eclipse-tractusx/item-relationship-service/issues/207 |    
| State         | DRAFT                                                                         | 

## Table of Contents

1. [Overview](#overview)
2. [Problem Statement](#problem-statement)
3. [Concept](#concept)

## Overview
Currently summary section of a JobResponse contains information about asyncFetchedItems which covers DigitalTwin Registry requests responses and Submodel  Server requests responses.  
Results are shown together for both of mentioned responses:  
```
"summary": {
  "asyncFetchedItems": {
    "completed": 1,
    "failed": 0,
    "running": 0
  }
}
```

## Problem Statement
We would like to have separate summary response for  DigitalTwin Registry requests and Submodel Server.
Also there should be information about actual tree depth for given JobResponse.

## Concept
This diagram shows current flow:
![alt text](https://eclipse-tractusx.github.io/item-relationship-service/docs/arc42/architecture-constraints/execute-job.svg)

Steps number 4 and 6 are respectively requesting AAS and Submodel. Then both are stored in BlobStore by method `addTransferProcess` in `JobStore`.  
They should be distinguished by the type, so when step 12 (complete) is executed we can query the store for completed, failed and running items and add them to the response by type.
```
"summary": {
    "asyncFetchedItemsRegistry": {
        "running": 0,
        "completed": 1,
        "failed": 1
    },
   "asyncFetchedItemsSubmodelServer": {
        "running": 0,
        "completed": 1,
        "failed": 1
    }
},
``` 
Tree is stored as list of nodes in list of Relationships as filed `private List<Relationship> relationships;` in `ItemContainer`.  
Tree is assebmled in class `ItemTreesAssembler` in method `retrieveItemGraph`. 
We should get the depth of the tree and add it to the response. Consider using https://www.geeksforgeeks.org/depth-n-ary-tree/
