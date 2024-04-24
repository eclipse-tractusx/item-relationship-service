# https://github.com/eclipse-tractusx/item-relationship-service/issues/518
Feature: Policy Store

	Background:
		Given the IRS URL "https://irs.dev.demo.catena-x.net/"
		And the admin user api key


	Scenario: Register and get all policies
		Given Clean up all test policies with prefix "integration-test-policy-"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-02" for BPN "BPNL1234567890AB" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-02" for BPN "BPNL1234567890CD" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-03" for BPN "BPNL1234567890CD" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-04" for BPN "BPNL1234567890CD" and validUntil "2222-12-11T10:09:08.777Z"
		When I fetch all policies
        Then the BPN "BPNL1234567890AB" should have the following policies:
			| integration-test-policy-01 |
			| integration-test-policy-02 |
        Then the BPN "BPNL1234567890CD" should have the following policies:
			| integration-test-policy-02 |
			| integration-test-policy-03 |
			| integration-test-policy-04 |


	Scenario: Update a policy validUntil (valid date)
		Given Clean up all test policies with prefix "integration-test-policy-"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2111-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-02" for BPN "BPNL1234567890AB" and validUntil "2222-12-11T10:09:08.777Z"
		When I perform update policy "integration-test-policy-02", BPN "BPNL1234567890AB", validUntil "2333-02-02T02:02:02.222Z"
		When I fetch all policies
		Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-01" and validUntil "2111-12-11T10:09:08.777Z"
		Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-02" and validUntil "2333-02-02T02:02:02.222Z"


	Scenario: Add BPN to policy
		Given Clean up all test policies with prefix "integration-test-policy-"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2222-11-10T09:09:08.777Z"
		Given a policy with policyId "integration-test-policy-02" for BPN "BPNL1234567890CD" and validUntil "3333-11-10T09:09:08.777Z"
		When I perform update policy "integration-test-policy-01", BPN "BPNL1234567890CD", validUntil "4444-11-10T09:09:08.777Z"
		When I fetch all policies
		Then the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-02" and validUntil "3333-11-10T09:09:08.777Z"
		Then the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-01" and validUntil "4444-11-10T09:09:08.777Z"
		# TODO (mfischer): #518 currently the update removes the policy from each BPN not specified in the update request (A), is this intended?
		#                          If not, fix implementation, remove (A) and activate (B), otherwise remove (B).
		# (A)
		Then the BPN "BPNL1234567890AB" should have 0 policies having policyId starting with "integration-test-policy-"
	    # (B)
		#Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-01" and validUntil "2222-11-10T09:09:08.777Z"


#	TODO (mfischer): #518 Scenario: Add policyId to given BPNLs - Check result
#	TODO (mfischer): #518 Scenario: Delete at least 3 policies and - Check result

# 	TODO (mfischer): #518 maybe update with multiple
# 	TODO (mfischer): #518 maybe add tests for default policy handling

#		TODO (mfischer): #518 Test is linked to a suitable Xray Testplan or new one is created
#		TODO (mfischer): #518 Test is executed via CI/CD pipeline