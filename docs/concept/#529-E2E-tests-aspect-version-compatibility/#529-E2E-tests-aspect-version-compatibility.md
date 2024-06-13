
# #529 E2E Tests for Aspect Version Compatibility

- [The cucumber test](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - checks for compatibility of [supported model versions](../../../COMPATIBILITY_MATRIX.md)



## SingleLevelBomAsBuilt


Test data:
- [CX_Testdata_529_compatibility_SingleLevelBomAsBuilt.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelBomAsBuilt.json)


Structure of test data:
- SingleLevelBomAsBuilt 3.0.0, BPNL00E2EASPCPT1, urn:uuid:a0589022-a790-4560-aad1-5e8f7939fe92
  - SerialPart 1.0.1, urn:uuid:6d759367-12fe-4f04-9a8c-5c9432135e41
  - SingleLevelBomAsBuilt 3.1.0, BPNL00E2EASPCP11, urn:uuid:4b1b50f7-686d-4c3a-b81a-5f6fc6ae5c42
    - futureAddedField
    - SerialPart 2.0.0, urn:uuid:ff5d998c-aadb-4189-9cf0-9ab5c42c94fe
    - SerialPart 3.0.0, urn:uuid:976dbbce-5a5f-4118-a2bd-5fbca2aeabb5
  - SingleLevelBomAsBuilt 2.0.0, BPNL00E2EASPCP12, urn:uuid:62aa5c23-780c-4036-8416-97922e3f71a4
    - SerialPart 2.0.0, urn:uuid:024f941b-594f-43ce-a42e-d17d1c302685
    - SingleLevelBomAsBuilt 2.0.0, BPNL00E2EASPC121, urn:uuid:2507bfdd-22df-43c2-99ab-9c2cf9ac0251
      - SerialPart 2.0.0, urn:uuid:34c790e9-afa3-475c-a41b-3a86a9969f61

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



