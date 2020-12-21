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
package org.joyqueue.broker.handler;

import com.alibaba.fastjson.JSON;
import com.google.common.primitives.Shorts;
import org.joyqueue.broker.BrokerContext;
import org.joyqueue.broker.cluster.ClusterManager;
import org.joyqueue.broker.election.ElectionService;
import org.joyqueue.domain.Broker;
import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.exception.JoyQueueCode;
import org.joyqueue.exception.JoyQueueException;
import org.joyqueue.network.command.BooleanAck;
import org.joyqueue.network.transport.Transport;
import org.joyqueue.network.transport.command.Command;
import org.joyqueue.network.transport.command.Type;
import org.joyqueue.network.transport.command.handler.CommandHandler;
import org.joyqueue.network.transport.exception.TransportException;
import org.joyqueue.nsr.config.NameServiceConfig;
import org.joyqueue.nsr.network.command.CreatePartitionGroup;
import org.joyqueue.nsr.network.command.NsrCommandType;
import org.joyqueue.store.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author wylixiaobin
 * Date: 2018/10/8
 */
@Deprecated
public class CreatePartitionGroupHandler implements CommandHandler, Type {
    private static Logger logger = LoggerFactory.getLogger(CreatePartitionGroupHandler.class);
    private ClusterManager clusterManager;
    private ElectionService electionService;
    private StoreService storeService;
    private NameServiceConfig config;

    public CreatePartitionGroupHandler(BrokerContext brokerContext) {
        this.clusterManager = brokerContext.getClusterManager();
        this.electionService = brokerContext.getElectionService();
        this.storeService = brokerContext.getStoreService();
        this.config = new NameServiceConfig(brokerContext.getPropertySupplier());
    }

    @Override
    public int type() {
        return NsrCommandType.NSR_CREATE_PARTITIONGROUP;
    }

    @Override
    public Command handle(Transport transport, Command command) throws TransportException {
        if (!config.getMessengerIgniteEnable()) {
            return BooleanAck.build();
        }
        if (command == null) {
            logger.error("CreatePartitionGroupHandler request command is null");
            return null;
        }
        CreatePartitionGroup request = ((CreatePartitionGroup)command.getPayload());
        try{
            PartitionGroup group = request.getPartitionGroup();
            if(logger.isDebugEnabled())logger.debug("begin createPartitionGroup topic[{}] partitionGroupRequest [{}] ",group.getTopic(),JSON.toJSONString(request));
            if (!request.isRollback()) {
                commit(group);
            } else {
                rollback(group);
            }
            return BooleanAck.build();
        }catch (JoyQueueException e) {
            logger.error(String.format("CreatePartitionGroupHandler request command[%s] error",request),e);
            return BooleanAck.build(e.getCode(),e.getMessage());
        } catch (Exception e) {
            logger.error(String.format("CreatePartitionGroupHandler request command[%s] error",request),e);
            return BooleanAck.build(JoyQueueCode.CN_UNKNOWN_ERROR,e.getMessage());
        }
    }

    private void commit(PartitionGroup group) throws Exception {
        if(logger.isDebugEnabled())logger.debug("topic[{}] add partitionGroup[{}]",group.getTopic(),group.getGroup());
        //if (!storeService.partitionGroupExists(group.getTopic(),group.getGroup())) {
            storeService.createPartitionGroup(group.getTopic().getFullName(), group.getGroup(), Shorts.toArray(group.getPartitions()));
            //}
            Set<Integer> replicas = group.getReplicas();
            List<Broker> list = new ArrayList<>(replicas.size());
            replicas.forEach(brokerId->{
                list.add(clusterManager.getBrokerById(brokerId));
            });
            electionService.onPartitionGroupCreate(group.getElectType(),group.getTopic(),group.getGroup(),list,group.getLearners(),clusterManager.getBrokerId(),group.getLeader());
    }
    private void rollback(PartitionGroup group){
        if(logger.isDebugEnabled())logger.debug("topic[{}] remove partitionGroup[{}]",group.getTopic(),group.getGroup());
            storeService.removePartitionGroup(group.getTopic().getFullName(),group.getGroup());
            electionService.onPartitionGroupRemove(group.getTopic(),group.getGroup());
    }
}
