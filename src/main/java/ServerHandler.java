import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by johnson on 12/1/14.
 */
public class ServerHandler extends ChannelHandlerAdapter{
    static Set<OrderHandler> orderHandlers = new HashSet<OrderHandler>();
    static Logger logger = LogManager.getLogger();

    static {
        orderHandlers.add(new QueryOrderHandler());
        orderHandlers.add(new AddOrderHandler());
        orderHandlers.add(new DeleteOrderHandler());
        orderHandlers.add(new EditOrderHandler());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf received = (ByteBuf)msg;
        while (received.readableBytes() > 0) {
            boolean check = false;
            for (OrderHandler orderHandler: orderHandlers) {
                if (check = orderHandler.check(received)) {
                    ByteBuf response = orderHandler.operate(received);
                    ctx.writeAndFlush(response);
                    break;
                }
            }
            logger.debug("bytes remaining: " + received.readableBytes());
            if (!check) {
                logger.error("no handler to handler request");
                throw new Exception("no handler to handler request");
            }
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        logger.entry();
//        while (byteBuf.readableBytes() > 0) {
//            boolean check = false;
//            for (OrderHandler orderHandler: orderHandlers) {
//                if (check = orderHandler.check(byteBuf)) {
//                    ByteBuf response = orderHandler.operate(byteBuf);
//                    logger.debug(response.readableBytes());
//                    while (response.readableBytes() > (1 << 10)) {
//                        ByteBuf byteBuf1 = Unpooled.buffer();
//                        response.readBytes(byteBuf1, 0, 1 << 10);
//                        logger.debug(byteBuf1.readableBytes());
//                        ctx.writeAndFlush(byteBuf1);
//                    }
//                    ctx.writeAndFlush(response);
//                    break;
//                }
//            }
//            if (!check) {
//                throw new Exception("Handle request error");
//            }
//        }
//        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
