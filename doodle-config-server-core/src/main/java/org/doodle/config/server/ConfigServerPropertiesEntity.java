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
package org.doodle.config.server;

import org.doodle.design.config.ConfigEntity;
import org.springframework.data.mongodb.core.mapping.Document;

/** 服务 SpringBoot properties 配置，通常作为公共配置被服务配置关联引用 */
@Document(collection = ConfigServerPropertiesEntity.COLLECTION)
public class ConfigServerPropertiesEntity extends ConfigEntity {
  public static final String COLLECTION = "server-properties";
}
