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
package org.joyqueue.server.retry.remote.command;

import org.joyqueue.network.command.CommandType;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import org.joyqueue.server.retry.model.RetryMessageModel;

import java.util.List;

/**
 * 重试应答
 * <p>
 * Created by chengzhiliang on 2019/2/14.
 */
public class GetRetryAck extends JoyQueuePayload {

    // 存储的消息
    protected List<RetryMessageModel> messages;

    @Override
    public int type() {
        return CommandType.GET_RETRY_ACK;
    }

    public GetRetryAck() {
    }

    public List<RetryMessageModel> getMessages() {
        return messages;
    }

    public void setMessages(List<RetryMessageModel> messages) {
        this.messages = messages;
    }

}
