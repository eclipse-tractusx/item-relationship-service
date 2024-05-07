Feature: Default

	Background:
		#@TRI-1949
		Given the IRS URL "https://irs.dev.demo.catena-x.net/" -- policystore
		And the admin user api key -- policystore
		

	#{*}Used testdataset{*}: —
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Register policies for different BPNLs
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Register policies (for associating with more than one BPNL POST + UPDATE is needed)
	# # Get all policies and get policies for BPNLs and check
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
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
	# * IRS api admin
	@TRI-1950 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Policy Store API: Register policies
		# cleanup
		Given no policies with prefix "integration-test-policy-" exist
		
		# act and assert
		Given I want to register a policy with policyId "integration-test-policy-1111"
		And the policy should be associated to BPN "BPNL1234567890AB"
		And the policy should have validUntil "1111-11-11T11:11:11.111Z"
		When I register the policy
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"
		
		Given I want to register a policy with policyId "integration-test-policy-2222"
		And the policy should be associated to the following BPNs:
		  | BPNL1234567890AB |
		  | BPNL1234567890CD |
		And the policy should have validUntil "2222-11-11T11:11:11.111Z"
		When I register the policy
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-2222"
		
		When a policy with policyId "integration-test-policy-3333" is registered for BPN "BPNL1234567890CD" and validUntil "3333-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-3333"
		
		When a policy with policyId "integration-test-policy-4444" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-4444"
		
		When I successfully fetch all policies
		Then the BPNs should be associated with policies as follows:
		  | BPN              | policyId                     |
		  | BPNL1234567890AB | integration-test-policy-1111 |
		  | BPNL1234567890AB | integration-test-policy-2222 |
		  | BPNL1234567890CD | integration-test-policy-2222 |
		  | BPNL1234567890CD | integration-test-policy-3333 |
		  | BPNL1234567890CD | integration-test-policy-4444 |
		
		When I fetch policies for BPNs:
		  | BPNL1234567890AB |
		Then the fetch policies for BPN response should have HTTP status 200
		And the BPNs should be associated with policies as follows:
		  | BPN              | policyId                     |
		  | BPNL1234567890AB | integration-test-policy-1111 |
		  | BPNL1234567890AB | integration-test-policy-2222 |
		
		When I fetch policies for BPNs:
		  | BPNL1234567890CD |
		Then the fetch policies for BPN response should have HTTP status 200
		And the BPNs should be associated with policies as follows:
		  | BPN | policyId |
		  | BPNL1234567890CD | integration-test-policy-2222 |
		  | BPNL1234567890CD | integration-test-policy-3333 |
		  | BPNL1234567890CD | integration-test-policy-4444 |
		
		When I fetch policies for BPNs:
		  | BPNL1234567890AB |
		  | BPNL1234567890CD |
		Then the fetch policies for BPN response should have HTTP status 200
		And the BPNs should be associated with policies as follows:
		  | BPN              | policyId                     |
		  | BPNL1234567890AB | integration-test-policy-1111 |
		  | BPNL1234567890AB | integration-test-policy-2222 |
		  | BPNL1234567890CD | integration-test-policy-2222 |
		  | BPNL1234567890CD | integration-test-policy-3333 |
		  | BPNL1234567890CD | integration-test-policy-4444 |	

	#{*}Used testdataset{*}: —
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Update a policies validUntil date and check if successful
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Register a policy
	# # Update its validUntil
	# # Assert that update was successful
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
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
	# * IRS api admin
	@TRI-1951 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Policy Store API: Update a policy validUntil date
		# cleanup
		Given no policies with prefix "integration-test-policy-" exist
		
		# set up testdata
		Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"
		
		# act and assert
		When I update policy "integration-test-policy-1111", BPN "BPNL1234567890AB", validUntil "1112-11-11T11:11:11.111Z"
		Then the update policy response should have HTTP status 200
		When I successfully fetch all policies
		Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-1111" and validUntil "1112-11-11T11:11:11.111Z"	

	#{*}Used testdataset{*}: —
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Update validUntil of a policy that is assocoated to multiple BPNLs and check if successful
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Register a policy and asssociate it with multiple BPNLs
	# # Update its validUntil
	# # Assert that update was successful
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
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
	# * IRS api admin
	@TRI-1952 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Policy Store API:  Update a policy validUntil date for a policy that is associated to multiple BPNs
		# cleanup
		Given no policies with prefix "integration-test-policy-" exist
		
		# set up testdata
		Given I want to register a policy with policyId "integration-test-policy-1111"
		And the policy should be associated to BPN "BPNL1234567890AB"
		And the policy should have validUntil "1111-11-11T11:11:11.111Z"
		When I register the policy
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"
		
		Given I want to register a policy with policyId "integration-test-policy-2222"
		And the policy should be associated to BPN "BPNL1234567890AB"
		And the policy should have validUntil "2222-11-11T11:11:11.111Z"
		And the policy should be associated to the following BPNs:
		  | BPNL1234567890AB |
		  | BPNL1234567890CD |
		When I register the policy
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-2222"
		
		When a policy with policyId "integration-test-policy-3333" is registered for BPN "BPNL1234567890CD" and validUntil "3333-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-3333"
		
		When a policy with policyId "integration-test-policy-4444" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-4444"
		
		# check the testdata preconditions
		When I successfully fetch all policies
		Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-1111" and validUntil "1111-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-2222" and validUntil "2222-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-2222" and validUntil "2222-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-3333" and validUntil "3333-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-4444" and validUntil "4444-11-11T11:11:11.111Z"
		
		# act
		When I update policy with policyId "integration-test-policy-2222" and given BPNs using validUntil "2223-11-11T11:11:11.111Z":
		  | BPNL1234567890AB |
		  | BPNL1234567890CD |
		Then the update policy response should have HTTP status 200
		
		# assert
		When I successfully fetch all policies
		Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-1111" and validUntil "1111-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-2222" and validUntil "2223-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-2222" and validUntil "2223-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-3333" and validUntil "3333-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-4444" and validUntil "4444-11-11T11:11:11.111Z"	

	#{*}Used testdataset{*}: —
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Add BPNL to a policy and check if successful
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Register some policies
	# # Add another BPNL to one of them
	# # Assert that update was successful
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
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
	# * IRS api admin
	@TRI-1953 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Policy Store API: Add BPN to policy
		# cleanup
		Given no policies with prefix "integration-test-policy-" exist
		
		# set up testdata
		Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"
		
		Given a policy with policyId "integration-test-policy-2222" is registered for BPN "BPNL1234567890CD" and validUntil "2222-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-2222"
		
		# act and assert
		When I update policy "integration-test-policy-1111", BPN "BPNL1234567890CD", validUntil "1112-11-11T11:11:11.111Z"
		Then the update policy response should have HTTP status 200
		
		When I successfully fetch all policies
		Then the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-2222" and validUntil "2222-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-1111" and validUntil "1112-11-11T11:11:11.111Z"
		And the BPN "BPNL1234567890AB" should have 0 policies having policyId starting with "integration-test-policy-"	

	#{*}Used testdataset{*}: —
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Register policy without validUntil should fail
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Register policy without validUntil
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
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
	# * IRS api admin
	@TRI-1965 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Policy Store API: Register policies without validUntil (bad case)
		# cleanup
		Given no policies with prefix "integration-test-policy-" exist
		
		# act
		Given I want to register a policy with policyId "integration-test-policy-1111"
		And the policy should be associated to BPN "BPNL1234567890AB"
		And the policy should have no validUntil
		When I register the policy
		
		# assert
		Then the create policy response should have HTTP status 400	

	#{*}Used testdataset{*}: —
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Add a policy to BPNLs and check if successful
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Register policy with BPNL1
	# # Update using BPNL1 and BPNL2 in order to add it to BPNL2 
	# # Assert that update was successful
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
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
	# * IRS api admin
	@TRI-1954 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Policy Store API: Add policyId to given BPNs
		# cleanup
		Given no policies with prefix "integration-test-policy-" exist
		
		# set up testdata
		Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
		Then  the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"
		
		# check the testdata preconditions
		When   I successfully fetch all policies
		Then   the BPN "BPNL1234567890AB" should have the following policies:
		  | integration-test-policy-1111 |
		And the BPN "BPNL1234567890CD" should have 0 policies having policyId starting with "integration-test-policy-"
		
		# act and assert
		When I want to update the policy with policyId "integration-test-policy-1111"
		And the policy should be associated to the following BPNs:
		  | BPNL1234567890AB |
		  | BPNL1234567890CD |
		And the policy should have validUntil "1112-11-11T11:11:11.111Z"
		And I update the policy
		Then the update policy response should have HTTP status 200
		
		When I successfully fetch all policies
		Then the BPNs should be associated with policies as follows:
		  | BPN              | policyId                     |
		  | BPNL1234567890AB | integration-test-policy-1111 |
		  | BPNL1234567890CD | integration-test-policy-1111 |	

	#{*}Used testdataset{*}: —
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * Delete some policies and check if successful
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Register some policies
	# # Delete some of them
	# # Assert that deletion was successful
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
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
	# * IRS api admin
	@TRI-1955 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Policy Store API: Delete some policies
		# cleanup
		Given no policies with prefix "integration-test-policy-" exist
		
		# set up testdata
		Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "2222-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"
		
		Given I add policyId "integration-test-policy-1111" to given BPNs using validUntil "1111-11-11T11:11:11.111Z":
		  | BPNL1234567890AB |
		  | BPNL1234567890EF |
		Then the update policy response should have HTTP status 200
		
		Given a policy with policyId "integration-test-policy-2222" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-2222"
		
		Given I add policyId "integration-test-policy-2222" to given BPNs using validUntil "1111-11-11T11:11:11.111Z":
		  | BPNL1234567890AB |
		  | BPNL1234567890CD |
		Then the update policy response should have HTTP status 200
		
		Given  a policy with policyId "integration-test-policy-3333" is registered for BPN "BPNL1234567890CD" and validUntil "2222-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-3333"
		
		Given   a policy with policyId "integration-test-policy-4444" is registered for BPN "BPNL1234567890CD" and validUntil "2222-11-11T11:11:11.111Z"
		Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-4444"
		
		# check the testdata preconditions
		When I successfully fetch all policies
		Then the BPNs should be associated with policies as follows:
		  | BPN              | policyId                     |
		  | BPNL1234567890AB | integration-test-policy-1111 |
		  | BPNL1234567890EF | integration-test-policy-1111 |
		  | BPNL1234567890AB | integration-test-policy-2222 |
		  | BPNL1234567890CD | integration-test-policy-2222 |
		  | BPNL1234567890CD | integration-test-policy-3333 |
		  | BPNL1234567890CD | integration-test-policy-4444 |
		
		# act and assert
		When I delete the policy "integration-test-policy-2222"
		Then the delete policy response should have HTTP status 200
		
		When I delete the policy "integration-test-policy-3333"
		Then the delete policy response should have HTTP status 200
		
		When I delete the policy "integration-test-policy-4444"
		Then the delete policy response should have HTTP status 200
		
		When  I successfully fetch all policies
		Then the BPN "BPNL1234567890CD" should have 0 policies having policyId starting with "integration-test-policy-"
		And  the BPNs should be associated with policies as follows:
		  | BPN              | policyId                     |
		  | BPNL1234567890AB | integration-test-policy-1111 |
		  | BPNL1234567890EF | integration-test-policy-1111 |	

	#{*}Used testdataset{*}: —
	#h3. Test Objective:
	#
	#_<What is the test objective>_
	# * 
	#Fetching policies by BPNLs should fail for invalid BPNLs
	#
	#h3. Description:
	#
	#_<Short description of the overall test case>_
	# # Register policy 
	# # Fetch policies via BPNLs with some invalid BPNLs
	# # Check that failed for the invalid BPNLs
	#
	#h3. Preconditions:
	#
	#_<Preconditions which have to be fulfilled before test execution>_
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
	# * IRS api admin
	@TRI-1966 @TRI-873 @DEV @INTEGRATION_TEST
	Scenario: 🔨🧩[DEV-TEST] Policy Store API: Fetching policies by BPNLs should fail for invalid BPNLs (bad case)
		# cleanup
		Given no policies with prefix "integration-test-policy-" exist
		
		# test data setup
		Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
		Given a policy with policyId "integration-test-policy-2222" is registered for BPN "BPNL1234567890CD" and validUntil "2222-11-11T11:11:11.111Z"
		Given a policy with policyId "integration-test-policy-3333" is registered for BPN "BPNL1234567890EF" and validUntil "3333-11-11T11:11:11.111Z"
		
		# act
		When I fetch policies for BPNs:
		  | BPNL1234567890AB  |
		  | BPNL1234567890CD |
		Then the fetch policies for BPN response should have HTTP status 200
		
		When I fetch policies for BPNs:
		  | BPNL1234567890CD |
		  | INVALID          |
		Then the fetch policies for BPN response should have HTTP status 400
		
		When I fetch policies for BPNs:
		  | BPNACB |
		Then the fetch policies for BPN response should have HTTP status 400
		
		When I fetch policies for BPNs:
		  | ERRRES |
		Then the fetch policies for BPN response should have HTTP status 400
		
		When I fetch policies for BPNs:
		  | DELETE * FROM Table |
		Then the fetch policies for BPN response should have HTTP status 400