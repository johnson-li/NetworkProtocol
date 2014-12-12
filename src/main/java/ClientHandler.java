import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by johnson on 12/1/14.
 */
public class ClientHandler extends ChannelHandlerAdapter{
    private ByteBuf byteBuf;
    private final ByteBuf received = Unpooled.buffer();
    static Logger logger = LogManager.getLogger();

    public ClientHandler() {
        byteBuf = Unpooled.buffer();

//        queryTest(byteBuf);
//        deleteTest(byteBuf);
//        addTest(byteBuf);
        editTest(byteBuf);
    }

    ByteBuf editTest(ByteBuf byteBuf) {
        return edit(byteBuf, 2, Columns.sex, "male");
    }

    ByteBuf deleteTest(ByteBuf byteBuf) {
        return delete(byteBuf, 3);
    }

    ByteBuf addTest(ByteBuf byteBuf) {
        Map<Columns, String> map = new HashMap<Columns, String>();
        map.put(Columns.id, "3");
        map.put(Columns.name, "grace");
        map.put(Columns.sex, "female");
        return add(byteBuf, map);
    }

    ByteBuf queryTest(ByteBuf byteBuf) {
        int id = 1;
        byteBuf = query(byteBuf, id, Columns.name);
        return byteBuf;
    }

    ByteBuf edit(ByteBuf byteBuf, int id, Columns columns, String str) {
        byteBuf.writeBytes("edit".getBytes());
        byteBuf.writeInt(id);
        byteBuf.writeInt(columns.ordinal());
        byteBuf.writeInt(str.getBytes().length);
        byteBuf.writeBytes(str.getBytes());
        return byteBuf;
    }

    ByteBuf delete(ByteBuf byteBuf, int id) {
        byteBuf.writeBytes("delete".getBytes());
        byteBuf.writeInt(id);
        return byteBuf;
    }

    ByteBuf add(ByteBuf byteBuf, Map<Columns, String> map) {
        byteBuf.writeBytes("add".getBytes());
        byteBuf.writeInt(map.size());
        if (map.containsKey(Columns.id)) {
            byteBuf.writeInt(Columns.id.ordinal());
            byteBuf.writeInt(map.get(Columns.id).getBytes().length);
            byteBuf.writeBytes(map.get(Columns.id).getBytes());
        }
        if (map.containsKey(Columns.name)) {
            byteBuf.writeInt(Columns.name.ordinal());
            byteBuf.writeInt(map.get(Columns.name).getBytes().length);
            byteBuf.writeBytes(map.get(Columns.name).getBytes());
        }
        if (map.containsKey(Columns.sex)) {
            byteBuf.writeInt(Columns.sex.ordinal());
            byteBuf.writeInt(map.get(Columns.sex).getBytes().length);
            byteBuf.writeBytes(map.get(Columns.sex).getBytes());
        }
        if (map.containsKey(Columns.picture)) {
            byteBuf.writeInt(Columns.picture.ordinal());
            byteBuf.writeInt(map.get(Columns.picture).getBytes().length);
            byteBuf.writeBytes(map.get(Columns.picture).getBytes());
        }
        return byteBuf;
    }

    ByteBuf query(ByteBuf byteBuf, int id, Columns columns) {
        byteBuf.writeBytes("query".getBytes());
        byteBuf.writeInt(id);
        byteBuf.writeInt(columns.ordinal());
        return byteBuf;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        received.writeBytes((ByteBuf)msg);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        while (received.readableBytes() > 0) {
            int mediaType = received.readInt();
            if (mediaType == MediaType.String.ordinal()) {
                String str = readString(received);
                logger.debug(str);
            }
            else if (mediaType == MediaType.File.ordinal()) {
                storeFile(received);
            }
            else {
                throw new Exception("channel can not read fully");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    String readString(ByteBuf byteBuf) {
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return new String(bytes);
    }

    void storeFile(ByteBuf byteBuf) throws Exception{
        String path = readString(byteBuf);
        logger.info("received remote path: " + path);
        Channel channel = Client.fileChannelFuture.channel();

        if (!channel.isWritable()) {
            throw new Exception("file channel is not writable");
        }
        ByteBuf response = Unpooled.buffer();
        response.writeBytes("file".getBytes());
//        response.writeInt(path.length());
        response.writeBytes(path.getBytes());
        channel.writeAndFlush(response);
        logger.info("received file request: " + new String(response.array()));
    }
}
