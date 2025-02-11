/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huaweicloud.governance.adapters.webmvc;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.governance.handler.IdentifierRateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.CheckedConsumer;

public class IdentifierRateLimitingFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierRateLimitingFilter.class);

  private static final Object EMPTY_HOLDER = new Object();

  private final IdentifierRateLimitingHandler identifierRateLimitingHandler;

  public IdentifierRateLimitingFilter(IdentifierRateLimitingHandler identifierRateLimitingHandler) {
    this.identifierRateLimitingHandler = identifierRateLimitingHandler;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    GovernanceRequestExtractor governanceRequest = WebMvcUtils.convert((HttpServletRequest) request);
    try {
      RateLimiter rateLimiter = identifierRateLimitingHandler.getActuator(governanceRequest);
      if (rateLimiter != null) {
        CheckedConsumer<Object> next = (v) -> chain.doFilter(request, response);
        DecorateConsumer<Object> decorateConsumer = Decorators.ofConsumer(next.unchecked());
        decorateConsumer.withRateLimiter(rateLimiter);
        decorateConsumer.accept(EMPTY_HOLDER);
        return;
      }
      chain.doFilter(request, response);
    } catch (Throwable e) {
      if (e instanceof RequestNotPermitted) {
        ((HttpServletResponse) response).setStatus(429);
        response.getWriter().print("rate limited.");
        LOGGER.warn("the request is rate limit by policy : {}",
            e.getMessage());
      } else {
        throw e;
      }
    }
  }
}
