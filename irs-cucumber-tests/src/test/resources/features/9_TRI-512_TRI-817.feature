@TRI-512
Feature: 👍 [AS_PLANNED] BomLifecycle 'asPlanned' implementation (1.0.0)
	#*As* IRS-Consumer,
	#*I want* to be able to traverse through a BOM as planned,
	#*so that* I can use the IRS to access these structures.
	#
	#h2. Details: 
	#* [Details| https://confluence.catena-x.net/pages/viewpage.action?pageId=53248769]
	#* (!) BOMAsPlanned : Update and extension of BOMAsPlanned is planned for Iteration 2. 
	#* (-) Traversal Aspect SingleLevelBomAsPlanned is used for bomLifecycle": "asPlanned" and direction "downwards"
	#* (-) In case no aspect is choose by the api caller, default aspect "PartAsPlanned" is used for bomLifecycle "asPlanned"
	#
	#h2. Hint / Context : 
	#----
	#* Check Semantic Hub for more detailed information about the Aspects
	#* Aspects are: "PartAsPlanned“ and „SingleLevelBomAsPlanned“
	#* Contact Simon Schulz if further information is needed
	#* Check if namespace starts with _io.catenax_
	#* "SingleLevelBomAsPlanned" results in QueryParameter => "bomLifecycle": "asPlanned",
	#* update local Testdataset with reasonable Testdata (2 levels, 7 Component, each component has PartAsPlanned and SingleLevelBomAsPlanned with 2 children except level2 leafs)
	#* use similar Structure as BomAsBuilt Dataset
	#* https://confluence.catena-x.net/display/PL/Release+2+test+dataset%27s
	#* Testdata: https://confluence.catena-x.net/display/PL/BoMAsPlanned+test+dataset version 1.3.4 asplanned alpha
	#
	#
	#h2. TODO:
	#----
	#* (-) implement functionality
	#* (-) create Test-dataset for BoM as Planned data
	#* (-) create Tests
	#* (-) run Tests
	#* (-) refactor API Documentation
	#
	#h2. Background: 
	#* (/) [~thomas.braun3@zf.com] will take care that testdata for BoM asPlanned are prepared. 
	#* (/) Aspect BoM asPlanned is already available. There is an update until Iteration 2 for . Version 1.0.0
	#* (/) Implementation  BoM AsPlanned im IRS  (Alignment Trace-X Implementation ESS in PI6)
	#* (!) BoM As Planned will be extended Version 1.1.0 will be avaiable in iteration 2.

	Background:
		#@TRI-817
		Given the IRS URL "https://irs.int.demo.catena-x.net"
		And the admin user api key
		

	#*Used testdataset*: 1.4.0
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
	@TRI-872 @TRI-1452 @TRI-874 @INT @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🪓🧩[INT-TEST] End 2 End for BomLifecycle 'asPlanned' Vehicle Model A for BMW [BPNL00000003AYRE]
		Given I register an IRS job for globalAssetId "urn:uuid:0733946c-59c6-41ae-9570-cb43a6e4c79e" and BPN "BPNL00000003AYRE"
		And collectAspects "true"
		And depth 10
		And direction "downward"
		And bomLifecycle "asPlanned"
		And aspects :
		  | urn:bamm:io.catenax.part_as_planned:1.0.1#PartAsPlanned     |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if "relationships" are equal to "TRI-872-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-872-expected-submodels.json"	

	#*Used testdataset*: 1.4.0
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
	@TRI-894 @TRI-1452 @TRI-874 @INT @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🔨🧩[INT-TEST] End 2 End for BomLifecycle 'asPlanned' Vehicle Model C for SAP [BPNL00000003AZQP]
		Given I register an IRS job for globalAssetId "urn:uuid:65e1554e-e5cd-4560-bac1-1352582122fb" and BPN "BPNL00000003AZQP"
		And collectAspects "true"
		And depth 10
		And direction "downward"
		And bomLifecycle "asPlanned"
		And aspects :
		  | urn:bamm:io.catenax.part_as_planned:1.0.1#PartAsPlanned     |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if "relationships" are equal to "TRI-894-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-894-expected-submodels.json"	

	#*Used testdataset*: 1.4.0
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
	@TRI-892 @TRI-1452 @TRI-874 @INT @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🔨🧩[INT-TEST] End 2 End for BomLifecycle 'asPlanned' Vehicle Model B for MercedesBenz [BPNL00000003AVTH]
		Given I register an IRS job for globalAssetId "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b" and BPN "BPNL00000003AVTH"
		And collectAspects "true"
		And depth 10
		And direction "downward"
		And bomLifecycle "asPlanned"
		And aspects :
		  | urn:bamm:io.catenax.part_as_planned:1.0.1#PartAsPlanned     |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if "relationships" are equal to "TRI-892-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-892-expected-submodels.json"