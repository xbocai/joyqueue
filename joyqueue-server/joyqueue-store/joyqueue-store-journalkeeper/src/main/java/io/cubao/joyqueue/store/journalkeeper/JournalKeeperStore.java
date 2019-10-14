package io.cubao.joyqueue.store.journalkeeper;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.monitor.BufferPoolMonitorInfo;
import io.chubao.joyqueue.store.PartitionGroupStore;
import io.chubao.joyqueue.store.StoreManagementService;
import io.chubao.joyqueue.store.StoreService;
import io.chubao.joyqueue.store.transaction.TransactionStore;
import io.chubao.joyqueue.toolkit.config.Property;
import io.chubao.joyqueue.toolkit.config.PropertySupplier;
import io.chubao.joyqueue.toolkit.config.PropertySupplierAware;
import io.journalkeeper.core.api.RaftServer;
import io.journalkeeper.rpc.URIParser;
import io.journalkeeper.utils.spi.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author LiYue
 * Date: 2019-09-19
 */
public class JournalKeeperStore implements StoreService, PropertySupplierAware, BrokerContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JournalKeeperStore.class);
    private static final String TOPICS_PATH = "topics";
    public static final String STORE_PATH = "store";
    private Map<TopicPartitionGroup, JournalKeeperPartitionGroupStore> storeMap = new ConcurrentHashMap<>();
    private File base;
    private PropertySupplier propertySupplier;
    private BrokerContext brokerContext;
    private final JoyQueueUriParser joyQueueUriParser =
            (JoyQueueUriParser )ServiceSupport.load(URIParser.class, JoyQueueUriParser.class.getCanonicalName());

    @Override
    public boolean partitionGroupExists(String topic, int partitionGroup) {
        return storeMap.containsKey(new TopicPartitionGroup(topic, partitionGroup));
    }

    @Override
    public boolean topicExists(String topic) {
        if(topic == null ) {
            throw new IllegalArgumentException("Topic can not be null!");
        }
        return storeMap.keySet().stream()
                .anyMatch(topicPartitionGroup -> topic.equals(topicPartitionGroup.getTopic()));
    }

    @Override
    public TransactionStore getTransactionStore(String topic) {
        // TODO
        return null;
    }

    @Override
    public List<TransactionStore> getAllTransactionStores() {
        // TODO
        return null;
    }

    @Override
    public void removePartitionGroup(String topic, int partitionGroup) {
        JournalKeeperPartitionGroupStore store = storeMap.remove(new TopicPartitionGroup(topic, partitionGroup));
        if(null != store) {
            store.stop();
            store.delete();
        } else {
            logger.warn("Remove partition group failed, partition group not exist! Topic: {}, partitionGroup: {}.",
                    topic, partitionGroup);
        }
    }

    @Override
    public void restorePartitionGroup(String topic, int partitionGroup) {
        storeMap.computeIfAbsent(new TopicPartitionGroup(topic, partitionGroup), tg -> {
            try {
                File groupBase = new File(base, getPartitionGroupRelPath(tg.getTopic(), tg.getPartitionGroup()));
                Properties properties = new Properties();
                properties.setProperty("working_dir", groupBase.getAbsolutePath());
                //TODO: StoreConfig -> properties
                JournalKeeperPartitionGroupStore store =
                        new JournalKeeperPartitionGroupStore(
                                topic,
                                partitionGroup,
                                RaftServer.Roll.VOTER,
                                new LeaderReportEventWatcher(topic, partitionGroup, brokerContext.getClusterManager()),
                                properties);
                store.restore();
                store.start();
                return store;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public void createPartitionGroup(String topic, int partitionGroup, short[] partitions, List<Integer> brokerIds, int thisBrokerId) {
        storeMap.computeIfAbsent(new TopicPartitionGroup(topic, partitionGroup), tg -> {
            try {
                File groupBase = new File(base, getPartitionGroupRelPath(tg.getTopic(), tg.getPartitionGroup()));
                Properties properties = new Properties();
                properties.setProperty("working_dir", groupBase.getAbsolutePath());
                //TODO: StoreConfig -> properties
                JournalKeeperPartitionGroupStore store =
                        new JournalKeeperPartitionGroupStore(
                                topic,
                                partitionGroup,
                                RaftServer.Roll.VOTER,
                                new LeaderReportEventWatcher(topic, partitionGroup, brokerContext.getClusterManager()),
                                properties);
                store.init(toURIs(brokerIds, topic, partitionGroup), toURI(thisBrokerId, topic, partitionGroup));
                store.restore();
                store.start();
                return store;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public PartitionGroupStore getStore(String topic, int partitionGroup) {
        return storeMap.get(new TopicPartitionGroup(topic, partitionGroup));
    }

    @Override
    public List<PartitionGroupStore> getStore(String topic) {
        if(null == topic) {
            throw new IllegalArgumentException("Topic can not be null!");
        }
        return storeMap
                .entrySet().stream()
                .filter(entry-> topic.equals(entry.getKey().getTopic()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public void maybeRePartition(String topic, int partitionGroup, Collection<Short> partitions) {
        JournalKeeperPartitionGroupStore store = storeMap.get(new TopicPartitionGroup(topic, partitionGroup));
        if(null != store) {
            store.rePartition(partitions);
        } else {
            logger.warn("Repartition failed, partition group not exist! Topic: {}, partitionGroup: {}, partitions: {}.",
                    topic, partitionGroup, partitions);
        }
    }

    @Override
    public void maybeUpdateConfig(String topic, int partitionGroup, Collection<Integer> newBrokerIds) {
        JournalKeeperPartitionGroupStore store = storeMap.get(new TopicPartitionGroup(topic, partitionGroup));
        if(null != store) {
            store.maybeUpdateConfig(toURIs(new ArrayList<>(newBrokerIds), topic, partitionGroup));
        } else {
            logger.warn("Update config failed, partition group not exist! Topic: {}, partitionGroup: {}.",
                    topic, partitionGroup);
        }
    }

    @Override
    public StoreManagementService getManageService() {
        // TODO
        return null;
    }

    @Override
    public BufferPoolMonitorInfo monitorInfo() {
        // TODO
        return null;
    }

    private String getPartitionGroupRelPath(String topic, int partitionGroup) {
        return TOPICS_PATH + File.separator + topic.replace('/', '@') + File.separator + partitionGroup;
    }

    private List<URI> toURIs(List<Integer> brokerIds, String topic, int group) {
        return brokerIds.stream()
                .map(brokerId -> toURI(brokerId, topic, group))
                .collect(Collectors.toList());
    }

    private URI toURI(int brokerId, String topic, int group) {
        return joyQueueUriParser.create(topic, group, brokerId);
    }
//    private URI toURI(int brokerId, String topic, int group) {
//        Broker broker = brokerContext.getClusterManager().getBrokerById(brokerId);
//
//        return URI.create("jk://" + broker.getIp() + ":" + broker.getPort());
//    }
    private void checkOrCreateBase() throws IOException{
        if (!base.exists()) {
            if (!base.mkdirs()) {
                throw new IOException(String.format("Failed to create base directory: %s!", base.getAbsolutePath()));
            }
        } else {
            if (!base.isDirectory()) {
                throw new IOException(String.format("File %s is not a directory!", base.getAbsolutePath()));
            }
        }
    }

    @Override
    public void setSupplier(PropertySupplier supplier) {
        try {
            this.propertySupplier = supplier;
            Property property = propertySupplier.getProperty(Property.APPLICATION_DATA_PATH);
            base = new File(property.getString() + File.separator + STORE_PATH);
            checkOrCreateBase();
        }catch (Exception e) {
            logger.warn("Exception: ", e);
        }
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        joyQueueUriParser.setBrokerContext(brokerContext);
    }

    private static class TopicPartitionGroup {
        private final String topic;
        private final int partitionGroup;
        TopicPartitionGroup(String topic, int partitionGroup) {
            this.topic = topic;
            this.partitionGroup = partitionGroup;
        }
        public String getTopic() {
            return topic;
        }

        public int getPartitionGroup() {
            return partitionGroup;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TopicPartitionGroup that = (TopicPartitionGroup) o;
            return partitionGroup == that.partitionGroup &&
                    topic.equals(that.topic);
        }

        @Override
        public int hashCode() {
            return Objects.hash(topic, partitionGroup);
        }
    }
}
