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

import java.net.URI;
import java.util.*;
import lombok.SneakyThrows;
import org.doodle.design.common.util.URIUtils;
import org.doodle.design.config.ConfigConstants;
import org.doodle.design.config.ConfigId;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.*;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.util.StringUtils;

public class ConfigClientDataLocationResolver
    implements ConfigDataLocationResolver<ConfigClientDataResource> {

  @Override
  public boolean isResolvable(
      ConfigDataLocationResolverContext context, ConfigDataLocation location) {
    return location.hasPrefix(ConfigConstants.CONFIG_PREFIX)
        && context
            .getBinder()
            .bind(ConfigClientProperties.PREFIX + ".enabled", Boolean.class)
            .orElse(Boolean.FALSE);
  }

  @Override
  public List<ConfigClientDataResource> resolve(
      ConfigDataLocationResolverContext context, ConfigDataLocation location)
      throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
    return Collections.emptyList();
  }

  @Override
  public List<ConfigClientDataResource> resolveProfileSpecific(
      ConfigDataLocationResolverContext context, ConfigDataLocation location, Profiles profiles)
      throws ConfigDataLocationNotFoundException {
    ConfigClientProperties properties = bindProperties(context);
    ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
    bootstrapContext.registerIfAbsent(
        ConfigClientProperties.class, InstanceSupplier.of(properties));
    return loadConfigDataResource(location, profiles, properties);
  }

  private List<ConfigClientDataResource> loadConfigDataResource(
      ConfigDataLocation location, Profiles profiles, ConfigClientProperties properties) {
    URI uri = getUri(location);
    if (Objects.isNull(uri)) {
      throw new IllegalArgumentException("配置参数错误!");
    }
    Map<String, String> queryMap = URIUtils.getQueryMap(uri);
    String group = queryMap.getOrDefault(ConfigConstants.CONFIG_GROUP, properties.getGroup());
    if (!StringUtils.hasLength(group)) {
      throw new IllegalArgumentException("group 不能为空!");
    }
    String dataId = queryMap.getOrDefault(ConfigConstants.CONFIG_DATA_ID, properties.getDataId());
    if (!StringUtils.hasLength(dataId)) {
      throw new IllegalArgumentException("dataId 不能为空!");
    }
    List<ConfigClientDataResource> resources = new ArrayList<>();
    for (String profile : profiles.getAccepted()) {
      ConfigClientDataReference reference =
          new ConfigClientDataReference(
              properties,
              location.isOptional(),
              ConfigId.newBuilder().setGroup(group).setDataId(dataId).setProfile(profile).build());
      resources.add(new ConfigClientDataResource(reference));
    }
    return resources;
  }

  @SneakyThrows
  private URI getUri(ConfigDataLocation location) {
    String path = location.getNonPrefixedValue(ConfigConstants.CONFIG_PREFIX);
    if (!StringUtils.hasLength(path)) {
      return null;
    }

    if (!path.startsWith("/")) {
      path = "/" + path;
    }

    return new URI(path);
  }

  private ConfigClientProperties bindProperties(ConfigDataLocationResolverContext context) {
    Binder binder = context.getBinder();
    BindHandler bindHandler = context.getBootstrapContext().getOrElse(BindHandler.class, null);
    ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
    return bootstrapContext.isRegistered(ConfigClientProperties.class)
        ? bootstrapContext.get(ConfigClientProperties.class)
        : binder
            .bind(
                ConfigClientProperties.PREFIX,
                Bindable.of(ConfigClientProperties.class),
                bindHandler)
            .map(
                properties ->
                    binder
                        .bind(
                            ConfigClientProperties.PREFIX,
                            Bindable.ofInstance(properties),
                            bindHandler)
                        .orElse(properties))
            .orElseGet(
                () ->
                    binder
                        .bind(
                            ConfigClientProperties.PREFIX,
                            Bindable.of(ConfigClientProperties.class),
                            bindHandler)
                        .orElseGet(ConfigClientProperties::new));
  }
}
