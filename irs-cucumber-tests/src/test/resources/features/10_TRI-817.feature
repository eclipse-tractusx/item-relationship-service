Feature: Default

	Background:
		#@TRI-817
		Given the IRS URL "https://irs.int.demo.catena-x.net"
		And the admin user api key
		

	#h2. INT 
	#||Key||Value||
	#|globalAssetId|urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc|
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
	@TRI-818 @TRI-1452 @TRI-874 @INT @INTEGRATION_TEST @[INT-TEST]
	Scenario: 🔨🧩[INT-TEST] End 2 End for Tier A (ZF) [BPN:BPNL00000003B2OM]
		Given I register an IRS job for globalAssetId "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc" and BPN "BPNL00000003B2OM"
		And aspects : 
		  | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart                          |
		  | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
		  | urn:samm:io.catenax.batch:2.0.0#Batch                                     |
		  | urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling     |
		And collectAspects "true" 
		And lookupBPNs "false"
		And depth 100
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 12 completed and 0 failed items
		And I check, if "bpn summary" contains 4 completed and 0 failed items
		And I check, if "relationships" are equal to "TRI-704-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-704-expected-submodels.json"
		And I check, if submodels contains BPNL number "BPNL00000003B2OM" exactly 1 times	

	#h2. INT 
	#||Key||Value||
	#|globalAssetId|urn:uuid:ed333e9a-5afa-40b2-99da-bae2fd21501e|
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
	@TRI-1070 @TRI-1452 @TRI-874 @INT @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🔨🧩[INT-TEST] End 2 End for OEM B (Mercedes Benz) [BPN: BPNL00000003AVTH]
		Given I register an IRS job for globalAssetId "urn:uuid:ed333e9a-5afa-40b2-99da-bae2fd21501e" and BPN "BPNL00000003AVTH"
		And aspects :
		  | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart                          |
		  | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
		  | urn:samm:io.catenax.batch:2.0.0#Batch                                     |
		  | urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling     |
		  | urn:bamm:io.catenax.physical_dimension:1.0.0#PhysicalDimension            |
		And collectAspects "true" 
		And lookupBPNs "false"
		And depth 10
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 35 completed and 0 failed items
		And I check, if "bpn summary" contains 35 completed and 0 failed items
		And I check, if "relationships" are equal to "TRI-1070-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-1070-expected-submodels.json"
		And I check, if submodels contains BPNL number "BPNL00000003AVTH" exactly 35 times	

	#h2. INT 
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
	@TRI-819 @TRI-1452 @TRI-874 @TEST-315 @INT @INTEGRATION_TEST @[INT-TEST]
	Scenario: 🔨🧩[INT-TEST] End 2 End for OEM-B (MB) [BPN:BPNL00000003AVTH]
		Given I register an IRS job for globalAssetId "urn:uuid:6d505432-8b31-4966-9514-4b753372683f" and BPN "BPNL00000003AVTH"
		And collectAspects "true"
		And lookupBPNs "false"
		And depth 100
		And direction "downward"
		And aspects :
		  | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart                          |
		  | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
		  | urn:samm:io.catenax.batch:2.0.0#Batch                                     |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 271 completed and 0 failed items
		And I check, if "bpn summary" contains 83 completed and 0 failed items
		And I check, if "relationships" are equal to "TRI-767-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-767-expected-submodels.json"
		And I check, if submodels contains BPNL number "BPNL00000003AVTH" exactly 69 times	

	#h2. INT 
	#||Key||Value||
	#|globalAssetId|urn:uuid:e95f3ff2-c5e7-49a5-873b-aee2728917d3|
	#|BPNL| BPNL00000003B2OM |
	#
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
	@TRI-820 @TRI-1452 @TRI-874 @INT @INTEGRATION_TEST @[INT-TEST]
	Scenario: 🔨🧩[INT-TEST] End 2 End for aspect "MaterialForRecycling"
		Given I register an IRS job for globalAssetId "urn:uuid:e95f3ff2-c5e7-49a5-873b-aee2728917d3" and BPN "BPNL00000003B2OM"
		And aspects : 
		  | urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling  |
		And collectAspects "true" 
		And lookupBPNs "false"
		And depth 100
		And direction "downward"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
		And I check, if "summary" contains 5 completed and 3 failed items
		And I check, if "bpn summary" contains 4 completed and 0 failed items
		And I check, if "relationships" are equal to "TRI-528-expected-relationships.json"
		And I check, if "submodels" are equal to "TRI-528-expected-submodels.json"
		#And I check, if submodels contains BPNL number "BPNL00000003B2OM" exactly 1 times