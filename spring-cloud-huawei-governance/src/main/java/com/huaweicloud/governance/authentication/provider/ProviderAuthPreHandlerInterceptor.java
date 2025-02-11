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

package com.huaweicloud.governance.authentication.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.service.center.client.ServiceCenterClient;

import com.huaweicloud.common.adapters.webmvc.PreHandlerInterceptor;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.authentication.Const;
import com.huaweicloud.governance.authentication.UnAuthorizedException;

public class ProviderAuthPreHandlerInterceptor implements PreHandlerInterceptor {

  private final RSAProviderTokenManager authenticationTokenManager;

  public ProviderAuthPreHandlerInterceptor(ServiceCenterClient client, BlackWhiteListProperties blackWhiteListProperties) {
    authenticationTokenManager = new RSAProviderTokenManager(client, blackWhiteListProperties);
  }

  @Override
  public boolean handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String token = InvocationContextHolder
        .getOrCreateInvocationContext()
        .getContext(Const.AUTH_TOKEN);
    if (null == token || !authenticationTokenManager.valid(token)) {
      throw new UnAuthorizedException("UNAUTHORIZED.");
    }
    return true;
  }
}
