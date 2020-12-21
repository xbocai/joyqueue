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
package org.joyqueue.broker.election.command;

import org.joyqueue.broker.election.TopicPartitionGroup;
import org.joyqueue.network.transport.command.JoyQueuePayload;
import org.joyqueue.network.command.CommandType;

/**
 * author: zhuduohui
 * email: zhuduohui@jd.com
 * date: 2018/8/15
 */
public class TimeoutNowRequest extends JoyQueuePayload {
    private TopicPartitionGroup topicPartitionGroup;
    private int term;

    public TimeoutNowRequest(TopicPartitionGroup topicPartitionGroup, int term) {
        this.topicPartitionGroup = topicPartitionGroup;
        this.term = term;
    }

    public TopicPartitionGroup getTopicPartitionGroup() {
        return topicPartitionGroup;
    }

    public String getTopic() {
        return topicPartitionGroup.getTopic();
    }

    public int getPartitionGroup() {
        return topicPartitionGroup.getPartitionGroupId();
    }

    public int getTerm() {
        return term;
    }

    @Override
    public int type() {
        return CommandType.RAFT_TIMEOUT_NOW_REQUEST;
    }
}
