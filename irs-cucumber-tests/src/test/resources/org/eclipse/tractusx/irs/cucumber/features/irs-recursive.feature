#
# Copyright (c) 2026 Volkswagen AG
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

Feature: IRS Recursive API

  # Addresses, BPNLs and the root globalAssetId come from the environment configuration,
  # never from a scenario. See RecursiveChainEnvironment.
  Background:
    Given the recursive IRS chain of the test environment
    And the admin user api key -- recursive
    And the IRS policy store of the root instance holds accepted policies
    And a chain opening grant at the root instance allowing the granted partners

  # No environment tag on purpose. The Umbrella workflow runs against a kind cluster
  # with a single IRS instance, where a chain cannot exist. Which environment runs this
  # test is decided by the labels of the Xray issue, not by this file.
  @INTEGRATION_TEST
  @RECURSIVE
  @CXTPM-1062
  Scenario: [IRS] Recursive API: Complete result without tombstones
    Given a chain opening grant at the root instance allowing only 1 of the granted partners
    And I register a recursive IRS job for the configured root material
    And ttl "PT10M" -- recursive
    When I get the recursive job-id
    Then I check, if the recursive job has state "COMPLETED" within 10 minutes
    And I check, if the result was collected for bom lifecycle "asPlanned"
    And I check, if the result was collected for 3 aspects
    And I check, if the result contains 1 direct child items
    And I check, if the material tree contains 2 child items in total
    And I check, if the material tree is 2 levels deep
    And I check, if every material carries a quantity with unit "unit:piece"
    And I check, if every material carries 3 aspects
    And I check, if the result contains the material numbers:
      | ZX-55      |
      | 6740244-02 |
    And I check, if the result exposes no partner identities
    And I check, if the result contains no tombstones
    And I check, if the result status is "COMPLETE"

  @INTEGRATION_TEST
  @RECURSIVE
  @CXTPM-1068
  Scenario: [IRS] Recursive API: Only granted partners are included in the result
    Given a chain opening grant at the root instance allowing only 0 of the granted partners
    And I register a recursive IRS job for the configured root material
    And ttl "PT10M" -- recursive
    When I get the recursive job-id
    Then I check, if the recursive job has state "COMPLETED" within 10 minutes
    And I check, if the result status is "COMPLETE"
    And I check, if the result contains 0 direct child items
    And I check, if the result contains no tombstones

  @INTEGRATION_TEST
  @RECURSIVE
  @CXTPM-1070
  Scenario: [IRS] Recursive API: Job without a grant is rejected
    Given no chain opening grant at the root instance
    And I register a recursive IRS job for the configured root material
    Then the job registration is rejected with HTTP status 403 and code "CHAIN_OPENING_GRANT_REJECTED"

  @INTEGRATION_TEST
  @RECURSIVE
  @CXTPM-1069
  Scenario: [IRS] Recursive API: Expired job fails with a deadline tombstone
    Given I register a recursive IRS job for the configured root material
    And ttl "PT1S" -- recursive
    When I get the recursive job-id
    Then I check, if the recursive job has state "ERROR" within 15 minutes
    And I check, if the result status is "FAILED"
    And I check, if the result contains a tombstone with reason "RECURSIVE_DEADLINE_EXCEEDED"
