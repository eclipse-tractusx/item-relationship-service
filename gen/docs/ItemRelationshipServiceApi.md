# ItemRelationshipServiceApi

All URIs are relative to *https://api.server.test/api/v0.2*

Method | HTTP request | Description
------------- | ------------- | -------------
[**cancelJobForJobId**](ItemRelationshipServiceApi.md#cancelJobForJobId) | **PUT** /irs/jobs/{jobId}/cancel | 
[**getBOMForJobId**](ItemRelationshipServiceApi.md#getBOMForJobId) | **GET** /irs/jobs/{jobId} | 
[**getBomLifecycleByGlobalAssetId**](ItemRelationshipServiceApi.md#getBomLifecycleByGlobalAssetId) | **POST** /irs/items/{globalAssetId} | Get a BOM for a part
[**getJobsByProcessingState**](ItemRelationshipServiceApi.md#getJobsByProcessingState) | **GET** /irs/jobs/{processingState} | 


<a name="cancelJobForJobId"></a>
# **cancelJobForJobId**
> cancelJobForJobId(jobId)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemRelationshipServiceApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://api.server.test/api/v0.2");

    ItemRelationshipServiceApi apiInstance = new ItemRelationshipServiceApi(defaultClient);
    UUID jobId = new UUID(); // UUID | Id of the job in registry.
    try {
      apiInstance.cancelJobForJobId(jobId);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemRelationshipServiceApi#cancelJobForJobId");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **jobId** | [**UUID**](.md)| Id of the job in registry. |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Job with {jobId} was canceled |  -  |
**400** | Bad request. JobId must be a string in UUID format. |  -  |
**401** | Authorization information is missing or invalid. |  -  |
**404** | A job with the specified jobId was not found. |  -  |
**5XX** | Unexpected error. |  -  |

<a name="getBOMForJobId"></a>
# **getBOMForJobId**
> getBOMForJobId(jobId, returnUncompletedResultTree)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemRelationshipServiceApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://api.server.test/api/v0.2");

    ItemRelationshipServiceApi apiInstance = new ItemRelationshipServiceApi(defaultClient);
    UUID jobId = new UUID(); // UUID | Id of the job in registry. 
    Boolean returnUncompletedResultTree = true; // Boolean | If true, the endpoint returns the uncompleted results of the bom tree.
    try {
      apiInstance.getBOMForJobId(jobId, returnUncompletedResultTree);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemRelationshipServiceApi#getBOMForJobId");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **jobId** | [**UUID**](.md)| Id of the job in registry.  |
 **returnUncompletedResultTree** | **Boolean**| If true, the endpoint returns the uncompleted results of the bom tree. | [optional] [default to true]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | livecycle tree representation with the starting point of the given jobId |  -  |
**201** | job details for given jobId |  -  |
**206** | uncompleted livecycle tree representation with the starting point of the given jobId |  -  |
**404** | processing of job was canceled |  -  |
**417** | Processing of job failed |  -  |

<a name="getBomLifecycleByGlobalAssetId"></a>
# **getBomLifecycleByGlobalAssetId**
> getBomLifecycleByGlobalAssetId(globalAssetId, bomLifecycle, aspect, depth, direction)

Get a BOM for a part

Registers and starts a AAS crawler job for given {globalAssetId}

### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemRelationshipServiceApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://api.server.test/api/v0.2");

    ItemRelationshipServiceApi apiInstance = new ItemRelationshipServiceApi(defaultClient);
    String globalAssetId = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0"; // String | Readable ID of manufacturer including plant
    String bomLifecycle = "bomLifecycle_example"; // String | Unique identifier of a single, unique (sub)component/part/batch, given by its globalAssetId/ digital twin id
    List<String> aspect = Arrays.asList("SerialPartTypization"); // List<String> | Aspect information to add to the returned tree
    Integer depth = 1; // Integer | Max depth of the returned tree, if empty max depth is returned
    String direction = "downward"; // String | Direction in which the tree shall be traversed
    try {
      apiInstance.getBomLifecycleByGlobalAssetId(globalAssetId, bomLifecycle, aspect, depth, direction);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemRelationshipServiceApi#getBomLifecycleByGlobalAssetId");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **globalAssetId** | **String**| Readable ID of manufacturer including plant |
 **bomLifecycle** | **String**| Unique identifier of a single, unique (sub)component/part/batch, given by its globalAssetId/ digital twin id | [enum: asBuilt]
 **aspect** | [**List&lt;String&gt;**](String.md)| Aspect information to add to the returned tree | [optional] [enum: SerialPartTypization, AssemblyPartRelationship, PartDimension, SupplyRelationData, PCFCoreData, PCFTechnicalData, MarketPlaceOffer, MaterialAspect, BatteryPass, ProducDescription.Vehicle, ProducDescription.Battery, ReturnRequest, CertificateOfDestruction, CertificateOfDismantler, Adress, Contact]
 **depth** | **Integer**| Max depth of the returned tree, if empty max depth is returned | [optional] [default to 1]
 **direction** | **String**| Direction in which the tree shall be traversed | [optional] [default to downward] [enum: upward, downward]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | job id response for successful job registration |  -  |
**400** | Bad request |  -  |

<a name="getJobsByProcessingState"></a>
# **getJobsByProcessingState**
> getJobsByProcessingState(processingState)



### Example
```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.ItemRelationshipServiceApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("https://api.server.test/api/v0.2");

    ItemRelationshipServiceApi apiInstance = new ItemRelationshipServiceApi(defaultClient);
    String processingState = "running"; // String | List of jobs (globalAssetIds) for a certain processing state
    try {
      apiInstance.getJobsByProcessingState(processingState);
    } catch (ApiException e) {
      System.err.println("Exception when calling ItemRelationshipServiceApi#getJobsByProcessingState");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **processingState** | **String**| List of jobs (globalAssetIds) for a certain processing state | [default to running] [enum: running, failed, complete, canceled]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | list of jobs with given processingState |  -  |
**400** | Bad Request |  -  |

