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

package com.huaweicloud.servicecomb.discovery.context;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

import reactor.core.publisher.Mono;

public class GatewayAddServiceNameContext implements GlobalFilter, Ordered {
  private final ServiceCombRegistration registration;

  public GatewayAddServiceNameContext(ServiceCombRegistration registration) {
    this.registration = registration;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
    context.putContext(InvocationContext.CONTEXT_MICROSERVICE_NAME, registration.getServiceId());
    context.putContext(InvocationContext.CONTEXT_INSTANCE_ID, registration.getInstanceId());
    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    // this filter executed after ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER
    return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 20;
  }
}
