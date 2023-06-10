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
package org.doodle.config.autoconfigure.server;

import io.swagger.v3.oas.models.info.Info;
import org.doodle.broker.autoconfigure.client.BrokerClientAutoConfiguration;
import org.doodle.broker.client.BrokerClientRSocketRequester;
import org.doodle.config.server.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@AutoConfiguration(after = BrokerClientAutoConfiguration.class)
@ConditionalOnClass(ConfigServerProperties.class)
@EnableConfigurationProperties(ConfigServerProperties.class)
public class ConfigServerAutoConfiguration {

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(GroupedOpenApi.class)
  @ConditionalOnWebApplication
  public static class SpringDocConfiguration {
    @Bean
    public GroupedOpenApi configGroupedOpenApi() {
      return GroupedOpenApi.builder()
          .group("config-apis")
          .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("Config API")))
          .packagesToScan("org.doodle.config.server")
          .build();
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
  @EnableMongoAuditing
  @EnableMongoRepositories(basePackageClasses = ConfigServerInstanceRepository.class)
  public static class ServletConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ConfigServerServletController configServerRestController(
        ConfigServerInstanceRepository instanceRepository) {
      return new ConfigServerServletController(instanceRepository);
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(BrokerClientRSocketRequester.class)
  @ConditionalOnBean(BrokerClientRSocketRequester.class)
  public static class RSocketConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ConfigServerRSocketController configServerController(
        ConfigServerInstanceRepository instanceRepository, ConfigServerMapper mapper) {
      return new ConfigServerRSocketController(instanceRepository, mapper);
    }
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigServerMapper configServerMapper() {
    return new ConfigServerMapper();
  }
}
