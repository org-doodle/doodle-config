/*
 * Copyright (c) 2022-present Doodle. All rights reserved.
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
package org.doodle.config.autoconfigure.client;

import org.doodle.broker.client.BrokerClientRSocketRequester;
import org.doodle.config.client.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration(after = RSocketRequesterAutoConfiguration.class)
@EnableConfigurationProperties(ConfigClientProperties.class)
@ConditionalOnProperty(prefix = ConfigClientProperties.PREFIX, name = "enabled")
public class ConfigClientAutoConfiguration {

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
  public static class ServletConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ConfigClientServlet configClientServlet(RestTemplateBuilder builder) {
      return new ConfigClientServletImpl(builder.build());
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(BrokerClientRSocketRequester.class)
  @ConditionalOnBean(BrokerClientRSocketRequester.class)
  public static class RSocketConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ConfigClientRSocket configClientRSocket(
        BrokerClientRSocketRequester requester, ConfigClientProperties properties) {
      return new ConfigClientRSocketImpl(requester, properties);
    }
  }
}
