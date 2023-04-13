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
import lombok.AllArgsConstructor;
import org.doodle.design.config.ConfigInstanceDTO;
import org.doodle.design.config.ConfigService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@AllArgsConstructor
@MessageMapping("config")
@Controller
public class ConfigServerConfigController {
  private final ConfigService configService;

  @MessageMapping("{dataId}.{group}.{configId}")
  public Flux<ConfigInstanceDTO> getConfig(
      @DestinationVariable String dataId,
      @DestinationVariable String group,
      @DestinationVariable String configId)
      throws IOException {
    return configService.getConfig(dataId, group, configId);
  }
}
