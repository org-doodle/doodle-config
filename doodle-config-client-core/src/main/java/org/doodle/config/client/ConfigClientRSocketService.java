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

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.doodle.design.config.ConfigInstanceDTO;
import org.doodle.design.config.ConfigService;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

@AllArgsConstructor
public class ConfigClientRSocketService implements ConfigService {
  private final RSocketRequester requester;

  @SneakyThrows
  @Override
  public Flux<ConfigInstanceDTO> getConfig(String dataId, String group, String configId) {
    return requester
        .route("config.{dataId}.{group}.{configId}", dataId, group, configId)
        .retrieveFlux(ConfigInstanceDTO.class);
  }
}
