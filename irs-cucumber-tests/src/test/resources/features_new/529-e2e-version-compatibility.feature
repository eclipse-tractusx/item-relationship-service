Feature:  [TESTING] Integration E2E Tests for backward compatibility (Happy + Unhappy Path) #529 (copied from TRI-891)

  Background:
    Given the IRS URL "https://irs.dev.demo.catena-x.net"
    And the admin user api key


  ######################################################################################################################
  ## SingleLevelBomAsPlanned
  ######################################################################################################################

  #  SINGLE_LEVEL_BOM_AS_PLANNED("SingleLevelBomAsPlanned", SingleLevelBomAsPlanned.class, BomLifecycle.AS_PLANNED,
#  Direction.DOWNWARD),
#  SingleLevelBomAsPlanned 	[ 2.0.0; 3.0.0 ] 	- 	Model version

  Scenario: SingleLevelBomAsPlanned current version (3.0.0)
    Given I register an IRS job for globalAssetId "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsPlanned (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asPlanned"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-planned-3-0-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-planned-3-0-0-expected-submodels.json"


  Scenario: SingleLevelBomAsPlanned old version (2.0.0)
    Given I register an IRS job for globalAssetId "urn:uuid:4c1c49a2-3692-4977-aa46-352e668eee3c" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsPlanned (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asPlanned"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-planned-2-0-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-planned-2-0-0-expected-submodels.json"


  Scenario: SingleLevelBomAsPlanned future version (3.1.0)
    Given I register an IRS job for globalAssetId "urn:uuid:4e1b052b-4514-4ebe-a440-fc2728c07944" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsPlanned (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asPlanned"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-planned-3-1-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-planned-3-1-0-expected-submodels.json"


  ######################################################################################################################
  ## SingleLevelBomAsBuilt
  ######################################################################################################################

  #  SINGLE_LEVEL_BOM_AS_BUILT("SingleLevelBomAsBuilt", SingleLevelBomAsBuilt.class, BomLifecycle.AS_BUILT,
  #  Direction.DOWNWARD),
  #  SingleLevelBomAsBuilt 	[ 2.0.0; 3.0.0 ] 	- 	Model version

  Scenario: SingleLevelBomAsBuilt current version (3.0.0)
    Given I register an IRS job for globalAssetId "urn:uuid:15cf842e-b20e-4219-a61b-99c01cec42ea" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsBuilt (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asBuilt"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-built-3-0-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-built-3-0-0-expected-submodels.json"


  Scenario: SingleLevelBomAsBuilt old version (2.0.0)
    Given I register an IRS job for globalAssetId "urn:uuid:15cf842e-b20e-4219-a61b-99c01cec42ea" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsBuilt (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asBuilt"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-built-2-0-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-built-2-0-0-expected-submodels.json"


  Scenario: SingleLevelBomAsBuilt future version (3.1.0)
    Given I register an IRS job for globalAssetId "urn:uuid:a660bcd7-3f27-4a89-a2ef-bf90006e6b68" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsBuilt (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asBuilt"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-built-3-1-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-built-3-1-0-expected-submodels.json"



  ######################################################################################################################
  ## SingleLevelUsageAsBuilt
  ######################################################################################################################

#  SINGLE_LEVEL_USAGE_AS_BUILT("SingleLevelUsageAsBuilt", SingleLevelUsageAsBuilt.class, BomLifecycle.AS_BUILT,
#  Direction.UPWARD);
#  SingleLevelUsageAsBuilt 	3.0.0 	- 	Model version

  Scenario: SingleLevelUsageAsBuilt current version (3.0.0)
    Given I register an IRS job for globalAssetId "urn:uuid:15cf842e-b20e-4219-a61b-99c01cec42ea" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelUsageAsBuilt (see RelationshipAspect)
    And direction "upward"
    And bomLifecycle "asBuilt"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-usage-as-built-3-0-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-usage-as-built-3-0-0-expected-submodels.json"


  Scenario: SingleLevelUsageAsBuilt future version (3.1.0)
    Given I register an IRS job for globalAssetId "urn:uuid:a660bcd7-3f27-4a89-a2ef-bf90006e6b68" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelUsageAsBuilt (see RelationshipAspect)
    And direction "upward"
    And bomLifecycle "asBuilt"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-usage-as-built-3-1-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-usage-as-built-3-1-0-expected-submodels.json"



  ######################################################################################################################
  ## SingleLevelBomAsSpecified
  ######################################################################################################################

#  SINGLE_LEVEL_BOM_AS_SPECIFIED("SingleLevelBomAsSpecified", SingleLevelBomAsSpecified.class, BomLifecycle.AS_SPECIFIED,
#  Direction.DOWNWARD)
#  SingleLevelBomAsSpecified 	1.0.0 	- 	Model version


  Scenario: SingleLevelBomAsSpecified current version (1.0.0)
    Given I register an IRS job for globalAssetId "urn:uuid:15cf842e-b20e-4219-a61b-99c01cec42ea" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsSpecified (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asSpecified"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-specified-3-0-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-specified-3-0-0-expected-submodels.json"


  Scenario: SingleLevelBomAsSpecified future version (1.1.0)
    Given I register an IRS job for globalAssetId "urn:uuid:a660bcd7-3f27-4a89-a2ef-bf90006e6b68" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsSpecified (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asSpecified"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-specified-3-1-0-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-specified-3-1-0-expected-submodels.json"

