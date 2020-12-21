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
package org.joyqueue.monitor;

/**
 * 消费者信息
 *
 * author: gaohaoxiang
 * date: 2018/10/10
 */
public class ConsumerMonitorInfo extends BaseMonitorInfo {

    private String topic;
    private String app;
    private int connections;

    private DeQueueMonitorInfo deQueue;
    private RetryMonitorInfo retry;
    private PendingMonitorInfo pending;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public int getConnections() {
        return connections;
    }

    public void setConnections(int connections) {
        this.connections = connections;
    }

    public DeQueueMonitorInfo getDeQueue() {
        return deQueue;
    }

    public void setDeQueue(DeQueueMonitorInfo deQueue) {
        this.deQueue = deQueue;
    }

    public RetryMonitorInfo getRetry() {
        return retry;
    }

    public void setRetry(RetryMonitorInfo retry) {
        this.retry = retry;
    }

    public PendingMonitorInfo getPending() {
        return pending;
    }

    public void setPending(PendingMonitorInfo pending) {
        this.pending = pending;
    }
}