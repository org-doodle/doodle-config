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
import java.util.*;
import org.doodle.boot.core.LowestOrdered;
import org.doodle.design.common.util.MapUtils;
import org.doodle.design.config.ConfigEntity;
import org.doodle.design.config.ConfigService;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class ConfigClientDataLoader
    implements ConfigDataLoader<ConfigClientDataResource>, LowestOrdered {

  @Override
  public ConfigData load(ConfigDataLoaderContext context, ConfigClientDataResource resource)
      throws IOException, ConfigDataResourceNotFoundException {
    ConfigClientDataReference reference = resource.getReference();
    ConfigService configService = findConfigService(context, reference.getProperties());
    Map<String, ConfigEntity> entityMap =
        configService
            .getConfig(reference.getDataId(), reference.getGroup(), reference.getConfigId())
            .filter(
                entity ->
                    StringUtils.hasLength(entity.getDataId())
                        && StringUtils.hasLength(entity.getGroup())
                        && StringUtils.hasLength(entity.getConfigId())
                        && !CollectionUtils.isEmpty(entity.getConfigs()))
            .collectMap(
                entity -> entity.getDataId() + "@" + entity.getGroup() + "-" + entity.getConfigId())
            .block();
    if (CollectionUtils.isEmpty(entityMap)) {
      return new ConfigData(Collections.emptyList(), getOptions());
    }
    List<MapPropertySource> propertySources = new ArrayList<>();
    for (Map.Entry<String, ConfigEntity> entry : entityMap.entrySet()) {
      Map<String, Object> flattened = MapUtils.flatten(entry.getValue().getConfigs());
      MapPropertySource mps = new MapPropertySource(entry.getKey(), flattened);
      propertySources.add(mps);
    }
    return new ConfigData(propertySources, getOptions());
  }

  private ConfigData.Option[] getOptions() {
    List<ConfigData.Option> options = new ArrayList<>();
    options.add(ConfigData.Option.IGNORE_IMPORTS);
    options.add(ConfigData.Option.IGNORE_PROFILES);
    return options.toArray(new ConfigData.Option[0]);
  }

  private ConfigService findConfigService(
      ConfigDataLoaderContext context, ConfigClientProperties properties) {
    ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
    return bootstrapContext.isRegistered(ConfigService.class)
        ? bootstrapContext.get(ConfigService.class)
        : createConfigService(context, properties);
  }

  private ConfigService createConfigService(
      ConfigDataLoaderContext context, ConfigClientProperties properties) {
    ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
    ConfigClientRSocketService configService =
        new ConfigClientRSocketService(createRSocketRequester(properties));
    bootstrapContext.registerIfAbsent(ConfigService.class, InstanceSupplier.of(configService));
    return configService;
  }

  private RSocketRequester createRSocketRequester(ConfigClientProperties properties) {
    URI uri = properties.getUri();
    RSocketStrategies strategies =
        RSocketStrategies.builder().decoder(new Jackson2JsonDecoder()).build();
    return RSocketRequester.builder()
        .rsocketStrategies(strategies)
        .tcp(uri.getHost(), uri.getPort());
  }
}
