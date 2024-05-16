Feature: Default

	Background:
		#@TRI-1616
		Given the IRS URL "https://irs-dil.dev.demo.catena-x.net"
		And the admin user api key
		

	#*Used testdata* from TRI-1291.
	#
	#*TBD:*
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
	@TRI-1564 @TRI-1942 @TRI-873 @TRI-1843 @DIL @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🔨🧩[DIL] Check data integrity use case with one changed part
		Given I register an IRS job for globalAssetId "urn:uuid:05abf6ff-8c78-4b72-948b-40e08e9b83f3" and BPN "BPNL00000003AZQP"
		And integrityCheck "true"
		And collectAspects "true"
		And aspects :
		  | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart            |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 10 minutes
		# And I check, if "part-changed" tombstones for data integrity are existing
		And I check, if integrityState is "INVALID"
		
		#Vehicle A
		#urn:uuid:a1fa0f85-697d-4c9d-982f-2501af8e8636
		#Complete and valid integrity aspects
		#
		#Vehicle B
		#urn:uuid:05abf6ff-8c78-4b72-948b-40e08e9b83f3
		#1 Part urn:uuid:dc60fc50-c875-4ce6-a1b9-d59c4c1e0b17 has payload which changed after the hash was created
		#
		#Vehicle C
		##urn:uuid:17e11d67-0315-4504-82cd-8e70a8c33a6a
		#Signatures for Part urn:uuid:8c437b9d-f1b8-4397-b030-c3637eaf9b53 were created with a different private key
		#
		#Vehicle D
		#urn:uuid:5672e8ff-8a73-425e-b2a5-5561b5b21d7a
		#IntegrityAspect for part urn:uuid:ef3865b8-6811-4659-a1b5-e186f8e42258 is missing	

	#*Used testdata* from TRI-1291.
	#
	#*TBD:*
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
	@TRI-1565 @TRI-1942 @TRI-873 @TRI-1843 @DIL @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🔨🧩[DIL] Check data integrity use case with one part created with different private key
		Given I register an IRS job for globalAssetId "urn:uuid:17e11d67-0315-4504-82cd-8e70a8c33a6a" and BPN "BPNL00000003AZQP"
		And integrityCheck "true"
		And aspects :
		  | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt            |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 10 minutes
		#And I check, if "different private key" tombstones for data integrity are existing
		And I check, if integrityState is "INVALID"
		
		#Vehicle A
		#urn:uuid:a1fa0f85-697d-4c9d-982f-2501af8e8636
		#Complete and valid integrity aspects
		#
		#Vehicle B
		#urn:uuid:05abf6ff-8c78-4b72-948b-40e08e9b83f3
		#1 Part urn:uuid:dc60fc50-c875-4ce6-a1b9-d59c4c1e0b17 has payload which changed after the hash was created
		#
		#Vehicle C
		#urn:uuid:17e11d67-0315-4504-82cd-8e70a8c33a6a
		#Signatures for Part urn:uuid:8c437b9d-f1b8-4397-b030-c3637eaf9b53 were created with a different private key
		#
		#Vehicle D
		#urn:uuid:5672e8ff-8a73-425e-b2a5-5561b5b21d7a
		#IntegrityAspect for part urn:uuid:ef3865b8-6811-4659-a1b5-e186f8e42258 is missing	

	#*Used testdata* from TRI-1291.
	#
	#*TBD:*
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
	@TRI-1566 @TRI-1942 @TRI-873 @TRI-1843 @DIL @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🔨🧩[DIL] Check data integrity use case with missing integrity aspect for one part
		Given I register an IRS job for globalAssetId "urn:uuid:5672e8ff-8a73-425e-b2a5-5561b5b21d7a" and BPN "BPNL00000003AZQP"
		And integrityCheck "true"
		And collectAspects "true"
		And aspects :
		  | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart                          |
		  | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 10 minutes
		#And I check, if "missing integrity aspect" tombstones for data integrity are existing
		And I check, if integrityState is "INVALID"
		
		#Vehicle A
		#urn:uuid:a1fa0f85-697d-4c9d-982f-2501af8e8636
		#Complete and valid integrity aspects
		#
		#Vehicle B
		#urn:uuid:05abf6ff-8c78-4b72-948b-40e08e9b83f3
		#1 Part urn:uuid:dc60fc50-c875-4ce6-a1b9-d59c4c1e0b17 has payload which changed after the hash was created
		#
		#Vehicle C
		#urn:uuid:17e11d67-0315-4504-82cd-8e70a8c33a6a
		#Signatures for Part urn:uuid:8c437b9d-f1b8-4397-b030-c3637eaf9b53 were created with a different private key
		#
		#Vehicle D
		#urn:uuid:5672e8ff-8a73-425e-b2a5-5561b5b21d7a
		#IntegrityAspect for part urn:uuid:ef3865b8-6811-4659-a1b5-e186f8e42258 is missing	

	#*Used testdata* from TRI-1291.
	#
	#*TBD:*
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
	@TRI-1563 @TRI-1942 @TRI-873 @TRI-1843 @DIL @INTEGRATION_TEST @INTEGRATION_TEST_2.0.0
	Scenario: 🔨🧩[DIL] Check data integrity use case with complete and valid integrity aspects
		Given I register an IRS job for globalAssetId "urn:uuid:a1fa0f85-697d-4c9d-982f-2501af8e8636" and BPN "BPNL00000003AZQP"
		And integrityCheck "true"
		And aspects :
		  | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart                          |
		  | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
		When I get the job-id
		Then I check, if the job has status "COMPLETED" within 10 minutes
		#And I check, if "no" tombstones for data integrity are existing 
		And I check, if integrityState is "VALID"
		
		#Vehicle A
		#urn:uuid:a1fa0f85-697d-4c9d-982f-2501af8e8636
		#Complete and valid integrity aspects
		#
		#Vehicle B
		#urn:uuid:05abf6ff-8c78-4b72-948b-40e08e9b83f3
		#1 Part urn:uuid:dc60fc50-c875-4ce6-a1b9-d59c4c1e0b17 has payload which changed after the hash was created
		#
		#Vehicle C
		##urn:uuid:17e11d67-0315-4504-82cd-8e70a8c33a6a
		#Signatures for Part urn:uuid:8c437b9d-f1b8-4397-b030-c3637eaf9b53 were created with a different private key
		#
		#Vehicle D
		#urn:uuid:5672e8ff-8a73-425e-b2a5-5561b5b21d7a
		#IntegrityAspect for part urn:uuid:ef3865b8-6811-4659-a1b5-e186f8e42258 is missing