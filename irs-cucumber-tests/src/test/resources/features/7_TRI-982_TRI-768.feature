@TRI-982
Feature: 👍 SingleLevelUsageAsBuilt ready to use on Dev environment
	#*As a* Trace-X user
	#*I want* to be able to send notification upwards in the supply chain
	#*so that* I can tell my costumer, that there is an issue with a part
	#h2. Hint:
	# - Check the Model is correct
	# - expected Result for a Battery Cell is to have several relations in the relationship array
	# - Add testcase for objects focused in picture below
	#!screenshot-2.png|thumbnail!
	#
	#*Sprint Planning 2*
	# * Investigate on potential issue
	# * Create testcase for expected result

	Background:
		#@TRI-768
		Given the IRS URL "https://irs.dev.demo.catena-x.net"
		And the admin user api key
		

	#h2. DEV
	#||Key||Value||
	#|globalAssetId|urn:uuid:e7777066-e453-4431-beb3-4e99d042f923|
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
	@TRI-1009 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] End 2 End for upward direction and SingleLevelUsageAsBuilt ⌛
		Given I register an IRS job for globalAssetId "urn:uuid:e7777066-e453-4431-beb3-4e99d042f923" and BPN "BPNL00000003AVTH"
		And direction "upward"
		And lookupBPNs "false"
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if "summary" contains 2 completed and 0 failed items
		And I check, if "bpn summary" contains 1 completed and 0 failed items
		And I check, if "relationships" are equal to "TRI-1009-expected-relationships.json"