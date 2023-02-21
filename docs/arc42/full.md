Architecture documentation (arc42)
==================================

Table of Contents

-   [Introduction and goals](#introduction-and-goals)
    -   [Requirements overview](#requirements-overview)
    -   [Quality goals](#quality-goals)
-   [Architecture constraints](#architecture-constraints)
    -   [Technical Constraints](#technical-constraints)
    -   [Organizational Constraints](#organizational-constraints)
    -   [Political constraints](#political-constraints)
    -   [Development conventions](#development-conventions)
-   [System scope and context](#system-scope-and-context)
    -   [Business context](#business-context)
    -   [Technical context](#technical-context)
-   [Solution strategy](#solution-strategy)
    -   [Introduction](#introduction)
    -   [Technology](#technology)
    -   [Structure](#structure)
-   [Building block view](#building-block-view)
    -   [Whitebox overall system](#whitebox-overall-system)
    -   [Level 1](#level-1)
    -   [Level 2](#level-2)
    -   [IRS API](#irs-api-2)
-   [Runtime view](#runtime-view)
    -   [Overall](#overall)
    -   [Scenario 1: Create job](#scenario-1-create-job)
    -   [Scenario 2: Job execution](#scenario-2-job-execution)
    -   [Scenario 3: Request for JobResponse](#scenario-3-request-for-jobresponse)
    -   [Scenario 4: Cancel job execution](#scenario-4-cancel-job-execution)
-   [Deployment view](#deployment-view)
    -   [Local deployment](#local-deployment)
    -   [View Levels](#view-levels)
-   [Cross-cutting concepts](#cross-cutting-concepts)
    -   [Domain concepts](#domain-concepts)
    -   [Safety and security concepts](#safety-and-security-concepts)
    -   [Architecture and design patterns](#architecture-and-design-patterns)
    -   ["Under-the-hood" concepts](#under-the-hood-concepts)
    -   [Development concepts](#development-concepts)
    -   [Operational concepts](#operational-concepts)
-   [Quality requirements](#quality-requirements)
    -   [List of requirements](#list-of-requirements)
-   [Glossary](#glossary)

Introduction and goals
----------------------

This chapter gives you an overview about the goals of the service,
in which context the service runs and which stakeholders are involved.

### Requirements overview

#### What is the Item Relationship Service?

The IRS is a:

-   Functional federated component

-   API endpoint to retrieve the tree structures in a recursive way, which data assets are distributed throughout the Catena-X network

-   Reference implementation

-   Data chain provider

#### Substantial Features

-   provide a top-down BoM asBuilt tree structure along the submodel "AssemblyPartRelationship"

-   usage of EDC consumer for communicating with the Catena-X network

-   functionality of IRS provider will be handled by providers submodel servers

-   federated IRS service

-   'asBuild' BoM of serialized components

-   provides endpoints for submodel-descriptors

### Quality goals

<table>
<tbody>
<tr class="odd">
<td><em></em></td>
<td>The following table entries define overall IRS quality goals. The order of the topics do not resolve in a priority of the quality goals.</td>
</tr>
</tbody>
</table>

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Quality goal</th>
<th>Motivation and description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>running reference application</p></td>
<td><p>The IRS is built to traverse a distributed data chain across the automotive Industry. The goal for the IRS release 1 scope is to build a running solution to test the functionality of building a BoM as built of serialized components.</p></td>
</tr>
<tr class="even">
<td><p>multiple async job orchestration</p></td>
<td><p>The IRS is built to access multiple endpoints parallel. Since the for the Endpoint it is not clear yet how long a request will take to respond. The Service is built to handle multiple asynchronous requests.</p></td>
</tr>
<tr class="odd">
<td><p>cloud agnostic solution</p></td>
<td><p>The IRS is built as reference architecture and able to run on different cloud solutions. It uses helm charts, terraform and a abstracts the storage, so that it can easily be integrated on different systems.</p></td>
</tr>
<tr class="even">
<td><p>base security measures</p></td>
<td><p>The IRS is built with a base set of security features.</p></td>
</tr>
<tr class="odd">
<td><p>application reliability</p></td>
<td><p>The IRS architecture is set up so that the costumers can rely on reliable data chains</p></td>
</tr>
</tbody>
</table>

Architecture constraints
------------------------

### Technical Constraints

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Name</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>Cloud Agnostic Architecture approach</p></td>
<td><p>IRS provides a reference application/implementation which is deployable on any cloud ecosystem. There is no vendor lock to any cloud vendor.</p></td>
</tr>
<tr class="even">
<td><p>Spring Boot and the Spring framework is used as underlying framework for Java development.</p></td>
<td><p>Spring Boot and Framework is used to build an easy and production-grade based application which could be deployed without any further infrastructure components.</p>
<p>Orchestrating application components and integrating with other libraries/frameworks.</p></td>
</tr>
<tr class="odd">
<td><p>Lombok</p></td>
<td><p>Lombok for generating boilerplate code. Keeping code concise increases quality and maintainability.</p></td>
</tr>
<tr class="even">
<td><p>Kubernetes is used for Container Orchestration</p></td>
<td><p>Kubernetes as container orchestration system used for software deployment, scaling and management of the IRS application. This supports our software infrastructure and ensures efficient management and scalability of the IRS reference application.</p></td>
</tr>
<tr class="odd">
<td><p>Docker Container are used to provide a microservice oriented architecture</p></td>
<td><p>Deployment made on reliable production ready images. Avoiding repetitive, mundane configuration tasks for container orchestration.</p></td>
</tr>
<tr class="even">
<td><p>Docker Compose is used to define and tune multi container application based on docker container technologies.</p></td>
<td><p>Docker container to develop independently of the underlying OS.</p></td>
</tr>
</tbody>
</table>

### Organizational Constraints

<table>
<colgroup>
<col style="width: 33%" />
<col style="width: 33%" />
<col style="width: 33%" />
</colgroup>
<thead>
<tr class="header">
<th>Name</th>
<th>Description</th>
<th>Comment</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>CX-Services</p></td>
<td><p>Provide IRS as a C-X Shared Data Service.</p></td>
<td></td>
</tr>
<tr class="even">
<td><p>App Marketplace &amp; API Connection</p></td>
<td><p>IRS Application has to be accessible for the user in the App Marketplace.</p></td>
<td><p>App Marketplace &amp; API Connection</p></td>
</tr>
<tr class="odd">
<td><p>Federal Ministry for Economic Affairs and Energy (BMWi) promotion</p></td>
<td><p>The Federal Ministry for Economic Affairs and Energy (BMWi) promotes the project and provides funds for the project.</p></td>
<td></td>
</tr>
<tr class="even">
<td><p>Technology Readiness Level (TRL) for Products developed within the CX Consortia</p></td>
<td><p>As IRS is a reference implementation, the Technology Readiness Level (TRL) must not be above TRL 8.</p></td>
<td><div class="content">
<div class="paragraph">
<p>Technology Readiness Level - TRL</p>
</div>
<div class="imageblock">
<div class="content">
<img src="architecture-constraints/trl.png" alt="trl" />
</div>
</div>
</div></td>
</tr>
<tr class="odd">
<td><p>Operational Readiness for Release 1 has to be fulfilled</p></td>
<td><p>Minimum requirements for release 1 has to be archived. Later on, the Operational Readiness for Release has to be fulfilled accordingly to the requirements of the C-X consortia.</p></td>
<td></td>
</tr>
</tbody>
</table>

### Political constraints

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Name</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>Open Source</p></td>
<td><p>FOSS licenses approved by the eclipse foundation has to be used. It could represent the initial set that the CX community agrees on to regulate the content contribution under FOSS licenses.</p></td>
</tr>
<tr class="even">
<td><p>Apache License 2.0</p></td>
<td><p>Apache License 2.0 is one of the approved licenses which should be used to respect and guarantee Intellectual property (IP).</p></td>
</tr>
<tr class="odd">
<td><p>Java OpenJDK Version JDK &gt;= 11</p></td>
<td><p>IRS provides an open source application standard. OpenJDK is used, which is licensed under GNU General Public License (GNU GPL) Version 2.</p></td>
</tr>
</tbody>
</table>

### Development conventions

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Name</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>Architecture documentation</p></td>
<td><p>Architectural documentation of IRS reference application/implementation according to ARC42 template.</p></td>
</tr>
<tr class="even">
<td><p>Coding guidelines</p></td>
<td><p>We follow the Google Java Style Guide. That is ensured by using the unified code formatter in the team and enforcing the style via Maven and Checkstyle / PMD.</p></td>
</tr>
<tr class="odd">
<td><p>Executable Bundle provided over the App Marketplace</p></td>
<td><p>As IRS is available in the App Marketplace, the application should be provided in one executable bundle.</p></td>
</tr>
<tr class="even">
<td><p>Module structure</p></td>
<td><p>The entire build is driven from a Maven file, itself run from a single Dockerfile.</p></td>
</tr>
<tr class="odd">
<td><p>Code Analysis, Linting and Code Coverage</p></td>
<td><p>Consistent style increases readability and maintainability of the code base. Hence, we use analyzers to enforce consistency and style rules. We enforce the code style and rules in the CI to avoid merging code that does not comply with standards.</p></td>
</tr>
</tbody>
</table>

#### Code analysis, linting and code coverage

<table>
<colgroup>
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
<col style="width: 25%" />
</colgroup>
<thead>
<tr class="header">
<th>Tool</th>
<th>Scope</th>
<th>Rule</th>
<th>Configuration (via files / annotations)</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>Tidy</p></td>
<td><p>Enforce Maven POM Code Convention</p></td>
<td><p>Fail build on untidy pom.xml</p></td>
<td><p>N/A</p></td>
</tr>
<tr class="even">
<td><p>SpotBugs</p></td>
<td><p>Static analysis to look for bugs in Java code. Successor of popular FindBugs tool</p></td>
<td><p>Fail build on violations</p></td>
<td><p>ci/spotbugs-excludes.xml
@SuppressFBWarnings(…​)</p></td>
</tr>
<tr class="odd">
<td><p>FindSecBugs</p></td>
<td><p>SpotBugs plugin adding security bugs coverage</p></td>
<td><p>Fail build on violations</p></td>
<td><p>N/A</p></td>
</tr>
<tr class="even">
<td><p>Checkstyle</p></td>
<td><p>Enforce coding standard</p></td>
<td><p>Fail build on violations</p></td>
<td><p>ci/checkstyle-suppressions.xml
@SuppressWarnings("checkstyle:XXX")</p></td>
</tr>
<tr class="odd">
<td><p>PMD</p></td>
<td><p>Source code analyzer to finds common programming flaws</p></td>
<td><p>Fail build on violations</p></td>
<td><p>ci/pmd-rules.xml
@SuppressWarnings("PMD.XXX")</p></td>
</tr>
<tr class="even">
<td><p>JaCoCo</p></td>
<td><p>Test coverage</p></td>
<td><p>Fail build on coverage &lt; 80%</p></td>
<td><p>pom.xml
@ExcludeFromCodeCoverageGeneratedReport</p></td>
</tr>
<tr class="odd">
<td><p>Veracode</p></td>
<td><div class="content">
<div class="ulist">
<ul>
<li><p>Scan source code for vulnerabilities (SAST)</p></li>
<li><p>Scan dependencies for known vulnerabilities (SCA)</p></li>
<li><p>Check used licenses (FOSS Licenses)</p></li>
</ul>
</div>
</div></td>
<td></td>
<td><p><a href="https://web.analysiscenter.veracode.com/" class="bare">https://web.analysiscenter.veracode.com/</a></p></td>
</tr>
<tr class="even">
<td><p>Dependabot</p></td>
<td><p>Automated dependency updates built into GitHub. Provided pull requests on dependency updates.</p></td>
<td><p>Any dependency update generates a pull request automatically.</p></td>
<td><p>.github/dependabot.yml</p></td>
</tr>
<tr class="odd">
<td><p>CodeQl</p></td>
<td><p>Discover vulnerabilities across a codebase.</p></td>
<td></td>
<td><p>.github/workflows/codeql.yml</p></td>
</tr>
</tbody>
</table>

System scope and context
------------------------

The IRS acts as a middleware between consumers and manufacturers. This section describes the environment of IRS. Who are its users, and with which other systems does it interact with.

### Business context

<img src="architecture-constraints/business-context.svg" width="273" height="327" alt="business context" />

#### Consumer

The IRS API is being consumed by the dismantler dashboard and other parties which are part of the Catena-X network. They need to provide valid credentials issued by the Catena-X IAM. Additionally, they must provide a base global asset identifier to retrieve information for as well as configuration details for the view on that information.

#### Catena-X network

The IRS retrieves data from the Catena-X network (using the necessary infrastructure, see Technical Context), aggregates it and provides it back to the consumers. This connection is mandatory. If the Catena-X services are unavailable, the IRS cannot perform any work.

As of now, the IRS uses its own IAM credentials to gather the required data. This might be changed to use the consumer credentials in the future.

### Technical context

<img src="architecture-constraints/integrated-overview.svg" width="1262" height="417" alt="integrated overview" />

#### Component overview

##### IRS-API

We provide a REST API that can be consumed by any system registered in the Catena-X Keycloak, e.g. the Dismantler Dashboard. The development of such a consumer service is not part of the IRS application. Each system that acts as a client to the Restful application IRS can be used instead, if it supports any REST call of the designed REST endpoints in the REST Controller of the IRS application. For communication, the transport protocol HTTP(S) should be established.

In order to consume the Restful application IRS, the security aspect should be taken in consideration. IRS is a Spring Boot based application and is secured with the OpenID connector provider Keycloak and the OAuth2. This means for the consumers (users) that they need to authenticate themselves in order to be authorized to get access to the IRS. They generate a bearer token that they get from Keycloak and attach it to the HTTP header parameter Authorization. Certainly, both a consumer and the IRS should use the same configured Keycloak Realm.

##### Registry API

The IRS acts as a consumer of the component Asset Administration Shell Registry. The IRS contains a Restful client (REST template) that build a REST call to the mentioned Digital Twin Registry API based on its known URL (the AAS registry URL is configured in the IRS Restful API). The request contains the given "globalAssetId" by the consumer. Like described in the above section, the security aspect is required in order to achieve a REST call against the AAS Registry. As a response, the IRS gets the corresponding asset administration shell descriptor. The last one contains a list of submodel descriptors which can be filtered by the aspect type entered by the consumer. An aspect type like AssemblyPartRelationship, SerialPartTypization etc. And as mentioned above, the transport protocol HTTP(S) is used for the REST call communication.

##### EDC API

The integrated EDC client in the IRS is responsible for creating restful requests to the component EDC. The IRS application builds from the retrieved AAS Descriptor (see previous section) the corresponding submodel endpoint URLs, negotiates an EDC contract and sends via the submodel REST client requests to the EDC. The EDC responds with the corresponding submodel data.

Solution strategy
-----------------

### Introduction

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Quality goal</th>
<th>Matching approaches in the solution</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>application reliability</p></td>
<td><div class="content">
<div class="ulist">
<ul>
<li><p>only data source is the Catena-X network, data is fetched directly from the data owner</p></li>
<li><p>IRS can be hosted decentralized by every participant by being an open source reference implementation</p></li>
</ul>
</div>
</div></td>
</tr>
<tr class="even">
<td><p>base security measures</p></td>
<td><div class="content">
<div class="ulist">
<ul>
<li><p>API protection using OAuth2.0/OIDC</p></li>
<li><p>automatic static and dynamic code analysis tools as part of the pipeline</p></li>
</ul>
</div>
</div></td>
</tr>
<tr class="odd">
<td><p>cloud agnostic solution</p></td>
<td><div class="content">
<div class="ulist">
<ul>
<li><p>IRS is provided as a Docker image</p></li>
<li><p>Helm charts assist in deploying the application in any Kubernetes environment</p></li>
</ul>
</div>
</div></td>
</tr>
<tr class="even">
<td><p>multiple async job orchestration</p></td>
<td><div class="content">
<div class="ulist">
<ul>
<li><p>Separate job executor decouples data requests from the job status API</p></li>
<li><p>Multiple jobs with multiple transfer requests each can be handled in parallel, depending on the deployment resources</p></li>
</ul>
</div>
</div></td>
</tr>
<tr class="odd">
<td><p>running reference application</p></td>
<td><div class="content">
<div class="ulist">
<ul>
<li><p>Working application can be used as reference by anyone due to open source publishing</p></li>
</ul>
</div>
</div></td>
</tr>
</tbody>
</table>

### Technology

The IRS is developed using Java and the Spring Boot framework. This choice was made due to the technical knowledge of the team and the widespread support of the framework.

Hosting the application is done using Docker and Kubernetes, which is widely used and vendor-independent regarding the hosting provider (e.g. AWS, Google Cloud, Azure, …​).

Inter-service communication is done using HTTP REST. This is the standard method in the Catena-X landscape and makes the IRS API easy to use for any third party client.

For persistence, blob storage was chosen as the payloads retrieved for each job vary for every aspect and the format can be unknown to the application, as it’s just being tunneled through to the client.

### Structure

The IRS consists of 4 main components:

1.  the REST API to view and control Jobs

2.  the asynchronous job processing engine

3.  the job and payload persistence

4.  the AAS connector

The REST API classes are separated from the application logic and can be replaced by a different interface easily. The actual entrypoint into the application logic are the \*Service classes.

The job processing engine handles execution of the data requests for each job. It uses the AAS connector to retrieve the data and stores it into the persistence. The actual implementation of the persistence is decoupled from the logical representation and can be replaced easily as well.

Building block view
-------------------

### Whitebox overall system

The interfaces show how the components interact with each other and which interfaces the IRS is providing.

#### Component diagram

<img src="architecture-constraints/whitebox-overview.svg" width="781" height="838" alt="whitebox overview" />

#### Component description

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Components</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>IRSApiConsumer</p></td>
<td><p>Proxy for any consumer of the IRS api.</p></td>
</tr>
<tr class="even">
<td><p>IRS</p></td>
<td><p>The IRS consumes relationship information across the CX-Network and builds the graph view. Within this Documentation, the focus lies on the IRS</p></td>
</tr>
<tr class="odd">
<td><p>AAS Proxy</p></td>
<td><p>The AAS Proxy is a System, which enables the consumer to simplify the communication with other CX Partners.</p></td>
</tr>
<tr class="even">
<td><p>EDC Consumer</p></td>
<td><p>The EDC Consumer Component is there to fulfill the GAIA-X and IDSA-data sovereignty principles. The EDC Consumer consists out of a control plane and a data plane.</p></td>
</tr>
<tr class="odd">
<td><p>EDC Provider</p></td>
<td><p>The EDC Provider Component connects with EDC Consumer component and  forms the end point for the actual exchange of data. It handles automatic contract negotiation and the subsequent exchange of data assets for connected applications.</p></td>
</tr>
<tr class="even">
<td><p>Submodel Server</p></td>
<td><p>The Submodel Server offers endpoints for requesting the Submodel aspects.</p></td>
</tr>
<tr class="odd">
<td><p>IAM/DAPS</p></td>
<td><p>DAPS as central Identity Provider</p></td>
</tr>
</tbody>
</table>

### Level 1

#### Component diagram

<img src="architecture-constraints/level-1.svg" width="671" height="845" alt="level 1" />

#### Component description

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Components</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p><strong>IRS</strong></p></td>
<td><p>The IRS builds a digital representation of a product (digital twin) and the relationships of items the product consists of in a hierarchical structure.</p>
<p>The result is an item graph in which each node represents a digital item of the product - this graph is called "Item Graph".</p></td>
</tr>
<tr class="even">
<td><p><strong>IRS API</strong></p></td>
<td><p>The <strong>IRS API</strong> is the Interface over which the Data Consumer is communicating.</p></td>
</tr>
<tr class="odd">
<td><p><strong>IrsController</strong></p></td>
<td><p>The <strong>IrsController</strong> provides an REST Interface for retrieving IRS processed data and job details of the current item graph retrieval process.</p></td>
</tr>
<tr class="even">
<td><p><strong>IrsItemGraphQueryService</strong></p></td>
<td><p>The <strong>IrsItemGraphQueryService</strong> implements the REST Interface of the IrsController.</p></td>
</tr>
<tr class="odd">
<td><p><strong>JobOrchestrator</strong></p></td>
<td><p>The <strong>JobOrchestrator</strong> is a component which manages (start, end, cancel, resume) the jobs which execute the item graph retrieval process.</p></td>
</tr>
<tr class="even">
<td><p><strong>RecursiveJobHandler</strong></p></td>
<td><p>The <strong>RecursiveJobHandler</strong> handles the job execution recursively until a given abort criteria is reached or the complete item graph is build.</p></td>
</tr>
<tr class="odd">
<td><p><strong>TransferProcessManager</strong></p></td>
<td><div class="content">
<div class="paragraph">
<p>The TransferProcessManager handles the outgoing requests to the AASProxy.</p>
</div>
<div class="olist arabic">
<ol>
<li><p>Initiation of the job and preparation of the stream of <strong>DataRequests</strong></p></li>
<li><p><strong>RecursiveJobHandler</strong> requesting for AAS via the Digital Twin registry.</p></li>
<li><p>Analyzing the structure of the AAS response by collecting the AssemblyPartRelationship Aspects</p></li>
<li><p>Requesting for SubmodelEndpoints for given AssemblyPartRelationship children</p></li>
<li><p>Recursively iteration over step 2-4 until an abort criterion is reached.</p></li>
<li><p>Assembles the complete item graph</p></li>
</ol>
</div>
</div></td>
</tr>
<tr class="even">
<td><p><strong>BlobStore</strong></p></td>
<td><p>The BlobStore is the database where the relationships and tombstones are stored for a requested item.</p></td>
</tr>
<tr class="odd">
<td><p><strong>JobStore</strong></p></td>
<td><p>The JobStore is the database where the jobs with the information about the requested item are stored.</p></td>
</tr>
<tr class="even">
<td><p><strong>AASProxy</strong></p></td>
<td><p>The AASProxy is the interface to the EDC Network. It provides an interface for the Asset Administration Shells and for the Submodels.</p></td>
</tr>
</tbody>
</table>

### Level 2

#### IRS controller

The IRS REST controller to provide a RESTful web service.

##### Component diagram

<img src="architecture-constraints/level-2-controller.svg" width="659" height="421" alt="level 2 controller" />

##### Component description

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Components</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>IrsController</p></td>
<td><p>Application REST controller.</p></td>
</tr>
<tr class="even">
<td><p>IrsItemGraphQueryService</p></td>
<td><p>Service for retrieving item graph.</p></td>
</tr>
<tr class="odd">
<td><p>JobOrchestrator</p></td>
<td><p>Orchestrator service for recursive MultiTransferJobs that potentially comprise multiple transfers.</p></td>
</tr>
<tr class="even">
<td><p>JobStore</p></td>
<td><p>Spring configuration for job-related beans.</p></td>
</tr>
<tr class="odd">
<td><p>BlobstorePersistence</p></td>
<td><p>Interface for storing data blobs.</p></td>
</tr>
</tbody>
</table>

#### RecursiveJobHandler

The **RecursiveJobHandler** component provide the logic to build jobs with recursive logic to retrieve items over the complete C-X network and assembles the partial results into a single item graph result.

##### Component diagram

<img src="architecture-constraints/level-2-jobhandler.svg" width="752" height="280" alt="level 2 jobhandler" />

##### Component description

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Components</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>AASRecursiveJobHandler</p></td>
<td><p>Recursive job handler for AAS data</p></td>
</tr>
<tr class="even">
<td><p>TreeRecursiveLogic</p></td>
<td><p>Retrieves item graphs from potentially multiple calls to IRS API behind multiple EDC Providers, and assembles their outputs into one overall item graph.</p></td>
</tr>
<tr class="odd">
<td><p>ItemTreesAssembler</p></td>
<td><p>Assembles multiple partial item graphs into one overall item graph.</p></td>
</tr>
<tr class="even">
<td><p>BlobPersistence</p></td>
<td><p>Interface for storing data blobs.</p></td>
</tr>
</tbody>
</table>

#### TransferProcessManagment

The TransferProcessManager creates executions and provides them to the executor service. Each execution contains HTTP requests to the asset administration shell registry and to the submodel interface.

##### Component diagram

<img src="architecture-constraints/level-2-transfer-process-management.svg" width="893" height="814" alt="level 2 transfer process management" />

##### Component description

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Components</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>TransferProcessManager</p></td>
<td><p>The TransferProcessManager manages the requests to the EDC and DigitalTwinRegistry.</p></td>
</tr>
<tr class="even">
<td><p>DigitalTwinRegistryFacade</p></td>
<td><p>The DigitalTwinRegistryFacade calls the DigitalTwinRegistry to retrieve data form the AAS registry and transforms the response to internal data models.</p></td>
</tr>
<tr class="odd">
<td><p>SubmodelFacade</p></td>
<td><p>The SubmodelFacade calls the EDC to retrieve data from the submodel server and transforms the response to internal data models.</p></td>
</tr>
<tr class="even">
<td><p>BlobStore</p></td>
<td><p>The BlobStore is the database where the relationships and tombstones are stored for a requested item.</p></td>
</tr>
<tr class="odd">
<td><p>DigitalTwinRegistry</p></td>
<td><p>The DigitalTwinRegistry is the central database of registered assets.</p></td>
</tr>
<tr class="even">
<td><p>ExecutorService</p></td>
<td><p>The ExecutorService enables the simultaneous execution of requests of transfer processes.</p></td>
</tr>
</tbody>
</table>

### IRS API

#### References

The Swagger documentation can be found in the local deployment of the reference application. More information can be found in the GitHub repository: <a href="https://github.com/eclipse-tractusx/item-relationship-service/blob/main/README.md" class="bare">https://github.com/eclipse-tractusx/item-relationship-service/blob/main/README.md</a>

Since we cannot rely on synchronous responses regarding the requests of submodel endpoints, we designed the IRS in a way that it will handle the job management of requesting all needed endpoints in order to build a BoM tree.

#### IRS interaction diagram

<img src="architecture-constraints/irs-api.svg" width="491" height="577" alt="irs api" />

Runtime view
------------

This section covers the main processes of the IRS and explains how data is transfered and processed when a job is executed.

### Overall

This section describes the overall flow of the IRS

<img src="architecture-constraints/overall.svg" width="1347" height="1005" alt="overall" />

#### Submodel

This section describes how the IRS fetches submodel payload.

<img src="architecture-constraints/submodel-processing.svg" width="1365" height="721" alt="submodel processing" />

#### Job orchestration flow

This section describes the job orchestration in IRS.

<img src="architecture-constraints/job-orchestration.svg" width="2804" height="1441" alt="job orchestration" />

### Scenario 1: Create job

This section describes what happens when user creates a new job.

<img src="architecture-constraints/create-job.svg" width="1768" height="665" alt="create job" />

#### Overview

If a job is registered via the IRS API, it will be persisted (with its parameters) in the JobStore, where it can be retrieved by further calls and processes.
Then, the starting item ID is extracted, and a new transfer process is handed to the ExecutorService, which will process it asynchronously (see Scenario 2: Job Execution).

In the meantime, the JobOrchestrator returns a response to the API caller, which contains the UUID of the new job.
This UUID can then be used by the caller to retrieve information about the job via the API.
The input provided by the caller determines how the job will operate (starting point, recursion depth, aspect filter, …​).

### Scenario 2: Job execution

This section describes how a job is asynchronously executed inside the IRS.

<img src="architecture-constraints/execute-job.svg" width="1408" height="656" alt="execute job" />

#### Overview

After a job has been created (see Scenario 1: Create Job), the first transfer containing the root item ID is passed to the ExecutorService. The transfer is then started asynchronously and retrieves the necessary information from the Catena-X network, first by fetching the AAS information from the DigitalTwin registry and then calling the SubmodelProviders for the submodel payload.

At least the aspect AssemblyPartRelationship is required for the tree to be built. If the customer that started the job provided more aspects to be fetched, they will be retrieved here too.
The result of each transfer is stored in the BlobStore.

After the transfer process has finished, any subsequent child IDs will be extracted and new transfer processes will be scheduled for those via the ExecutorService. This cycle repeats until all leafs were reached, the specified max depth has been reached, or the job was canceled externally.

As soon as all transfers are finished, the results will be combined and stored in the BlobStore again. The job itself will be marked as completed.

### Scenario 3: Request for JobResponse

<img src="architecture-constraints/request-job.svg" width="951" height="684" alt="request job" />

#### Overview

When a user requests job details, the IRS looks up the jobId in the persistent job store. If the job exists, it will proceed to fetch the job details and prepare a response object.
Only if the job is in state "COMPLETED" or if the caller has set the parameter "includePartialResults" to true, the IRS will fetch the payload data for the job (relationships, AAS shells and submodels, if applicable) and attach it to the response object.

This will then be passed to the caller.

### Scenario 4: Cancel job execution

<img src="architecture-constraints/cancel-job.svg" width="1068" height="637" alt="cancel job" />

#### Overview

When a user wants to cancel a job execution, the IRS will lookup that job in the persistent job store and transition it to the CANCELED state, if it exists. If a job is canceled, no further requests to the Catena-X network will be performed.

Afterwards, the IRS will return the updated job details of the canceled job to the user.

Deployment view
---------------

The deployment view shows the IRS application on ArgoCD, which is a continuous delivery tool for Kubernetes. Kubernetes manifests are specified using Helm charts. Helm is a package manager for Kubernetes. IRS is developed in a cloud-agnostic manner, so the application could be installed in any cloud infrastructure (on-premises, hybrid, or public cloud infrastructure).

<img src="architecture-constraints/deployment-view.svg" width="595" height="347" alt="deployment view" />

**Operator**

Manual preparation is required to initially set up the ArgoCD apps and the credentials in the HashiCorp Vault. This is done by the IRS system operator.

**ArgoCD**

Argo CD is a declarative, GitOps continuous delivery tool for Kubernetes. See <a href="https://argo-cd.readthedocs.io/" class="bare">https://argo-cd.readthedocs.io/</a>.

**Vault**

HashiCorp Vault stores credentials, which are picked up by ArgoCD to deploy them to the application.

<table>
<tbody>
<tr class="odd">
<td><em></em></td>
<td>Every secret information needed at runtime must be stored here and must never be part of the IRS Helm charts</td>
</tr>
</tbody>
</table>

**GitHub**

GitHub contains the application source code as well as the Helm charts used for deployment.
The IRS Helm charts can be found here: <a href="https://github.com/eclipse-tractusx/item-relationship-service/tree/main/charts" class="bare">https://github.com/eclipse-tractusx/item-relationship-service/tree/main/charts</a>

**GitHub Container Registry (GHCR)**

When the IRS is built by GitHub Action workflows, the final image is pushed to the GHCR, where it can be picked up for deployment.

**Kubernetes**

The kubernetes cluster manages the underlying hardware that is used to run the applications defined in the Helm charts.

### Local deployment

For information on how to run the application locally, please check the README documentation in GitHub: <a href="https://github.com/eclipse-tractusx/item-relationship-service/blob/main/README.md" class="bare">https://github.com/eclipse-tractusx/item-relationship-service/blob/main/README.md</a>

### View Levels

#### Level 0 - Cluster overview

##### Isolated environment

The isolated environment contains the IRS as well as the surrounding services, excluding the external IAM.

<img src="architecture-constraints/isolated.svg" width="1102" height="313" alt="isolated" />

##### Integrated environment

The integrated environment contains the IRS and is integrated with the rest of the Catena-X network.

<img src="architecture-constraints/integrated.svg" width="852" height="248" alt="integrated" />

#### Level 1 - IRS application

This section focuses only on the IRS itself, detached from its neighbors. It shows the resources deployed in Kubernetes for the IRS.

<img src="architecture-constraints/irs-resources.svg" width="557" height="537" alt="irs resources" />

**Pod**

This is the actual IRS Docker image which runs as a container. The ports are only available internally and can be opened up with the Service.

**Secrets**

The secret information (e.g. connection credentials) is stored here and provided to the Pod at runtime.

**Service**

The service resource opens up selected ports of the Pod so that other applications in the same cluster can access it or to be used by the Ingress.

**Ingress**

The ingress uses a reverse proxy to provide specified Service ports to the internet under a specified URL. This make the IRS API publicly available.

Cross-cutting concepts
----------------------

### Domain concepts

#### Domain entity model

<img src="architecture-constraints/domain-entity-model.svg" width="2747" height="1146" alt="domain entity model" />

#### Domain model

<img src="architecture-constraints/domain-model.svg" width="2045" height="646" alt="domain model" />

#### API Model

For detailed information about the API model, please refer to the [API specification](https://eclipse-tractusx.github.io/item-relationship-service/docs/api-specification/api-specification.html).

#### JobStatus

A job can be in one of the following states:

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>State</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>UNSAVED</p></td>
<td><p>The job was created, but not yet stored by the system.</p></td>
</tr>
<tr class="even">
<td><p>INITIAL</p></td>
<td><p>The job was stored by the system and is now queued for processing.</p></td>
</tr>
<tr class="odd">
<td><p>IN_PROGRESS</p></td>
<td><p>The job is currently being processed.</p></td>
</tr>
<tr class="even">
<td><p>TRANSFERS_FINISHED</p></td>
<td><p>All transfers for the job have been finished, and it is now being finalized.</p></td>
</tr>
<tr class="odd">
<td><p>COMPLETED</p></td>
<td><p>The job has completed. See the job response for details on the data.</p></td>
</tr>
<tr class="even">
<td><p>ERROR</p></td>
<td><p>The job could not be processed correctly by the IRS due to a technical problem.</p></td>
</tr>
</tbody>
</table>

<img src="architecture-constraints/job-state-machine.svg" width="1533" height="282" alt="job state machine" />

#### Job Store Datamodel

<img src="architecture-constraints/inmemory-model.svg" width="2144" height="294" alt="inmemory model" />

#### Job Response Datamodel

<img src="architecture-constraints/job-response-model.svg" width="3585" height="865" alt="job response model" />

### Safety and security concepts

#### Authentication / Authorization

##### IRS API

The IRS is secured using OAuth2.0 / Open ID Connect. Every request to the IRS API requires a valid bearer token.
Additionally every IRS API endpoint requires 'view\_irs' role, that should be present in the token.

##### IRS as DTR client

The IRS acts as a client for the Digital Twin Registry (DTR), which is also secured using OAuth2.0 / Open ID Connect. The IRS uses client credentials to authenticate requests to the DTR. Due to this, the IRS account needs to have access to every item in the DTR, unrelated to the permissions of the account calling the IRS API.

##### IRS as EDC client

The IRS accesses the Catena-X network via the EDC consumer connector. This component requires authentication via a DAPS certificate, which was provided to the IRS via the network authority.

The DAPS certificate identifies the IRS and is used to acquire access permissions for the data transferred via EDC.

#### Credentials

Credentials must never be stored in Git!

### Architecture and design patterns

#### Dependency inversion

For the IRS, we utilize the dependency inversion mechanisms provided by Spring Boot as much as possible.

The principle says:

High-level modules should not import anything from low-level modules. Both should depend on abstractions (e.g., interfaces).
Abstractions should not depend on details. Details (concrete implementations) should depend on abstractions.

Adhering to this, we define clear interfaces between the different domains (e.g. job orchestration and AAS communication) in the IRS and let dependencies be injected by the framework. This improves testability of the classes as well.

#### Hexagonal architecture

The hexagonal architecture divides a system into several loosely-coupled interchangeable components, such as the application core, the database, the user interface, test scripts and interfaces with other systems. This approach is an alternative to the traditional layered architecture.

For the IRS, this means decoupling the application logic from components like the BLOB store, the REST API controllers or the AAS client connection. With an interface between the parts (so-called port), it is easy to switch to other implementations, e.g. if you want to change the persistence implementation. No changes to the application logic will be necessary.

<img src="architecture-constraints/architecture.svg" width="710" height="507" alt="architecture" />

### "Under-the-hood" concepts

#### Persistency

The IRS stores two types of data in a persistent way:

-   Job metadata

-   Job payloads, e.g. AAS shells or submodel data

All of this is data is stored in an object store. The currently used implementation is Minio (Amazon S3 compatible).
This reduces the complexity in storing and retrieving data. There also is no predefined model for the data, every document can be stored as it is.
The downside of this approach is lack of query functionality, as we can only search through the keys of the entries but not based on the value data.
In the future, another approach or an additional way to to index the data might be required.

To let the data survive system restarts, Minio needs to use a persistent volume for the data storage. A default configuration for this is provided in the Helm charts.

#### Transaction handling

There currently is no transaction management in the IRS.

#### Session handling

There is no session handling in the IRS, access is solely based on bearer tokens, the API is stateless.

#### Communication and integration

All interfaces to other systems are using RESTful calls over HTTP(S). Where central authentication is required, a common Keycloak instance is used.

For outgoing calls, the Spring RestTemplate mechanism is used and separate RestTemplates are created for the different ways of authentication.

For incoming calls, we utilize the Spring REST Controller mechanism, annotating the interfaces accordingly and also documenting the endpoints using OpenAPI annotations.

#### Exception and error handling

There are two types of potential errors in the IRS:

##### Technical errors

Technical errors occur when there is a problem with the application itself, its configuration or directly connected infrastructure, e.g. the Minio persistence. Usually, the application cannot solve these problems by itself and requires some external support (manual work or automated recovery mechanisms, e.g. Kubernetes liveness probes).

These errors are printed mainly to the application log and are relevant for the healthchecks.

##### Functional errors

Functional errors occur when there is a problem with the data that is being processed or external systems are unavailable and data cannot be sent / fetched as required for the process. While the system might not be able to provide the required function at that moment, it may work with a different dataset or as soon as the external systems recover.

These errors are reported in the Job response and do not directly affect application health.

##### Rules for exception handling

###### Throw or log, don’t do both

When catching an exception, either log the exception and handle the problem or rethrow it, so it can be handled at a higher level of the code. By doing both, an exception might be written to the log multiple times, which can be confusing.

###### Write own base exceptions for (internal) interfaces

By defining a common (checked) base exception for an interface, the caller is forced to handle potential errors, but can keep the logic simple. On the other hand, you still have the possibility to derive various, meaningful exceptions for different error cases, which can then be thrown via the API.

Of course, when using only RuntimeExceptions, this is not necessary - but those can be overlooked quite easily, so be careful there.

###### Central fallback exception handler

There will always be some exception that cannot be handled inside of the code correctly - or it may just have been unforeseen. A central fallback exception handler is required so all problems are visible in the log and the API always returns meaningful responses. In some cases, this is as simple as a HTTP 500.

###### Dont expose too much exception details over API

It’s good to inform the user, why their request did not work, but only if they can do something about it (HTTP 4xx). So in case of application problems, you should not expose details of the problem to the caller. This way, we avoid opening potential attack vectors.

#### Parallelization and threading

The heart of the IRS is the parallel execution of planned jobs. As almost each job requires multiple calls to various endpoints, those are done in parallel as well to reduce the total execution time for each job.

Tasks execution is orchestrated by the JobOrchestrator class. It utilizes a cental ExecutorService, which manages the number of threads and schedules new Task as they come in.

#### Plausibility checks and validation

Data validation happens at two points:

-   IRS API: the data sent by the client is validated to match the model defined in the IRS. If the validation fails, the IRS sends a HTTP 400 response and indicates the problem to the caller.

-   Submodel payload: each time a submodel payload is requested from via EDC, the data is validated against the model defined in the SemanticHub for the matching aspect type.

#### Caching

The IRS caches data provided externally to avoid unnecessary requests and reduce execution time.

Caching is implemented for the following services:

##### BPDM

Whenever a BPN is resolved via BPDM, the partner name is cached on IRS side, as this data does not change.

##### Semantics Hub

Whenever a semantic model schema is requested from the Semantic Hub, it is stored locally until the cache is evicted (configurable). The IRS can preload configured schema models on startup to reduce on demand call times.

Additionally, models can be deployed with the system as a backup to the real Semantic Hub service.

### Development concepts

#### Build, test, deploy

The IRS is built using Maven and utilizes all the standard concepts of it. Test execution is part of the build process and a minimum test coverage of 80% is enforced.

The project setup contains a multi-module Maven build. Commonly used classes (like the IRS data model) should be extracted into a separate submodule and reused across the project. However, this is not a "one-size-fits-all" solution. New submodules should be created with care and require a review by the team.

The Maven build alone only leads up to the JAR artifact of the IRS. Do create Docker images, the Docker build feature is used. This copies all resources into a builder image, builds the software and creates a final Docker image at the end that can then be deployed.

Although the Docker image can be deployed in various ways, the standard solution are the provided Helm charts, which describe the required components as well.

#### Code generation

There are two methods of code generation in the IRS:

##### Lombok

The Lombok library is heavily used to generate boilerplate code (like Constructors, Getters, Setters, Builders…​).
This way, code can be written faster and this boilerplate code is excluded from test coverage, which keeps the test base lean.

##### Swagger / OpenAPI

The API uses OpenAPI annotations to describe the endpoints with all necessary information. The annotations are then used to automatically generate the OpenAPI specification file, which can be viewed in the Swagger UI that is deployed with the application.

The generated OpenAPI specification file is automatically compared to a fixed, stored version of it to avoid unwanted changes of the API.

#### Migration

There currently is no data migration mechanism for the IRS.
In case the model of the persisted data (Jobs) changes, data is dropped and Jobs will need to be recreated.

#### Configurability

The IRS utilizes the configuration mechanism provided by Spring Boot. Configuration properties can be defined in the file `src/main/resources/application.yml`

For local testing purposes, there is an additional configuration file called `application-local.yml`. Values can be overriden there to support the local dev environment.

Other profiles should be avoided. Instead, any value that might need to change in a runtime environment must be overridable using environment variables. The operator must have total control over the configuration of the IRS.

### Operational concepts

#### Administration

##### Configuration

The IRS can be configured using two mechanisms:

###### application.yml

If you build the IRS yourself, you can modify the application.yml config that is shipped with the IRS. This file contains all possible config entries for the application.
Once the Docker image has been built, these values can only be overwritten using the Spring external config mechanism (see <a href="https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/html/boot-features-external-config.html" class="bare">https://docs.spring.io/spring-boot/docs/2.1.9.RELEASE/reference/html/boot-features-external-config.html</a>), e.g. by mounting a config file in the right path or using environment variables.

###### Helm Chart

The most relevant config properties are exposed as environment variables and must be set in the Helm chart so the application can run at all. Check the IRS Helm chart in Git for all available variables.

#### Disaster-Recovery

##### Ephemeral components

All components in the IRS deployment not listed in the persistent components section below are considered ephemeral and are easily replaced in a disaster scenario.
All deployment components are described using Helm charts, which can be used to restore the deployment with the Docker images.
Should the Docker images go missing, they can be restored by executing the build pipelines for the corresponding version tag of the component.

##### Persistent components

These components utilize data persistence, which needs to be backed up separately by the operator.

-   **Minio persistent volume**: Contains the stored Job information. In case of data loss, Jobs can be started again to retrieve the data from the network.

-   **Prometheus persistent volume**: Contains the monitoring data of the IRS. In case of data loss, no analysis can be done for past timeframes.

-   **Vault secrets**: In case of data loss, the credentials stored in the Vault need to be recreated manually. See the deployment view for an overview.

#### Scaling

If the number of consumers raises, the IRS can be scaled up by using more resources for the Deployment Pod. Those resources can be used to utilize more parallel threads to handle Job execution.

#### Clustering

The IRS can run in clustered mode, as each running job is only present in one pod at a time.
Note: as soon as a resume feature is implemented, this needs to be addressed here.

#### Logging

Logs are being written directly to stdout and are picked up by the cluster management.

#### Monitoring

The application can be monitored using Prometheus and Grafana. Both systems are defined in the Helm charts with a default setup.
A number of Grafana dashboards are deployed automatically, to display data about:

-   Pod / JVM resources

-   API metrics

-   Functional information about IRS Jobs

Quality requirements
--------------------

The quality scenarios in this section depict the fundamental quality goals as well as other required quality properties. They allow the evaluation of decision alternatives.

-   **Quality attribute**: A characteristic of software, or a generic term applying to quality factors, quality subfactors, or metric values.

-   **Quality factor**: A management-oriented attribute of software that contributes to its quality.

-   **Quality subfactor**: A decomposition of a quality factor or quality subfactor to its technical components.

-   **Metric value**: A metric output or an element that is from the range of a metric.

-   **Software quality metric**: A function whose inputs are software data and whose output is a single numerical value that can be interpreted as the degree to which software possesses a given attribute that affects its quality.

**Source**: IEEE standard 1061 “Standard for a Software Quality Metrics Methodology”

### List of requirements

This section will be filled soon™.

Glossary
--------

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Term</th>
<th>Description</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p>AAS</p></td>
<td><p>Asset Administration Shell (Industry 4.0)</p></td>
</tr>
<tr class="even">
<td><p>Aspect servers (submodel endpoints)</p></td>
<td><p>Companies participating in the interorganizational data exchange provides their data over aspect servers. The so called "submodel-descriptors" in the AAS shells are pointing to these AspectServers which provide the data-assets of the participating these companies in Catena-X.</p></td>
</tr>
<tr class="odd">
<td><p>BoM</p></td>
<td><p>Bill of Materials</p></td>
</tr>
<tr class="even">
<td><p>Edge</p></td>
<td><p>see Traversal Aspect</p></td>
</tr>
<tr class="odd">
<td><p>IRS</p></td>
<td><p>Item Relationship Service</p></td>
</tr>
<tr class="even">
<td><p>Item Graph</p></td>
<td><p>The result returned via the IRS. This corresponds to a tree structure in which each node represents a part of a virtual asset.</p></td>
</tr>
<tr class="odd">
<td><p>MTPDC</p></td>
<td><p>Formerly known Service Name: Multi Tier Parts Data Chain</p></td>
</tr>
<tr class="even">
<td><p>PRS</p></td>
<td><p>Formerly known Service Name: Parts Relationship Name</p></td>
</tr>
<tr class="odd">
<td><p>Traversal Aspect</p></td>
<td><p>aka Edge: Aspect which the IRS uses for traversal through the data chain. Identified by a parent-child or a child-parent relationship.</p>
<p>Samples: SingleLevelBomAsPlanned, AssemblyPartRelationship and SingleLevelUsageAsBuilt</p></td>
</tr>
</tbody>
</table>

Last updated 2023-02-21 15:46:11 UTC
