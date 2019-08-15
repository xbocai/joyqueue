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
package io.chubao.joyqueue.broker.protocol.network;

import io.chubao.joyqueue.broker.protocol.exception.JoyQueueException;
import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.network.command.BooleanAck;
import io.chubao.joyqueue.network.transport.Transport;
import io.chubao.joyqueue.network.transport.command.Command;
import io.chubao.joyqueue.network.transport.command.handler.ExceptionHandler;
import io.chubao.joyqueue.network.transport.exception.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * joyqueue异常处理
 *
 * author: gaohaoxiang
 * date: 2018/8/28
 */
public class JoyQueueExceptionHandler implements ExceptionHandler {

    protected static final Logger logger = LoggerFactory.getLogger(JoyQueueExceptionHandler.class);

    @Override
    public void handle(Transport transport, Command command, Throwable throwable) {
        logger.error("process command exception, header: {}, payload: {}, transport: {}",
                command.getHeader(), command.getPayload(), transport, throwable);

        if (command.getHeader().getQosLevel().equals(QosLevel.ONE_WAY)) {
            return;
        }

        try {
            int code = JoyQueueCode.CN_UNKNOWN_ERROR.getCode();
            String error = null;

            if (throwable instanceof TransportException) {
                TransportException transportException = (TransportException) throwable;
                code = transportException.getCode();
                error = transportException.getMessage();
            } else if (throwable instanceof JoyQueueException) {
                JoyQueueException joyQueueException = (JoyQueueException) throwable;
                code = joyQueueException.getCode();
                error = joyQueueException.getMessage();
            } else if (throwable instanceof JoyQueueException) {
                JoyQueueException joyQueueException = (JoyQueueException) throwable;
                code = joyQueueException.getCode();
                error = joyQueueException.getMessage();
            }

            transport.acknowledge(command, BooleanAck.build(code, error));
        } catch (Exception e) {
            logger.error("acknowledge command exception, header: {}, payload: {}, transport: {}",
                    command.getHeader(), command.getPayload(), transport, e);
        }
    }
}