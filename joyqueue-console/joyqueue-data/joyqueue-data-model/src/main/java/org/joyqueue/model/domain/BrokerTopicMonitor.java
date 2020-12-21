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
package org.joyqueue.model.domain;

import org.joyqueue.manage.PartitionGroupMetric;

import java.util.List;

/**
 * Created by wangxiaofei1 on 2019/3/13.
 */
public class BrokerTopicMonitor {
    private String topic;
    private List<BrokerTopicMonitorRecord> brokerTopicMonitorRecordList;
    private List<PartitionGroupMetric> partitionGroupMetricList;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<BrokerTopicMonitorRecord> getBrokerTopicMonitorRecordList() {
        return brokerTopicMonitorRecordList;
    }

    public void setBrokerTopicMonitorRecordList(List<BrokerTopicMonitorRecord> brokerTopicMonitorRecordList) {
        this.brokerTopicMonitorRecordList = brokerTopicMonitorRecordList;
    }

    public List<PartitionGroupMetric> getPartitionGroupMetricList() {
        return partitionGroupMetricList;
    }

    public void setPartitionGroupMetricList(List<PartitionGroupMetric> partitionGroupMetricList) {
        this.partitionGroupMetricList = partitionGroupMetricList;
    }
}
