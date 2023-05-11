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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.*;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;

public class ConfigClientDataLocationResolver
    implements ConfigDataLocationResolver<ConfigClientDataResource> {

  @Override
  public boolean isResolvable(
      ConfigDataLocationResolverContext context, ConfigDataLocation location) {
    // TODO: 2023/5/11  add implementation
    return false;
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
    // TODO: 2023/5/11 add implementation
    return resources;
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
