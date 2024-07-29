#
# Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
# Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# SPDX-License-Identifier: Apache-2.0

Feature: IRS Policy Store

  Background:
    Given the IRS URL "http://localhost:8080" -- policystore
    And the admin user api key -- policystore

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-2005
  Scenario: Policy Store API: Delete policy for BPN
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# set up testdata
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "BPNL1234567890AB" and validUntil "4444-11-11T11:11:11.111Z"
    Given I add policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" to given BPNs using validUntil "3333-11-11T11:11:11.111Z":
      | BPNL1234567890AB |
      | BPNL1234567890EF |
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Given I add policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" to given BPNs using validUntil "3333-11-11T11:11:11.111Z":
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    Given  a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
    Given   a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
		
		# check the testdata preconditions
    When I successfully fetch all policies
    Then the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890EF | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-555555555555 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-666666666666 |
		
		# act and assert
    When I remove the policy "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" from BPN "BPNL1234567890AB"
    Then the delete policy response should have HTTP status 200
    When  I successfully fetch all policies
    Then the BPN "BPNL1234567890AB" should have 1 policies having policyId starting with "aaaaaaaa-aaaa-aaaa-aaaa-"
    And  the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890EF | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-555555555555 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-666666666666 |

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-2004
  Scenario: Policy Store API: Trying to register a policy without payload should fail (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# act and assert
    When a policy WITHOUT payload is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400
    And the create policy response should have message containing "payload"

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-111111111111" WITH EMPTY definition is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400
    And the create policy response should have message containing "does not contain all required fields"

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1996
  Scenario: Policy Store API: Try to update policy with invalid policyId (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# act and assert
    When I update policy "#INVALID-POLICY-ID#", BPN "BPNL1234567890AB", validUntil "3334-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 400
    And the update policy response should have message containing "must only contain safe URL path variable characters"

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1995
  Scenario: Policy Store API: Try to delete policy with invalid policyId (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# act and assert
    When I delete the policy "#INVALID-POLICY-ID#"
    Then the delete policy response should have HTTP status 400

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1993
  Scenario: Policy Store API: Registering a duplicate policy for same BPNL fails (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# act and assert
    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-111111111111" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201
    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-111111111111" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400
    And the create policy response should have message containing "already exists"

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1988
  Scenario: Policy Store API: Try to register policy with invalid policyId (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# act and assert
    When a policy with policyId "#INVALID-POLICY-ID#" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1984
  Scenario: Policy Store API: Updating policy for invalid BPNL (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
		
		# act and assert
    When I update policy "aaaaaaaa-aaaa-aaaa-aaaa-333333333333", BPN "BPNA1234567890AB", validUntil "3333-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 400

    When I update policy "aaaaaaaa-aaaa-aaaa-aaaa-333333333333", BPN "BPNS1234567890AB", validUntil "3333-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 400

    When I update policy "aaaaaaaa-aaaa-aaaa-aaaa-333333333333", BPN "BPNACB", validUntil "3333-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 400

    When I update policy "aaaaaaaa-aaaa-aaaa-aaaa-333333333333", BPN "ERRRES", validUntil "3333-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 400

    When I update policy "aaaaaaaa-aaaa-aaaa-aaaa-333333333333", BPN "DELETE * FROM Table", validUntil "3333-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 400

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1981
  Scenario: Policy Store API: Registering policy for invalid BPNL (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# act and assert
    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "BPNA1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "BPNS1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "BPNACB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "ERRRES" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "DELETE * FROM Table" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 400

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1966
  Scenario: Policy Store API: Fetching policies by BPNLs should fail for invalid BPNLs (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# test data setup
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555" is registered for BPN "BPNL1234567890EF" and validUntil "5555-11-11T11:11:11.111Z"
		
		# act
    When I fetch policies for BPNs:
      | BPNL1234567890AB |
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

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1965
  Scenario: Policy Store API: Register policy without validUntil (bad case)
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# act
    Given I want to register a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333"
    And the policy should be associated to BPN "BPNL1234567890AB"
    And the policy should have no validUntil
    When I register the policy
		
		# assert
    Then the create policy response should have HTTP status 400

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1955
  Scenario: Policy Store API: Delete some policies
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# set up testdata
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "BPNL1234567890AB" and validUntil "4444-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333"

    Given I add policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" to given BPNs using validUntil "3333-11-11T11:11:11.111Z":
      | BPNL1234567890AB |
      | BPNL1234567890EF |
    Then the update policy response should have HTTP status 200

    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444"

    Given I add policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" to given BPNs using validUntil "3333-11-11T11:11:11.111Z":
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    Then the update policy response should have HTTP status 200

    Given  a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555"

    Given   a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666"
		
		# check the testdata preconditions
    When I successfully fetch all policies
    Then the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890EF | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-555555555555 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-666666666666 |
		
		# act and assert
    When I delete the policy "aaaaaaaa-aaaa-aaaa-aaaa-444444444444"
    Then the delete policy response should have HTTP status 200

    When I delete the policy "aaaaaaaa-aaaa-aaaa-aaaa-555555555555"
    Then the delete policy response should have HTTP status 200

    When I delete the policy "aaaaaaaa-aaaa-aaaa-aaaa-666666666666"
    Then the delete policy response should have HTTP status 200

    When  I successfully fetch all policies
    Then the BPN "BPNL1234567890CD" should have 0 policies having policyId starting with "aaaaaaaa-aaaa-aaaa-aaaa-"
    And  the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890EF | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1954
  Scenario: Policy Store API: Add policyId to given BPNs
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# set up testdata
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then  the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333"
		
		# check the testdata preconditions
    When   I successfully fetch all policies
    Then   the BPN "BPNL1234567890AB" should have the following policies:
      | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
    And the BPN "BPNL1234567890CD" should have 0 policies having policyId starting with "aaaaaaaa-aaaa-aaaa-aaaa-"
		
		# act and assert
    When I want to update the policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333"
    And the policy should be associated to the following BPNs:
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    And the policy should have validUntil "3334-11-11T11:11:11.111Z"
    And I update the policy
    Then the update policy response should have HTTP status 200

    When I successfully fetch all policies
    Then the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1953
  Scenario: Policy Store API: Add BPN to policy
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# set up testdata
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333"

    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444"
		
		# act and assert
    When I update policy "aaaaaaaa-aaaa-aaaa-aaaa-33333333333", BPN "BPNL1234567890CD", validUntil "3334-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 200

    When I successfully fetch all policies
    Then the BPN "BPNL1234567890CD" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" and validUntil "4444-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333" and validUntil "3334-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890AB" should have 0 policies having policyId starting with "aaaaaaaa-aaaa-aaaa-aaaa-"

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1952
  Scenario: Policy Store API:  Update a policy validUntil date for a policy that is associated to multiple BPNs
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# set up testdata
    Given I want to register a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333"
    And the policy should be associated to BPN "BPNL1234567890AB"
    And the policy should have validUntil "3333-11-11T11:11:11.111Z"
    When I register the policy
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333"

    Given I want to register a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444"
    And the policy should be associated to BPN "BPNL1234567890AB"
    And the policy should have validUntil "4444-11-11T11:11:11.111Z"
    And the policy should be associated to the following BPNs:
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    When I register the policy
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444"

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555" is registered for BPN "BPNL1234567890CD" and validUntil "5555-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555"

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666" is registered for BPN "BPNL1234567890CD" and validUntil "6666-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666"
		
		# check the testdata preconditions
    When I successfully fetch all policies
    Then the BPN "BPNL1234567890AB" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333" and validUntil "3333-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890AB" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" and validUntil "4444-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" and validUntil "4444-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555" and validUntil "5555-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666" and validUntil "6666-11-11T11:11:11.111Z"
		
		# act
    When I update policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" and given BPNs using validUntil "2223-11-11T11:11:11.111Z":
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    Then the update policy response should have HTTP status 200
		
		# assert
    When I successfully fetch all policies
    Then the BPN "BPNL1234567890AB" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333" and validUntil "3333-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890AB" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" and validUntil "2223-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444" and validUntil "2223-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555" and validUntil "5555-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666" and validUntil "6666-11-11T11:11:11.111Z"

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1951
  Scenario: Policy Store API: Update a policy validUntil date
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# set up testdata
    Given a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333" is registered for BPN "BPNL1234567890AB" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333"
		
		# act and assert
    When I update policy "aaaaaaaa-aaaa-aaaa-aaaa-33333333333", BPN "BPNL1234567890AB", validUntil "3334-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 200
    When I successfully fetch all policies
    Then the BPN "BPNL1234567890AB" should have a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-33333333333" and validUntil "3334-11-11T11:11:11.111Z"

  @UMBRELLA @INTEGRATION_TEST
  @POLICY_STORE_API
  @TRI-1950
  Scenario: Policy Store API: Register policies
		# cleanup
    Given no policies with prefix "aaaaaaaa-aaaa-aaaa-aaaa-" exist
		
		# act and assert
    Given I want to register a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333"
    And the policy should be associated to BPN "BPNL1234567890AB"
    And the policy should have validUntil "3333-11-11T11:11:11.111Z"
    When I register the policy
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-333333333333"

    Given I want to register a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444"
    And the policy should be associated to the following BPNs:
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    And the policy should have validUntil "4444-11-11T11:11:11.111Z"
    When I register the policy
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-444444444444"

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555" is registered for BPN "BPNL1234567890CD" and validUntil "5555-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-555555555555"

    When a policy with policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666" is registered for BPN "BPNL1234567890CD" and validUntil "6666-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "aaaaaaaa-aaaa-aaaa-aaaa-666666666666"

    When I successfully fetch all policies
    Then the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-555555555555 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-666666666666 |

    When I fetch policies for BPNs:
      | BPNL1234567890AB |
    Then the fetch policies for BPN response should have HTTP status 200
    And the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |

    When I fetch policies for BPNs:
      | BPNL1234567890CD |
    Then the fetch policies for BPN response should have HTTP status 200
    And the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-555555555555 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-666666666666 |

    When I fetch policies for BPNs:
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    Then the fetch policies for BPN response should have HTTP status 200
    And the BPNs should be associated with policies as follows:
      | BPN              | policyId                             |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-333333333333 |
      | BPNL1234567890AB | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-444444444444 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-555555555555 |
      | BPNL1234567890CD | aaaaaaaa-aaaa-aaaa-aaaa-666666666666 |