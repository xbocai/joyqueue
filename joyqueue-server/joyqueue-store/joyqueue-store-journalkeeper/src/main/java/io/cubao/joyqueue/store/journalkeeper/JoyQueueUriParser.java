package io.cubao.joyqueue.store.journalkeeper;

import io.chubao.joyqueue.broker.BrokerContext;
import io.chubao.joyqueue.broker.BrokerContextAware;
import io.chubao.joyqueue.domain.Broker;
import io.journalkeeper.rpc.URIParser;
import io.journalkeeper.utils.spi.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author LiYue
 * Date: 2019/9/30
 */
@Singleton
public class JoyQueueUriParser implements URIParser, BrokerContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JoyQueueUriParser.class);
    private BrokerContext brokerContext;
    private static final String SCHEME = "joyqueue";
    private int portOffset = 5;
    @Override
    public String[] supportedSchemes() {
        return new String [] {SCHEME};
    }

    @Override
    public InetSocketAddress parse(URI uri) {
        if(null != brokerContext) {
            Broker broker = brokerContext.getClusterManager().getBrokerById(Integer.parseInt(uri.getAuthority()));
            return new InetSocketAddress(broker.getIp(), broker.getPort() + portOffset);
        }
        return null;
    }

    @Override
    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }

    public URI create(String topic, int group, int brokerId) {
        try {
            return new URI(SCHEME, String.valueOf(brokerId),  "/" + topic + "/" + group, null);
        } catch (URISyntaxException e) {
            logger.warn("Create uri failed!", e);
            return null;
        }
    }
}
