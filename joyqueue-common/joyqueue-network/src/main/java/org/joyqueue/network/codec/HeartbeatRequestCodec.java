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
package org.joyqueue.network.codec;

import org.joyqueue.network.command.HeartbeatRequest;
import org.joyqueue.network.command.JoyQueueCommandType;
import org.joyqueue.network.transport.codec.JoyQueueHeader;
import org.joyqueue.network.transport.codec.PayloadCodec;
import org.joyqueue.network.transport.command.Type;
import io.netty.buffer.ByteBuf;

/**
 * HeartbeatRequestCodec
 *
 * author: gaohaoxiang
 * date: 2018/12/28
 */
public class HeartbeatRequestCodec implements PayloadCodec<JoyQueueHeader, HeartbeatRequest>, Type {

    @Override
    public HeartbeatRequest decode(JoyQueueHeader header, ByteBuf buffer) throws Exception {
        return new HeartbeatRequest();
    }

    @Override
    public void encode(HeartbeatRequest payload, ByteBuf buffer) throws Exception {

    }

    @Override
    public int type() {
        return JoyQueueCommandType.HEARTBEAT_REQUEST.getCode();
    }
}