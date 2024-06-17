Feature:  [TESTING] Integration E2E Tests for backward compatibility #529

  Background:
    Given the IRS URL "https://irs.dev.demo.catena-x.net"
    And the admin user api key



  ######################################################################################################################
  ## SingleLevelBomAsBuilt
  ######################################################################################################################

  Scenario: SingleLevelBomAsBuilt
    Given I register an IRS job for globalAssetId "urn:uuid:bec0a457-4d6b-4c1c-88f7-125d04f04d68" and BPN "BPNL00000007QG00"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsBuilt (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asBuilt"

    And aspects :
      | urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt |
      | urn:samm:io.catenax.serial_part:1.0.1#SerialPart                          |
      | urn:samm:io.catenax.serial_part:2.0.0#SerialPart                          |
      | urn:samm:io.catenax.serial_part:3.0.0#SerialPart                          |

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-built-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-built-expected-submodels.json"


  ######################################################################################################################
  ## SingleLeveBomAsPlanned
  ######################################################################################################################

  Scenario: SingleLevelBomAsPlanned
    Given I register an IRS job for globalAssetId "urn:uuid:db884234-9266-4c4a-a5f7-de87d95d0cb5" and BPN "BPNL00000003AYRE"
    And collectAspects "true"
    And depth 10

    # SingleLevelBomAsPlanned (see RelationshipAspect)
    And direction "downward"
    And bomLifecycle "asPlanned"

    And aspects :
      | urn:samm:io.catenax.part_as_planned:2.0.0#PartAsPlanned |

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-bom-as-planned-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-bom-as-planned-expected-submodels.json"



  ######################################################################################################################
  ## SingleLevelUsageAsBuilt
  ######################################################################################################################

  Scenario: SingleLevelUsageAsBuilt
    Given I register an IRS job for globalAssetId "urn:uuid:15cf842e-b20e-4219-a61b-99c01cec42ea" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelUsageAsBuilt (see RelationshipAspect)
    And direction "upward"
    And bomLifecycle "asBuilt"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-usage-as-built-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-usage-as-built-expected-submodels.json"



  ######################################################################################################################
  ## SingleLevelUsageAsPlanned
  ######################################################################################################################

  Scenario: SingleLevelUsageAsPlanned
    Given I register an IRS job for globalAssetId "urn:uuid:15cf842e-b20e-4219-a61b-99c01cec42ea" and BPN "BPNL00000003AVTH"
    And collectAspects "true"
    And depth 10

    # SingleLevelUsageAsPlanned (see RelationshipAspect)
    And direction "upward"
    And bomLifecycle "asPlanned"

    When I get the job-id
    Then I check, if the job has status "COMPLETED" within 20 minutes
    And I check, if "relationships" are equal to "529-single-level-usage-as-planned-expected-relationships.json"
    And I check, if "submodels" are equal to "529-single-level-usage-as-planned-expected-submodels.json"


