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

import org.doodle.broker.autoconfigure.client.BrokerClientAutoConfiguration;
import org.doodle.broker.client.BrokerClientRSocketRequester;
import org.doodle.config.server.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@AutoConfiguration(after = BrokerClientAutoConfiguration.class)
@ConditionalOnClass(ConfigServerProperties.class)
@ConditionalOnBean(BrokerClientRSocketRequester.class)
@EnableConfigurationProperties(ConfigServerProperties.class)
@EnableReactiveMongoRepositories(basePackageClasses = ConfigServerInstanceRepo.class)
public class ConfigServerAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ConfigServerMapper configServerMapper() {
    return new ConfigServerMapper();
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigServerService configServerService(
      ConfigServerMapper mapper, ConfigServerInstanceRepo instanceRepo) {
    return new ConfigServerService(mapper, instanceRepo);
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigServerController configServerController(ConfigServerService service) {
    return new ConfigServerController(service);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
  public ConfigServerRestController configServerRestController(ConfigServerService service) {
    return new ConfigServerRestController(service);
  }
}
