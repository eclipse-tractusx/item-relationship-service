Feature: Policy Store

	Background:
		Given the IRS URL "https://irs.dev.demo.catena-x.net/"
		And the admin user api key
		Given Clean up all test policies with prefix "integration-test-policy-"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2025-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-02" for BPN "BPNL1234567890AB" and validUntil "2025-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-03" for BPN "BPNL1234567890CD" and validUntil "2025-12-12T11:10:09.888Z"
		Given a policy with policyId "integration-test-policy-04" for BPN "BPNL1234567890CD" and validUntil "2025-12-11T11:10:09.888Z"
		Given a policy with policyId "integration-test-policy-05" for BPN "BPNL1234567890EF" and validUntil "2025-12-13T12:11:10.999Z"
		Given a policy with policyId "integration-test-policy-06" for BPN "BPNL1234567890EF" and validUntil "2025-12-13T12:11:10.999Z"
		Given a policy with policyId "integration-test-policy-07" for BPN "BPNL1234567890GH" and validUntil "2025-12-14T13:12:11.111Z"
		Given a policy with policyId "integration-test-policy-08" for BPN "BPNL1234567890GH" and validUntil "2025-12-14T13:12:11.111Z"
		Given a policy with policyId "integration-test-policy-09" for BPN "BPNL1234567890IJ" and validUntil "2025-12-15T14:13:12.222Z"
		Given a policy with policyId "integration-test-policy-10" for BPN "BPNL1234567890IJ" and validUntil "2025-12-15T14:13:12.222Z"


	Scenario: Get all policies
		When I fetch all policies
        Then the BPN "BPNL1234567890AB" should have the following policies:
			| integration-test-policy-01 |
			| integration-test-policy-02 |
        Then the BPN "BPNL1234567890CD" should have the following policies:
			| integration-test-policy-03 |
			| integration-test-policy-04  |
        Then the BPN "BPNL1234567890EF" should have the following policies:
			| integration-test-policy-05 |
			| integration-test-policy-06 |
        Then the BPN "BPNL1234567890GH" should have the following policies:
			| integration-test-policy-07 |
			| integration-test-policy-08 |
        Then the BPN "BPNL1234567890IJ" should have the following policies:
			| integration-test-policy-09 |
			| integration-test-policy-10 |


#	Scenario: Update a policy valid
#		When: I update validUntil on policy "integration-test-policy-01", BPN "BPNL1234567890AB" to "2026-06-05T04:03:02.111Z"
#		Then: ValidUntil on policy "integration-test-policy-01", BPN "BPNL1234567890AB" should equal "2026-06-05T04:03:02.111Z"
#
#	Scenario: Add BPNL to policy - Check result
#
#	Scenario: Add policyId to given BPNLs - Check result
#
#	Scenario: Delete at least 3 policies and - Check result


	# TODO maybe add tests for default policy handling

# https://github.com/eclipse-tractusx/item-relationship-service/issues/518
#	Integration tests (integration or Tavern) covering policy store happy path executed in Ci/CD pipeline before PR is merged
#	Test covers journey
#		Registration of more than 10 different policies for at least 5 BPNLs
#		Requesting all policies > All registered policies are available for the related BPNLs
#		Update a policy validUntil (valid date)
#		Add BPNL to policy - Check result
#		Add policyId to given BPNLs - Check result
#		Delete at least 3 policies and - Check result
#	Test is linked to a suitable Xray Testplan or new one is created
#	Test is executed via CI/CD pipeline