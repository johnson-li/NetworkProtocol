import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by johnson on 12/1/14.
 */
public class Client {
    Logger logger = LogManager.getLogger();
    static ChannelFuture clientChannelFuture, fileChannelFuture;

    public static void main(String args[]) throws Exception{
        /*if (true) {
            throw new Exception("123");
        }*/
        final int port = Server.PORT;
        final String host = "127.0.0.1";
        new Thread(new FileTransferClient(port, host)).start();
        new Client().connect(port, host);
    }

    void connect(int port, String host) throws Exception{
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ClientHandler());
                        }
                    });
            clientChannelFuture = bootstrap.connect(host, port).sync();
            clientChannelFuture.channel().closeFuture().sync();
        }
        finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    static class FileTransferClient implements Runnable{
        int port;
        String host;

        public FileTransferClient(int port, String host) {
            this.port = port;
            this.host = host;
        }

        @Override
        public void run() {
            EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(new FileClientHandler());
                            }
                        });
                fileChannelFuture = bootstrap.connect(host, port).sync();
                fileChannelFuture.channel().closeFuture().sync();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                eventLoopGroup.shutdownGracefully();
            }
        }
    }
}
