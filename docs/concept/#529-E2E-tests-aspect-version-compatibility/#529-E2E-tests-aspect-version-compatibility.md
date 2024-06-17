
# #529 E2E Tests for Aspect Version Compatibility

- [The cucumber test](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - checks for compatibility of [supported model versions](../../../COMPATIBILITY_MATRIX.md)



## SingleLevelBomAsBuilt


Test data:
- [CX_Testdata_529_compatibility_SingleLevelBomAsBuilt.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelBomAsBuilt.json)


Structure of test data:
- OK: SingleLevelBomAsBuilt 3.0.0, BPNL00000007QG00, urn:uuid:bec0a457-4d6b-4c1c-88f7-125d04f04d68
  - OK: SerialPart 1.0.1, urn:uuid:25bba830-bcd8-4123-be72-e5507b2ae827
  - ??: SingleLevelBomAsBuilt 3.1.0, BPNL00000007RI31, urn:uuid:589d7f27-d200-4009-b24c-27b0f4b81528 --> Tombstone: Unable to find any of the requested shells. Why?
    - ??: futureAddedField
    - ??: SerialPart 2.0.0, urn:uuid:1c151801-0d9d-4fc6-af91-0ddcfe8d86e3
    - ??: SerialPart 3.0.0, urn:uuid:5bd8e513-b69d-4b08-a387-24d787ac5545
  - SingleLevelBomAsBuilt 2.0.0, BPNL00000003CSAP, urn:uuid:6e709d64-bdc7-49f4-a87d-4e1f7e2c3b7c
    - OK: SerialPart 2.0.0, urn:uuid:162cb1f1-c619-47db-ab7f-44a111a762fc
    - OK: SingleLevelBomAsBuilt 2.0.0, BPNL00000007RNYV, urn:uuid:d23f278b-2d8b-4f11-af76-2a9a7bb91cfc
      - OK: SerialPart 2.0.0, urn:uuid:bc8b6912-4be3-4b4c-a28a-919ba405a5c2

Test:

- [Cucumber test - scenario "SingleLevelBomAsBuilt"](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - [529-single-level-bom-as-built-expected-relationships.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-bom-as-built-expected-relationships.json)
  - [529-single-level-bom-as-built-expected-submodels.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-bom-as-built-expected-submodels.json)


## SingleLevelBomAsPlanned

Test data:
- [CX_Testdata_529_compatibility_SingleLevelBomAsPlanned.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelBomAsPlanned.json)

Structure of test data:
- SingleLevelBomAsPlanned 3.0.0, BPNL00E2EASPCPT1, urn:uuid:db884234-9266-4c4a-a5f7-de87d95d0cb5 
  - SingleLevelBomAsPlanned 3.1.0, BPNL00E2EASPCP11, urn:uuid:cb5fdf70-9ddd-42cd-b8c1-0e09bc561601
    - futureAddedField
    - PartAsPlanned 2.0.0, urn:uuid:4bf0acbc-fd1c-46f3-a4df-6da98be2ee8d
  - SingleLevelBomAsPlanned 2.0.0, BPNL00E2EASPCP12, urn:uuid:8acc354c-5ca3-4cd7-83bf-a28681e99f28
    - PartAsPlanned 2.0.0, urn:uuid:1ddca474-2fd6-48c4-aa16-54a03fe40cd7
    - SingleLevelBomAsPlanned 2.0.0, urn:uuid:3161de8c-923a-415a-9a6b-0b902e3153b9
      - PartAsPlanned 2.0.0, urn:uuid:b091ce38-22dc-4f79-bb3b-7e1af648b176
      
  
Test:

- [Cucumber test - scenario "SingleLevelBomAsPlanned"](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
    - [529-single-level-bom-as-planned-expected-relationships.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-bom-as-planned-expected-relationships.json)
    - [529-single-level-bom-as-planned-expected-submodels.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-bom-as-planned-expected-submodels.json)


## SingleLevelUsageAsBuilt

Tests without submodel aspects because there are already tests for SerialPart in the SingleLevelBomAsBuilt tests.


Test data:
- [CX_Testdata_529_compatibility_SingleLevelUsageAsBuilt.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelUsageAsBuilt.json)


Structure of test data:
- SingleLevelUsageAsBuilt 3.0.0, BPNL00E2EASPCPT1, urn:uuid:c7b5c21b-d2c0-4b86-8283-a8932baf03b3
  - SingleLevelUsageAsBuilt 3.1.0, BPNL00E2EASPCP11, urn:uuid:13d35275-3b9b-4f0c-bc5e-00f656bb7cbc
    - futureAddedField
  - SingleLevelUsageAsBuilt 2.0.0, BPNL00E2EASPCP12, urn:uuid:2ea93a69-7ecb-4747-94f4-960c2535dc7b


Test:

- [Cucumber test - scenario "SingleLevelBomAsBuilt"](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - [529-single-level-usage-as-built-expected-relationships.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-usage-as-built-expected-relationships.json)
  - [529-single-level-usage-as-built-expected-submodels.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-usage-as-built-expected-submodels.json)


## SingleLevelUsageAsPlanned

Tests without submodel aspects because there are already tests for PartAsPlanned in the SingleLevelBomAsPlanned tests.

Test data:
- [CX_Testdata_529_compatibility_SingleLevelUsageAsPlanned.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelUsageAsPlanned.json)


Structure of test data:
- SingleLevelUsageAsPlanned 3.0.0, BPNL00E2EASPCPT1, urn:uuid:c4fa7e6c-6aed-4bc4-acc9-b8839c94d13c
  - SingleLevelUsageAsPlanned 3.1.0, BPNL00E2EASPCP11, urn:uuid:d7a1adba-5c99-4b86-b1c6-9f0bb341f170
    - futureAddedField
  - SingleLevelUsageAsPlanned 2.0.0, BPNL00E2EASPCP12, urn:uuid:bdacb7fa-aeb2-4c4d-b555-c8b92b014785


Test:

- [Cucumber test - scenario "SingleLevelUsageAsPlanned"](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - [529-single-level-usage-as-planned-expected-relationships.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-usage-as-planned-expected-relationships.json)
  - [529-single-level-usage-as-planned-expected-submodels.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-usage-as-planned-expected-submodels.json)



