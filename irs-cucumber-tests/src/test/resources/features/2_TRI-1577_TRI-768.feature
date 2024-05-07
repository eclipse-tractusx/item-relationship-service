@TRI-1577
Feature: 👍[RELEASE_3.2.0][HOTFIX] Update Batch Aspectmodel v. 2.0.0
	#h2. Business Value
	# # (y) [ ] *User-business value:*
	# # (y) [ ] *Risk reduction:*
	# # (y) [x] *Regulatory value:*
	# # (y) [ ] *Commercial value:*
	# # (y) [ ] *Market value:*
	# # (y) [ ] *Efficiency value:*
	# # (y) [ ] *Future value:*
	#
	#h2. User Story
	#
	#*As a PO*
	#*I want to fullfill the compliance guardrails*
	#*so that _our application / project have success_*
	#h2. Outcome
	# - (/) aspect model for batch updated to version 2.0.0 >> IRS no longer uses Aspect Models Batch or SerialPart in Code so these changes are solely for plain submodels. I verified locally that Batch 2.0.0 is sucessfully passed in "submodels" in the job response, using the example payload generated from the .ttl files of
	# - (/) Extend the IRS testdata file with new batch model 2.0.0 >> see [PR #502|https://github.com/catenax-ng/tx-item-relationship-service/pull/502]
	# - (/) Test adjusted testdata new on Batch semantic model (Environment: DEV) >> We are still depending from the testdata set -> Requirement defined TDG-26
	# - (/) Add a new story for removal Batch and SerialPart dependencies from code (FUP) >> No follow up is required there are no changes necessary
	# - (/) Check if SubmodelModels are still needed if in the case it is create a new story to get rid of this code >> IRS no longer uses Aspect Models Batch or SerialPart in Code so these changes are solely for plain submodels. I verified locally that Batch 2.0.0 is sucessfully passed in "submodels" in the job response, using the example payload generated from the .ttl files of
	# - (/) Cucumber tests are adjusted to new batch models >> Show successfully execution of cucumber tests [~alexander.bulgakov@partner.doubleslash.de] -> See execution [TRI-1603|https://jira.catena-x.net/secure/XrayExecuteTest!default.jspa?testExecIssueKey=TRI-1603&testIssueKey=TRI-1598]
	#
	#h2. Hints / Details :
	# * PR for Batch Aspect [https://github.com/eclipse-tractusx/sldt-semantic-models/pull/286]
	#
	#Changers:
	# - Semantic ID will change
	# - customerPartId deleted
	# - nameAtCustomer deleted
	#
	#h2. NFR
	#h2. Dependency
	# # (?) [ ] * Portal / Access Management
	# # (?) [ ] * Testdata Management
	# # (?) [ ] * Test Management AND/OR Release Management
	# # (?) [ ] * Semantic Model Teams
	# # (?) [ ] * EDC
	# # (?) [ ] * Decentral twin infrastructure (discovery finders * )
	# # (?) [ ] * IRS
	# # (?) [ ] * To be extended ....
	#
	#h2. TODO:
	# * (-) Fill out description
	# * (-) Fill out Story Points
	# * (-) (Assign an Assignee - might be done during the Sprint)
	# * (-) define Acceptance Criteria
	# * (-) [DoR |https://confluence.catena-x.net/pages/viewpage.action?pageId=917505]

	Background:
		#@TRI-768
		Given the IRS URL "https://irs.dev.demo.catena-x.net"
		And the admin user api key
		

	#*Used testdataset*: 1.5.3
	#
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Get a complete item graph response for a requested and valid globalAssetId over IRS api.
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Job execution for bom lifecycle "asBuild" and direction downward (AssemblyPartRelationship ) without given graph depth of 2 is registered in IRS processing for requested globalAssetId is initated by an irs api consumer
	# # Job processed builds an item graph using AssemblyPartRelationship aspects discovered over DigitalTwin registry and collected over the related endpoint addresses.
	# # Job is processing the complete item graph and stores the job in the internal job store where it could be requested by the api consumer
	# # Api consumer gets an item graph document with the results for the requested globalAssetId
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
	# # DT Twin Registry is up and running and contains valid test data set
	# # Submodel Servers up and running and providing consistent submodel aspects test data aligned with AAS shell endpoints
	# # EDC Consumer is set up and configures to the DT Registry and Submodel 
	# # AASWrapper and AASProxy infrastructure is up and running and configured to EDC Consume
	# # An valid and existing globalAssetId is available, containing AssemblyPartRelationship aspects
	## Refer to *Hints* section for test Setup. 
	#
	#h3. Hints:
	#
	#_<Hints which supports to execute the test case>_
	# * Environment, settings and variables could be found here [IRS - Integration Test (Environments & Settings)|https://confluence.catena-x.net/pages/viewpage.action?pageId=42403839]
	# * *IRS request collection:* Using IRS request collection to execute REST calls [Insomnia Collection |https://github.com/catenax-ng/tx-item-relationship-service/blob/main/local/testing/IRS_Request_Collection.json]
	#* *Keycloak authentication:* Authorization of curl calls refer to Keycloak authentication [IRS Deployment | https://confluence.catena-x.net/pages/viewpage.action?pageId=40503341]
	#* *Remark:* It is important to add client_secret into request - it can be found in shared keypass file CX-IRS.kdbx. Please contact [~Johannes.Zahn@bmw.de] or [~martin.kanal@doubleslash.de] to receive the client_secret.
	#
	#h3. Role:
	#
	#_<Role executing the test case>_
	# * IRS api consumer (role has t.b.d.)
	@TRI-1598 @TRI-1843 @TRI-1942 @TRI-873 @TRI-1770 @TRI-1682 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] BomLifecycle 'asBuilt' for testing "Batch"-Model [BPNL00000003AVTH] 🌟
		Given I register an IRS job for globalAssetId "urn:uuid:8724338c-5c85-4c34-91eb-e2735d58fb0d" and BPN "BPNL00000003AVTH"
		And collectAspects "true"
		And depth 10
		And direction "downward"
		And bomLifecycle "asBuilt"
		And lookupBPNs "false"
		And aspects :
		  | urn:samm:io.catenax.batch:3.0.0#Batch |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if "relationships" are equal to "TRI-1598-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-1598-expected-submodels.json"