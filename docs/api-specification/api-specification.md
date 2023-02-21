API specification
=================

Table of Contents

-   [OpenAPI Specification](#openapi-specification)

OpenAPI Specification
---------------------

IRS API
=======

The API of the Item Relationship Service (IRS) for retrieving item graphs along the value chain of CATENA-X partners.

More information: <https://openapi-generator.tech>

Contact Info: [team@openapitools.org](team@openapitools.org)

Version: 2.0

BasePath:

All rights reserved

http://apache.org/licenses/LICENSE-2.0.html

Access
------

1.  OAuth AuthorizationUrl:TokenUrl:http://localhost

<span id="__Methods">Methods</span>
-----------------------------------

\[ Jump to [Models](#-Models) \]

### Table of Contents

#### [AspectModels](#AspectModels)

-   [`get /irs/aspectmodels`](#getAllAspectModels)

#### [ItemRelationshipService](#ItemRelationshipService)

-   [`put /irs/jobs/{id}`](#cancelJobByJobId)
-   [`get /irs/jobs/{id}`](#getJobForJobId)
-   [`get /irs/jobs`](#getJobsByJobStates)
-   [`post /irs/jobs`](#registerJobForGlobalAssetId)

<span id="AspectModels">AspectModels</span>
===========================================

<span id="getAllAspectModels"></span>

<a href="#__Methods" class="up">Up</a>

    get /irs/aspectmodels

Get all available aspect models from semantic hub or local models. (<span class="nickname">getAllAspectModels</span>)

Get all available aspect models from semantic hub or local models.

### Return type

[AspectModels](#AspectModels)

### Example data

Content-Type: application/json

    {
      "lastUpdated" : "lastUpdated",
      "models" : [ {
        "urn" : "urn",
        "name" : "name",
        "type" : "type",
        "version" : "version",
        "status" : "status"
      }, {
        "urn" : "urn",
        "name" : "name",
        "type" : "type",
        "version" : "version",
        "status" : "status"
      } ]
    }

### Produces

This API call produces the following media types according to the <span class="header">Accept</span> request header;
the media type will be conveyed by the <span class="header">Content-Type</span> response header.

-   `application/json`

### Responses

#### 200

Returns all available aspect models.
[AspectModels](#AspectModels)

#### 401

Authorized failed.
[ErrorResponse](#ErrorResponse)

#### 403

Authorized failed.
[ErrorResponse](#ErrorResponse)

------------------------------------------------------------------------

<span id="ItemRelationshipService">ItemRelationshipService</span>
=================================================================

<span id="cancelJobByJobId"></span>

<a href="#__Methods" class="up">Up</a>

    put /irs/jobs/{id}

Cancel job for requested jobId. (<span class="nickname">cancelJobByJobId</span>)

Cancel job for requested jobId.

### Path parameters

id (required)

<span class="param-type">Path Parameter</span> — Id of the job. default: null format: uuid

### Return type

[Job](#Job)

### Example data

Content-Type: \*/\*

    {
      "exception" : {
        "exception" : "exception",
        "errorDetail" : "errorDetail",
        "exceptionDate" : "2000-01-23T04:56:07.000+00:00"
      },
      "owner" : "owner",
      "summary" : {
        "bpnLookups" : {
          "running" : 1280358508,
          "completed" : 1294386358,
          "failed" : 314780940
        },
        "asyncFetchedItems" : {
          "running" : 1280358508,
          "completed" : 1294386358,
          "failed" : 314780940
        }
      },
      "completedOn" : "2000-01-23T04:56:07.000+00:00",
      "parameter" : {
        "depth" : 171976544,
        "bomLifecycle" : "asBuilt",
        "collectAspects" : true,
        "aspects" : "aspects",
        "lookupBPNs" : true,
        "callbackUrl" : "callbackUrl",
        "direction" : "upward"
      },
      "globalAssetId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
      "id" : "046b6c7f-0b8a-43b9-b35d-6489e6daee91",
      "state" : "UNSAVED",
      "createdOn" : "2000-01-23T04:56:07.000+00:00",
      "startedOn" : "2000-01-23T04:56:07.000+00:00",
      "lastModifiedOn" : "2000-01-23T04:56:07.000+00:00"
    }

### Example data

Content-Type: application/json

    {
      "exception" : {
        "exception" : "exception",
        "errorDetail" : "errorDetail",
        "exceptionDate" : "2000-01-23T04:56:07.000+00:00"
      },
      "owner" : "owner",
      "summary" : {
        "bpnLookups" : {
          "running" : 1280358508,
          "completed" : 1294386358,
          "failed" : 314780940
        },
        "asyncFetchedItems" : {
          "running" : 1280358508,
          "completed" : 1294386358,
          "failed" : 314780940
        }
      },
      "completedOn" : "2000-01-23T04:56:07.000+00:00",
      "parameter" : {
        "depth" : 171976544,
        "bomLifecycle" : "asBuilt",
        "collectAspects" : true,
        "aspects" : "aspects",
        "lookupBPNs" : true,
        "callbackUrl" : "callbackUrl",
        "direction" : "upward"
      },
      "globalAssetId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
      "id" : "046b6c7f-0b8a-43b9-b35d-6489e6daee91",
      "state" : "UNSAVED",
      "createdOn" : "2000-01-23T04:56:07.000+00:00",
      "startedOn" : "2000-01-23T04:56:07.000+00:00",
      "lastModifiedOn" : "2000-01-23T04:56:07.000+00:00"
    }

### Produces

This API call produces the following media types according to the <span class="header">Accept</span> request header;
the media type will be conveyed by the <span class="header">Content-Type</span> response header.

-   `*/*`
-   `application/json`

### Responses

#### 200

Job with requested jobId canceled.
[Job](#Job)

#### 400

Cancel job failed.
[ErrorResponse](#ErrorResponse)

#### 401

Authorized failed.
[ErrorResponse](#ErrorResponse)

#### 403

Authorized failed.
[ErrorResponse](#ErrorResponse)

#### 404

Job for requested jobId not found.
[ErrorResponse](#ErrorResponse)

------------------------------------------------------------------------

<span id="getJobForJobId"></span>

<a href="#__Methods" class="up">Up</a>

    get /irs/jobs/{id}

Return job with optional item graph result for requested id. (<span class="nickname">getJobForJobId</span>)

Return job with optional item graph result for requested id.

### Path parameters

id (required)

<span class="param-type">Path Parameter</span> — Id of the job. default: null format: uuid

### Query parameters

returnUncompletedJob (optional)

<span class="param-type">Query Parameter</span> — Return job with current processed item graph. Return job with item graph if job is in state , otherwise job. default: true

### Return type

[Jobs](#Jobs)

### Example data

Content-Type: application/json

    {
      "relationships" : [ {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "aspectType" : "aspectType",
        "linkedItem" : {
          "quantity" : {
            "quantityNumber" : 1210617418.2099607,
            "measurementUnit" : {
              "datatypeURI" : "datatypeURI",
              "lexicalValue" : "lexicalValue"
            }
          },
          "lifecycleContext" : "asBuilt",
          "assembledOn" : "2000-01-23T04:56:07.000+00:00",
          "childCatenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
          "lastModifiedOn" : "2000-01-23T04:56:07.000+00:00"
        }
      }, {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "aspectType" : "aspectType",
        "linkedItem" : {
          "quantity" : {
            "quantityNumber" : 1210617418.2099607,
            "measurementUnit" : {
              "datatypeURI" : "datatypeURI",
              "lexicalValue" : "lexicalValue"
            }
          },
          "lifecycleContext" : "asBuilt",
          "assembledOn" : "2000-01-23T04:56:07.000+00:00",
          "childCatenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
          "lastModifiedOn" : "2000-01-23T04:56:07.000+00:00"
        }
      }, {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "aspectType" : "aspectType",
        "linkedItem" : {
          "quantity" : {
            "quantityNumber" : 1210617418.2099607,
            "measurementUnit" : {
              "datatypeURI" : "datatypeURI",
              "lexicalValue" : "lexicalValue"
            }
          },
          "lifecycleContext" : "asBuilt",
          "assembledOn" : "2000-01-23T04:56:07.000+00:00",
          "childCatenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
          "lastModifiedOn" : "2000-01-23T04:56:07.000+00:00"
        }
      }, {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "aspectType" : "aspectType",
        "linkedItem" : {
          "quantity" : {
            "quantityNumber" : 1210617418.2099607,
            "measurementUnit" : {
              "datatypeURI" : "datatypeURI",
              "lexicalValue" : "lexicalValue"
            }
          },
          "lifecycleContext" : "asBuilt",
          "assembledOn" : "2000-01-23T04:56:07.000+00:00",
          "childCatenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
          "lastModifiedOn" : "2000-01-23T04:56:07.000+00:00"
        }
      }, {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "aspectType" : "aspectType",
        "linkedItem" : {
          "quantity" : {
            "quantityNumber" : 1210617418.2099607,
            "measurementUnit" : {
              "datatypeURI" : "datatypeURI",
              "lexicalValue" : "lexicalValue"
            }
          },
          "lifecycleContext" : "asBuilt",
          "assembledOn" : "2000-01-23T04:56:07.000+00:00",
          "childCatenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
          "lastModifiedOn" : "2000-01-23T04:56:07.000+00:00"
        }
      } ],
      "tombstones" : [ {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "endpointURL" : "endpointURL",
        "processingError" : {
          "retryCounter" : 494379917,
          "errorDetail" : "errorDetail",
          "lastAttempt" : "2000-01-23T04:56:07.000+00:00",
          "processStep" : "SUBMODEL_REQUEST"
        }
      }, {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "endpointURL" : "endpointURL",
        "processingError" : {
          "retryCounter" : 494379917,
          "errorDetail" : "errorDetail",
          "lastAttempt" : "2000-01-23T04:56:07.000+00:00",
          "processStep" : "SUBMODEL_REQUEST"
        }
      }, {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "endpointURL" : "endpointURL",
        "processingError" : {
          "retryCounter" : 494379917,
          "errorDetail" : "errorDetail",
          "lastAttempt" : "2000-01-23T04:56:07.000+00:00",
          "processStep" : "SUBMODEL_REQUEST"
        }
      }, {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "endpointURL" : "endpointURL",
        "processingError" : {
          "retryCounter" : 494379917,
          "errorDetail" : "errorDetail",
          "lastAttempt" : "2000-01-23T04:56:07.000+00:00",
          "processStep" : "SUBMODEL_REQUEST"
        }
      }, {
        "catenaXId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "endpointURL" : "endpointURL",
        "processingError" : {
          "retryCounter" : 494379917,
          "errorDetail" : "errorDetail",
          "lastAttempt" : "2000-01-23T04:56:07.000+00:00",
          "processStep" : "SUBMODEL_REQUEST"
        }
      } ],
      "shells" : [ {
        "identification" : "identification",
        "idShort" : "idShort",
        "specificAssetIds" : [ {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        } ],
        "administration" : {
          "version" : "version",
          "revision" : "revision"
        },
        "description" : [ {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        } ],
        "submodelDescriptors" : [ {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        } ],
        "globalAssetId" : {
          "value" : [ "value", "value", "value", "value", "value" ]
        }
      }, {
        "identification" : "identification",
        "idShort" : "idShort",
        "specificAssetIds" : [ {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        } ],
        "administration" : {
          "version" : "version",
          "revision" : "revision"
        },
        "description" : [ {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        } ],
        "submodelDescriptors" : [ {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        } ],
        "globalAssetId" : {
          "value" : [ "value", "value", "value", "value", "value" ]
        }
      }, {
        "identification" : "identification",
        "idShort" : "idShort",
        "specificAssetIds" : [ {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        } ],
        "administration" : {
          "version" : "version",
          "revision" : "revision"
        },
        "description" : [ {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        } ],
        "submodelDescriptors" : [ {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        } ],
        "globalAssetId" : {
          "value" : [ "value", "value", "value", "value", "value" ]
        }
      }, {
        "identification" : "identification",
        "idShort" : "idShort",
        "specificAssetIds" : [ {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        } ],
        "administration" : {
          "version" : "version",
          "revision" : "revision"
        },
        "description" : [ {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        } ],
        "submodelDescriptors" : [ {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        } ],
        "globalAssetId" : {
          "value" : [ "value", "value", "value", "value", "value" ]
        }
      }, {
        "identification" : "identification",
        "idShort" : "idShort",
        "specificAssetIds" : [ {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        }, {
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "value" : "value",
          "key" : "key",
          "subjectId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          }
        } ],
        "administration" : {
          "version" : "version",
          "revision" : "revision"
        },
        "description" : [ {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        }, {
          "language" : "language",
          "text" : "text"
        } ],
        "submodelDescriptors" : [ {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        }, {
          "endpoints" : [ {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          }, {
            "interface" : "interface",
            "protocolInformation" : {
              "subprotocolBodyEncoding" : "subprotocolBodyEncoding",
              "endpointAddress" : "endpointAddress",
              "subprotocol" : "subprotocol",
              "endpointProtocolVersion" : "endpointProtocolVersion",
              "subprotocolBody" : "subprotocolBody",
              "endpointProtocol" : "endpointProtocol"
            }
          } ],
          "semanticId" : {
            "value" : [ "value", "value", "value", "value", "value" ]
          },
          "identification" : "identification",
          "idShort" : "idShort",
          "administration" : {
            "version" : "version",
            "revision" : "revision"
          },
          "description" : [ {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          }, {
            "language" : "language",
            "text" : "text"
          } ]
        } ],
        "globalAssetId" : {
          "value" : [ "value", "value", "value", "value", "value" ]
        }
      } ],
      "job" : {
        "exception" : {
          "exception" : "exception",
          "errorDetail" : "errorDetail",
          "exceptionDate" : "2000-01-23T04:56:07.000+00:00"
        },
        "owner" : "owner",
        "summary" : {
          "bpnLookups" : {
            "running" : 1280358508,
            "completed" : 1294386358,
            "failed" : 314780940
          },
          "asyncFetchedItems" : {
            "running" : 1280358508,
            "completed" : 1294386358,
            "failed" : 314780940
          }
        },
        "completedOn" : "2000-01-23T04:56:07.000+00:00",
        "parameter" : {
          "depth" : 171976544,
          "bomLifecycle" : "asBuilt",
          "collectAspects" : true,
          "aspects" : "aspects",
          "lookupBPNs" : true,
          "callbackUrl" : "callbackUrl",
          "direction" : "upward"
        },
        "globalAssetId" : "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0",
        "id" : "046b6c7f-0b8a-43b9-b35d-6489e6daee91",
        "state" : "UNSAVED",
        "createdOn" : "2000-01-23T04:56:07.000+00:00",
        "startedOn" : "2000-01-23T04:56:07.000+00:00",
        "lastModifiedOn" : "2000-01-23T04:56:07.000+00:00"
      },
      "bpns" : [ {
        "manufacturerName" : "manufacturerName",
        "manufacturerId" : "manufacturerId"
      }, {
        "manufacturerName" : "manufacturerName",
        "manufacturerId" : "manufacturerId"
      }, {
        "manufacturerName" : "manufacturerName",
        "manufacturerId" : "manufacturerId"
      }, {
        "manufacturerName" : "manufacturerName",
        "manufacturerId" : "manufacturerId"
      }, {
        "manufacturerName" : "manufacturerName",
        "manufacturerId" : "manufacturerId"
      } ],
      "submodels" : [ {
        "identification" : "identification",
        "payload" : {
          "key" : "{}"
        },
        "aspectType" : "aspectType"
      }, {
        "identification" : "identification",
        "payload" : {
          "key" : "{}"
        },
        "aspectType" : "aspectType"
      }, {
        "identification" : "identification",
        "payload" : {
          "key" : "{}"
        },
        "aspectType" : "aspectType"
      }, {
        "identification" : "identification",
        "payload" : {
          "key" : "{}"
        },
        "aspectType" : "aspectType"
      }, {
        "identification" : "identification",
        "payload" : {
          "key" : "{}"
        },
        "aspectType" : "aspectType"
      } ]
    }

### Produces

This API call produces the following media types according to the <span class="header">Accept</span> request header;
the media type will be conveyed by the <span class="header">Content-Type</span> response header.

-   `application/json`

### Responses

#### 200

Return job with item graph for the requested id.
[Jobs](#Jobs)

#### 206

Return job with current processed item graph for the requested id.
[Jobs](#Jobs)

#### 400

Return job failed.
[ErrorResponse](#ErrorResponse)

#### 401

Authorized failed.
[ErrorResponse](#ErrorResponse)

#### 403

Authorized failed.
[ErrorResponse](#ErrorResponse)

#### 404

Job with the requested jobId not found.
[ErrorResponse](#ErrorResponse)

------------------------------------------------------------------------

<span id="getJobsByJobStates"></span>

<a href="#__Methods" class="up">Up</a>

    get /irs/jobs

Returns paginated jobs with state and execution times. (<span class="nickname">getJobsByJobStates</span>)

Returns paginated jobs with state and execution times.

### Query parameters

states (optional)

<span class="param-type">Query Parameter</span> — Requested job states. default: null

page (optional)

<span class="param-type">Query Parameter</span> — Zero-based page index (0..N) default: 0

size (optional)

<span class="param-type">Query Parameter</span> — The size of the page to be returned default: 20

sort (optional)

<span class="param-type">Query Parameter</span> — Sorting criteria in the format: property,(asc|desc). Default sort order is ascending. Multiple sort criteria are supported. default: null

### Return type

[PageResult](#PageResult)

### Example data

Content-Type: application/json

    {
      "pageCount" : 0,
      "pageNumber" : 6,
      "pageSize" : 1,
      "content" : [ {
        "completedOn" : "2000-01-23T04:56:07.000+00:00",
        "id" : "046b6c7f-0b8a-43b9-b35d-6489e6daee91",
        "state" : "UNSAVED",
        "startedOn" : "2000-01-23T04:56:07.000+00:00"
      }, {
        "completedOn" : "2000-01-23T04:56:07.000+00:00",
        "id" : "046b6c7f-0b8a-43b9-b35d-6489e6daee91",
        "state" : "UNSAVED",
        "startedOn" : "2000-01-23T04:56:07.000+00:00"
      } ],
      "totalElements" : 5
    }

### Produces

This API call produces the following media types according to the <span class="header">Accept</span> request header;
the media type will be conveyed by the <span class="header">Content-Type</span> response header.

-   `application/json`

### Responses

#### 200

Paginated list of jobs with state and execution times for requested job states.
[PageResult](#PageResult)

#### 400

Return jobs for requested job states failed.
[ErrorResponse](#ErrorResponse)

#### 401

Authorized failed.
[ErrorResponse](#ErrorResponse)

#### 403

Authorized failed.
[ErrorResponse](#ErrorResponse)

------------------------------------------------------------------------

<span id="registerJobForGlobalAssetId"></span>

<a href="#__Methods" class="up">Up</a>

    post /irs/jobs

Register an IRS job to retrieve an item graph for given {globalAssetId}. (<span class="nickname">registerJobForGlobalAssetId</span>)

Register an IRS job to retrieve an item graph for given {globalAssetId}.

### Consumes

This API call consumes the following media types via the <span class="header">Content-Type</span> request header:

-   `application/json`

### Request body

RegisterJob [RegisterJob](#RegisterJob) (required)

<span class="param-type">Body Parameter</span> —

### Return type

[JobHandle](#JobHandle)

### Example data

Content-Type: application/json

    {
      "id" : "046b6c7f-0b8a-43b9-b35d-6489e6daee91"
    }

### Produces

This API call produces the following media types according to the <span class="header">Accept</span> request header;
the media type will be conveyed by the <span class="header">Content-Type</span> response header.

-   `application/json`

### Responses

#### 201

Returns id of registered job.
[JobHandle](#JobHandle)

#### 400

Job registration failed.
[ErrorResponse](#ErrorResponse)

#### 401

Authorized failed.
[ErrorResponse](#ErrorResponse)

#### 403

Authorized failed.
[ErrorResponse](#ErrorResponse)

------------------------------------------------------------------------

<span id="__Models">Models</span>
---------------------------------

\[ Jump to [Methods](#-Methods) \]

### Table of Contents

1.  [`AdministrativeInformation` -](#AdministrativeInformation)
2.  [`AspectModel` -](#AspectModel)
3.  [`AspectModels` -](#AspectModels)
4.  [`AssetAdministrationShellDescriptor` -](#AssetAdministrationShellDescriptor)
5.  [`AsyncFetchedItems` -](#AsyncFetchedItems)
6.  [`Bpn` -](#Bpn)
7.  [`Endpoint` -](#Endpoint)
8.  [`ErrorResponse` -](#ErrorResponse)
9.  [`IdentifierKeyValuePair` -](#IdentifierKeyValuePair)
10. [`Job` -](#Job)
11. [`JobErrorDetails` -](#JobErrorDetails)
12. [`JobHandle` -](#JobHandle)
13. [`JobParameter` -](#JobParameter)
14. [`JobStatusResult` -](#JobStatusResult)
15. [`Jobs` -](#Jobs)
16. [`LangString` -](#LangString)
17. [`LinkedItem` -](#LinkedItem)
18. [`MeasurementUnit` -](#MeasurementUnit)
19. [`PageResult` -](#PageResult)
20. [`ProcessingError` -](#ProcessingError)
21. [`ProtocolInformation` -](#ProtocolInformation)
22. [`Quantity` -](#Quantity)
23. [`Reference` -](#Reference)
24. [`RegisterJob` -](#RegisterJob)
25. [`Relationship` -](#Relationship)
26. [`Submodel` -](#Submodel)
27. [`SubmodelDescriptor` -](#SubmodelDescriptor)
28. [`Summary` -](#Summary)
29. [`Tombstone` -](#Tombstone)

### <span id="AdministrativeInformation">`AdministrativeInformation` -</span> <a href="#__Models" class="up">Up</a>

revision (optional)

<span class="param-type">[String](#string)</span>

version (optional)

<span class="param-type">[String](#string)</span>

### <span id="AspectModel">`AspectModel` -</span> <a href="#__Models" class="up">Up</a>

name (optional)

<span class="param-type">[String](#string)</span>

status (optional)

<span class="param-type">[String](#string)</span>

type (optional)

<span class="param-type">[String](#string)</span>

urn (optional)

<span class="param-type">[String](#string)</span>

version (optional)

<span class="param-type">[String](#string)</span>

### <span id="AspectModels">`AspectModels` -</span> <a href="#__Models" class="up">Up</a>

lastUpdated (optional)

<span class="param-type">[String](#string)</span>

models (optional)

<span class="param-type">[array\[AspectModel\]](#AspectModel)</span>

### <span id="AssetAdministrationShellDescriptor">`AssetAdministrationShellDescriptor` -</span> <a href="#__Models" class="up">Up</a>

AAS shells.

administration (optional)

<span class="param-type">[AdministrativeInformation](#AdministrativeInformation)</span>

description (optional)

<span class="param-type">[array\[LangString\]](#LangString)</span>

globalAssetId (optional)

<span class="param-type">[Reference](#Reference)</span>

idShort (optional)

<span class="param-type">[String](#string)</span>

identification (optional)

<span class="param-type">[String](#string)</span>

specificAssetIds (optional)

<span class="param-type">[array\[IdentifierKeyValuePair\]](#IdentifierKeyValuePair)</span>

submodelDescriptors (optional)

<span class="param-type">[array\[SubmodelDescriptor\]](#SubmodelDescriptor)</span>

### <span id="AsyncFetchedItems">`AsyncFetchedItems` -</span> <a href="#__Models" class="up">Up</a>

Statistics of job execution.

completed (optional)

<span class="param-type">[Integer](#integer)</span> Number of completed item transfers. format: int32

failed (optional)

<span class="param-type">[Integer](#integer)</span> Number of failed item transfers. format: int32

running (optional)

<span class="param-type">[Integer](#integer)</span> Number of running item transfers. format: int32

### <span id="Bpn">`Bpn` -</span> <a href="#__Models" class="up">Up</a>

Business partner id with name

manufacturerId (optional)

<span class="param-type">[String](#string)</span>

manufacturerName (optional)

<span class="param-type">[String](#string)</span>

### <span id="Endpoint">`Endpoint` -</span> <a href="#__Models" class="up">Up</a>

interface (optional)

<span class="param-type">[String](#string)</span>

protocolInformation (optional)

<span class="param-type">[ProtocolInformation](#ProtocolInformation)</span>

### <span id="ErrorResponse">`ErrorResponse` -</span> <a href="#__Models" class="up">Up</a>

Error response.

messages (optional)

<span class="param-type">[array\[String\]](#string)</span> List of error messages.

error (optional)

<span class="param-type">[String](#string)</span> Error.

statusCode (optional)

<span class="param-type">[String](#string)</span> Error code.

Enum:

100 CONTINUE

101 SWITCHING\_PROTOCOLS

102 PROCESSING

103 CHECKPOINT

200 OK

201 CREATED

202 ACCEPTED

203 NON\_AUTHORITATIVE\_INFORMATION

204 NO\_CONTENT

205 RESET\_CONTENT

206 PARTIAL\_CONTENT

207 MULTI\_STATUS

208 ALREADY\_REPORTED

226 IM\_USED

300 MULTIPLE\_CHOICES

301 MOVED\_PERMANENTLY

302 FOUND

302 MOVED\_TEMPORARILY

303 SEE\_OTHER

304 NOT\_MODIFIED

305 USE\_PROXY

307 TEMPORARY\_REDIRECT

308 PERMANENT\_REDIRECT

400 BAD\_REQUEST

401 UNAUTHORIZED

402 PAYMENT\_REQUIRED

403 FORBIDDEN

404 NOT\_FOUND

405 METHOD\_NOT\_ALLOWED

406 NOT\_ACCEPTABLE

407 PROXY\_AUTHENTICATION\_REQUIRED

408 REQUEST\_TIMEOUT

409 CONFLICT

410 GONE

411 LENGTH\_REQUIRED

412 PRECONDITION\_FAILED

413 PAYLOAD\_TOO\_LARGE

413 REQUEST\_ENTITY\_TOO\_LARGE

414 URI\_TOO\_LONG

414 REQUEST\_URI\_TOO\_LONG

415 UNSUPPORTED\_MEDIA\_TYPE

416 REQUESTED\_RANGE\_NOT\_SATISFIABLE

417 EXPECTATION\_FAILED

418 I\_AM\_A\_TEAPOT

419 INSUFFICIENT\_SPACE\_ON\_RESOURCE

420 METHOD\_FAILURE

421 DESTINATION\_LOCKED

422 UNPROCESSABLE\_ENTITY

423 LOCKED

424 FAILED\_DEPENDENCY

425 TOO\_EARLY

426 UPGRADE\_REQUIRED

428 PRECONDITION\_REQUIRED

429 TOO\_MANY\_REQUESTS

431 REQUEST\_HEADER\_FIELDS\_TOO\_LARGE

451 UNAVAILABLE\_FOR\_LEGAL\_REASONS

500 INTERNAL\_SERVER\_ERROR

501 NOT\_IMPLEMENTED

502 BAD\_GATEWAY

503 SERVICE\_UNAVAILABLE

504 GATEWAY\_TIMEOUT

505 HTTP\_VERSION\_NOT\_SUPPORTED

506 VARIANT\_ALSO\_NEGOTIATES

507 INSUFFICIENT\_STORAGE

508 LOOP\_DETECTED

509 BANDWIDTH\_LIMIT\_EXCEEDED

510 NOT\_EXTENDED

511 NETWORK\_AUTHENTICATION\_REQUIRED

### <span id="IdentifierKeyValuePair">`IdentifierKeyValuePair` -</span> <a href="#__Models" class="up">Up</a>

key (optional)

<span class="param-type">[String](#string)</span>

semanticId (optional)

<span class="param-type">[Reference](#Reference)</span>

subjectId (optional)

<span class="param-type">[Reference](#Reference)</span>

value (optional)

<span class="param-type">[String](#string)</span>

### <span id="Job">`Job` -</span> <a href="#__Models" class="up">Up</a>

Executable unit with meta information and item graph result.

completedOn (optional)

<span class="param-type">[Date](#DateTime)</span> format: date-time

createdOn (optional)

<span class="param-type">[Date](#DateTime)</span> format: date-time

exception (optional)

<span class="param-type">[JobErrorDetails](#JobErrorDetails)</span>

globalAssetId

<span class="param-type">[String](#string)</span> Part global unique id in the format urn:uuid:uuid4.

id

<span class="param-type">[UUID](#UUID)</span> Id of the job. format: uuid

lastModifiedOn (optional)

<span class="param-type">[Date](#DateTime)</span> format: date-time

owner (optional)

<span class="param-type">[String](#string)</span> The IRS api consumer.

parameter (optional)

<span class="param-type">[JobParameter](#JobParameter)</span>

startedOn (optional)

<span class="param-type">[Date](#DateTime)</span> format: date-time

state

<span class="param-type">[String](#string)</span>

Enum:

UNSAVED

INITIAL

RUNNING

TRANSFERS\_FINISHED

COMPLETED

CANCELED

ERROR

summary (optional)

<span class="param-type">[Summary](#Summary)</span>

### <span id="JobErrorDetails">`JobErrorDetails` -</span> <a href="#__Models" class="up">Up</a>

Job error details.

errorDetail (optional)

<span class="param-type">[String](#string)</span> Detailed exception information.

exception (optional)

<span class="param-type">[String](#string)</span> Exception name.

exceptionDate (optional)

<span class="param-type">[Date](#DateTime)</span> Datetime error occurs. format: date-time

### <span id="JobHandle">`JobHandle` -</span> <a href="#__Models" class="up">Up</a>

id (optional)

<span class="param-type">[UUID](#UUID)</span> Id of the job. format: uuid

### <span id="JobParameter">`JobParameter` -</span> <a href="#__Models" class="up">Up</a>

Job parameter of job processing.

aspects (optional)

<span class="param-type">[String](#string)</span>

bomLifecycle (optional)

<span class="param-type">[String](#string)</span> The lifecycle context in which the child part was assembled into the parent part.

Enum:

asBuilt

asPlanned

callbackUrl (optional)

<span class="param-type">[String](#string)</span>

collectAspects (optional)

<span class="param-type">[Boolean](#boolean)</span>

depth (optional)

<span class="param-type">[Integer](#integer)</span> format: int32

direction (optional)

<span class="param-type">[String](#string)</span> Item graph traversal direction.

Enum:

upward

downward

lookupBPNs (optional)

<span class="param-type">[Boolean](#boolean)</span>

### <span id="JobStatusResult">`JobStatusResult` -</span> <a href="#__Models" class="up">Up</a>

completedOn (optional)

<span class="param-type">[Date](#DateTime)</span> format: date-time

id (optional)

<span class="param-type">[UUID](#UUID)</span> format: uuid

startedOn (optional)

<span class="param-type">[Date](#DateTime)</span> format: date-time

state (optional)

<span class="param-type">[String](#string)</span>

Enum:

UNSAVED

INITIAL

RUNNING

TRANSFERS\_FINISHED

COMPLETED

CANCELED

ERROR

### <span id="Jobs">`Jobs` -</span> <a href="#__Models" class="up">Up</a>

Container for a job with item graph.

bpns (optional)

<span class="param-type">[set\[Bpn\]](#Bpn)</span> Collection of bpn mappings

job (optional)

<span class="param-type">[Job](#Job)</span>

relationships (optional)

<span class="param-type">[array\[Relationship\]](#Relationship)</span> Relationships between parent and child items.

shells (optional)

<span class="param-type">[array\[AssetAdministrationShellDescriptor\]](#AssetAdministrationShellDescriptor)</span> AAS shells.

submodels (optional)

<span class="param-type">[array\[Submodel\]](#Submodel)</span> Collection of requested Submodels

tombstones (optional)

<span class="param-type">[array\[Tombstone\]](#Tombstone)</span> Collection of not resolvable endpoints as tombstones. Including cause of error and endpoint URL.

### <span id="LangString">`LangString` -</span> <a href="#__Models" class="up">Up</a>

language (optional)

<span class="param-type">[String](#string)</span>

text (optional)

<span class="param-type">[String](#string)</span>

### <span id="LinkedItem">`LinkedItem` -</span> <a href="#__Models" class="up">Up</a>

Set of child parts the parent object is assembled by (one structural level down).

assembledOn (optional)

<span class="param-type">[Date](#DateTime)</span> Datetime of assembly. format: date-time

childCatenaXId (optional)

<span class="param-type">[String](#string)</span> CatenaX child global asset id in the format urn:uuid:uuid4.

lastModifiedOn (optional)

<span class="param-type">[Date](#DateTime)</span> Last datetime item was modified. format: date-time

lifecycleContext (optional)

<span class="param-type">[String](#string)</span> The lifecycle context in which the child part was assembled into the parent part.

Enum:

asBuilt

asPlanned

quantity (optional)

<span class="param-type">[Quantity](#Quantity)</span>

### <span id="MeasurementUnit">`MeasurementUnit` -</span> <a href="#__Models" class="up">Up</a>

datatypeURI (optional)

<span class="param-type">[String](#string)</span>

lexicalValue (optional)

<span class="param-type">[String](#string)</span>

### <span id="PageResult">`PageResult` -</span> <a href="#__Models" class="up">Up</a>

content (optional)

<span class="param-type">[array\[JobStatusResult\]](#JobStatusResult)</span>

pageCount (optional)

<span class="param-type">[Integer](#integer)</span> format: int32

pageNumber (optional)

<span class="param-type">[Integer](#integer)</span> format: int32

pageSize (optional)

<span class="param-type">[Integer](#integer)</span> format: int32

totalElements (optional)

<span class="param-type">[Integer](#integer)</span> format: int32

### <span id="ProcessingError">`ProcessingError` -</span> <a href="#__Models" class="up">Up</a>

errorDetail (optional)

<span class="param-type">[String](#string)</span>

lastAttempt (optional)

<span class="param-type">[Date](#DateTime)</span> format: date-time

processStep (optional)

<span class="param-type">[String](#string)</span>

Enum:

SUBMODEL\_REQUEST

DIGITAL\_TWIN\_REQUEST

SCHEMA\_VALIDATION

SCHEMA\_REQUEST

BPDM\_REQUEST

BPDM\_VALIDATION

retryCounter (optional)

<span class="param-type">[Integer](#integer)</span> format: int32

### <span id="ProtocolInformation">`ProtocolInformation` -</span> <a href="#__Models" class="up">Up</a>

endpointAddress (optional)

<span class="param-type">[String](#string)</span>

endpointProtocol (optional)

<span class="param-type">[String](#string)</span>

endpointProtocolVersion (optional)

<span class="param-type">[String](#string)</span>

subprotocol (optional)

<span class="param-type">[String](#string)</span>

subprotocolBody (optional)

<span class="param-type">[String](#string)</span>

subprotocolBodyEncoding (optional)

<span class="param-type">[String](#string)</span>

### <span id="Quantity">`Quantity` -</span> <a href="#__Models" class="up">Up</a>

Quantity component.

measurementUnit (optional)

<span class="param-type">[MeasurementUnit](#MeasurementUnit)</span>

quantityNumber (optional)

<span class="param-type">[Double](#double)</span> format: double

### <span id="Reference">`Reference` -</span> <a href="#__Models" class="up">Up</a>

value (optional)

<span class="param-type">[array\[String\]](#string)</span>

### <span id="RegisterJob">`RegisterJob` -</span> <a href="#__Models" class="up">Up</a>

The requested job definition.

aspects (optional)

<span class="param-type">[array\[String\]](#string)</span>

bomLifecycle (optional)

<span class="param-type">[String](#string)</span> The lifecycle context in which the child part was assembled into the parent part.

Enum:

asBuilt

asPlanned

callbackUrl (optional)

<span class="param-type">[String](#string)</span> Callback url to notify requestor when job processing is finished. There are two uri variable placeholders that can be used: jobId and jobState.

collectAspects (optional)

<span class="param-type">[Boolean](#boolean)</span> Flag to specify whether aspects should be requested and collected. Default is false.

depth (optional)

<span class="param-type">[Integer](#integer)</span> Max depth of the item graph returned. If no depth is set item graph with max depth is returned. format: int32

direction (optional)

<span class="param-type">[String](#string)</span> Item graph traversal direction.

Enum:

upward

downward

globalAssetId

<span class="param-type">[String](#string)</span> Id of global asset.

lookupBPNs (optional)

<span class="param-type">[Boolean](#boolean)</span> Flag to specify whether BPNs should be collected and resolved via the configured BPDM URL. Default is false.

### <span id="Relationship">`Relationship` -</span> <a href="#__Models" class="up">Up</a>

Relationships between parent and child items.

aspectType (optional)

<span class="param-type">[String](#string)</span>

catenaXId (optional)

<span class="param-type">[String](#string)</span> CATENA-X global asset id in the format urn:uuid:uuid4.

linkedItem (optional)

<span class="param-type">[LinkedItem](#LinkedItem)</span>

### <span id="Submodel">`Submodel` -</span> <a href="#__Models" class="up">Up</a>

Submodel with identification of SubmodelDescriptor, aspect type and payload as String

aspectType (optional)

<span class="param-type">[String](#string)</span>

identification (optional)

<span class="param-type">[String](#string)</span>

payload (optional)

<span class="param-type">[map\[String, Object\]](#object)</span>

### <span id="SubmodelDescriptor">`SubmodelDescriptor` -</span> <a href="#__Models" class="up">Up</a>

administration (optional)

<span class="param-type">[AdministrativeInformation](#AdministrativeInformation)</span>

description (optional)

<span class="param-type">[array\[LangString\]](#LangString)</span>

endpoints (optional)

<span class="param-type">[array\[Endpoint\]](#Endpoint)</span>

idShort (optional)

<span class="param-type">[String](#string)</span>

identification (optional)

<span class="param-type">[String](#string)</span>

semanticId (optional)

<span class="param-type">[Reference](#Reference)</span>

### <span id="Summary">`Summary` -</span> <a href="#__Models" class="up">Up</a>

Summary of the job with statistics of the job processing.

asyncFetchedItems (optional)

<span class="param-type">[AsyncFetchedItems](#AsyncFetchedItems)</span>

bpnLookups (optional)

<span class="param-type">[AsyncFetchedItems](#AsyncFetchedItems)</span>

### <span id="Tombstone">`Tombstone` -</span> <a href="#__Models" class="up">Up</a>

Tombstone with information about request failure

catenaXId (optional)

<span class="param-type">[String](#string)</span> CATENA-X global asset id in the format urn:uuid:uuid4.

endpointURL (optional)

<span class="param-type">[String](#string)</span>

processingError (optional)

<span class="param-type">[ProcessingError](#ProcessingError)</span>

Last updated 2023-02-21 15:46:11 UTC
