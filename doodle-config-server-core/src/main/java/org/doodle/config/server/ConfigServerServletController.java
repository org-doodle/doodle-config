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

import io.swagger.v3.oas.annotations.Operation;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.doodle.design.common.Result;
import org.doodle.design.config.ConfigPullOps;
import org.doodle.design.config.model.dto.ConfigIdInfoDto;
import org.doodle.design.config.model.dto.ConfigPropsInfoDto;
import org.doodle.design.config.model.payload.reply.ConfigPullReply;
import org.doodle.design.config.model.payload.request.ConfigPullRequest;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
public class ConfigServerServletController implements ConfigPullOps.Servlet {
  private final ConfigServerInstanceRepository instanceRepository;

  @Operation(summary = "获取配置")
  @PostMapping("/config/pull")
  @Override
  public Result<ConfigPullReply> pull(@RequestBody ConfigPullRequest request) {
    ConfigIdInfoDto configId = request.getConfigId();
    ConfigServerInstanceEntity sample =
        ConfigServerInstanceEntity.builder()
            .group(configId.getGroup())
            .dataId(configId.getDataId())
            .profile(configId.getProfile())
            .build();
    ConfigServerInstanceEntity instanceEntity =
        instanceRepository.findOne(Example.of(sample)).orElseThrow();
    ConfigPropsInfoDto configProps =
        ConfigPropsInfoDto.builder()
            .configId(configId)
            .props(instanceEntity.getConfigProps())
            .build();
    ConfigPullReply reply = ConfigPullReply.builder().configProps(configProps).build();
    return Result.ok(reply);
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Result<ConfigPullReply> handleException() {
    return Result.bad();
  }
}
