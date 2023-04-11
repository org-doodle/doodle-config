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
import org.doodle.config.server.ConfigServerConfigRepository;
import org.doodle.config.server.ConfigServerConfigService;
import org.doodle.config.server.ConfigServerProperties;
import org.doodle.config.server.ConfigServerPropertiesRepository;
import org.doodle.design.config.ConfigService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@AutoConfiguration(before = MongoDataAutoConfiguration.class)
@ConditionalOnClass(ConfigServerProperties.class)
@EnableConfigurationProperties(ConfigServerProperties.class)
@EnableMongoRepositories(
    basePackageClasses = {
      ConfigServerConfigRepository.class,
      ConfigServerPropertiesRepository.class
    })
public class ConfigServerAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ConfigService configService(ConfigServerConfigRepository configRepository) {
    return new ConfigServerConfigService(configRepository);
  }

  @Bean
  @ConditionalOnMissingBean
  public ConfigServerConfigController configServerConfigController(ConfigService configService) {
    return new ConfigServerConfigController(configService);
  }

  @Bean
  @ConditionalOnMissingBean(MongoConverter.class)
  MappingMongoConverter mappingMongoConverter(
      MongoDatabaseFactory factory,
      MongoMappingContext context,
      MongoCustomConversions conversions) {
    DbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
    MappingMongoConverter mappingConverter = new MappingMongoConverter(dbRefResolver, context);
    mappingConverter.setCustomConversions(conversions);
    // 配置 map key 支持使用 "."
    mappingConverter.setMapKeyDotReplacement(".");
    return mappingConverter;
  }
}
