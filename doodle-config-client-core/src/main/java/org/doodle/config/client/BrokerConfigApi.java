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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.doodle.broker.client.BrokerClientRSocketRequester;
import org.doodle.design.broker.frame.BrokerFrame;
import org.doodle.design.broker.frame.BrokerFrameMimeTypes;
import org.doodle.design.broker.frame.BrokerFrameUtils;
import org.doodle.design.config.ConfigId;
import org.doodle.design.config.ConfigProps;
import org.springframework.lang.NonNull;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

@Getter
@RequiredArgsConstructor
public class BrokerConfigApi implements ConfigApi {
  private final BrokerClientRSocketRequester requester;
  private final ConfigClientProperties properties;
  private final BrokerFrame routingFrame;

  public BrokerConfigApi(
      BrokerClientRSocketRequester requester, ConfigClientProperties properties) {
    this.requester = requester;
    this.properties = properties;
    this.routingFrame = BrokerFrameUtils.unicast(properties.getServer().getTags());
  }

  @Override
  public Mono<ConfigProps> pull(@NonNull ConfigId configId) {
    return route("config.pull").data(configId).retrieveMono(ConfigProps.class);
  }

  protected RSocketRequester.RequestSpec route(String route) {
    return this.requester
        .route(route)
        .metadata(this.routingFrame, BrokerFrameMimeTypes.BROKER_FRAME_MIME_TYPE);
  }
}
