
# #529 E2E Tests for Aspect Version Compatibility

- [The cucumber test](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - checks for compatibility of [supported model versions](../../../COMPATIBILITY_MATRIX.md)



## SingleLevelBomAsBuilt


Test data:
- [CX_Testdata_529_compatibility_SingleLevelBomAsBuilt.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelBomAsBuilt.json)


Structure of test data:
- SingleLevelBomAsBuilt 3.0.0, BPNL00000007QG00, urn:uuid:bec0a457-4d6b-4c1c-88f7-125d04f04d68
  - SerialPart 1.0.1
  - SingleLevelBomAsBuilt 3.1.0, BPNL00000007RI31, urn:uuid:589d7f27-d200-4009-b24c-27b0f4b81528
    - futureAddedField
    - SerialPart 2.0.0
    - SerialPart 3.0.0
  - SingleLevelBomAsBuilt 2.0.0, BPNL00000003CSAP, urn:uuid:6e709d64-bdc7-49f4-a87d-4e1f7e2c3b7c
    - SerialPart 2.0.0
    - SingleLevelBomAsBuilt 2.0.0, BPNL00000007RNYV, urn:uuid:d23f278b-2d8b-4f11-af76-2a9a7bb91cfc
      - SerialPart 2.0.0

Test:

- [Cucumber test - scenario "SingleLevelBomAsBuilt"](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - [529-single-level-bom-as-built-expected-relationships.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-bom-as-built-expected-relationships.json)
  - [529-single-level-bom-as-built-expected-submodels.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-bom-as-built-expected-submodels.json)


## SingleLevelBomAsPlanned

Test data:
- [CX_Testdata_529_compatibility_SingleLevelBomAsPlanned.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelBomAsPlanned.json)

Structure of test data:
- SingleLevelBomAsPlanned 3.0.0, BPNL00000007QG00, urn:uuid:0bc18367-69c3-428f-925d-6f8a461edefd
  - SingleLevelBomAsPlanned 3.1.0, BPNL00000007QG00, urn:uuid:db60f953-8894-4568-ab74-746ce00e78a1
    - futureAddedField
    - PartAsPlanned 2.0.0 Test part 1
  - SingleLevelBomAsPlanned 2.0.0, BPNL00000007QG00, urn:uuid:4e8dd2c1-0d21-4794-af92-03a12f85a2eb
    - PartAsPlanned 2.0.0 Test part 2
    - SingleLevelBomAsPlanned 2.0.0, BPNL00000007QG00, urn:uuid:7eb80b65-2e43-438c-8c2a-32b814d2cb88
      - PartAsPlanned 2.0.0 Test part 3


Test:

- [Cucumber test - scenario "SingleLevelBomAsPlanned"](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - [529-single-level-bom-as-planned-expected-relationships.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-bom-as-planned-expected-relationships.json)
  - [529-single-level-bom-as-planned-expected-submodels.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-bom-as-planned-expected-submodels.json)


## SingleLevelUsageAsBuilt

Tests without submodel aspects because there are already tests for SerialPart in the SingleLevelBomAsBuilt tests.


Test data:
- [CX_Testdata_529_compatibility_SingleLevelUsageAsBuilt.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelUsageAsBuilt.json)


Structure of test data:
- SingleLevelUsageAsBuilt 3.0.0, BPNL00000007QG00, urn:uuid:677582a5-bdca-45e1-a671-7a98ff5ddcb7
  - SingleLevelUsageAsBuilt 3.1.0, BPNL00000007QG00, urn:uuid:13d35275-3b9b-4f0c-bc5e-00f656bb7cbc
    - futureAddedField
    - SingleLevelUsageAsBuilt 2.0.0, BPNL00000007QG00, urn:uuid:2ea93a69-7ecb-4747-94f4-960c2535dc7b


Test:

- [Cucumber test - scenario "SingleLevelBomAsBuilt"](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - [529-single-level-usage-as-built-expected-relationships.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-usage-as-built-expected-relationships.json)
  - [529-single-level-usage-as-built-expected-submodels.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-usage-as-built-expected-submodels.json)


## SingleLevelUsageAsPlanned

Tests without submodel aspects because there are already tests for PartAsPlanned in the SingleLevelBomAsPlanned tests.

Test data:
- [CX_Testdata_529_compatibility_SingleLevelUsageAsPlanned.json](../../../local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelUsageAsPlanned.json)


Structure of test data:
- SingleLevelUsageAsPlanned 3.0.0, BPNL00000007QG00, urn:uuid:4e8aa114-cf50-4780-811c-11723c9f0647
  - SingleLevelUsageAsPlanned 3.1.0, BPNL00000007QG00, urn:uuid:01f4b57e-1ab3-4822-ab1b-e20651f6fd32
    - futureAddedField
    - SingleLevelUsageAsPlanned 2.0.0, BPNL00000007QG00, urn:uuid:7fe9ac70-23c4-449a-88c1-3832a1cc6da6


Test:

- [Cucumber test - scenario "SingleLevelUsageAsPlanned"](../../../irs-cucumber-tests/src/test/resources/features_new/529-version-compatibility.feature)
  - [529-single-level-usage-as-planned-expected-relationships.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-usage-as-planned-expected-relationships.json)
  - [529-single-level-usage-as-planned-expected-submodels.json](../../../irs-cucumber-tests/src/test/resources/expected-files/529-single-level-usage-as-planned-expected-submodels.json)



