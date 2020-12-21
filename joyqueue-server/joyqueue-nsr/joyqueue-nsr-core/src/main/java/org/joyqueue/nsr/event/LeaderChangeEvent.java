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
package org.joyqueue.nsr.event;

import org.joyqueue.domain.PartitionGroup;
import org.joyqueue.domain.TopicName;
import org.joyqueue.event.EventType;
import org.joyqueue.event.MetaEvent;

/**
 * LeaderChangeEvent
 * author: gaohaoxiang
 * date: 2019/8/28
 */
public class LeaderChangeEvent extends MetaEvent {

    private TopicName topic;
    private PartitionGroup oldPartitionGroup;
    private PartitionGroup newPartitionGroup;

    public LeaderChangeEvent() {

    }

    public LeaderChangeEvent(TopicName topic, PartitionGroup oldPartitionGroup, PartitionGroup newPartitionGroup) {
        this.topic = topic;
        this.oldPartitionGroup = oldPartitionGroup;
        this.newPartitionGroup = newPartitionGroup;
    }

    public LeaderChangeEvent(EventType eventType, TopicName topic, PartitionGroup oldPartitionGroup, PartitionGroup newPartitionGroup) {
        super(eventType);
        this.topic = topic;
        this.oldPartitionGroup = oldPartitionGroup;
        this.newPartitionGroup = newPartitionGroup;
    }

    public TopicName getTopic() {
        return topic;
    }

    public void setTopic(TopicName topic) {
        this.topic = topic;
    }

    public PartitionGroup getOldPartitionGroup() {
        return oldPartitionGroup;
    }

    public void setOldPartitionGroup(PartitionGroup oldPartitionGroup) {
        this.oldPartitionGroup = oldPartitionGroup;
    }

    public PartitionGroup getNewPartitionGroup() {
        return newPartitionGroup;
    }

    public void setNewPartitionGroup(PartitionGroup newPartitionGroup) {
        this.newPartitionGroup = newPartitionGroup;
    }

    @Override
    public String getTypeName() {
        return EventType.LEADER_CHANGE.name();
    }
}