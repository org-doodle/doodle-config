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

import org.doodle.config.server.ConfigServerConfigController;
import org.doodle.config.server.ConfigServerConfigService;
import org.doodle.config.server.ConfigServerInstanceEntityRepository;
import org.doodle.design.config.ConfigService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ConfigServerRSocketConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ConfigService configService(ConfigServerInstanceEntityRepository configRepository) {
    return new ConfigServerConfigService(configRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigServerConfigController configServerConfigController(ConfigService configService) {
    return new ConfigServerConfigController(configService);
  }
}
