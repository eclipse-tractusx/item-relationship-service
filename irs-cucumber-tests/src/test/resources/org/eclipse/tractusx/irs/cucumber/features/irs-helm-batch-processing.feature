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

@TRI-1018
Feature: IRS Batch Processing

  Background:
    Given the IRS URL "http://localhost:8080"
    And the admin user api key


  @UMBRELLA @INTEGRATION_TEST
  @TRI-1267 @TRI-1941 @TRI-1942 @TRI-873 @TRI-1843 @TRI-1770
  Scenario: Check correct job executions of batch processing (SingleLevelBomAsBuilt/SerialPart 3.0.0)
    Given I register an IRS batch job for globalAssetIds and BPNs:
      | globalAssetId                                 | bpn              |
      | urn:uuid:771d2ccc-a081-4d3a-bcb2-46c6a0a33743 | BPNS00000008BDFH |
      | urn:uuid:3db730be-9de5-4db5-a58d-684de36484e7 | BPNS00000008BDFH |
      | urn:uuid:73173bf5-08df-4898-9d6d-8899015c161e | BPNS00000008BDFH |
      | urn:uuid:07e0997f-4212-4456-8f27-164b30fc8355 | BPNS00000008BDFH |
      | urn:uuid:88f51be1-3771-4335-8b5c-4c5050123127 | BPNS00000008BDFH |
      | urn:uuid:d3c0bf85-d44f-47c5-990d-fec8a36065c6 | BPNS00000008BDFH |
      | urn:uuid:51ff7c73-34e9-45d4-816c-d92578843e68 | BPNS00000008BDFH |
      | urn:uuid:b21cfd5b-dcf4-46fa-9227-3eb693567dd8 | BPNS00000008BDFH |
      | urn:uuid:8f9d8c7f-6d7a-48f1-9959-9fa3a1a7a891 | BPNS00000008BDFH |
      | urn:uuid:ceb6b964-5779-49c1-b5e9-0ee70528fcbd | BPNS00000008BDFH |
      | urn:uuid:a4a26b9c-9460-4cc5-8645-85916b86adb0 | BPNS00000008BDFH |
      | urn:uuid:7b87f5d6-f75e-40f1-a439-779ae9f57a21 | BPNS00000008BDFH |
      | urn:uuid:8914a66e-b59b-405f-afff-b97d71ebece3 | BPNS00000008BDFH |
      | urn:uuid:a1082992-cc3b-4da1-af6b-aa692ed71461 | BPNS00000008BDFH |
      | urn:uuid:0ea1aa79-10d4-4df1-8a5a-5b7eafd26163 | BPNS00000008BDFH |
      | urn:uuid:1e35e091-3d3d-421e-9c7e-14cf1c9442a6 | BPNS00000008BDFH |
      | urn:uuid:cc8e9448-b294-46e7-8110-337e8bfa3001 | BPNS00000008BDFH |
      | urn:uuid:fa5804f1-8d4e-437c-aca2-a5491be61758 | BPNS00000008BDFH |
      | urn:uuid:a0f6803c-e4dc-4cda-8ad2-91cc57868449 | BPNS00000008BDFH |
      | urn:uuid:492781f5-62ff-4fb2-876c-3498e2844d13 | BPNS00000008BDFH |
      | urn:uuid:d6142601-5e09-45fe-9b42-e53cf8cd458c | BPNS00000008BDFH |
    And collectAspects "true"
    And depth 1
    And direction "downward"
    And bomLifecycle "asBuilt"
    And batchStrategy "PRESERVE_BATCH_JOB_ORDER"
    And batchSize 10
    And callbackUrl "https://www.check123.com"
    And aspects :
      | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
      | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |

    When I get the order-id
    Then I check, if the order contains 3 batches
    And I check, if batch 3 contains 1 job

    When I get the batch-id of "first" batch
    Then I check, if the batch contains 10 jobs
    And I check, if the batchNumber is 1
    And batchTotal is 3
    And totalJobs is 21
    And jobsInBatchChecksum is 10

    When I get the "first" job-id from batch
    Then I check, if job parameter are set with aspects:
      | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
      | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |
    And collectAspects is "true"
    And depth is 1
    And direction is "downward"
    And bomLifecycle is "asBuilt"
    And callbackUrl is "https://www.check123.com"
