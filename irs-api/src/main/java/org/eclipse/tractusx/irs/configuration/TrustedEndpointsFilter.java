/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.configuration;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;

/**
 * Filter do differ between trusted and untrusted calls
 */
@Slf4j
public class TrustedEndpointsFilter implements Filter {

    private final int trustedPortNum;
    private final String trustedPathPrefix;

    /* package */ TrustedEndpointsFilter(final String trustedPort, final String trustedPathPrefix) {
        if (trustedPort != null && trustedPathPrefix != null && !"null".equals(trustedPathPrefix)) {
            trustedPortNum = Integer.parseInt(trustedPort);
            this.trustedPathPrefix = trustedPathPrefix;
        } else {
            trustedPortNum = 0;
            this.trustedPathPrefix = "";
        }
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {
        if (trustedPortNum != 0) {

            if (isRequestForTrustedEndpoint(servletRequest) && servletRequest.getLocalPort() != trustedPortNum) {
                log.warn("denying request for trusted endpoint on untrusted port");
                ((ResponseFacade) servletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
                servletResponse.getOutputStream().close();
                return;
            }

            if (!isRequestForTrustedEndpoint(servletRequest) && servletRequest.getLocalPort() == trustedPortNum) {
                log.warn("denying request for untrusted endpoint on trusted port");
                ((ResponseFacade) servletResponse).setStatus(HttpServletResponse.SC_NOT_FOUND);
                servletResponse.getOutputStream().close();
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isRequestForTrustedEndpoint(final ServletRequest servletRequest) {
        return ((RequestFacade) servletRequest).getRequestURI().startsWith(trustedPathPrefix);
    }
}
