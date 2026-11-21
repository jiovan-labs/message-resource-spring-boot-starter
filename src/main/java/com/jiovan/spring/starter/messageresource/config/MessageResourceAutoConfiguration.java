/*
 * Copyright (c) 2016 Jiovan Ltd. All rights reserved.
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

package com.jiovan.spring.starter.messageresource.config;


import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import com.jiovan.plexus.common.toolkit.constant.CharVariantConst;
import com.jiovan.plexus.common.toolkit.util.CollectionPlainWraps;
import com.jiovan.plexus.common.toolkit.util.DurationUtilsWraps;
import com.jiovan.plexus.common.toolkit.util.StringUtilsWraps;
import com.jiovan.plexus.spring.toolkit.constant.SpringPropertyConst;
import com.jiovan.plexus.spring.toolkit.processor.MessageSourceAccessorProcessor;
import com.jiovan.plexus.spring.toolkit.util.ResourceUtilsWraps;
import com.jiovan.spring.starter.messageresource.processor.MessageResourcePriorityProcessor;
import com.jiovan.spring.starter.messageresource.property.MessageResourceProperties;
import com.jiovan.spring.starter.messageresource.util.MessageResourceDetectorUtils;


/**
 * Configuration for message resource
 *
 * @author David Hsing
 *
 * @see org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = MessageResourceAutoConfiguration.PROPERTIES_PREFIX, name = "enabled", matchIfMissing = true)
@AutoConfigureBefore(value = MessageSourceAutoConfiguration.class)
@AutoConfigureOrder(value = Ordered.HIGHEST_PRECEDENCE)
@EnableConfigurationProperties(value = MessageResourceProperties.class)
public class MessageResourceAutoConfiguration {
    public static final String PROPERTIES_PREFIX = "jiovan.message-resource";    // $NON-NLS-1$
    public static final String MESSAGE_SOURCE = "detectiveResourceMessageSource";    // $NON-NLS-1$

    /**
     * Returns the {@link org.springframework.context.MessageSource} assembled from configured and detected resource bundle base names
     *
     * @param environment the environment providing the configured {@code spring.messages.basename} property
     * @param properties the message resource properties governing bundle detection, encoding, caching and locale fallback
     *
     * @return a configured {@link org.springframework.context.support.AbstractResourceBasedMessageSource}, or {@code null} if no base names are available
     */
    @Bean(name = MESSAGE_SOURCE)
    @ConditionalOnMissingBean(name = MESSAGE_SOURCE)
    public MessageSource messageSource(@Nonnull Environment environment, @Nonnull MessageResourceProperties properties) {
        Set<String> existsNames = null;
        Set<String> configNames = StringUtilsWraps.splitByToSet(environment.getProperty(SpringPropertyConst.MESSAGES_BASENAME), CharVariantConst.COMMA, true);
        if (!CollectionUtils.isEmpty(configNames)) {
            existsNames = configNames.stream().filter(StringUtils::isNotBlank).map(element -> element.replace(CharVariantConst.DOT, CharVariantConst.SLASH)).filter(ResourceUtilsWraps::existsClassPathResource).collect(Collectors.toSet());
        }
        MessageResourceProperties.MessageBundle props = properties.getMessageBundle();
        Set<String> bundleNames = MessageResourceDetectorUtils.detectBaseNames(BooleanUtils.isTrue(props.getAddInternalBundles()), BooleanUtils.isTrue(props.getScanRecursive()), props.getScanResources());
        Set<String> unionNames = CollectionPlainWraps.newLinkedHashSetWithinAll(existsNames, bundleNames);
        if (CollectionUtils.isEmpty(unionNames)) {
            return null;
        }
        AbstractResourceBasedMessageSource result = BooleanUtils.isTrue(props.getReloadable()) ?  new ReloadableResourceBundleMessageSource() : new ResourceBundleMessageSource();
        result.setBasenames(unionNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        Optional.ofNullable(props.getDefaultEncoding()).ifPresent(element -> result.setDefaultEncoding(element.name()));
        Optional.ofNullable(properties.getDefaultLocale()).ifPresent(result::setDefaultLocale);
        DurationUtilsWraps.ifPositive(props.getCacheDuration(), element -> result.setCacheMillis(element.toMillis()));
        result.setFallbackToSystemLocale(BooleanUtils.isTrue(props.getFallbackToSystemLocale()));
        result.setAlwaysUseMessageFormat(BooleanUtils.isTrue(props.getAlwaysUseMessageFormat()));
        result.setUseCodeAsDefaultMessage(BooleanUtils.isTrue(props.getUseCodeAsDefaultMessage()));
        return result;
    }

    /**
     * Returns the {@link com.jiovan.spring.starter.messageresource.processor.MessageResourcePriorityProcessor} that raises this message source's priority
     *
     * @param properties the message resource properties providing the processor order
     *
     * @return a {@link com.jiovan.spring.starter.messageresource.processor.MessageResourcePriorityProcessor} with the configured order
     */
    @Bean
    @ConditionalOnBooleanProperty(prefix = PROPERTIES_PREFIX + ".message-resource", name = "primary-message-resource", matchIfMissing = true)
    @ConditionalOnMissingBean
    public MessageResourcePriorityProcessor messageSourcePriorityProcessor(@Nonnull MessageResourceProperties properties) {
        MessageResourcePriorityProcessor result = new MessageResourcePriorityProcessor();
        Optional.ofNullable(properties.getMessageBundle().getProcessorOrder()).ifPresent(result::setOrder);
        return result;
    }

    /**
     * Returns the {@link com.jiovan.plexus.spring.toolkit.processor.MessageSourceAccessorProcessor} that registers message source accessors
     *
     * @param context the application context used to register the accessors
     * @param properties the message resource properties providing the default locale and processor order
     *
     * @return a {@link com.jiovan.plexus.spring.toolkit.processor.MessageSourceAccessorProcessor} with the configured order
     */
    @Bean
    @ConditionalOnBooleanProperty(prefix = PROPERTIES_PREFIX + ".accessor-aware", name = "enabled", matchIfMissing = true)
    @ConditionalOnMissingBean
    public MessageSourceAccessorProcessor messageSourceAccessorProcessor(@Nonnull ConfigurableApplicationContext context, @Nonnull MessageResourceProperties properties) {
        MessageSourceAccessorProcessor result = new MessageSourceAccessorProcessor(context, properties.getDefaultLocale());
        Optional.ofNullable(properties.getAccessorAware().getProcessorOrder()).ifPresent(result::setOrder);
        return result;
    }
}
