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

package com.huaweicloud.governance.adapters.feign;

import com.huaweicloud.common.adapters.feign.OrderedRequestInterceptor;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.authentication.consumer.RSAConsumerTokenManager;

public class FeignAddTokenContext implements OrderedRequestInterceptor {

  private final RSAConsumerTokenManager authenticationTokenManager;

  public FeignAddTokenContext(RSAConsumerTokenManager authenticationTokenManager) {
    this.authenticationTokenManager = authenticationTokenManager;
  }

  @Override
  public void apply(feign.RequestTemplate requestTemplate) {
    authenticationTokenManager.setToken(InvocationContextHolder.getOrCreateInvocationContext());
  }
}
