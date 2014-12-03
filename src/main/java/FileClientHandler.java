import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by johnson on 12/1/14.
 */
public class FileClientHandler extends ChannelHandlerAdapter{
    static Logger logger = LogManager.getLogger();
    static Path downloadPath = Paths.get("./");
    static final byte[] END = "end".getBytes();
    public static final int MAGIC = 12345;
    long fileLength = 0;
    String pathStr;
    ByteBuf received = Unpooled.buffer();
    FileChannel fileChannel;
    boolean started = false;

    {
        try {
//            Path path = Paths.get(downloadPath.toAbsolutePath().toString(), "sell.png");
//            RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "rw");
//            fileChannel = randomAccessFile.getChannel();
//            logger.debug(path.toAbsolutePath());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf)msg;
        if (!started) {
            checkStart(byteBuf);
        }
        logger.info("file download process: " + received.readableBytes() + "/" + fileLength);
        received.writeBytes(byteBuf);
        ReferenceCountUtil.release(msg);
        if (checkEnd()) complete();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    boolean checkEnd() {
        return received.readableBytes() >= fileLength;
    }

    boolean checkEnd(ByteBuf byteBuf) {
        byte[] bytes = new byte[END.length];
        byteBuf.getBytes(0, bytes, 0, bytes.length);
        return Arrays.equals(END, bytes);
    }

    void checkStart(ByteBuf byteBuf) {
        int magic;
        if (MAGIC == (magic = byteBuf.readInt())) {
            int pathLength = byteBuf.readInt();
            byte[] pathBytes = new byte[pathLength];
            byteBuf.readBytes(pathBytes);
            this.fileLength = byteBuf.readLong();
            this.pathStr = new String(pathBytes);
            this.started = true;
            logger.info("started to download file: " + this.pathStr);
            logger.info("file length: " + fileLength);
            try {
                RandomAccessFile randomAccessFile = new RandomAccessFile(pathStr, "rw");
                this.fileChannel = randomAccessFile.getChannel();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            logger.warn("MAGIC check failed: " + String.valueOf(magic));
        }
    }

    void complete() {
        logger.info("file download completed: " + received.readableBytes() + "/" + fileLength);
        started = false;
        ByteBuffer byteBuffer = received.nioBuffer();
        try {
            while (byteBuffer.hasRemaining()) {
                fileChannel.write(byteBuffer);
            }
            fileChannel.close();
        }
        catch (Exception e) {
            logger.catching(e);
        }
        byteBuffer.clear();
        received.clear();
    }
}
