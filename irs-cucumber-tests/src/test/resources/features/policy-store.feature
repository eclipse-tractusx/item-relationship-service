# https://github.com/eclipse-tractusx/item-relationship-service/issues/518
Feature: Policy Store

	Background:
		# TODO (mfischer): #518 How do we reuse these from E2ETestStepDefinitions (step names must be unique!)
		Given the IRS URL "https://irs.dev.demo.catena-x.net/" -- policystore
		And the admin user api key -- policystore

    # TODO (mfischer): #518: Use builders
    # TODO (mfischer): #518: Get rid of the need to fetch all policies before then
    # TODO (mfischer): #518 Test is linked to a suitable Xray Testplan or new one is created


	Scenario: Register and get all policies
		Given Clean up all test policies with prefix "integration-test-policy-"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2222-12-11T10:09:08.777Z"
		# TODO (mfischer): #518: this is not the correct way to associate a policy with two BPN, it should be created for the first BPN via POST and then associated to the second too via UPDATE
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
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2222-12-11T10:09:08.777Z"
		When I perform update policy "integration-test-policy-01", BPN "BPNL1234567890AB", validUntil "3333-12-11T10:09:08.777Z"
		When I fetch all policies
		Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-01" and validUntil "3333-12-11T10:09:08.777Z"


	Scenario: Add BPN to policy
		Given Clean up all test policies with prefix "integration-test-policy-"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2222-11-10T09:09:08.777Z"
		Given a policy with policyId "integration-test-policy-02" for BPN "BPNL1234567890CD" and validUntil "3333-11-10T09:09:08.777Z"
		When I perform update policy "integration-test-policy-01", BPN "BPNL1234567890CD", validUntil "4444-11-10T09:09:08.777Z"
		When I fetch all policies
		Then the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-02" and validUntil "3333-11-10T09:09:08.777Z"
		Then the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-01" and validUntil "4444-11-10T09:09:08.777Z"
		# TODO (mfischer): #518: currently the update removes the policy from each BPN not specified in the update request (A)
		# (A) policy removed from BPNL1234567890AB
		Then the BPN "BPNL1234567890AB" should have 0 policies having policyId starting with "integration-test-policy-"
	    # (B)
		#Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-01" and validUntil "2222-11-10T09:09:08.777Z"



	Scenario: Add policyId to given BPNs
		Given Clean up all test policies with prefix "integration-test-policy-"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2222-12-11T10:09:08.777Z"
		When I perform update policy "integration-test-policy-01", BPN "BPNL1234567890AB", validUntil "2222-12-11T10:09:08.777Z"
		When I fetch all policies
		Then the BPN "BPNL1234567890AB" should have the following policies:
			| integration-test-policy-01 |
		When I add policyId "integration-test-policy-01" to given BPNs using validUntil "2222-12-11T10:09:08.777Z":
			| BPNL1234567890AB |
			| BPNL1234567890CD |
		When I fetch all policies
		Then the BPN "BPNL1234567890AB" should have the following policies:
			| integration-test-policy-01 |
		Then the BPN "BPNL1234567890CD" should have the following policies:
			| integration-test-policy-01 |




	Scenario: Delete 3 policies
		Given Clean up all test policies with prefix "integration-test-policy-"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890AB" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-02" for BPN "BPNL1234567890AB" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-02" for BPN "BPNL1234567890CD" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-03" for BPN "BPNL1234567890CD" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-04" for BPN "BPNL1234567890CD" and validUntil "2222-12-11T10:09:08.777Z"
		Given a policy with policyId "integration-test-policy-01" for BPN "BPNL1234567890EF" and validUntil "2222-12-11T10:09:08.777Z"
		When I fetch all policies
		Then the BPN "BPNL1234567890AB" should have the following policies:
			| integration-test-policy-01 |
			| integration-test-policy-02 |
		Then the BPN "BPNL1234567890CD" should have the following policies:
			| integration-test-policy-02 |
			| integration-test-policy-03 |
			| integration-test-policy-04 |
		Then the BPN "BPNL1234567890EF" should have the following policies:
			| integration-test-policy-01 |
		When I delete the following policies:
			| integration-test-policy-02 |
			| integration-test-policy-03 |
			| integration-test-policy-04 |
		When I fetch all policies
		Then the BPN "BPNL1234567890AB" should have the following policies:
			| integration-test-policy-01 |
		Then the BPN "BPNL1234567890CD" should have 0 policies having policyId starting with "integration-test-policy-"
		Then the BPN "BPNL1234567890EF" should have the following policies:
			| integration-test-policy-01 |



