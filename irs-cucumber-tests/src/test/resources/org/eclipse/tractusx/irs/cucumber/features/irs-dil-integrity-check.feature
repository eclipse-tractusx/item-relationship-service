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

Feature: Data Integrity Layer

  Background:
    Given the IRS URL "https://irs-dil.dev.demo.catena-x.net"
    And the admin user api key


  @DIL @INTEGRATION_TEST
  @TRI-1566 @TRI-1942 @TRI-873 @TRI-1843
  Scenario: Check data integrity use case with missing integrity aspect for one part
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

  @DIL @INTEGRATION_TEST
  @TRI-1565 @TRI-1942 @TRI-873 @TRI-1843
  Scenario: Check data integrity use case with one part created with different private key
    Given I register an IRS job for globalAssetId "urn:uuid:17e11d67-0315-4504-82cd-8e70a8c33a6a" and BPN "BPNL00000003AZQP"
    And integrityCheck "true"
    And aspects :
      | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 10 minutes
		#And I check, if "different private key" tombstones for data integrity are existing
    And I check, if integrityState is "INVALID"

  @DIL @INTEGRATION_TEST
  @TRI-1564 @TRI-1942 @TRI-873 @TRI-1843
  Scenario: Check data integrity use case with one changed part
    Given I register an IRS job for globalAssetId "urn:uuid:05abf6ff-8c78-4b72-948b-40e08e9b83f3" and BPN "BPNL00000003AZQP"
    And integrityCheck "true"
    And collectAspects "true"
    And aspects :
      | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart |
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 10 minutes
		# And I check, if "part-changed" tombstones for data integrity are existing
    And I check, if integrityState is "INVALID"

  @DIL @INTEGRATION_TEST
  @TRI-1563 @TRI-1942 @TRI-873 @TRI-1843
  Scenario: Check data integrity use case with complete and valid integrity aspects
    Given I register an IRS job for globalAssetId "urn:uuid:a1fa0f85-697d-4c9d-982f-2501af8e8636" and BPN "BPNL00000003AZQP"
    And integrityCheck "true"
    And aspects :
      | urn:bamm:io.catenax.serial_part:1.0.1#SerialPart                          |
      | urn:bamm:io.catenax.single_level_bom_as_built:2.0.0#SingleLevelBomAsBuilt |
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 10 minutes
		#And I check, if "no" tombstones for data integrity are existing
    And I check, if integrityState is "VALID"
