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

import org.doodle.config.server.ConfigServerInstanceRepository;
import org.doodle.config.server.ConfigServerSharedRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@AutoConfiguration
@EnableMongoAuditing
@EnableMongoRepositories(
    basePackageClasses = {ConfigServerInstanceRepository.class, ConfigServerSharedRepository.class})
public class ConfigServerMongodbConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ValidatingMongoEventListener validatingMongoEventListener(
      LocalValidatorFactoryBean factory) {
    return new ValidatingMongoEventListener(factory);
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
