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
package org.joyqueue.client.internal.transport.config;

import com.google.common.base.Preconditions;

/**
 * TransportConfigChecker
 *
 * author: gaohaoxiang
 * date: 2019/2/19
 */
public class TransportConfigChecker {

    public static void check(TransportConfig config) {
        Preconditions.checkArgument(config != null, "transport not null");
        Preconditions.checkArgument(config.getConnections() >= 1, "transport.connections must be greater than 0");
    }
}