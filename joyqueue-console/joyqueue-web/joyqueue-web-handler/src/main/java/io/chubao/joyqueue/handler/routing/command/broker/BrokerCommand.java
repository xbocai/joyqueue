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
package io.chubao.joyqueue.handler.routing.command.broker;

import io.chubao.joyqueue.handler.error.ConfigException;
import io.chubao.joyqueue.handler.routing.command.NsrCommandSupport;
import io.chubao.joyqueue.model.domain.Broker;
import io.chubao.joyqueue.model.query.QBroker;
import io.chubao.joyqueue.service.BrokerService;
import com.jd.laf.web.vertx.annotation.Body;
import com.jd.laf.web.vertx.annotation.Path;
import com.jd.laf.web.vertx.annotation.QueryParam;
import com.jd.laf.web.vertx.response.Response;
import com.jd.laf.web.vertx.response.Responses;
import org.apache.commons.net.telnet.TelnetClient;

import static io.chubao.joyqueue.handler.Constants.ID;

/**
 * @author wylixiaobin
 * Date: 2018/10/17
 */
public class BrokerCommand extends NsrCommandSupport<Broker,BrokerService,QBroker> {

    @Override
    @Path("delete")
    public Response delete(@QueryParam(ID) String id) throws Exception {
        Broker newModel = service.findById(Long.valueOf(id));
        int count = service.delete(newModel);
        if (count <= 0) {
            throw new ConfigException(deleteErrorCode());
        }
        //publish(); 暂不进行发布消息
        return Responses.success();
    }

    @Path("get")
    public Response get(@QueryParam(ID) Long id) throws Exception {
        Broker newModel = service.findById(id);
        if (newModel == null) {
            throw new ConfigException(getErrorCode());
        }
        return Responses.success(newModel);
    }

    @Path("findByTopic")
    public Response findByTopic(@Body(type = Body.BodyType.TEXT) String topicFullName) throws Exception {
        return Responses.success(service.findByTopic(topicFullName));
    }

    @Path("telnet")
    public Response telnet(@QueryParam("ip") String ip,@QueryParam("port") int port) throws Exception {
        TelnetClient telnetClient = new TelnetClient("vt200");  //指明Telnet终端类型，否则会返回来的数据中文会乱码
        telnetClient.setDefaultTimeout(5000); //socket延迟时间：5000ms
        try {
            telnetClient.connect(ip,port);  //建立一个连接,默认端口是23
        } catch (Exception e) {
            return Responses.error(500,"未存活");
        }
        return Responses.success();
    }
}
