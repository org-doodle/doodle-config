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

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.doodle.design.common.Result;
import org.doodle.design.common.util.ProtoUtils;
import org.doodle.design.config.*;
import org.springframework.data.domain.Example;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class ConfigServerRSocketController implements ConfigPullOps.RSocket {
  private final ConfigServerInstanceRepository instanceRepository;
  private final ConfigServerMapper mapper;

  @MessageMapping("config.pull")
  @Override
  public Mono<ConfigPullReply> pull(ConfigPullRequest request) {
    ConfigIdInfo configId = request.getConfigId();
    ConfigServerInstanceEntity sample =
        ConfigServerInstanceEntity.builder()
            .group(configId.getGroup())
            .dataId(configId.getDataId())
            .profile(configId.getProfile())
            .build();
    return Mono.fromCallable(() -> instanceRepository.findOne(Example.of(sample)))
        .flatMap(
            optional -> {
              try {
                return Mono.just(optional.orElseThrow());
              } catch (NoSuchElementException e) {
                return Mono.error(e);
              }
            })
        .map(
            instanceEntity ->
                ConfigPullReply.newBuilder()
                    .setConfigProps(mapper.toProto(instanceEntity))
                    .build());
  }

  @MessageExceptionHandler(NoSuchElementException.class)
  public Mono<ConfigPullReply> handleException() {
    return Mono.just(mapper.toError(ProtoUtils.toProto(Result.bad())));
  }
}
