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
package org.joyqueue.network.session;

import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 生产者ID
 */
public class ProducerId {

    public static AtomicLong PRODUCER_ID = new AtomicLong(0);
    // 连接ID
    private ConnectionId connectionId;
    // 序号
    private long sequence;
    // 生产者ID
    private String producerId;

    /**
     * 构造函数
     *
     * @param connectionId 连接ID
     */
    public ProducerId(final ConnectionId connectionId) {
        setup(connectionId, 0);
    }

    /**
     * 构造函数
     *
     * @param connectionId 连接ID
     * @param sequence     序号
     */
    public ProducerId(final ConnectionId connectionId, final long sequence) {
        setup(connectionId, sequence);
    }

    /**
     * 构造函数
     *
     * @param parts 字符串分割
     */
    public ProducerId(final String[] parts) {
        setup(new ConnectionId(parts), Long.parseLong(parts[5]));
    }

    /**
     * @param producerId 生产者ID
     */
    public ProducerId(final String producerId) {
        if (producerId == null || producerId.isEmpty()) {
            throw new IllegalArgumentException("producerId can not be empty");
        }
        String[] parts = new String[]{null, null, null, null, null, null};
        int index = 0;
        StringTokenizer tokenizer = new StringTokenizer(producerId, "-");
        while (tokenizer.hasMoreTokens()) {
            parts[index++] = tokenizer.nextToken();
            if (index >= parts.length) {
                break;
            }
        }
        if (index < parts.length) {
            throw new IllegalArgumentException("producerId is invalid.");
        }
        setup(new ConnectionId(parts), Long.parseLong(parts[parts.length - 1]));
    }

    /**
     * 初始化
     *
     * @param connectionId 连接ID
     * @param sequence     序号
     */
    protected void setup(final ConnectionId connectionId, final long sequence) {
        if (connectionId == null) {
            throw new IllegalArgumentException("connectionId must not be null");
        }
        long seq = sequence;
        if (seq <= 0) {
            seq = PRODUCER_ID.incrementAndGet();
        }
        this.connectionId = connectionId;
        this.sequence = seq;
        // 在构造函数中创建，防止延迟加载并发问题
        this.producerId = connectionId.getConnectionId() + "-" + seq;
    }

    public ConnectionId getConnectionId() {
        return this.connectionId;
    }

    public long getSequence() {
        return this.sequence;
    }

    public String getProducerId() {
        return this.producerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProducerId that = (ProducerId) o;

        if (sequence != that.sequence) {
            return false;
        }
        if (connectionId != null ? !connectionId.equals(that.connectionId) : that.connectionId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = connectionId != null ? connectionId.hashCode() : 0;
        result = 31 * result + (int) (sequence ^ (sequence >>> 32));
        return result;
    }
}