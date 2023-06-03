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

import lombok.RequiredArgsConstructor;
import org.doodle.design.common.Result;
import org.doodle.design.common.util.ProtoUtils;
import org.doodle.design.config.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ConfigServerService implements ConfigOperation, ConfigPullOperation.RestPullOperation {
  private final ConfigServerMapper mapper;
  private final ConfigServerInstanceRepo instanceRepo;

  @Override
  public Mono<Result<org.doodle.design.config.model.payload.reply.ConfigPullReply>> pull(
      org.doodle.design.config.model.payload.request.ConfigPullRequest request) {
    return Mono.just(request).map(mapper::toProto).flatMap(this::pull).map(mapper::fromProto);
  }

  @Override
  public Mono<ConfigPullReply> pull(ConfigPullRequest request) {
    return Mono.just(request)
        .map(ConfigPullRequest::getConfigId)
        .flatMap(instanceRepo::findByConfigId)
        .map(mapper::toProto)
        .map(mapper::toReply)
        .onErrorReturn(mapper.toError(ProtoUtils.toProto(Result.bad())));
  }
}
