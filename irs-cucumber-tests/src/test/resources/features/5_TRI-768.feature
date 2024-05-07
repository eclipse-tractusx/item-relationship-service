Feature: Default

	Background:
		#@TRI-768
		Given the IRS URL "https://irs.dev.demo.catena-x.net"
		And the admin user api key
		

	#h2. DEV
	#||Key||Value||
	#|globalAssetId|urn:uuid:6d505432-8b31-4966-9514-4b753372683f|
	#|BPNL|BPNL00000003AVTH|
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
	@TRI-767 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682 @DEV @INTEGRATION_TEST @TESTKEY
	Scenario: 🔨🧩[DEV-TEST] End 2 End for OEM-B (MB) [BPN:BPNL00000003AVTH] (SerialPart 3.0.0, SingleLevelBomAsBuilt 3.0.0 , Batch 3.0.0)
		Given I register an IRS job for globalAssetId "urn:uuid:8724338c-5c85-4c34-91eb-e2735d58fb0d" and BPN "BPNL00000003AVTH"
		And collectAspects "true"
		And lookupBPNs "false"
		And depth 100
		And direction "downward"
		And aspects :
		  | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |
		 # | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
		  #| urn:samm:io.catenax.just_in_sequence_part:3.0.0#JustInSequencePart        |
		 # | urn:samm:io.catenax.batch:3.0.0#Batch                                     |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 175 completed and 0 failed items
		And I check, if "bpn summary" contains 0 completed and 0 failed items
		And I check, if "relationships" are equal to "TRI-767-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-767-expected-submodels.json"
		And I check, if submodels contains BPNL number "BPNL00000003AVTH" exactly 69 times	

	#h2. DEV
	#||Key||Value||
	#|globalAssetId|urn:uuid:c6d2d642-a055-4ddf-87e3-1a3b02c689e3|
	#|BPNL|BPNL00000000BJTL|
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
	# # Refer to *Hints* section for test Setup.
	#
	#h3. Hints:
	#
	#_<Hints which supports to execute the test case>_
	# * Environment, settings and variables could be found here [IRS - Integration Test (Environments & Settings)|https://confluence.catena-x.net/pages/viewpage.action?pageId=42403839]
	# * *IRS request collection:* Using IRS request collection to execute REST calls [Insomnia Collection |https://github.com/catenax-ng/tx-item-relationship-service/blob/main/local/testing/IRS_Request_Collection.json]
	# * *Keycloak authentication:* Authorization of curl calls refer to Keycloak authentication [IRS Deployment |https://confluence.catena-x.net/pages/viewpage.action?pageId=40503341]
	# * *Remark:* It is important to add client_secret into request - it can be found in shared keypass file CX-IRS.kdbx. Please contact [~Johannes.Zahn@bmw.de] or [~martin.kanal@doubleslash.de] to receive the client_secret.
	#
	#h3. Role:
	#
	#_<Role executing the test case>_
	# * IRS api consumer (role has t.b.d.)
	@TRI-1913 @TRI-1942 @TRI-1843 @TRI-1682 @TRI-1770 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] End 2 End for aspects "3.0.0#JustInSequencePart", "3.0.0#SerialPart" with "1.0.0#PartSiteInformationAsBuilt"
		Given I register an IRS job for globalAssetId "urn:uuid:c6d2d642-a055-4ddf-87e3-1a3b02c689e3" and BPN "BPNL00000000BJTL"
		And aspects : 
		  | urn:samm:io.catenax.just_in_sequence_part:3.0.0#JustInSequencePart  |
		  | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                    |
		And collectAspects "true" 
		And lookupBPNs "false"
		And depth 1
		And bomLifecycle "asPlanned"
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 1 completed and 0 failed items
		And I check, if "submodels" are equal to "TRI-1913-expected-submodels.json"	

	#h2. DEV
	#||Key||Value||
	#|globalAssetId|urn:uuid:ee9b9c9e-1416-45a3-b683-d98d5d88e548|
	#|BPNL|BPNL00000003AXS3|
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
	# # Refer to *Hints* section for test Setup.
	#
	#h3. Hints:
	#
	#_<Hints which supports to execute the test case>_
	# * Environment, settings and variables could be found here [IRS - Integration Test (Environments & Settings)|https://confluence.catena-x.net/pages/viewpage.action?pageId=42403839]
	# * *IRS request collection:* Using IRS request collection to execute REST calls [Insomnia Collection |https://github.com/catenax-ng/tx-item-relationship-service/blob/main/local/testing/IRS_Request_Collection.json]
	# * *Keycloak authentication:* Authorization of curl calls refer to Keycloak authentication [IRS Deployment |https://confluence.catena-x.net/pages/viewpage.action?pageId=40503341]
	# * *Remark:* It is important to add client_secret into request - it can be found in shared keypass file CX-IRS.kdbx. Please contact [~Johannes.Zahn@bmw.de] or [~martin.kanal@doubleslash.de] to receive the client_secret.
	#
	#h3. Role:
	#
	#_<Role executing the test case>_
	# * IRS api consumer (role has t.b.d.)
	@TRI-1914 @TRI-1942 @TRI-1843 @TRI-1770 @TRI-1682 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] End 2 End for aspects "3.0.0#Batch" with "1.0.0#PartSiteInformationAsBuilt"
		Given I register an IRS job for globalAssetId "urn:uuid:ee9b9c9e-1416-45a3-b683-d98d5d88e548" and BPN "BPNL00000003AXS3"
		And aspects :
		  | urn:samm:io.catenax.batch:3.0.0#Batch |
		And collectAspects "true" 
		And lookupBPNs "false"
		And depth 1
		And bomLifecycle "asBuilt"
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 1 completed and 0 failed items
		And I check, if "submodels" are equal to "TRI-1914-expected-submodels.json"	

	#h2. DEV
	#||Key||Value||
	#|globalAssetId|urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97|
	#|BPNL|BPNL00000003AYRE|
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
	# # Refer to *Hints* section for test Setup.
	#
	#h3. Hints:
	#
	#_<Hints which supports to execute the test case>_
	# * Environment, settings and variables could be found here [IRS - Integration Test (Environments & Settings)|https://confluence.catena-x.net/pages/viewpage.action?pageId=42403839]
	# * *IRS request collection:* Using IRS request collection to execute REST calls [Insomnia Collection |https://github.com/catenax-ng/tx-item-relationship-service/blob/main/local/testing/IRS_Request_Collection.json]
	# * *Keycloak authentication:* Authorization of curl calls refer to Keycloak authentication [IRS Deployment |https://confluence.catena-x.net/pages/viewpage.action?pageId=40503341]
	# * *Remark:* It is important to add client_secret into request - it can be found in shared keypass file CX-IRS.kdbx. Please contact [~Johannes.Zahn@bmw.de] or [~martin.kanal@doubleslash.de] to receive the client_secret.
	#
	#h3. Role:
	#
	#_<Role executing the test case>_
	# * IRS api consumer (role has t.b.d.)
	@TRI-1915 @TRI-1942 @TRI-1843 @TRI-1682 @TRI-1770 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] End 2 End for aspects "3.0.0#SingleLevelBomAsPlanned", "2.0.0#PartAsPlanned"
		Given I register an IRS job for globalAssetId "urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97" and BPN "BPNL00000003AYRE"
		And aspects :
		  | urn:samm:io.catenax.single_level_bom_as_planned:3.0.0#SingleLevelBomAsPlanned   |
		  | urn:samm:io.catenax.part_as_planned:2.0.0#PartAsPlanned                         |
		And collectAspects "true"
		And lookupBPNs "false"
		And depth 1
		And bomLifecycle "asPlanned"
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 3 completed and 0 failed items
		And I check, if "submodels" are equal to "TRI-1915-expected-submodels.json"	

	#*Used testdataset*: 1.6.0
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
	@TRI-1647 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682 @DEV @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🪓🧩[DEV-TEST] End 2 End for aspect "TractionBatteryCode"
		Given I register an IRS job for globalAssetId "urn:uuid:f204622a-f4f3-4be7-b255-06c27524984b" and BPN "BPNL00000003AVTH"
		And collectAspects "true"
		And depth 2
		And direction "downward"
		And bomLifecycle "asBuilt"
		And aspects :
		  | urn:samm:io.catenax.traction_battery_code:1.0.0#TractionBatteryCode |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if "submodels" are equal to "TRI-1647-expected-submodels.json"	

	#h2. DEV
	#||Key||Value||
	#|globalAssetId|urn:uuid:1ad0892a-59c1-4118-8b52-601540973f31|
	#|BPNL|BPNL00000003CSGV|
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
	# # Refer to *Hints* section for test Setup.
	#
	#h3. Hints:
	#
	#_<Hints which supports to execute the test case>_
	# * Environment, settings and variables could be found here [IRS - Integration Test (Environments & Settings)|https://confluence.catena-x.net/pages/viewpage.action?pageId=42403839]
	# * *IRS request collection:* Using IRS request collection to execute REST calls [Insomnia Collection |https://github.com/catenax-ng/tx-item-relationship-service/blob/main/local/testing/IRS_Request_Collection.json]
	# * *Keycloak authentication:* Authorization of curl calls refer to Keycloak authentication [IRS Deployment |https://confluence.catena-x.net/pages/viewpage.action?pageId=40503341]
	# * *Remark:* It is important to add client_secret into request - it can be found in shared keypass file CX-IRS.kdbx. Please contact [~Johannes.Zahn@bmw.de] or [~martin.kanal@doubleslash.de] to receive the client_secret.
	#
	#h3. Role:
	#
	#_<Role executing the test case>_
	# * IRS api consumer (role has t.b.d.)
	@TRI-1918 @TRI-1942 @TRI-1843 @TRI-1682 @TRI-1770 @TRI-873 @DEV @INTEGRATION_TEST 
	Scenario: 🔨🧩[DEV-TEST] End 2 End for aspects "3.0.0#SingleLevelBomAsBuilt" 
		Given I register an IRS job for globalAssetId "urn:uuid:1ad0892a-59c1-4118-8b52-601540973f31" and BPN "BPNL00000003CSGV"
		And aspects : 
		  | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt   |
		And collectAspects "true" 
		And lookupBPNs "false"
		And depth 1
		And bomLifecycle "asBuilt"
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 2 completed and 0 failed items
		And I check, if "submodels" are equal to "TRI-1918-expected-submodels.json"	

	#h2. DEV
	#||Key||Value||
	#|globalAssetId|urn:uuid:e097c30e-9010-4eb9-8a8c-064736aceab8|
	#|BPNL|BPNL00000003B2OM|
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
	@TRI-704 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] End 2 End for Tier A (ZF) [BPN:BPNL00000003B2OM] (SerialPart 3.0.0, SingleLevelBomAsBuilt 3.0.0 , Batch 3.0.0)
		Given I register an IRS job for globalAssetId "urn:uuid:e097c30e-9010-4eb9-8a8c-064736aceab8" and BPN "BPNL00000003B2OM"
		And aspects : 
		  | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |
		  | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
		  | urn:samm:io.catenax.batch:3.0.0#Batch                                     |
		  | urn:samm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling     |
		And collectAspects "true" 
		And lookupBPNs "false"
		And depth 100
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 5 completed and 0 failed items
		And I check, if "bpn summary" contains 4 completed and 0 failed items
		And I check, if "relationships" are equal to "TRI-704-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-704-expected-submodels.json"
		And I check, if submodels contains BPNL number "BPNL00000003B2OM" exactly 1 times	

	#h2. DEV
	#||Key||Value||
	#|globalAssetId|urn:uuid:015aa300-f0ea-47cd-84d5-969f2cb2cf75|
	#|BPNL| BPNL00000003B2OM |
	#
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
	@TRI-528 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] End 2 End for aspect "MaterialForRecycling"
		Given I register an IRS job for globalAssetId "urn:uuid:015aa300-f0ea-47cd-84d5-969f2cb2cf75" and BPN "BPNL00000003B2OM"
		And aspects : 
		  | urn:samm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling  |
		And collectAspects "true" 
		And lookupBPNs "false"
		And depth 1
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 4 completed and 0 failed items
		And I check, if "bpn summary" contains 3 completed and 0 failed items
		And I check, if "relationships" are equal to "TRI-528-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-528-expected-submodels.json"