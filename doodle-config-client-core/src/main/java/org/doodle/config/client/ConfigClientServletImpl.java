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

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.doodle.design.common.Result;
import org.doodle.design.config.model.payload.reply.ConfigPullReply;
import org.doodle.design.config.model.payload.request.ConfigPullRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class ConfigClientServletImpl implements ConfigClientServlet {
  private final RestTemplate restTemplate;

  public static final ParameterizedTypeReference<Result<ConfigPullReply>> PULL_REPLY_TYPE =
      new ParameterizedTypeReference<>() {};

  @Override
  public Result<ConfigPullReply> pull(ConfigPullRequest request) {
    return this.restTemplate
        .exchange(
            "/config/pull",
            HttpMethod.POST,
            new HttpEntity<>(request, createHttpHeaders()),
            PULL_REPLY_TYPE)
        .getBody();
  }

  private HttpHeaders createHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    return HttpHeaders.readOnlyHttpHeaders(headers);
  }
}
