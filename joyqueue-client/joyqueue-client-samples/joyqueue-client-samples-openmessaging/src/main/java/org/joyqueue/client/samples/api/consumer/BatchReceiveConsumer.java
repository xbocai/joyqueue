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
package org.joyqueue.client.samples.api.consumer;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.OMS;
import io.openmessaging.OMSBuiltinKeys;
import io.openmessaging.consumer.Consumer;
import io.openmessaging.message.Message;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * BatchReceiveConsumer
 *
 * author: gaohaoxiang
 * date: 2019/2/20
 */
public class BatchReceiveConsumer {

    public static void main(String[] args) throws Exception {
        KeyValue keyValue = OMS.newKeyValue();
        keyValue.put(OMSBuiltinKeys.ACCOUNT_KEY, "test_token");

        MessagingAccessPoint messagingAccessPoint = OMS.getMessagingAccessPoint("oms:joyqueue://test_app@127.0.0.1:50088/UNKNOWN", keyValue);

        Consumer consumer = messagingAccessPoint.createConsumer();
        consumer.start();

        // 绑定主题，将要消费的主题
        consumer.bindQueue("test_topic_0");

        // 这里只是简单例子，根据具体情况进行调度处理
        // 参数是超时时间，只是网络请求的超时
        while (true) {
            System.out.println("doReceive");

            // 批量拉取消息
            List<Message> messages = consumer.batchReceive(1000 * 10);

            if (CollectionUtils.isEmpty(messages)) {
                continue;
            }

            for (Message message : messages) {
                // 拉取到消息，打印日志并ack
                System.out.println(String.format("receive, message: %s", messages));
                consumer.ack(message.getMessageReceipt());
            }

            Thread.currentThread().sleep(1000 * 1);
        }
    }
}
