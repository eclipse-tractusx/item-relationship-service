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
package org.eclipse.tractusx.irs.edc.client.util;

import java.net.URI;
import java.net.URISyntaxException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A utility class for joining paths and appending paths to URLs.
 * <p>This class provides methods for joining paths together and appending paths to URLs. The methods ensure that
 * the resulting paths and URLs are correctly formatted and handle any necessary encoding or special characters.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UriPathJoiner {

    /**
     * Appends a given path to an existing URL.
     * <p>This method takes a base URL and a path to append, and returns a new URL that is the result of appending the path
     * to the base URL. The method ensures that the resulting URL is correctly formatted by joining the paths together and
     * handling any necessary encoding or special characters.
     *
     * @param url          The base URL to which the path will be appended.
     * @param pathToAppend The path to append to the base URL.
     * @return A new URL representing the base URL with the appended path.
     * @throws URISyntaxException If the base URL or the appended path is not a valid URL.
     */
    public static String appendPath(final String url, final String pathToAppend) throws URISyntaxException {
        if (url == null || url.isEmpty()) {
            throw new URISyntaxException(String.valueOf(url), "Base URL cannot be null or empty");
        }

        final URI uri = new URI(url);
        final String pathWithAppendix = joinPaths(uri.getPath(), pathToAppend);
        final URI uriWithAppendix = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                pathWithAppendix, uri.getQuery(), uri.getFragment());

        return uriWithAppendix.toString();
    }

    /**
     * Joins two paths together, ensuring that the resulting path is correctly formatted.
     * <p>This method takes two strings representing paths and returns a new string that is the result of joining
     * them together. The paths are joined by appending the second path to the first, ensuring that there is exactly
     * one forward slash ("/") between them. If either of the input paths is null or empty, the method returns the
     * non-null, non-empty path.
     *
     * @param firstPath  The first path to join.
     * @param secondPath The second path to join.
     * @return A new string representing the joined paths.
     */
    public static String joinPaths(final String firstPath, final String secondPath) {
        final String joinedPath;
        if (firstPath == null || firstPath.isEmpty()) {
            joinedPath = secondPath != null ? secondPath : "";
        } else if (secondPath == null || secondPath.isEmpty()) {
            joinedPath = firstPath;
        } else {

            final String trimmedFirstPath = firstPath.endsWith("/")
                    ? firstPath.substring(0, firstPath.length() - 1)
                    : firstPath;
            final String trimmedSecondPath = secondPath.startsWith("/") ? secondPath.substring(1) : secondPath;

            joinedPath = trimmedFirstPath + "/" + trimmedSecondPath;
        }
        return joinedPath.startsWith("/") ? joinedPath : "/" + joinedPath;
    }
}
