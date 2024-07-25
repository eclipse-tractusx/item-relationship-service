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

@TRI-982
Feature: SingleLevelUsageAsBuilt

  Background:
    Given the IRS URL "http://localhost:8080"
    And the admin user api key


  @UMBRELLA @INTEGRATION_TEST
  @TRI-1009 @TRI-1941 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682
  Scenario: End 2 End for upward direction and SingleLevelUsageAsBuilt
    Given I register an IRS job for globalAssetId "urn:uuid:97eb9ea6-7e66-4ad8-aefe-6ed8aa78ccce" and BPN "BPNL00000003AYRE"
    And direction "upward"
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "summary" contains 2 completed and 0 failed items
    And I check, if "relationships" are equal to "TRI-1009-expected-relationships.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-2035 @TRI-1942 @TRI-1843 @TRI-1682 @TRI-1770 @TRI-1941 @TRI-873
  Scenario: Tombstone for not found GlobalAssetId
    Given I register an IRS job for globalAssetId "urn:uuid:aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee" and BPN "BPNL00000003AYRE"
    And depth 1
    And direction "downward"
    And bomLifecycle "asBuilt"
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 2 minutes
    And I check, if "tombstones" are equal to "TRI-2035-expected-tombstones.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-1647 @TRI-1941 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682
  Scenario: End 2 End for aspect "TractionBatteryCode"
    Given I register an IRS job for globalAssetId "urn:uuid:8f5a73b3-766e-47c7-8780-4760a22329af" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 2
    And direction "downward"
    And bomLifecycle "asBuilt"
    And aspects :
      | urn:samm:io.catenax.traction_battery_code:1.0.0#TractionBatteryCode |
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "submodels" are equal to "TRI-1647-expected-submodels.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-767 @TRI-1941 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682
  Scenario: End 2 End for OEM-B (MB) [BPN:BPNL00000003AYRE] (SerialPart 3.0.0, SingleLevelBomAsBuilt 3.0.0 , Batch 3.0.0)
    Given I register an IRS job for globalAssetId "urn:uuid:4509ea7c-c8d2-41a1-83ca-c214ee34af6c" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 100
    And direction "downward"
    And aspects :
      | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |
      | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
      | urn:samm:io.catenax.just_in_sequence_part:3.0.0#JustInSequencePart        |
      | urn:samm:io.catenax.batch:3.0.0#Batch                                     |
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 30 minutes
    And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
    And I check, if "summary" contains 175 completed and 1 failed items
    And I check, if "relationships" are equal to "TRI-767-expected-relationships.json"
    And I check, if "submodels" are equal to "TRI-767-expected-submodels.json"
    And I check, if submodels contains BPNL number "BPNL00000003AYRE" exactly 248 times

  @UMBRELLA @INTEGRATION_TEST
  @TRI-704 @TRI-1941 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770 @TRI-1682
  Scenario: End 2 End for Tier A (ZF) [BPN:BPNL00000003AYRE] (SerialPart 3.0.0, SingleLevelBomAsBuilt 3.0.0 , Batch 3.0.0)
    Given I register an IRS job for globalAssetId "urn:uuid:aeada37a-a1d8-4535-a476-5f5e1142e3fe" and BPN "BPNL00000003AYRE"
    And aspects :
      | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |
      | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
      | urn:samm:io.catenax.batch:3.0.0#Batch                                     |
      | urn:samm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling     |
    And collectAspects "true"
    And depth 100
    And direction "downward"
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
    And I check, if "summary" contains 5 completed and 0 failed items
    And I check, if "relationships" are equal to "TRI-704-expected-relationships.json"
    And I check, if "submodels" are equal to "TRI-704-expected-submodels.json"
    And I check, if submodels contains BPNL number "BPNL00000003AYRE" exactly 7 times