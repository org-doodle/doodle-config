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
package org.doodle.config.autoconfigure.condition;

import java.util.Map;
import org.doodle.config.server.ConfigServerDataType;
import org.doodle.config.server.ConfigServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

final class OnConfigServerDataTypeCondition extends SpringBootCondition {

  @Override
  public ConditionOutcome getMatchOutcome(
      ConditionContext context, AnnotatedTypeMetadata metadata) {
    Map<String, Object> attributes =
        metadata.getAnnotationAttributes(ConditionalOnConfigServerDataType.class.getName());
    ConfigServerDataType dataType = (ConfigServerDataType) attributes.get("value");
    return getMatchOutcome(context.getEnvironment(), dataType);
  }

  private ConditionOutcome getMatchOutcome(Environment environment, ConfigServerDataType dataType) {
    String name = dataType.name();
    ConditionMessage.Builder message =
        ConditionMessage.forCondition(ConditionalOnConfigServerDataType.class);

    BindResult<ConfigServerProperties> specified =
        Binder.get(environment).bind(ConfigServerProperties.PREFIX, ConfigServerProperties.class);
    ConfigServerProperties properties = specified.orElseGet(ConfigServerProperties::new);
    if (properties.getDataType() == dataType) {
      return ConditionOutcome.match();
    }
    return ConditionOutcome.noMatch(message.didNotFind(name).atAll());
  }
}
