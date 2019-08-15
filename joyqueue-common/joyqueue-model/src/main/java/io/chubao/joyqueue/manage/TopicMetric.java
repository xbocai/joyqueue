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
package io.chubao.joyqueue.manage;

import java.io.Serializable;

public class TopicMetric implements Serializable {
    private static final long serialVersionUID = 1L;
    private String topic;
    private PartitionGroupMetric[] partitionGroupMetrics;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public PartitionGroupMetric[] getPartitionGroupMetrics() {
        return partitionGroupMetrics;
    }

    public void setPartitionGroupMetrics(PartitionGroupMetric[] partitionGroupMetrics) {
        this.partitionGroupMetrics = partitionGroupMetrics;
    }
}