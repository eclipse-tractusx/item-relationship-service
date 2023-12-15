/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.WebUtils;

@TestConfiguration
public class MockMvcRestExceptionConfiguration implements WebMvcConfigurer {

    private final BasicErrorController errorController;

    public MockMvcRestExceptionConfiguration(final BasicErrorController basicErrorController) {
        this.errorController = basicErrorController;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
                    final Object handler, final Exception ex) throws Exception {

                final int status = response.getStatus();

                if (status >= 400) {
                    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
                    request.setAttribute(WebUtils.ERROR_STATUS_CODE_ATTRIBUTE, status);
                    request.setAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE, request.getRequestURI().toString());
                    // The original exception is already saved as an attribute request
                    Exception exception = (Exception) request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
                    if (exception != null) {
                        request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, exception);
                        request.setAttribute(WebUtils.ERROR_MESSAGE_ATTRIBUTE, exception.getMessage());
                    }
                    new ObjectMapper().writeValue(response.getOutputStream(),
                            MockMvcRestExceptionConfiguration.this.errorController.error(request).getBody());
                }
            }
        });
    }
}