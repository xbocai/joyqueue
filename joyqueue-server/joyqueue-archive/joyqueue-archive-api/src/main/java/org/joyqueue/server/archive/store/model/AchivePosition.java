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
package org.joyqueue.server.archive.store.model;

/**
 * 归档位置
 * <p>
 * Created by chengzhiliang on 2018/12/13.
 */
public class AchivePosition {
    private String topic;
    private short partition;
    private long index;

    public AchivePosition(String topic, short partition, long index) {
        this.topic = topic;
        this.partition = partition;
        this.index = index;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public short getPartition() {
        return partition;
    }

    public void setPartition(short partition) {
        this.partition = partition;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }
}
