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
import java.net.URISyntaxException;
import java.util.*;
import org.doodle.design.common.util.URIUtils;
import org.doodle.design.config.ConfigConstants;
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
    if (!location.hasPrefix(ConfigConstants.PREFIX)) {
      return false;
    }

    return context
        .getBinder()
        .bind(ConfigClientProperties.PREFIX + ".enabled", Boolean.class)
        .orElse(false);
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
    List<ConfigClientDataResource> resources = new ArrayList<>();
    URI uri = getUri(location);

    if (Objects.isNull(uri) || !StringUtils.hasLength(dataIdFor(uri))) {
      throw new IllegalArgumentException("必须设置 dataId");
    }

    ConfigClientDataReference reference =
        new ConfigClientDataReference(
            properties,
            location.isOptional(),
            profiles,
            dataIdFor(uri),
            groupFor(uri, properties),
            configIdFor(uri, properties));
    ConfigClientDataResource resource = new ConfigClientDataResource(reference);
    resources.add(resource);
    return resources;
  }

  private String dataIdFor(URI uri) {
    String path = uri.getPath();
    if (Objects.isNull(path) || path.length() <= 1) {
      return "";
    }
    String[] parts = path.substring(1).split("/");
    if (parts.length != 1) {
      throw new IllegalArgumentException("非法参数 dataId");
    }

    return parts[0];
  }

  private String configIdFor(URI uri, ConfigClientProperties properties) {
    return URIUtils.getQueryMap(uri).getOrDefault(ConfigConstants.CONFIG_ID, properties.getGroup());
  }

  private String groupFor(URI uri, ConfigClientProperties properties) {
    return URIUtils.getQueryMap(uri).getOrDefault(ConfigConstants.GROUP, properties.getConfigId());
  }

  private URI getUri(ConfigDataLocation location) {
    String path = location.getNonPrefixedValue(ConfigConstants.PREFIX);
    if (!StringUtils.hasLength(path)) {
      return null;
    }

    if (!path.startsWith("/")) {
      path = "/" + path;
    }

    URI uri;
    try {
      uri = new URI(path);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("非法参数: " + path, e);
    }
    return uri;
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
