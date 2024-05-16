@TRI-1018
Feature: 🎏⭐⭐⭐ [27.03.2023] 👍 Batch Processing for a bunch of globalAssetIds
	#h2. User Story
	#
	#*As* a system which calculates the Remaining Useful Life of a vehicle type based on a bunch of parts 
	#*I want* to pass a number of globalAssetId to the IRS in one API call 
	#*so that* a related research for parts are bundled in a single IRS api call
	#h2. Outcome
	# - <Outcome of this story after implementation>
	#
	#h2. Hints / Details / . Hints & NFR (Technical, Design & Content))* : 
	#
	#*<write down here the developer notes, code snippets and Infos that are important
	#h2. TODO:
	# * (/) Fill out description
	# * (-) Fill out Story Points
	# * (-) Assign an Assignee
	# * (/) define Acceptance Criteria
	# * (-) [DoR |https://confluence.catena-x.net/pages/viewpage.action?pageId=917505] 
	#
	#!screenshot-1.png|thumbnail!
	#
	# 
	#
	# 
	#h2. Sprint Planning 2:
	# * Define model classes
	# ** Define API dependending on model
	# * Implement actual handling of Batches
	# ** Accept Order request with configuration
	# ** Persist it
	# ** Create batches based on Order configuration
	# ** Persist batches
	# *** Persisted batch model may differ from the API batch model (e.g. jobs could only contain the ID of the job in the persistence, and contain more information in the API model)
	# ** Start first jobs (e.g. all jobs of the first batch)
	# *** Store the job id of the started job in the batch it belongs to
	# *** Store the batch id in the MultiTransferJob for cross-reference
	# ** Send back response to caller (Order accepted)
	# ** Listen to finished jobs event
	# *** If all jobs in a batch are done, update the batch status -> trigger callback, if applicable
	# * Implement error handling (determine batch status based on aggregated job status)
	# * Persist and load batches
	# * Extend tavern tests to cover new API
	#
	#h2. Sprint Planning 2 (04.04.2023):
	# * Review current PR first
	# * Address any bugs found in review or on DEV/INT
	# * Implement missing checksum (discuss feature with Martin + Jan first)
	# * Implement timeouts

	Background:
		#@TRI-768
		Given the IRS URL "https://irs.dev.demo.catena-x.net"
		And the admin user api key
		

	#*Used testdataset*: 1.5.0
	#
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Get job created by batch processing to check correct parameter.
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
	@TRI-1267 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Check correct job executions of batch processing (SingleLevelBomAsBuilt/SerialPart 3.0.0)
		Given I register an IRS batch job for globalAssetIds and BPNs:
		  | globalAssetId        							| bpn   		   |
		  | urn:uuid:771d2ccc-a081-4d3a-bcb2-46c6a0a33743   | BPNL0000000XX0X0 |
		  | urn:uuid:3db730be-9de5-4db5-a58d-684de36484e7   | BPNL0000000XX0X0 |
		  | urn:uuid:73173bf5-08df-4898-9d6d-8899015c161e   | BPNL0000000XX0X0 |
		  | urn:uuid:07e0997f-4212-4456-8f27-164b30fc8355   | BPNL0000000XX0X0 |
		  | urn:uuid:88f51be1-3771-4335-8b5c-4c5050123127   | BPNL0000000XX0X0 |
		  | urn:uuid:d3c0bf85-d44f-47c5-990d-fec8a36065c6   | BPNL0000000XX0X0 |
		  | urn:uuid:51ff7c73-34e9-45d4-816c-d92578843e68   | BPNL0000000XX0X0 |
		  | urn:uuid:b21cfd5b-dcf4-46fa-9227-3eb693567dd8   | BPNL0000000XX0X0 |
		  | urn:uuid:8f9d8c7f-6d7a-48f1-9959-9fa3a1a7a891   | BPNL0000000XX0X0 |
		  | urn:uuid:ceb6b964-5779-49c1-b5e9-0ee70528fcbd   | BPNL0000000XX0X0 |
		  | urn:uuid:a4a26b9c-9460-4cc5-8645-85916b86adb0   | BPNL0000000XX0X0 |
		  | urn:uuid:7b87f5d6-f75e-40f1-a439-779ae9f57a21   | BPNL0000000XX0X0 |
		  | urn:uuid:8914a66e-b59b-405f-afff-b97d71ebece3   | BPNL0000000XX0X0 |
		  | urn:uuid:a1082992-cc3b-4da1-af6b-aa692ed71461   | BPNL0000000XX0X0 |
		  | urn:uuid:0ea1aa79-10d4-4df1-8a5a-5b7eafd26163   | BPNL0000000XX0X0 |
		  | urn:uuid:1e35e091-3d3d-421e-9c7e-14cf1c9442a6   | BPNL0000000XX0X0 |
		  | urn:uuid:cc8e9448-b294-46e7-8110-337e8bfa3001   | BPNL0000000XX0X0 |
		  | urn:uuid:fa5804f1-8d4e-437c-aca2-a5491be61758   | BPNL0000000XX0X0 |
		  | urn:uuid:a0f6803c-e4dc-4cda-8ad2-91cc57868449   | BPNL0000000XX0X0 |
		  | urn:uuid:492781f5-62ff-4fb2-876c-3498e2844d13   | BPNL0000000XX0X0 |
		  | urn:uuid:d6142601-5e09-45fe-9b42-e53cf8cd458c   | BPNL0000000XX0X0 |
		And collectAspects "true"
		And depth 1
		And direction "downward"
		And lookupBPNs "false"
		And bomLifecycle "asBuilt"
		And batchStrategy "PRESERVE_BATCH_JOB_ORDER"
		And batchSize 10
		And callbackUrl "https://www.check123.com"
		And aspects :
		  | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
		  | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |
		  
		When I get the order-id
		Then I check, if the order contains 3 batches
		And I check, if batch 3 contains 1 job
		#check batchNumber = 3 and jobsInBatchChecksum = 1
		
		When I get the batch-id of "first" batch
		Then I check, if the batch contains 10 jobs
		And I check, if the batchNumber is 1
		And batchTotal is 3
		And totalJobs is 21
		And jobsInBatchChecksum is 10
		
		When I get the "first" job-id from batch
		Then I check, if job parameter are set with aspects:
		  | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
		  | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |
		And collectAspects is "true"
		And depth is 1
		And direction is "downward"
		And lookupBPNs is "false"
		And bomLifecycle is "asBuilt"
		And callbackUrl is "https://www.check123.com"