# https://github.com/eclipse-tractusx/item-relationship-service/issues/518
Feature: Policy Store

  Background:

    # TODO (later): re-use test step from other class (instead of copy under different name)
    Given the IRS URL "https://irs.dev.demo.catena-x.net/" -- policystore
    And the admin user api key -- policystore


  # https://jira.catena-x.net/browse/TRI-1950
  Scenario: Register and get all policies

    # cleanup
    Given no policies with prefix "integration-test-policy-" exist

    # act and assert
    Given I want to register a policy with policyId "integration-test-policy-1111"
    And the policy should be associated to BPN "BPNL1234567890AB"
    And the policy should have validUntil "1111-11-11T11:11:11.111Z"
    When I register the policy
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"

    Given I want to register a policy with policyId "integration-test-policy-2222"
    And the policy should be associated to the following BPNs:
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    And the policy should have validUntil "2222-11-11T11:11:11.111Z"
    When I register the policy
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-2222"

    When a policy with policyId "integration-test-policy-3333" is registered for BPN "BPNL1234567890CD" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-3333"

    When a policy with policyId "integration-test-policy-4444" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-4444"

    When I successfully fetch all policies
    Then the BPNs should be associated with policies as follows:
      | BPN              | policyId                     |
      | BPNL1234567890AB | integration-test-policy-1111 |
      | BPNL1234567890AB | integration-test-policy-2222 |
      | BPNL1234567890CD | integration-test-policy-2222 |
      | BPNL1234567890CD | integration-test-policy-3333 |
      | BPNL1234567890CD | integration-test-policy-4444 |

    When I fetch policies for BPNs:
      | BPNL1234567890AB |
    Then the fetch policies for BPN response should have HTTP status 200
    And the BPNs should be associated with policies as follows:
      | BPN              | policyId                     |
      | BPNL1234567890AB | integration-test-policy-1111 |
      | BPNL1234567890AB | integration-test-policy-2222 |

    When I fetch policies for BPNs:
      | BPNL1234567890CD |
    Then the fetch policies for BPN response should have HTTP status 200
    And the BPNs should be associated with policies as follows:
      | BPN | policyId |
      | BPNL1234567890CD | integration-test-policy-2222 |
      | BPNL1234567890CD | integration-test-policy-3333 |
      | BPNL1234567890CD | integration-test-policy-4444 |

    When I fetch policies for BPNs:
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    Then the fetch policies for BPN response should have HTTP status 200
    And the BPNs should be associated with policies as follows:
      | BPN              | policyId                     |
      | BPNL1234567890AB | integration-test-policy-1111 |
      | BPNL1234567890AB | integration-test-policy-2222 |
      | BPNL1234567890CD | integration-test-policy-2222 |
      | BPNL1234567890CD | integration-test-policy-3333 |
      | BPNL1234567890CD | integration-test-policy-4444 |



  # https://jira.catena-x.net/browse/TRI-1951
  Scenario: Update a policy validUntil date

    # cleanup
    Given no policies with prefix "integration-test-policy-" exist

    # set up testdata
    Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"

    # act and assert
    When I update policy "integration-test-policy-1111", BPN "BPNL1234567890AB", validUntil "1112-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 200
    When I successfully fetch all policies
    Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-1111" and validUntil "1112-11-11T11:11:11.111Z"


  # https://jira.catena-x.net/browse/TRI-1952
  Scenario: Update a policy validUntil date for a policy that is associated to multiple BPNs

    # cleanup
    Given no policies with prefix "integration-test-policy-" exist

    # set up testdata
    Given I want to register a policy with policyId "integration-test-policy-1111"
    And the policy should be associated to BPN "BPNL1234567890AB"
    And the policy should have validUntil "1111-11-11T11:11:11.111Z"
    When I register the policy
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"

    Given I want to register a policy with policyId "integration-test-policy-2222"
    And the policy should be associated to BPN "BPNL1234567890AB"
    And the policy should have validUntil "2222-11-11T11:11:11.111Z"
    And the policy should be associated to the following BPNs:
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    When I register the policy
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-2222"

    When a policy with policyId "integration-test-policy-3333" is registered for BPN "BPNL1234567890CD" and validUntil "3333-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-3333"

    When a policy with policyId "integration-test-policy-4444" is registered for BPN "BPNL1234567890CD" and validUntil "4444-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-4444"

    # check the testdata preconditions
    When I successfully fetch all policies
    Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-1111" and validUntil "1111-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-2222" and validUntil "2222-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-2222" and validUntil "2222-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-3333" and validUntil "3333-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-4444" and validUntil "4444-11-11T11:11:11.111Z"

    # act
    When I update policy with policyId "integration-test-policy-2222" and given BPNs using validUntil "2223-11-11T11:11:11.111Z":
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    Then the update policy response should have HTTP status 200

    # assert
    When I successfully fetch all policies
    Then the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-1111" and validUntil "1111-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890AB" should have a policy with policyId "integration-test-policy-2222" and validUntil "2223-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-2222" and validUntil "2223-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-3333" and validUntil "3333-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-4444" and validUntil "4444-11-11T11:11:11.111Z"


  # https://jira.catena-x.net/browse/TRI-1953
  Scenario: Add BPN to policy

    # cleanup
    Given no policies with prefix "integration-test-policy-" exist

    # set up testdata
    Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"

    Given a policy with policyId "integration-test-policy-2222" is registered for BPN "BPNL1234567890CD" and validUntil "2222-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-2222"

    # act and assert
    When I update policy "integration-test-policy-1111", BPN "BPNL1234567890CD", validUntil "1112-11-11T11:11:11.111Z"
    Then the update policy response should have HTTP status 200

    When I successfully fetch all policies
    Then the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-2222" and validUntil "2222-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890CD" should have a policy with policyId "integration-test-policy-1111" and validUntil "1112-11-11T11:11:11.111Z"
    And the BPN "BPNL1234567890AB" should have 0 policies having policyId starting with "integration-test-policy-"


  # https://jira.catena-x.net/browse/TRI-1954
  Scenario: Add policyId to given BPNs

    # cleanup
    Given no policies with prefix "integration-test-policy-" exist

    # set up testdata
    Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
    Then  the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"

    # check the testdata preconditions
    When   I successfully fetch all policies
    Then   the BPN "BPNL1234567890AB" should have the following policies:
      | integration-test-policy-1111 |
    And the BPN "BPNL1234567890CD" should have 0 policies having policyId starting with "integration-test-policy-"

    # act and assert
    When I want to update the policy with policyId "integration-test-policy-1111"
    And the policy should be associated to the following BPNs:
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    And the policy should have validUntil "1112-11-11T11:11:11.111Z"
    And I update the policy
    Then the update policy response should have HTTP status 200

    When I successfully fetch all policies
    Then the BPNs should be associated with policies as follows:
      | BPN              | policyId                     |
      | BPNL1234567890AB | integration-test-policy-1111 |
      | BPNL1234567890CD | integration-test-policy-1111 |


  # https://jira.catena-x.net/browse/TRI-1955
  Scenario: Delete some policies

    # cleanup
    Given no policies with prefix "integration-test-policy-" exist

    # set up testdata
    Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "2222-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-1111"

    Given I add policyId "integration-test-policy-1111" to given BPNs using validUntil "1111-11-11T11:11:11.111Z":
      | BPNL1234567890AB |
      | BPNL1234567890EF |
    Then the update policy response should have HTTP status 200

    Given a policy with policyId "integration-test-policy-2222" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-2222"

    Given I add policyId "integration-test-policy-2222" to given BPNs using validUntil "1111-11-11T11:11:11.111Z":
      | BPNL1234567890AB |
      | BPNL1234567890CD |
    Then the update policy response should have HTTP status 200

    Given  a policy with policyId "integration-test-policy-3333" is registered for BPN "BPNL1234567890CD" and validUntil "2222-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-3333"

    Given   a policy with policyId "integration-test-policy-4444" is registered for BPN "BPNL1234567890CD" and validUntil "2222-11-11T11:11:11.111Z"
    Then the create policy response should have HTTP status 201 and policyId "integration-test-policy-4444"

    # check the testdata preconditions
    When I successfully fetch all policies
    Then the BPNs should be associated with policies as follows:
      | BPN              | policyId                     |
      | BPNL1234567890AB | integration-test-policy-1111 |
      | BPNL1234567890EF | integration-test-policy-1111 |
      | BPNL1234567890AB | integration-test-policy-2222 |
      | BPNL1234567890CD | integration-test-policy-2222 |
      | BPNL1234567890CD | integration-test-policy-3333 |
      | BPNL1234567890CD | integration-test-policy-4444 |

    # act and assert
    When I delete the policy "integration-test-policy-2222"
    Then the delete policy response should have HTTP status 200

    When I delete the policy "integration-test-policy-3333"
    Then the delete policy response should have HTTP status 200

    When I delete the policy "integration-test-policy-4444"
    Then the delete policy response should have HTTP status 200

    When  I successfully fetch all policies
    Then the BPN "BPNL1234567890CD" should have 0 policies having policyId starting with "integration-test-policy-"
    And  the BPNs should be associated with policies as follows:
      | BPN              | policyId                     |
      | BPNL1234567890AB | integration-test-policy-1111 |
      | BPNL1234567890EF | integration-test-policy-1111 |


  # https://jira.catena-x.net/browse/TRI-1965
  Scenario: Registering a policy without validUntil should fail

    # cleanup
    Given no policies with prefix "integration-test-policy-" exist

    # act
    Given I want to register a policy with policyId "integration-test-policy-1111"
    And the policy should be associated to BPN "BPNL1234567890AB"
    And the policy should have no validUntil
    When I register the policy

    # assert
    Then the create policy response should have HTTP status 400


  # https://jira.catena-x.net/browse/TRI-1966
  Scenario: Fetching policies by BPNLs should fail for invalid BPNLs

    # cleanup
    Given no policies with prefix "integration-test-policy-" exist

    # test data setup
    Given a policy with policyId "integration-test-policy-1111" is registered for BPN "BPNL1234567890AB" and validUntil "1111-11-11T11:11:11.111Z"
    Given a policy with policyId "integration-test-policy-2222" is registered for BPN "BPNL1234567890CD" and validUntil "2222-11-11T11:11:11.111Z"
    Given a policy with policyId "integration-test-policy-3333" is registered for BPN "BPNL1234567890EF" and validUntil "3333-11-11T11:11:11.111Z"

    # act
    When I fetch policies for BPNs:
      | BPNL1234567890AB  |
      | BPNL1234567890CD |
    Then the fetch policies for BPN response should have HTTP status 200

    When I fetch policies for BPNs:
      | BPNL1234567890CD |
      | INVALID          |
    Then the fetch policies for BPN response should have HTTP status 400

    When I fetch policies for BPNs:
      | BPNACB |
    Then the fetch policies for BPN response should have HTTP status 400

    When I fetch policies for BPNs:
      | ERRRES |
    Then the fetch policies for BPN response should have HTTP status 400

    When I fetch policies for BPNs:
      | DELETE * FROM Table |
    Then the fetch policies for BPN response should have HTTP status 400



