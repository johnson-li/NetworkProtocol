import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by johnson on 12/2/14.
 */
public class FileServerHandler extends ChannelHandlerAdapter{
    Logger logger = LogManager.getLogger();
    static final byte[] HEADER = "file".getBytes();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] header = new byte[HEADER.length];
        ByteBuf byteBuf = (ByteBuf)msg;
        (byteBuf).getBytes(0, header);
        if (!Arrays.equals(HEADER, header)) {
            super.channelRead(ctx, msg);
        }
        else {
            byteBuf.readBytes(header);
//            int length = byteBuf.readInt();
//            byte[] path = new byte[length];
            byte[] path = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(path);
            String pathStr = new String(path);
            int padding = 0;
            if (pathStr.endsWith("\r\n")) {
                padding = 2;
            }
            else if (pathStr.endsWith("\n")) {
                padding = 1;
            }
            transferFile(ctx, pathStr.substring(0, pathStr.length() - padding));
        }
    }

    void transferFile(ChannelHandlerContext ctx, String pathStr) {
        logger.info("Transferring file: " + pathStr);
        Path path = Paths.get(pathStr);
        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                FileChannel fileChannel = FileChannel.open(path);
                FileRegion fileRegion = new DefaultFileRegion(fileChannel, 0, fileChannel.size());
                logger.info(path + ": " + String.valueOf(fileChannel.size()) + " bytes");
                ByteBuf headBuf = Unpooled.buffer();
                headBuf.writeInt(FileClientHandler.MAGIC);
                String fileName = path.getFileName().toString();
                headBuf.writeInt(fileName.length());
                headBuf.writeBytes(fileName.getBytes());
                headBuf.writeLong(fileChannel.size());
                ctx.writeAndFlush(headBuf);
                ctx.writeAndFlush(fileRegion);
                ByteBuf endBuf = Unpooled.buffer();
                endBuf.writeBytes("end".getBytes());
                Thread.sleep(100);
                ctx.writeAndFlush(endBuf);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            logger.warn("file not found: " + path.toAbsolutePath());
            byte[] bytes = ("file not found: " + pathStr).getBytes();
            ByteBuf error = Unpooled.buffer();
            error.writeBytes(bytes);
            ctx.writeAndFlush(error);
        }
    }
}
