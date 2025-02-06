/********************************************************************************
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.testing.testdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
class TestDataGeneratorTest {
    public static final String BPNL_OEM = "BPNL000000002BR4";
    public static final String BPNS_OEM = "BPNS000000002BR4";
    public static final String BPNL_TIER_1 = "BPNL000000002CS4";
    public static final String BPNS_TIER_1 = "BPNS000000002CS4";
    public static final String BPNL_TIER_2 = "BPNL000000002BR4";
    public static final String BPNS_TIER_2 = "BPNS000000002BR4";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final String SERIAL_PART_TEMPLATE_PATH = "src/test/resources/SerialPart-template.json";

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Generates test data for a specified number of vehicles and writes the data to JSON files.
     * Test data is written into the format of the python upload tool and trace-x upload.
     * <p>
     * Output is written to
     * <ul>
     * <li> target/python-load-test-data.json </li>
     * <li> target/traceX-import-load-test-data.json </li>
     * </ul>
     * <p>
     * This test is disabled and intended to be run locally for creating test data.
     *
     * @throws IOException if an I/O error occurs during file writing.
     */
    @Test
    @Disabled("Disabled test, since this is only intended to be run locally to create testdata.")
    void writeTestDataContainerToJson() throws IOException {
        final int numberOfVehicles = 10_000;
        final List<Map<String, Object>> loadTestData = generateLoadTestData(numberOfVehicles, TestdataType.NOX_SENSOR);
        final HashMap<String, Object> pythonTestdataMap = toPythonUploaderJsonFormat(loadTestData);
        writeToFile(pythonTestdataMap, "target/python-load-test-data.json");

        final TraceXAssetData tracexTestdataMap = toTracexImportJsonFormat(loadTestData);
        writeToFile(tracexTestdataMap, "target/traceX-import-load-test-data.json");

    }

    private TraceXAssetData toTracexImportJsonFormat(final List<Map<String, Object>> loadTestData) {
        final var assets = loadTestData.stream().map(TestDataGeneratorTest::mapToTracexAsset).toList();
        return new TraceXAssetData(assets);
    }

    private static TraceXAssetData.Asset mapToTracexAsset(final Map<String, Object> twinMap) {
        return TraceXAssetData.Asset.builder()
                                    .assetMetaInfo(TraceXAssetData.AssetMetaInfo.builder()
                                                                                .catenaXId(twinMap.get("catenaXId")
                                                                                                  .toString())
                                                                                .build())
                                    .submodels(twinMap.entrySet()
                                                      .stream()
                                                      .filter(entry -> entry.getKey().startsWith("urn:"))
                                                      .map(stringObjectEntry -> TraceXAssetData.Submodel.builder()
                                                                                                        .aspectType(
                                                                                                                stringObjectEntry.getKey())
                                                                                                        .payload(
                                                                                                                stringObjectEntry.getValue())
                                                                                                        .build())
                                                      .toList())
                                    .build();
    }

    private @NotNull HashMap<String, Object> toPythonUploaderJsonFormat(final List<Map<String, Object>> loadTestData)
            throws IOException {
        final HashMap<String, Object> testdataMap = new HashMap<>();
        testdataMap.put("policies", readTestdataPolicy("src/test/resources/testdata-policy.json"));
        testdataMap.put("https://catenax.io/schema/TestDataContainer/1.0.0", loadTestData);
        return testdataMap;
    }

    private @NotNull List<Map<String, Object>> generateLoadTestData(final int numberOfVehicles,
            final TestdataType testdataType) throws IOException {
        final List<Map<String, Object>> loadTestData = new ArrayList<>();
        for (int i = 0; i < numberOfVehicles; i++) {
            switch (testdataType) {
                case BATTERY:
                    loadTestData.addAll(buildVehicleBatteryTree());
                    break;
                case NOX_SENSOR:
                    loadTestData.addAll(buildVehicleWithNOXSensors());
                    break;
            }
        }
        return loadTestData;
    }

    private List<Map<String, Object>> buildVehicleBatteryTree() throws IOException {
        final List<Map<String, Object>> testdataContainer = new ArrayList<>();
        final String catenaXIdBattery = TestDataGenerator.randomUUID();
        final String catenaXIdVehicle = TestDataGenerator.randomUUID();

        // Create Battery Modules
        final int numberOfParts = 8;
        final var batteryChildItems = createPartsAndMapToChildParts(testdataContainer, numberOfParts, "HV MODUL",
                BPNL_TIER_2, BPNS_TIER_2, catenaXIdBattery, BPNL_TIER_1);

        // Create Battery
        final Map<String, Object> battery = new HashMap<>(baseTwin(catenaXIdBattery, BPNL_TIER_1));
        battery.put(TestDataGenerator.SINGLE_LEVEL_BOM_AS_BUILT_URN,
                new SingleLevelBomAsBuilt(catenaXIdBattery, batteryChildItems));

        final SerialPart batterySerialPart = createSerialPartFromTemplate(catenaXIdBattery, BPNL_TIER_1, "Battery",
                BPNS_TIER_1);
        battery.put(TestDataGenerator.SERIAL_PART_URN, batterySerialPart);

        final SingleLevelUsageAsBuilt batteryUsageAsBuilt = new SingleLevelUsageAsBuilt(catenaXIdBattery,
                List.of(TestDataGenerator.createParentItem(catenaXIdVehicle, BPNL_OEM)), List.of(BPNL_TIER_1));
        battery.put(TestDataGenerator.SINGLE_LEVEL_USAGE_AS_BUILT_URN, batteryUsageAsBuilt);

        testdataContainer.add(battery);

        // Create Vehicle
        final Map<String, Object> vehicle = new HashMap<>(baseTwin(catenaXIdVehicle, BPNL_OEM));

        vehicle.put(TestDataGenerator.SINGLE_LEVEL_BOM_AS_BUILT_URN, new SingleLevelBomAsBuilt(catenaXIdVehicle,
                List.of(TestDataGenerator.createChildItem(catenaXIdBattery, BPNL_TIER_1))));

        final SerialPart vehicleSerialPart = createSerialPartFromTemplate(catenaXIdVehicle, BPNL_OEM,
                "Vehicle Fully Electric", BPNS_OEM);
        vehicle.put(TestDataGenerator.SERIAL_PART_URN, vehicleSerialPart);

        testdataContainer.add(vehicle);

        return testdataContainer;
    }

    private List<Map<String, Object>> buildVehicleWithNOXSensors() throws IOException {
        final List<Map<String, Object>> testdataContainer = new ArrayList<>();
        final String catenaXIdVehicle = TestDataGenerator.randomUUID();

        // Create NOX Sensors
        int numberOfSensors = 1 + (int) (Math.random() * 4); // Randomly choose between 1 and 4 sensors
        final var childItems = createPartsAndMapToChildParts(testdataContainer, numberOfSensors, "NOX Sensor",
                BPNL_TIER_1, BPNS_TIER_1, catenaXIdVehicle, BPNL_OEM);

        // Create Vehicle
        final Map<String, Object> vehicle = new HashMap<>(baseTwin(catenaXIdVehicle, BPNL_OEM));
        final SerialPart vehicleSerialPart = createSerialPartFromTemplate(catenaXIdVehicle, BPNL_OEM, "Vehicle",
                BPNS_OEM);
        vehicle.put(TestDataGenerator.SERIAL_PART_URN, vehicleSerialPart);
        vehicle.put(TestDataGenerator.SINGLE_LEVEL_BOM_AS_BUILT_URN,
                new SingleLevelBomAsBuilt(catenaXIdVehicle, childItems));
        testdataContainer.add(vehicle);

        return testdataContainer;
    }

    private @NotNull List<SingleLevelBomAsBuilt.ChildItem> createPartsAndMapToChildParts(
            final List<Map<String, Object>> testdataContainer, final int numberOfParts, final String partName,
            final String bpnl, final String bpns, final String parentCatenaXId, final String parentBpnl)
            throws IOException {
        final List<String> catenaXIds = new ArrayList<>();
        for (int i = 0; i < numberOfParts; i++) {
            final String catenaXId = TestDataGenerator.randomUUID();
            catenaXIds.add(catenaXId);

            final SerialPart serialPart = createSerialPartFromTemplate(catenaXId, bpnl, partName, bpns);
            final SingleLevelUsageAsBuilt singleLevelUsageAsBuilt = TestDataGenerator.createSingleLevelUsageAsBuilt(
                    catenaXId, List.of(TestDataGenerator.createParentItem(parentCatenaXId, parentBpnl)));

            final Map<String, Object> part = new HashMap<>(baseTwin(catenaXId, bpnl));
            part.put(TestDataGenerator.SERIAL_PART_URN, serialPart);
            part.put(TestDataGenerator.SINGLE_LEVEL_USAGE_AS_BUILT_URN, singleLevelUsageAsBuilt);
            testdataContainer.add(part);
        }
        return catenaXIds.stream()
                         .map(childCatenaXId -> TestDataGenerator.createChildItem(childCatenaXId, bpnl))
                         .toList();
    }

    private static HashMap<String, String> baseTwin(final String catenaXId, final String manufacturerId) {
        return new HashMap<>(Map.of("catenaXId", catenaXId, "bpnl", manufacturerId));
    }

    private SerialPart createSerialPartFromTemplate(final String catenaXId, final String bpnl, final String partName,
            final String bpns) throws IOException {
        final Map<String, String> randomReplacementValues = Map.of("manufacturerId", bpnl, "catenaXId", catenaXId,
                "catenaXsiteId", bpns, "nameAtManufacturer", partName, "nameAtCustomer", partName, "partInstanceId",
                TestDataGenerator.randomPartInstanceId(), "country", TestDataGenerator.randomCountryCode3(),
                "manufacturingDate", TestDataGenerator.randomTimestamp().toString(), "manufacturerPartId",
                TestDataGenerator.randomManufacturerPartId(), "customerPartId", TestDataGenerator.randomPartId());
        final String templatedSerialPart = generateFromTemplate(SERIAL_PART_TEMPLATE_PATH, randomReplacementValues);

        return MAPPER.readValue(templatedSerialPart, SerialPart.class);
    }

    public String generateFromTemplate(String templatePath, Map<String, String> values) throws IOException {
        String content = readFileAsString(templatePath);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return content;
    }

    private static @NotNull String readFileAsString(final String templatePath) throws IOException {
        return Files.readString(Paths.get(templatePath));
    }

    private Map<String, Object> readTestdataPolicy(final String path) throws IOException {
        return MAPPER.readValue(readFileAsString(path), Map.class);
    }

    private static void writeToFile(final Object testdataMap, final String path) throws IOException {
        final String jsonOutput = MAPPER.writeValueAsString(testdataMap);
        Files.writeString(Paths.get(path), jsonOutput);
    }

}