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

import org.doodle.config.autoconfigure.condition.ConditionalOnConfigServerDataType;
import org.doodle.config.server.ConfigServerDataType;
import org.doodle.config.server.ConfigServerInstanceEntityRepository;
import org.doodle.config.server.ConfigServerSharedEntityRepository;
import org.doodle.design.config.ConfigInstanceEntity;
import org.doodle.design.config.ConfigSharedEntity;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableJpaAuditing
@EntityScan(basePackageClasses = {ConfigInstanceEntity.class, ConfigSharedEntity.class})
@EnableJpaRepositories(
    basePackageClasses = {
      ConfigServerInstanceEntityRepository.class,
      ConfigServerSharedEntityRepository.class
    })
@ConditionalOnConfigServerDataType(ConfigServerDataType.JPA)
public class ConfigServerJpaConfiguration {}
