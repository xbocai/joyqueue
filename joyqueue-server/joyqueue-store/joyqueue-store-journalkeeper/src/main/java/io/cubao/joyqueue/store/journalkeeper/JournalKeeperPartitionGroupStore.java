package io.cubao.joyqueue.store.journalkeeper;

import io.chubao.joyqueue.domain.QosLevel;
import io.chubao.joyqueue.exception.JoyQueueCode;
import io.chubao.joyqueue.store.PartitionGroupStore;
import io.chubao.joyqueue.store.ReadResult;
import io.chubao.joyqueue.store.WriteRequest;
import io.chubao.joyqueue.store.WriteResult;
import io.chubao.joyqueue.toolkit.concurrent.EventListener;
import io.chubao.joyqueue.toolkit.service.Service;
import io.journalkeeper.core.api.AdminClient;
import io.journalkeeper.core.api.RaftEntry;
import io.journalkeeper.core.api.RaftServer;
import io.journalkeeper.core.api.ResponseConfig;
import io.journalkeeper.exceptions.NotLeaderException;
import io.journalkeeper.journalstore.JournalStoreClient;
import io.journalkeeper.journalstore.JournalStoreServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author LiYue
 * Date: 2019-09-19
 */
public class JournalKeeperPartitionGroupStore extends Service implements PartitionGroupStore {
    private static final Logger logger = LoggerFactory.getLogger(JournalKeeperPartitionGroupStore.class);
    private final JournalStoreServer server;
    private final String topic;
    private final int group;
    private JournalStoreClient client;
    private AdminClient adminClient;
    JournalKeeperPartitionGroupStore(String topic, int group, RaftServer.Roll roll, Properties properties){
        this.topic = topic;
        this.group = group;
        server = new JournalStoreServer(roll, properties);

    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        server.start();
        this.client = server.createClient();
        this.adminClient = server.getAdminClient();
    }

