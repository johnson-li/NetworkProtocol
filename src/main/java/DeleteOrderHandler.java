import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Created by johnson on 12/1/14.
 */
public class DeleteOrderHandler implements OrderHandler{
    static Logger logger = LogManager.getLogger();
    static final byte[] HEADER = "delete".getBytes();

    @Override
    public boolean check(ByteBuf byteBuf) {
        byte[] header = new byte[HEADER.length];
        byteBuf.getBytes(byteBuf.readerIndex(), header, 0, header.length);
        if (!Arrays.equals(HEADER, header)) {
            return false;
        }
        logger.debug("order type: " + new String(header));
        byteBuf.readBytes(header);
        return true;
    }

    @Override
    public ByteBuf operate(ByteBuf byteBuf) {
        ByteBuf response = Unpooled.buffer();
        int id = byteBuf.readInt();
        logger.debug("starting to delete id: " + id);
        String ret = "delete succeeded";
        try {
            JDBCUtils.delete(id);
        }
        catch (Exception e) {
            logger.catching(e);
            ret = "delete failed";
        }
        response.writeInt(MediaType.String.ordinal());
        response.writeInt(ret.getBytes().length);
        response.writeBytes(ret.getBytes());
        return response;
    }
}
