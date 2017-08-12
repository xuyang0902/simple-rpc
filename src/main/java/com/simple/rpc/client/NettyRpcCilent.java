package com.simple.rpc.client;

import com.simple.RemotingService;
import com.simple.protocol.RpcDecoder;
import com.simple.protocol.RpcEncoder;
import com.simple.register.core.ZkRegister;
import com.simple.rpc.strategy.ChooseStrategy;
import com.simple.rpc.strategy.DefaultChooseStrategy;
import com.simple.rpc.exception.RpcException;
import com.simple.rpc.model.RpcFuture;
import com.simple.rpc.model.RpcResponse;
import com.simple.rpc.model.RpcURL;
import com.simple.rpc.model.NodeConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 远程Client 实现
 *
 * @author by xu.qiang
 * @date 2016/12/8.
 */
public class NettyRpcCilent implements RemotingService, RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcCilent.class);

    private final Bootstrap bootstrap;
    private final EventLoopGroup worker;

    /**
     * 保存每次调用的RpcFutrue
     */
    private final ConcurrentHashMap<Integer, RpcFuture> rpcFutureTables = new ConcurrentHashMap<Integer, RpcFuture>();

    /**
     * 通道map存储lock
     */
    private final Lock lockChannelTables = new ReentrantLock();

    /**
     * key: 192.168.1.110:7777
     * value: io.netty.channel.ChannelFuture
     */
    private final ConcurrentMap<String, ChannelFuture> channelTables = new ConcurrentHashMap<String, ChannelFuture>();

    //节点选择器
    private static final ChooseStrategy chooseStrategy = new DefaultChooseStrategy();

    public NettyRpcCilent() {
        this.bootstrap = new Bootstrap();
        this.worker = new NioEventLoopGroup(4);

    }

    public void start() {
        logger.info(" netty sever starting");

        Bootstrap handler = this.bootstrap.group(worker)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, 655336)
                .option(ChannelOption.SO_RCVBUF, 65536)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {

                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decodeRpc", new RpcDecoder(RpcResponse.class));
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                        pipeline.addLast("encodeRpc", new RpcEncoder(RpcURL.class));
                        pipeline.addLast("nettyRpcClientHandler", new NettyRpcClientHandler());
                    }
                });

        logger.info(" netty sever finished");
    }

    public void shutdown() {
        worker.shutdownGracefully();
        logger.info("client shutdownGracefully!");
    }


    /**
     * rpc 客户端处理器
     */
    class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {

            try {
                int opaque = response.getOpaque();

                if (opaque <= 0) {
                    throw new RpcException("response property opaque can not be  <=0 ");
                }
                //通知当前的futrue 可以拿到响应信息了。
                rpcFutureTables.get(opaque).recieve(response);

                rpcFutureTables.remove(opaque);
            } catch (Exception e) {
                logger.error("e", e);
            }


        }
    }


    @Override
    public RpcResponse invokeSync(RpcURL request, long timeoutMillis) {

        //01.选择消费端在注册中心发现的 提供方节点
        List<NodeConfig> nodes = ZkRegister.consumerCachedProviderNodes.get(request.getServicePath());
        if (nodes == null || nodes.size() == 0) {
            throw new RpcException("no provider " + request.getServicePath());
        }

        NodeConfig node = chooseStrategy.choose(nodes);
        String address = node.getAddress();

        /**
         *  02.根据本地通道表查询  是否和提供方节点已经存在通信通道
         *  存在直接使用
         *  不存在  新增
         */
        ChannelFuture channelFuture = channelTables.get(address);
        Channel channel = null;
        if (channelFuture != null && channelFuture.channel() != null && channelFuture.channel().isActive()) {
            channel = channelFuture.channel();
        } else {

            lockChannelTables.lock();
            try {
                ChannelFuture connect = this.bootstrap.connect(node.getIp(), node.getPort());

                final CountDownLatch latch = new CountDownLatch(1);

                connect.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
                            latch.countDown();
                        }
                    }
                });
                latch.await();

                channel = connect.channel();
                channelTables.put(address, connect);
            } catch (Exception e) {

            } finally {
                lockChannelTables.unlock();
            }
        }


        //03.给server发送消息
        channel.writeAndFlush(request);

        //04.同步等待消息返回
        RpcFuture rpcFuture = new RpcFuture(request);

        //05.保存每次请求的 future
        rpcFutureTables.put(request.getOpaque(), rpcFuture);

        return rpcFuture.get(timeoutMillis, TimeUnit.MILLISECONDS);
    }

}