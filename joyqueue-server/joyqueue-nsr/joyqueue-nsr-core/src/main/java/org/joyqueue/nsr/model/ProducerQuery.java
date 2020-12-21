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
package org.joyqueue.nsr.model;

import org.joyqueue.model.Query;

import java.util.List;

public class ProducerQuery implements Query {
    /**
     * 应用
     */
    private String app;
    /**
     * 主题
     */
    private String topic;
    /**
     * 命名空间
     */
    private String namespace;
    /**
     * 客户端类型
     */
    private byte clientType;

    private List<String> appList;


    public ProducerQuery() {
    }


    public ProducerQuery(String app) {
        this.app = app;
    }

    public ProducerQuery(String topic, String namespace) {
        this.topic = topic;
        this.namespace = namespace;
    }

    public ProducerQuery(String app, String topic, String namespace) {
        this.app = app;
        this.topic = topic;
        this.namespace = namespace;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public byte getClientType() {
        return clientType;
    }

    public void setClientType(byte clientType) {
        this.clientType = clientType;
    }

    public List<String> getAppList() {
        return appList;
    }

    public void setAppList(List<String> appList) {
        this.appList = appList;
    }
}
