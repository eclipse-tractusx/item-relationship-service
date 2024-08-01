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

Feature: BomLifecycle 'AsBuilt' implementation

  Background:
    Given the IRS URL "https://irs.int.demo.catena-x.net"
    And the admin user api key


  @INT @INTEGRATION_TEST
  @TRI-1070 @TRI-1452 @TRI-874
  Scenario: 🔨🧩[INT-TEST] End 2 End for OEM B (Mercedes Benz) [BPN: BPNL00000003AVTH]
    Given I register an IRS job for globalAssetId "urn:uuid:ed333e9a-5afa-40b2-99da-bae2fd21501e" and BPN "BPNL00000003AVTH"
    And aspects :
      | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart                          |
      | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
      | urn:samm:io.catenax.batch:2.0.0#Batch                                     |
      | urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling     |
      | urn:bamm:io.catenax.physical_dimension:1.0.0#PhysicalDimension            |
    And collectAspects "true"
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

  @INT @INTEGRATION_TEST
  @TRI-820 @TRI-1452 @TRI-874
  Scenario: 🔨🧩[INT-TEST] End 2 End for aspect "MaterialForRecycling"
    Given I register an IRS job for globalAssetId "urn:uuid:e95f3ff2-c5e7-49a5-873b-aee2728917d3" and BPN "BPNL00000003B2OM"
    And aspects :
      | urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling |
    And collectAspects "true"
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

  @INT @INTEGRATION_TEST
  @TRI-819 @TRI-1452 @TRI-874 @TEST-315
  Scenario: 🔨🧩[INT-TEST] End 2 End for OEM-B (MB) [BPN:BPNL00000003AVTH]
    Given I register an IRS job for globalAssetId "urn:uuid:6d505432-8b31-4966-9514-4b753372683f" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
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

  @INT @INTEGRATION_TEST
  @TRI-818 @TRI-1452 @TRI-874
  Scenario: 🔨🧩[INT-TEST] End 2 End for Tier A (ZF) [BPN:BPNL00000003B2OM]
    Given I register an IRS job for globalAssetId "urn:uuid:334cce52-1f52-4bc9-9dd1-410bbe497bbc" and BPN "BPNL00000003B2OM"
    And aspects :
      | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart                          |
      | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
      | urn:samm:io.catenax.batch:2.0.0#Batch                                     |
      | urn:bamm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling     |
    And collectAspects "true"
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