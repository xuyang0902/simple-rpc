package com.simple.rpc.server;

import com.simple.BootServer;
import com.simple.RemotingService;
import com.simple.protocol.RpcDecoder;
import com.simple.protocol.RpcEncoder;
import com.simple.rpc.model.NodeConfig;
import com.simple.rpc.model.RpcResponse;
import com.simple.rpc.model.RpcURL;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

/**
 * 远程Server实现
 *
 * @author by xu.qiang
 * @date 2016/12/8.
 */
public class NettyRpcServer implements RemotingService {

    private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private NodeConfig nodeConfig;

    public NettyRpcServer(NodeConfig nodeConfig) {
        this.serverBootstrap = new ServerBootstrap();
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup(4);
        this.nodeConfig = nodeConfig;
    }


    public void start() {

        logger.info("rpc netty sever starting");

        ServerBootstrap handler = this.serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 4096)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_SNDBUF, 655336)
                .option(ChannelOption.SO_RCVBUF, 65536)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {

                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast("decodeRpc", new RpcDecoder(RpcURL.class));
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
                        pipeline.addLast("encodeRpc", new RpcEncoder(RpcResponse.class));
                        pipeline.addLast("rpcServerHandler", new NettyRpcServerHandler());
                    }
                });

        try {

            handler.bind(new InetSocketAddress(nodeConfig.getIp(),nodeConfig.getPort()));

            logger.info("server started finished!");

        } catch (Exception e) {
            logger.info("server starting exception", e);
        }

    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        logger.info("rpc server shutdownGracefully!");
    }

    /**
     * rpc服务端处理器
     */
    class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcURL> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RpcURL url) throws Exception {


            /**
             * 根据rpc的请求  服务端 调用本地提供者节点服务  将信息返回给客户端
             *
             *  服务端处理异常  直接catch调所有异常   返回给客户端
             */
            RpcResponse response = new RpcResponse();
            response.setOpaque(url.getOpaque());
            try {
                Object serviceBean = BootServer.getHandlerMap().get(url.getServicePath());


                Class<?> serviceClass = serviceBean.getClass();

                String methodName = url.getMethodName();
                Class<?>[] parameterTypes = url.getParameterTypes();
                Object[] parameters = url.getParameters();

                // JDK reflect
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                method.setAccessible(true);
                Object result = method.invoke(serviceBean, parameters);
                response.setResult(result);

                /*  Cglib reflect
                FastClass serviceFastClass = FastClass.create(serviceClass);
                FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
                serviceFastMethod.invoke(serviceBean, parameters);
                */
            } catch (Throwable throwable) {
                response.setError(throwable.getMessage());
                response.setResult(null);
            } finally {
                ctx.channel().writeAndFlush(response);
            }


        }

    }

}
