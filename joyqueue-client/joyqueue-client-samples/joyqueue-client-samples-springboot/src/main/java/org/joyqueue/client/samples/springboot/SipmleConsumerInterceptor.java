/**
 * Copyright 2019 The JoyQueue Authors.
 *
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
package org.joyqueue.client.samples.springboot;

import io.openmessaging.interceptor.ConsumerInterceptor;
import io.openmessaging.interceptor.Context;
import io.openmessaging.message.Message;
import io.openmessaging.spring.boot.annotation.OMSInterceptor;

/**
 * SimpleConsumerInterceptor
 *
 * author: gaohaoxiang
 * date: 2019/3/8
 */
@OMSInterceptor
public class SipmleConsumerInterceptor implements ConsumerInterceptor {

    @Override
    public void preReceive(Message message, Context attributes) {
        System.out.println(String.format("preReceive, message: %s", message));
    }

    @Override
    public void postReceive(Message message, Context attributes) {
        System.out.println(String.format("postReceive, message: %s", message));
    }
}