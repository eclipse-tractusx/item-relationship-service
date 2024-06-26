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

Feature: Dummy (just for testing during dev)

  Background:
    Given the IRS URL "https://irs.dev.demo.catena-x.net/" -- policystore
    And the admin user api key -- policystore

  @DEV @INTEGRATION_TEST
  @DUMMY
  Scenario: Asdf
    # just do something for testing workflow here
    When I successfully fetch all policies
