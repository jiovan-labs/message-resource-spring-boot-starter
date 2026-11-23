/*
 * Copyright (c) 2021 Jiovan Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiovan.spring.starter.messageresource.util;


import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jakarta.annotation.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.jiovan.plexus.common.toolkit.constant.CharVariantConst;
import com.jiovan.plexus.common.toolkit.constant.StringVariantConst;
import com.jiovan.plexus.common.toolkit.util.ArrayUtilsWraps;
import com.jiovan.plexus.common.toolkit.util.CollectionPlainWraps;
import com.jiovan.plexus.common.toolkit.util.RegexUtilsWraps;
import com.jiovan.plexus.common.toolkit.util.StringUtilsWraps;
import com.jiovan.plexus.spring.toolkit.util.ClassUtilsWraps;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;


/**
 * Utilities for detecting {@link org.springframework.context.MessageSource} bundles
 *
 * @author David Hsing
 *
 * @see "https://stackoverflow.com/questions/44772900/message-localisation-in-spring-boot"
 * @see "org.springframework.security.core.SpringSecurityMessageSource"
 * @see "java.util.spi.AbstractResourceBundleProvider"
 */
@SuppressWarnings("unused")
public abstract class MessageResourceDetectorUtils {
    private static final String HIBERNATE_VALIDATOR = "org.hibernate.validator.HibernateValidator";    // $NON-NLS-1$
    private static final String HIBERNATE_MESSAGES = "org.hibernate.validator.ValidationMessages";    // $NON-NLS-1$
    private static final String SECURITY_VERSION = "org.springframework.security.core.SpringSecurityCoreVersion";    // $NON-NLS-1$
    private static final String SECURITY_MESSAGES = "org.springframework.security.messages";    // $NON-NLS-1$
    private static final String FILE_PATTERN_2 = "[_\\.]{1}[a-z]{2}\\." + StringVariantConst.PROPERTIES;    // $NON-NLS-1$
    private static final String FILE_PATTERN_4 = "[_\\.]{1}[a-z]{2}[_\\-]{1}[A-Z]{2}\\." + StringVariantConst.PROPERTIES;    // $NON-NLS-1$
    private static final String FILE_PATTERN_6 = "[_\\.]{1}[a-z]{2}[_\\-]{1}[A-Z]{4}\\." + StringVariantConst.PROPERTIES;    // $NON-NLS-1$
    private static final String FILE_PATTERN_7 = "[_\\.]{1}[a-z]{3}[_\\-]{1}[A-Z]{4}\\." + StringVariantConst.PROPERTIES;    // $NON-NLS-1$
    private static final String FILE_PATTERN_8 = "[_\\.]{1}[a-z]{2}[_\\-]{1}[A-Z]{2}[_\\-]{1}[A-Z]{4}\\." + StringVariantConst.PROPERTIES;    // $NON-NLS-1$

    /**
     * Returns the detected message bundle base names for the given resource paths
     *
     * @param addInternal whether to include the Hibernate Validator and Spring Security message bundles when present
     * @param scanRecursive whether to scan the given resource paths recursively
     * @param scanClasspath whether to scan the classpath, required by ClassGraph 5
     * @param scanResources the resource paths to scan for properties files, may be {@code null}
     *
     * @return the detected base names, or {@code null} if none are found
     */
    @Nullable
    public static Set<String> detectBaseNames(boolean addInternal, boolean scanRecursive, boolean scanClasspath, @Nullable String... scanResources) {
        return detectBaseNames(addInternal, scanRecursive, scanClasspath, ArrayUtilsWraps.asList(scanResources));
    }

    /**
     * Returns the detected message bundle base names, optionally including internal bundles and scanning the given resource paths
     *
     * @param addInternal whether to include the Hibernate Validator and Spring Security message bundles when present
     * @param scanClasspath whether to scan the classpath, required by ClassGraph 5
     * @param scanRecursive whether to scan the given resource paths recursively
     * @param scanResources the resource paths to scan for properties files, may be {@code null}
     *
     * @return the detected base names, or {@code null} if none are found
     */
    @Nullable
    public static Set<String> detectBaseNames(boolean addInternal, boolean scanClasspath, boolean scanRecursive, @Nullable List<String> scanResources) {
        if (!addInternal && CollectionUtils.isEmpty(scanResources)) {
            return null;
        }
        Set<String> result = new LinkedHashSet<>();
        if (addInternal && ClassUtilsWraps.isPresent(HIBERNATE_VALIDATOR)) {
            result.add(HIBERNATE_MESSAGES);
        }
        if (addInternal && ClassUtilsWraps.isPresent(SECURITY_VERSION)) {
            result.add(SECURITY_MESSAGES);
        }
        if (!CollectionUtils.isEmpty(scanResources)) {
            String[] paths = StringUtils.toStringArray(scanResources);
            ClassGraph graph = (scanRecursive ? new ClassGraph().acceptPaths(paths) : new ClassGraph().acceptPathsNonRecursive(paths));
            if (scanClasspath) {
                graph.enableClasspath();
            }
            try (ScanResult scanResult = graph.scan()) {
                List<String> founds = scanResult.getResourcesWithExtension(StringVariantConst.PROPERTIES).getPaths();
                if (!CollectionUtils.isEmpty(founds)) {
                    for (String found : founds) {
                        found = RegexUtilsWraps.removeAll(found, FILE_PATTERN_8, FILE_PATTERN_7, FILE_PATTERN_6, FILE_PATTERN_4, FILE_PATTERN_2);
                        found = StringUtilsWraps.substringBeforeLast(found, CharVariantConst.DOT);
                        CollectionPlainWraps.addIfNotBlank(result, found);
                    }
                }
            }
        }
        return CollectionUtils.isEmpty(result) ? null : result;
    }
}
