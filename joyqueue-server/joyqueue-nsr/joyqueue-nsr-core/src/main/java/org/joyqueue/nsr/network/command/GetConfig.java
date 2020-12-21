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
package org.joyqueue.nsr.network.command;

import org.joyqueue.network.transport.command.JoyQueuePayload;

/**
 * @author wylixiaobin
 * Date: 2019/1/29
 */
public class GetConfig extends JoyQueuePayload {
    private String group;
    private String key;
    public GetConfig group(String group){
        this.group = group;
        return this;
    }

    public GetConfig key(String key){
        this.key = key;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public String getKey() {
        return key;
    }

    @Override
    public int type() {
        return NsrCommandType.GET_CONFIG;
    }

    @Override
    public String toString() {
        return "GetConfig{" +
                "group='" + group + '\'' +
                ", key='" + key + '\'' +
                '}';
    }
}
