package ru.aksndr;

/**
 * Created by aksndr on 19.09.2014.
 */

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Netty {

    public static class EchoServerHandler extends SimpleChannelUpstreamHandler {

//        @Override
//        public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
//
//            // Log all channel state changes.
//            if (e instanceof ChannelStateEvent) {
//                System.out.println("Channel state changed: " + e);
//            }
//
//            super.handleUpstream(ctx, e);
//
//        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {

            //Use channel buffer to get the message from MessageEvent e and copy each character read to a string
//check for occurence of '\n' or '\r' characters and if yes then print the above string
            Channel channel = e.getChannel();
            ChannelBuffer buf = (ChannelBuffer) e.getMessage();
            ChannelBuffer bufr = new DynamicChannelBuffer(1);
            StringBuffer stringBuffer = new StringBuffer();
            while (buf.readable()){
                bufr = buf.slice();
                stringBuffer.append(bufr.array());
            }
//            String msg = (String) e.getMessage();

            channel.write(stringBuffer.toString());

        }

//        @Override
//        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
//
//        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            e.getCause().printStackTrace();
            System.out.println(e.toString());
            Channel ch = e.getChannel();
            ch.close();
        }
    }

    public static class NettyServer {
        int port, maxBufLength;

        public NettyServer(int port, int maxBufLength) {
            this.port = port;
            this.maxBufLength = maxBufLength;
        }

        public void start() throws Exception {
            ChannelFactory factory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool());

            ServerBootstrap bootstrap = new ServerBootstrap(factory);

            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() {
                    return Channels.pipeline(
//                            new DelimiterBasedFrameDecoder(maxBufLength, true, Delimiters.lineDelimiter()),
//                            new StringDecoder(CharsetUtil.UTF_8),
//                            new StringEncoder(CharsetUtil.UTF_8),
                            new EchoServerHandler()
                            );
                }
            });

            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.keepAlive", true);

            bootstrap.bind(new InetSocketAddress(port));
            System.out.println("Enter text (Ctrl+c to end)");
        }
    }

    public static void main(String[] args) throws Exception {

        int port, maxBufLength;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
            maxBufLength = Integer.parseInt(args[1]);
        } else {
            port = 8080;
            maxBufLength = 1024;
        }

        NettyServer ns = new NettyServer(port, maxBufLength);
        ns.start();

    }
}
