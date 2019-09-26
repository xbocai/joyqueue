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
package io.chubao.joyqueue.broker.network.support;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.index.handler.ConsumeIndexQueryHandler;
import io.chubao.joyqueue.broker.index.handler.ConsumeIndexStoreHandler;
import io.chubao.joyqueue.broker.producer.transaction.handler.TransactionCommitRequestHandler;
import io.chubao.joyqueue.broker.producer.transaction.handler.TransactionRollbackRequestHandler;
import io.chubao.joyqueue.network.command.CommandType;
import io.chubao.joyqueue.network.transport.command.support.DefaultCommandHandlerFactory;
import io.chubao.joyqueue.server.retry.remote.handler.RemoteRetryMessageHandler;

/**
 * BrokerCommandHandlerRegistrar
 *
 * author: gaohaoxiang
 * date: 2018/9/17
 */
// 用BrokerCommandHandler作为处理接口，通过spi方式加载
@Deprecated
public class BrokerCommandHandlerRegistrar {

    public static void register(BrokerContext brokerContext, DefaultCommandHandlerFactory commandHandlerFactory) {
        //  retry
        RemoteRetryMessageHandler remoteRetryMessageHandler = new RemoteRetryMessageHandler(brokerContext.getRetryManager());
        commandHandlerFactory.register(CommandType.PUT_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.GET_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.UPDATE_RETRY, remoteRetryMessageHandler);
        commandHandlerFactory.register(CommandType.GET_RETRY_COUNT, remoteRetryMessageHandler);

        // consume position related command
        commandHandlerFactory.register(CommandType.CONSUME_INDEX_QUERY_REQUEST, new ConsumeIndexQueryHandler(brokerContext));
        commandHandlerFactory.register(CommandType.CONSUME_INDEX_STORE_REQUEST, new ConsumeIndexStoreHandler(brokerContext));

        // transaction
        commandHandlerFactory.register(CommandType.TRANSACTION_COMMIT_REQUEST, new TransactionCommitRequestHandler(brokerContext));
        commandHandlerFactory.register(CommandType.TRANSACTION_ROLLBACK_REQUEST, new TransactionRollbackRequestHandler(brokerContext));
    }
}
