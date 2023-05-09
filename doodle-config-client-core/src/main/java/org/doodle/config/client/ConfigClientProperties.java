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
package org.doodle.config.client;

import java.net.URI;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = ConfigClientProperties.PREFIX)
public class ConfigClientProperties {
  public static final String PREFIX = "doodle.config.client";

  /** 配置服务端 rsocket 地址(暂不支持集群模式) */
  private URI uri = URI.create("tcp://localhost:9891");

  /** 默认配置分组 */
  private String group;

  /** 默认配置实例ID */
  private String configId;
}
