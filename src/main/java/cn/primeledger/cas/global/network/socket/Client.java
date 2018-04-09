package cn.primeledger.cas.global.network.socket;


import cn.primeledger.cas.global.config.AppConfig;
import cn.primeledger.cas.global.network.Peer;
import cn.primeledger.cas.global.network.PeerManager;
import cn.primeledger.cas.global.network.socket.connection.ClientChannelInitializer;
import cn.primeledger.cas.global.network.socket.connection.ConnectionManager;
import cn.primeledger.cas.global.utils.ThreadFactoryUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMessageSizeEstimator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>The client launches the connection to the peer node. Usually, it will connect to the peer be successful unless
 * which connected with peers already or the peer is self or the out bound connections reach to the maximum. All the
 * connections are long-period connections.
 * </p>
 *
 * @author zhao xiaogang
 */

@Component
@Slf4j
public class Client {

    private NioEventLoopGroup workerGroup;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private ClientChannelInitializer clientChannelInitializer;

    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private PeerManager peerManager;

    public Client() {
        workerGroup = new NioEventLoopGroup(0, ThreadFactoryUtils.createThreadFactory("client"));
    }

    /**
     * Connect to the peer node after some pre-conditions.
     */
    public void connect(Peer peer) {
        if (!connectionManager.canConnect(true)) {
            return;
        }

        if (StringUtils.equals(peer.getId(), peerManager.getSelf().getId())) {
            return;
        }

        if (connectionManager.isConnected(peer.getId())) {
            return;
        }

        doConnect(peer);
        LOGGER.info("create new connection to peer: {}", peer.getId());
    }

    /**
     * Connect to the peer node asynchronously.
     */
    public ChannelFuture doConnect(Peer peer) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);

        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.MESSAGE_SIZE_ESTIMATOR, DefaultMessageSizeEstimator.DEFAULT);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, appConfig.getConnectionTimeout());

        String ip = peer.getIp();
        int port = peer.getSocketServerPort();
        bootstrap.remoteAddress(ip, port);
        bootstrap.handler(clientChannelInitializer);

        return bootstrap.connect().addListener(listener -> {
            if (listener.isSuccess()) {
                LOGGER.info("Successfully connect to peer address {}:{}", ip, port);
                return;
            }

            if (listener.cause() != null) {
                LOGGER.error("Failed to connect to peer address {}:{}. Exception : {}",
                        ip, port, listener.cause().getMessage());
                return;
            }

            if (listener.isCancelled()) {
                LOGGER.warn("Connected to peer address {}:{} been cancelled", ip, port);
            }
        });
    }
}
