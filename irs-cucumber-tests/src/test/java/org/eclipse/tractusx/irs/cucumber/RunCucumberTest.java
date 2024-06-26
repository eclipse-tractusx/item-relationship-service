/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.cucumber;

import static io.cucumber.junit.platform.engine.Constants.ANSI_COLORS_DISABLED_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite(failIfNoTests = false)
@IncludeEngines("cucumber")
@SelectPackages("org.eclipse.tractusx.irs.cucumber")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "org.eclipse.tractusx.irs.cucumber")
@ConfigurationParameter(key = PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME, value = "CUCUMBER_TOKEN_IRS_PLACEHOLDER")
@ConfigurationParameter(key = ANSI_COLORS_DISABLED_PROPERTY_NAME, value = "true")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
                        value = "pretty,junit:target/cucumber-junit-report.xml,html:target/report.html")
public class RunCucumberTest {

    //    private static final String PUBLISH_REPORT_PROPERTY = "publish.report";
    //
    //    static {
    //        String token = "CUCUMBER_TOKEN_IRS_PLACEHOLDER";
    //        if (StringUtils.isNotBlank(token)) {
    //            System.setProperty(PLUGIN_PROPERTY_NAME,
    //                    "pretty,junit:target/cucumber-junit-report.xml,html:target/report.html");
    //            System.setProperty(PLUGIN_PUBLISH_TOKEN_PROPERTY_NAME, token);
    //        }
    //    }
}
