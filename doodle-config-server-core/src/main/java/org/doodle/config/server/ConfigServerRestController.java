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

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.doodle.design.common.Result;
import org.doodle.design.config.ConfigPullOperation;
import org.doodle.design.config.model.payload.reply.ConfigPullReply;
import org.doodle.design.config.model.payload.request.ConfigPullRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigServerRestController implements ConfigPullOperation.RestPullOperation {
  private final ConfigServerService service;

  @Operation(summary = "获取配置")
  @PostMapping("/pull")
  @Override
  public Mono<Result<ConfigPullReply>> pull(@RequestBody ConfigPullRequest request) {
    return this.service.pull(request);
  }
}
