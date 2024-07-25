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


# Tests for Aspect Version Compatibility
Feature: Integration Tests for Aspect Version Compatibility #529

  # Notes:
  # - For possible values of "direction" and "bomLifecycle" see RelationshipAspect.
  # - These tests require the following test data:
  #     - CX_Testdata_529_compatibility_*.json


  Background:
    Given the IRS URL "http://localhost:8080"
    And the admin user api key


  # Requires test data CX_Testdata_529_compatibility_SingleLevelBomAsBuilt.json
  @UMBRELLA @INTEGRATION_TEST
  @TRI-2047
  Scenario: SingleLevelBomAsBuilt Version Compatibility
    Given I register an IRS job for globalAssetId "urn:uuid:bec0a457-4d6b-4c1c-88f7-125d04f04d68" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 10

    And direction "downward"
    And bomLifecycle "asBuilt"

    And aspects :
      | urn:samm:io.catenax.serial_part:1.0.1#SerialPart |
      | urn:samm:io.catenax.serial_part:2.0.0#SerialPart |
      | urn:samm:io.catenax.serial_part:3.0.0#SerialPart |

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-built-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-built-expected-submodels.json"


  # Requires test data CX_Testdata_529_compatibility_SingleLevelBomAsPlanned.json
  @UMBRELLA @INTEGRATION_TEST
  @TRI-2048
  Scenario: SingleLevelBomAsPlanned Version Compatibility
    Given I register an IRS job for globalAssetId "urn:uuid:0bc18367-69c3-428f-925d-6f8a461edefd" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 10

    And direction "downward"
    And bomLifecycle "asPlanned"

    And aspects :
      | urn:samm:io.catenax.part_as_planned:2.0.0#PartAsPlanned |

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-planned-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-planned-expected-submodels.json"


  # Requires test data CX_Testdata_529_compatibility_SingleLevelUsageAsBuilt.json
  @UMBRELLA @INTEGRATION_TEST
  @TRI-2049
  Scenario: SingleLevelUsageAsBuilt Version Compatibility
    Given I register an IRS job for globalAssetId "urn:uuid:2ea93a69-7ecb-4747-94f4-960c2535dc7b" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 10

    And direction "upward"
    And bomLifecycle "asBuilt"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-usage-as-built-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-usage-as-built-expected-submodels.json"


  # Requires test data CX_Testdata_529_compatibility_SingleLevelUsageAsPlanned.json
  @UMBRELLA @INTEGRATION_TEST
  @TRI-2050
  Scenario: SingleLevelUsageAsPlanned Version Compatibility
    Given I register an IRS job for globalAssetId "urn:uuid:7fe9ac70-23c4-449a-88c1-3832a1cc6da6" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 10

    And direction "upward"
    And bomLifecycle "asPlanned"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-usage-as-planned-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-usage-as-planned-expected-submodels.json"


