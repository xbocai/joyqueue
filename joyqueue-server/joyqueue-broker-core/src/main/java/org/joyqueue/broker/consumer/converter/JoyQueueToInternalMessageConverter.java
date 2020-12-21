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
package org.joyqueue.broker.consumer.converter;

import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.Message;
import org.joyqueue.message.SourceType;
import org.joyqueue.network.serializer.BatchMessageSerializer;

import java.util.List;

/**
 * JoyQueueToInternalMessageConverter
 *
 * author: gaohaoxiang
 * date: 2019/4/24
 */
public class JoyQueueToInternalMessageConverter extends AbstractInternalMessageConverter {

    @Override
    public BrokerMessage convert(BrokerMessage message) {
        if (!message.isCompressed() || Message.CompressionType.ZLIB.equals(message.getCompressionType())) {
            return message;
        }
        message.setBody(message.getDecompressedBody());
        message.setCompressed(false);
        return message;
    }

    @Override
    public List<BrokerMessage> convertBatch(BrokerMessage message) {
        message.setBody(message.getDecompressedBody());
        return BatchMessageSerializer.deserialize(message);
    }

    @Override
    public Byte type() {
        return SourceType.JOYQUEUE.getValue();
    }
}