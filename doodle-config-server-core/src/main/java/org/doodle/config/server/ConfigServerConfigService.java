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

import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.doodle.design.config.ConfigEntity;
import org.doodle.design.config.ConfigService;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

@AllArgsConstructor
public class ConfigServerConfigService implements ConfigService {

  private final ConfigServerConfigRepository configRepository;

  @Override
  public Flux<ConfigEntity> getConfig(String dataId, String group, String configId)
      throws IOException {
    List<ConfigServerConfigEntity> entities = configRepository.findConfig(dataId, group, configId);
    if (CollectionUtils.isEmpty(entities)) {
      return Flux.empty();
    }

    if (entities.size() > 1) {
      throw new Error("");
    }

    return Flux.fromIterable(entities);
  }
}
