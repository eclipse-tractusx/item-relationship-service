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

@TRI-1312
Feature: IRS support for different aspect models

  Background:
    Given the IRS URL "http://localhost:8080"
    And the admin user api key


  @UMBRELLA @INTEGRATION_TEST
  @TRI-1537
  Scenario: BomLifecycle 'asBuilt' for testing "JustInSequencePart-Model [BPNL00000003AYRE]
    Given I register an IRS job for globalAssetId "urn:uuid:57be2fd7-501e-4fea-9f3b-a6c746c3579d" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 10
    And direction "downward"
    And bomLifecycle "asBuilt"
    And aspects :
      | urn:samm:io.catenax.just_in_sequence_part:3.0.0#JustInSequencePart |
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "submodels" are equal to "TRI-1537-expected-submodels.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-1598
  Scenario: BomLifecycle 'asBuilt' for testing "Batch"-Model [BPNL00000003AYRE]
    Given I register an IRS job for globalAssetId "urn:uuid:54d9154a-01a4-47f1-a798-57d86a5744fa" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 10
    And direction "downward"
    And bomLifecycle "asBuilt"
    And aspects :
      | urn:samm:io.catenax.batch:3.0.0#Batch |
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "TRI-1598-expected-relationships.json"
    And I check, if "submodels" are equal to "TRI-1598-expected-submodels.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-1918
  Scenario: End 2 End for aspects "3.0.0#SingleLevelBomAsBuilt"
    Given I register an IRS job for globalAssetId "urn:uuid:5cfb33e5-0a95-40e9-8cec-392ee6da7cf1" and BPN "BPNL00000003AYRE"
    And aspects :
      | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
    And collectAspects "true"
    And depth 1
    And bomLifecycle "asBuilt"
    And direction "downward"
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
    And I check, if "summary" contains 7 completed and 0 failed items
    And I check, if "submodels" are equal to "TRI-1918-expected-submodels.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-1915
  Scenario: End 2 End for aspects "3.0.0#SingleLevelBomAsPlanned", "2.0.0#PartAsPlanned"
    Given I register an IRS job for globalAssetId "urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97" and BPN "BPNL00000003AYRE"
    And aspects :
      | urn:samm:io.catenax.single_level_bom_as_planned:3.0.0#SingleLevelBomAsPlanned |
      | urn:samm:io.catenax.part_as_planned:2.0.0#PartAsPlanned                       |
    And collectAspects "true"
    And depth 1
    And bomLifecycle "asPlanned"
    And direction "downward"
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
    And I check, if "summary" contains 3 completed and 0 failed items
    And I check, if "submodels" are equal to "TRI-1915-expected-submodels.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-1914
  Scenario: End 2 End for aspects "3.0.0#Batch" with "1.0.0#PartSiteInformationAsBuilt"
    Given I register an IRS job for globalAssetId "urn:uuid:d78045d5-4ee7-4980-b24c-31dafeb6f54f" and BPN "BPNL00000003AYRE"
    And aspects :
      | urn:samm:io.catenax.batch:3.0.0#Batch |
    And collectAspects "true"
    And depth 1
    And bomLifecycle "asBuilt"
    And direction "downward"
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
    And I check, if "summary" contains 1 completed and 0 failed items
    And I check, if "submodels" are equal to "TRI-1914-expected-submodels.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-1913
  Scenario: End 2 End for aspects "3.0.0#JustInSequencePart", "3.0.0#SerialPart" with "1.0.0#PartSiteInformationAsBuilt"
    Given I register an IRS job for globalAssetId "urn:uuid:fc9a079f-1b55-4ccc-8649-e188f899bf39" and BPN "BPNL00000003AYRE"
    And aspects :
      | urn:samm:io.catenax.just_in_sequence_part:3.0.0#JustInSequencePart |
      | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                   |
    And collectAspects "true"
    And depth 1
    And bomLifecycle "asBuilt"
    And direction "downward"
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
    And I check, if "summary" contains 1 completed and 0 failed items
    And I check, if "submodels" are equal to "TRI-1913-expected-submodels.json"

  @UMBRELLA @INTEGRATION_TEST
  @TRI-528
  Scenario: End 2 End for aspect "MaterialForRecycling"
    Given I register an IRS job for globalAssetId "urn:uuid:4b4d41c3-7a7c-4544-b718-8167a70f517d" and BPN "BPNL00000003AYRE"
    And aspects :
      | urn:samm:io.catenax.material_for_recycling:1.1.0#MaterialForRecycling |
    And collectAspects "true"
    And depth 1
    And direction "downward"
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if number of "tombstones" equals to "summary/asyncFetchedItems/failed"
    And I check, if "summary" contains 4 completed and 0 failed items
    And I check, if "relationships" are equal to "TRI-528-expected-relationships.json"
    And I check, if "submodels" are equal to "TRI-528-expected-submodels.json"