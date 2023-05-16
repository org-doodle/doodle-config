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
import org.doodle.design.config.ConfigId;
import org.doodle.design.config.ConfigProps;
import reactor.core.publisher.Flux;

@Getter
@RequiredArgsConstructor
public class BrokerConfigApi implements ConfigApi {
  private final BrokerClientRSocketRequester requester;
  private final ConfigClientProperties properties;

  @Override
  public Flux<ConfigProps> pull(ConfigId configId) {
    return this.requester.route("config.pull").data(configId).retrieveFlux(ConfigProps.class);
  }
}
