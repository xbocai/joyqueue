package io.chubao.joyqueue.broker.store;

import com.google.common.primitives.Shorts;
import io.chubao.joyqueue.broker.cluster.ClusterManager;
import io.chubao.joyqueue.broker.config.BrokerStoreConfig;
import io.chubao.joyqueue.domain.Broker;
import io.chubao.joyqueue.domain.PartitionGroup;
import io.chubao.joyqueue.domain.Replica;
import io.chubao.joyqueue.domain.TopicName;
import io.chubao.joyqueue.event.MetaEvent;
import io.chubao.joyqueue.nsr.NameService;
import io.chubao.joyqueue.nsr.event.AddPartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.AddTopicEvent;
import io.chubao.joyqueue.nsr.event.RemovePartitionGroupEvent;
import io.chubao.joyqueue.nsr.event.RemoveTopicEvent;
import io.chubao.joyqueue.nsr.event.UpdatePartitionGroupEvent;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.service.Service;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * StoreInitializer
 * author: gaohaoxiang
 * date: 2019/8/28
 */
// TODO 如果启动后元数据和选举不一致，以元数据为准
public class StoreInitializer extends Service implements EventListener<MetaEvent> {

    protected static final Logger logger = LoggerFactory.getLogger(StoreInitializer.class);

    private BrokerStoreConfig config;
    private NameService nameService;
    private ClusterManager clusterManager;
    private StoreService storeService;

    public StoreInitializer(BrokerStoreConfig config, NameService nameService, ClusterManager clusterManager, StoreService storeService) {
        this.config = config;
        this.nameService = nameService;
        this.clusterManager = clusterManager;
        this.storeService = storeService;
    }

    @Override
    protected void doStart() throws Exception {
        restore();
        clusterManager.addListener(this);
    }

    @Override
    protected void doStop() {
    }

    protected void restore() {
        Broker broker = clusterManager.getBroker();
        List<Replica> replicas = nameService.getReplicaByBroker(broker.getId());
        if (CollectionUtils.isEmpty(replicas)) {
            return;
        }

        for (Replica replica : replicas) {
            PartitionGroup group = clusterManager.getPartitionGroupByGroup(replica.getTopic(),replica.getGroup());
            if (group == null) {
                logger.warn("group is null topic {},replica {}", replica.getTopic(), replica.getGroup());
                throw new RuntimeException(String.format("group is null topic %s,replica %s", replica.getTopic(), replica.getGroup()));
            }
            if (!group.getReplicas().contains(broker.getId())) {
                continue;
            }
            doRestore(group, replica, broker);
        }
    }

    protected void doRestore(PartitionGroup group, Replica replica, Broker broker) {
        logger.info("begin restore topic {},group.no {} group {}",replica.getTopic().getFullName(),replica.getGroup(),group);

        try {
            storeService.restorePartitionGroup(group.getTopic().getFullName(), group.getGroup());
        } catch (Throwable t) {
            logger.warn("Restore partition group failed! Topic: {}, group: {}.",
                replica.getTopic().getFullName(), group, t);
            if(config.getForceRestore()) {
                storeService.createPartitionGroup(group.getTopic().getFullName(), group.getGroup(), Shorts.toArray(group.getPartitions()), new ArrayList<>(group.getBrokers().keySet()), broker.getId());
            }
        }
    }

    @Override
    public void onEvent(MetaEvent event) {
        try {
            switch (event.getEventType()) {
                case ADD_TOPIC: {
                    AddTopicEvent addTopicEvent = (AddTopicEvent) event;
                    for (PartitionGroup partitionGroup : addTopicEvent.getPartitionGroups()) {
                        onAddPartitionGroup(addTopicEvent.getTopic().getName(), partitionGroup);
                    }
                    break;
                }
                case REMOVE_TOPIC: {
                    RemoveTopicEvent removeTopicEvent = (RemoveTopicEvent) event;
                    for (PartitionGroup partitionGroup : removeTopicEvent.getPartitionGroups()) {
                        onRemovePartitionGroup(removeTopicEvent.getTopic().getName(), partitionGroup);
                    }
                    break;
                }
                case ADD_PARTITION_GROUP: {
                    AddPartitionGroupEvent addPartitionGroupEvent = (AddPartitionGroupEvent) event;
                    onAddPartitionGroup(addPartitionGroupEvent.getTopic(), addPartitionGroupEvent.getPartitionGroup());
                    break;
                }
                case UPDATE_PARTITION_GROUP: {
                    UpdatePartitionGroupEvent updatePartitionGroupEvent = (UpdatePartitionGroupEvent) event;
                    onUpdatePartitionGroup(updatePartitionGroupEvent.getTopic(), updatePartitionGroupEvent.getOldPartitionGroup(), updatePartitionGroupEvent.getNewPartitionGroup());
                    break;
                }
                case REMOVE_PARTITION_GROUP: {
                    RemovePartitionGroupEvent removePartitionGroupEvent = (RemovePartitionGroupEvent) event;
                    onRemovePartitionGroup(removePartitionGroupEvent.getTopic(), removePartitionGroupEvent.getPartitionGroup());
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("onEvent exception, event: {}", event, e);
            throw new RuntimeException(e);
        }
    }

    protected void onAddPartitionGroup(TopicName topicName, PartitionGroup partitionGroup) throws Exception {
        logger.info("onAddPartitionGroup, topic: {}, partitionGroup: {}", topicName, partitionGroup);
        Set<Integer> replicas = partitionGroup.getReplicas();
        storeService.createPartitionGroup(topicName.getFullName(), partitionGroup.getGroup(), Shorts.toArray(partitionGroup.getPartitions()), new ArrayList<>(replicas), clusterManager.getBrokerId());
    }

    protected void onUpdatePartitionGroup(TopicName topicName, PartitionGroup oldPartitionGroup, PartitionGroup newPartitionGroup) throws Exception {
        int currentBrokerId = clusterManager.getBrokerId();
        if(oldPartitionGroup.getReplicas().contains(currentBrokerId)) {
            // 先处理配置变更
            if(!oldPartitionGroup.getReplicas().equals(newPartitionGroup.getReplicas())) {
                storeService.maybeUpdateConfig(topicName.getFullName(), newPartitionGroup.getGroup(), newPartitionGroup.getReplicas());
            }
            // 再处理分区变更
            if(!oldPartitionGroup.getPartitions().equals(newPartitionGroup.getPartitions())) {
                storeService.maybeRePartition(topicName.getFullName(), oldPartitionGroup.getGroup(), newPartitionGroup.getPartitions());
            }
        }
    }

    protected void onRemovePartitionGroup(TopicName topicName, PartitionGroup partitionGroup) throws Exception {
        logger.info("onRemovePartitionGroup, topic: {}, partitionGroup: {}", topicName, partitionGroup);
        storeService.removePartitionGroup(topicName.getFullName(), partitionGroup.getGroup());
    }
}
