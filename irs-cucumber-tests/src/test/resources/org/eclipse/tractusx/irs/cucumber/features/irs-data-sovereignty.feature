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

Feature: Data Sovereignty

  Background:
    Given the IRS URL "https://irs.dev.demo.catena-x.net"
    And the admin user api key


  @INACTIVE
  @DEV @INTEGRATION_TEST
  @TRI-1983 @TRI-873
  Scenario: Data Sovereignty: Data Consumption - unsuccessfully view offer (restriced access)
    Given I register an IRS job for globalAssetId "urn:uuid:b0cc2dc9-b011-4b21-9c58-f48bd06d439f" and BPN "BPNL00000003B0Q0"
    And collectAspects "true"
    And depth 10
    And direction "downward"
    And aspects :
      | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |
      | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 10 minutes
	# UsagePolicyValidation with "errorDetail": "Consumption of asset 'urn:uuid:93a517a4-1497-4be7-96d0-88d1fdcf1003' is not permitted as the required catalog offer policies do not comply with defined IRS policies.
	#
	#	"tombstones": [
	#		{
	#			"catenaXId": "urn:uuid:b0cc2dc9-b011-4b21-9c58-f48bd06d439f",
	#			"endpointURL": "https://irs-provider-dataplane2.dev.demo.catena-x.net/api/public/urn:uuid:b7ea7ff6-e29f-44ba-b3b4-dc4d2badc02b",
	#			"businessPartnerNumber": "BPNL00000001CRHK",
	#			"processingError": {
	#				"processStep": "UsagePolicyValidation",
	#				"errorDetail": "Consumption of asset 'urn:uuid:93a517a4-1497-4be7-96d0-88d1fdcf1003' is not permitted as the required catalog offer policies do not comply with defined IRS policies.",
	#				"lastAttempt": "2024-05-07T10:15:00.841355517Z",
	#				"retryCounter": 0
	#			},
	#			"policy": {
	#				"permissions": [
	#					{
	#						"edctype": "dataspaceconnector:permission",
	#						"action": {
	#						"type": "USE",
	#							"includedIn": null,
	#							"constraint": null
	#						},
	#						"constraints": [
	#							{
	#								"edctype": "dataspaceconnector:orconstraint",
	#								"constraints": [
	#									{
	#										"edctype": "AtomicConstraint",
	#										"leftExpression": {
	#											"edctype": "dataspaceconnector:literalexpression",
	#											"value": "PURPOSE"
	#										},
	#										"rightExpression": {
	#											"edctype": "dataspaceconnector:literalexpression",
	#											"value": "ID 3.0 NotAccepted"
	#										},
	#										"operator": "EQ"
	#									}
	#								]
	#							}
	#						],
	#						"duties": []
	#					}
	#				],
	#				"prohibitions": [],
	#				"obligations": [],
	#				"extensibleProperties": {},
	#				"inheritsFrom": null,
	#				"assigner": "BPNL00000001CRHK",
	#				"assignee": null,
	#				"target": "urn:uuid:93a517a4-1497-4be7-96d0-88d1fdcf1003",
	#				"@type": {
	#					"@policytype": "offer"
	#				}
	#			}
	#		}
	#	],"
