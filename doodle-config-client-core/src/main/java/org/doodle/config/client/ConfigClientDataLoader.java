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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import org.doodle.design.common.util.MapUtils;
import org.doodle.design.common.util.ProtoUtils;
import org.doodle.design.config.ConfigId;
import org.doodle.design.config.ConfigProps;
import org.doodle.design.config.ConfigPullOperation;
import org.springframework.boot.BootstrapContextClosedEvent;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.protobuf.ProtobufDecoder;
import org.springframework.http.codec.protobuf.ProtobufEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import reactor.core.publisher.Mono;

public class ConfigClientDataLoader implements ConfigDataLoader<ConfigClientDataResource> {

  @Override
  public ConfigData load(ConfigDataLoaderContext context, ConfigClientDataResource resource)
      throws IOException, ConfigDataResourceNotFoundException {
    ConfigClientDataReference reference = resource.getReference();
    ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
    ConfigPullApi pullApi =
        bootstrapContext.isRegistered(ConfigPullApi.class)
            ? bootstrapContext.get(ConfigPullApi.class)
            : createConfigPullApi(context, reference.getProperties());
    return pullApi
        .pull(reference.getConfigId())
        .onErrorMap(e -> new ConfigDataResourceNotFoundException(resource, e))
        .map(this::flattenMapPropertySource)
        .map(List::of)
        .map(ConfigData::new)
        .block();
  }

  private MapPropertySource flattenMapPropertySource(ConfigProps configProps) {
    ConfigId configId = configProps.getId();
    Map<String, Object> propsMap = ProtoUtils.fromProto(configProps.getProps());
    String propsName =
        new StringJoiner("@")
            .add(configId.getGroup())
            .add(configId.getDataId())
            .add(configId.getProfile())
            .toString();
    return new MapPropertySource(propsName, MapUtils.flatten(propsMap));
  }

  private ConfigPullApi createConfigPullApi(
      ConfigDataLoaderContext context, ConfigClientProperties properties) {
    ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
    ConfigPullApi pullApi = new ConfigPullApi(createRequester(properties));
    bootstrapContext.registerIfAbsent(ConfigPullApi.class, InstanceSupplier.of(pullApi));
    bootstrapContext.addCloseListener(pullApi);
    return pullApi;
  }

  private RSocketRequester createRequester(ConfigClientProperties properties) {
    URI serverUri = properties.getServer().getUri();
    RSocketStrategies strategies =
        RSocketStrategies.builder()
            .decoder(new ProtobufDecoder())
            .encoder(new ProtobufEncoder())
            .build();
    return RSocketRequester.builder()
        .dataMimeType(MediaType.APPLICATION_PROTOBUF)
        .rsocketStrategies(strategies)
        .tcp(serverUri.getHost(), serverUri.getPort());
  }

  @RequiredArgsConstructor
  static class ConfigPullApi
      implements ConfigPullOperation, ApplicationListener<BootstrapContextClosedEvent> {
    final RSocketRequester requester;

    @Override
    public Mono<ConfigProps> pull(ConfigId configId) {
      return this.requester.route("config.pull").data(configId).retrieveMono(ConfigProps.class);
    }

    @Override
    public void onApplicationEvent(BootstrapContextClosedEvent ignored) {
      if (!this.requester.isDisposed()) {
        this.requester.dispose();
      }
    }
  }
}
