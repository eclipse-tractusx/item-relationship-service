@TRI-1312
Feature: 👍 [SPIKE][PoC] Enable IRS for multiple LifeCycles (asBuilt & asSpecified)
	#h2. User Story
	#
	#*As* CE Developer,
	#*I want* to use the IRS to fetch multiple trees at the same time,
	#*so that* the application Digital Product Pass can integrate the IRS.
	#h2. Hints / Details / NFR (Technical, Design & Content) : 
	# * contact [~kevin.tram@accenture.com] for further information
	# * asBuilt and AsSpecified
	# * downward
	# * for one globalAssetID
	# * [https://portal.int.demo.catena-x.net/semantichub/] SingleLevelBomAsSpecified BAMM 2.0.0 RELEASED
	# * The Twin aspect for asSpecified is SerialPart
	#
	#h2. External dependencies (due date: 11.07.2023 )
	# * (x) [~sasan.baba-esfahani@bmw.de] & [~luis.rickert@accenture.com] provides testdata for asSpecified BOM --> in a first step: CE provides consistant Testdata files of aspect models Serial Part to SingleLevelBomAsSpecified
	# * (x) [~sasan.baba-esfahani@bmw.de] & [~luis.rickert@accenture.com] provides a infrastructure Provider, EDC, dDTR to test the asSpecified BOM with on vehicle
	# * (x) [~sasan.baba-esfahani@bmw.de] & [~luis.rickert@accenture.com] is added to testdata set as a new requirement
	# * (x) [~sasan.baba-esfahani@bmw.de] & [~luis.rickert@accenture.com] adds asSpecified facets to INT environment
	#
	#
	#h2. Hint 
	# !asSpecified.png|thumbnail! 
	#
	#
	#h2. TODO:
	# * (-) Fill out description
	# * (-) Fill out Story Points
	# * (-) Assign an Assignee
	# * (-) define Acceptance Criteria
	# * (-) [DoR |https://confluence.catena-x.net/pages/viewpage.action?pageId=917505] 

	Background:
		#@TRI-768
		Given the IRS URL "https://irs.dev.demo.catena-x.net"
		And the admin user api key
		

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
	@TRI-1537 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682 @DEV @INTEGRATION_TEST
	Scenario: 🪓🧩[DEV-TEST] End 2 End for aspect "JustInSequencePart" 🌟
		Given I register an IRS job for globalAssetId "urn:uuid:c6d2d642-a055-4ddf-87e3-1a3b02c689e3" and BPN "BPNL00000000BJTL"
		And collectAspects "true"
		And depth 10
		And direction "downward"
		And bomLifecycle "asBuilt"
		And aspects :
		  | urn:samm:io.catenax.just_in_sequence_part:1.0.0#JustInSequencePart |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 20 minutes
		And I check, if "submodels" are equal to "TRI-1537-expected-submodels.json"