    @Override
    protected void doStop() {
        super.doStop();
        server.stop();
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public int getPartitionGroup() {
        return group;
    }

    @Override
    public Short[] listPartitions() {
        try {
            int [] partitions = client.listPartitions().get();
            return toShortArray(partitions);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Short[] toShortArray(int[] partitions) {
        Short [] shortArray = new Short[partitions.length];
        for (int i = 0; i < partitions.length; i++) {
            shortArray[i] = (short) partitions[i];
        }
        return shortArray;
    }

    @Override
    public long getTotalPhysicalStorageSize() {
        // TODO
        return 0;
    }

    @Override
    public long deleteMinStoreMessages(long targetDeleteTimeline, Map<Short, Long> partitionAckMap, boolean doNotDeleteConsumed) throws IOException {
        return 0;
    }

    @Override
    public long getLeftIndex(short partition) {
        try {
            Map<Integer, Long> map = client.minIndices().get();
            return map.getOrDefault((int) partition, -1L);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getRightIndex(short partition) {
        try {
            Map<Integer, Long> map = client.maxIndices().get();
            return map.getOrDefault((int) partition, -1L);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getIndex(short partition, long timestamp) {
        // TODO 二分查找
        return -1L;
    }

    @Override
    public Future<WriteResult> asyncWrite(QosLevel qosLevel, WriteRequest... writeRequests) {
        CompletableFuture<WriteResult> completableFuture = new CompletableFuture<>();
        asyncWrite(completableFuture::complete, qosLevel, writeRequests);
        return completableFuture;
    }

    @Override
    public void asyncWrite(EventListener<WriteResult> eventListener, QosLevel qosLevel, WriteRequest... writeRequests) {
        // TODO: 改成只支持单条写入
        List<CompletableFuture<Long>> futures = Arrays.stream(writeRequests)
                .map(writeRequest -> client.append(
                        writeRequest.getPartition(),
                        writeRequest.getBatchSize(),
                        writeRequest.getBuffer().array(),
                        qosLevelToResponseConfig(qosLevel))
                ).collect(Collectors.toList());
        WriteResult writeResult = new WriteResult();

        long [] indices = new long[futures.size()];
        try {
            for (int i = 0; i < futures.size(); i++) {
                indices[i] = futures.get(i).get();
            }
            writeResult.setCode(JoyQueueCode.SUCCESS);
            writeResult.setIndices(indices);
        } catch (ExecutionException | InterruptedException e) {
            writeResult.setCode(JoyQueueCode.SE_READ_FAILED);
        }

        eventListener.onEvent(writeResult);
    }

    @Override
    public ReadResult read(short partition, long index, int count, long maxSize) {
        try {
            return client.get(partition, index, count).thenApply(raftEntries -> {
                ReadResult readResult = new ReadResult();
                readResult.setMessages(raftEntries.stream().map(RaftEntry::getEntry).map(ByteBuffer::wrap).toArray(ByteBuffer[]::new));
                return readResult;
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO 细化异常处理
            ReadResult readResult = new ReadResult();
            readResult.setCode(JoyQueueCode.SE_READ_FAILED);
            return readResult;
        }
    }


    // TODO: journalkeeper增加ALL级别
    private ResponseConfig qosLevelToResponseConfig(QosLevel qosLevel) {
        switch (qosLevel) {
            case ONE_WAY: return ResponseConfig.ONE_WAY;
            case RECEIVE: return ResponseConfig.RECEIVE;
            case PERSISTENCE: return ResponseConfig.PERSISTENCE;
            default: return ResponseConfig.REPLICATION;
        }
    }
    // 删除所有文件
    void delete() {
        checkServiceState(ServiceState.STOPPED, ServiceState.WILL_START);

        // TODO
    }


    private void checkServiceState(ServiceState... expectedStates) {
        ServiceState state = getServiceState();
        for (ServiceState expectedState : expectedStates) {
            if(state == expectedState) {
                return;
            }
        }
        throw new IllegalStateException(String.format("Expected service states: [%s], current service state: %s!",
                Arrays.stream(expectedStates).map(Enum::name).collect(Collectors.joining(", ")),
                state.name()));
    }

    void restore() throws IOException {
        server.recover();
    }

    void init(List<URI> uriList, URI thisServer) throws IOException {
        server.init(thisServer, uriList);
    }

    void rePartition(Collection<Short> partitions) {

        adminClient.scalePartitions(partitions.stream().mapToInt(p -> (int) p).toArray())
                .whenComplete((aVoid, exception) -> {
                   if(null != exception) {
                       if(exception instanceof NotLeaderException) {
                           logger.info("Ignore scale partition command, I'm not the leader. Topic: {}, group: {}, new partitions: {}.",
                                   getTopic(), getPartitionGroup(), partitions);
                       } else {
                           logger.warn("Scale partition failed! Topic: {}, group: {}, new partitions: {}.",
                                   getTopic(), getPartitionGroup(), partitions);
                       }
                   } else {
                       logger.info("Scale partition success! Topic: {}, group: {}, new partitions: {}.",
                               getTopic(), getPartitionGroup(), partitions);
                   }
                });

    }

    void maybeUpdateConfig(List<URI> newConfigs) {
        adminClient.getClusterConfiguration(server.serverUri())
                .thenCompose(clusterConfiguration -> adminClient.updateVoters(clusterConfiguration.getVoters(), newConfigs))
                .whenComplete((success, exception) -> {
                    if(null != exception) {
                        if(exception instanceof NotLeaderException) {
                            logger.info("Ignore scale partition command, I'm not the leader. Topic: {}, group: {}, new configs: {}.",
                                    getTopic(), getPartitionGroup(), newConfigs);
                        } else {
                            logger.warn("Scale partition failed! Topic: {}, group: {}, new configs: {}.",
                                    getTopic(), getPartitionGroup(), newConfigs, exception);
                        }
                    } else if(success) {
                        logger.info("Scale partition success! Topic: {}, group: {}, new configs: {}.",
                                getTopic(), getPartitionGroup(), newConfigs);
                    } else {
                        logger.warn("Scale partition failed! Topic: {}, group: {}, new configs: {}.",
                                getTopic(), getPartitionGroup(), newConfigs);
                    }
                });

    }
}
