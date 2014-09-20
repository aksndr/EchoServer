package ru.aksndr;

/**
 * Created by aksndr on 18.09.2014.
 */

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jboss.netty.channel.Channels.pipeline;


public class EchoServer {
    private final int port;
    private final int maxBytes;

    public EchoServer(int port, int maxBytes) {
        this.port = port;
        this.maxBytes = maxBytes;
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        // Set up the pipeline factory.
        bootstrap.setPipelineFactory(new ServerPipelineFactory(maxBytes));
        // Bind and start to accept incoming connections.
        // The InetSocketAddress plays role of the wrapper facade in the Wrapper Facade pattern
        // The bind method plays role of the acceptor in the Acceptor-Connector pattern.
        bootstrap.bind(new InetSocketAddress(port));
    }

    /*
    * Handler implementation for the echo server. This class plays the concrete
    * event handler role in the Reactor pattern.
    */
    class EchoServerHandler extends SimpleChannelUpstreamHandler {
        private final Logger logger = Logger.getLogger(EchoServerHandler.class.getName());

        /*
        * This hook method is dispatched by the Reactor when data shows up from
        * a client.
        */
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
            // Cast to a String first.
            String request = (String) e.getMessage();
            // Generate and write a response.
            String response = null;
            boolean close = false;
            if ("\u0003".equals(request.toLowerCase())) {
                response = "Have a good day!";
                close = true;
            } else {
                response = request + "\r\n";
            }
            // Send back the received message to the remote peer.
            ChannelFuture future = e.getChannel().write(response);
            if (close) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
            logger.log(Level.WARNING, "Unexpected exception from downstream.", e.getCause());
            e.getChannel().close();
        }

        @Override
        public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e)
                throws Exception {
            if (e instanceof ChannelStateEvent) {
                logger.info(e.toString());
            }
            super.handleUpstream(ctx, e);
        }
    }

    class ServerPipelineFactory implements ChannelPipelineFactory {
        private final int maxBytes;

        ServerPipelineFactory(int maxBytes) {
            this.maxBytes = maxBytes;
        }

        public ChannelPipeline getPipeline() throws Exception {
            // Create a default pipeline implementation.
            ChannelPipeline pipeline = pipeline();
            // Add the text line codec combination first,
            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(maxBytes,
                    Delimiters.lineDelimiter()));
            pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
            pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
            // and then business logic.
            pipeline.addLast("handler", new EchoServerHandler());
            return pipeline;
        }
    }


    public static void main(String[] args) throws Exception {
        int port;
        int maxBytes;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
            maxBytes = Integer.parseInt(args[1]);
        } else {
            port = 8080;
            maxBytes = 8192;
        }
        new EchoServer(port,maxBytes).start();
    }
}