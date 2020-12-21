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
package org.joyqueue.tools.config;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

import java.util.HashMap;
import java.util.Map;

/**
 * ConsoleProducerConfig
 *
 * author: gaohaoxiang
 * date: 2019/6/26
 */
public class ConsoleProducerConfig {

    @Parameter(names = {"-s", "--server"}, description = "server bootstrap")
    private String bootstrap = "localhost:50088";

    @Parameter(names = {"-t", "--topic"}, description = "producer topic", required = true)
    private String topic;

    @Parameter(names = {"-n", "--namespace"}, description = "producer namespace")
    private String namespace;

    @Parameter(names = {"-r", "--region"}, description = "producer region")
    private String region;

    @Parameter(names ={"-a", "--app"}, description = "producer app", required = true)
    private String app;

    @Parameter(names ={"-T", "--token"}, description = "app token", required = true)
    private String token;

    @DynamicParameter(names = {"-p", "-params"}, description = "producer params")
    private Map<String, String> params = new HashMap<>();

    @Parameter(names = {"-k", "--key"}, description = "message key")
    private String key;

    @Parameter(names = {"-b", "--body"}, description = "message body")
    private String body;

    @Parameter(names = "--help", help = true)
    private boolean help;

    public String getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(String bootstrap) {
        this.bootstrap = bootstrap;
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

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public boolean isHelp() {
        return help;
    }
}