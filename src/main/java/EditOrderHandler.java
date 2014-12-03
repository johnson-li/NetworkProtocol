import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Created by johnson on 12/1/14.
 */
public class EditOrderHandler implements OrderHandler{
    static Logger logger = LogManager.getLogger();
    static final byte[] HEADER = "edit".getBytes();

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
        response.writeInt(MediaType.String.ordinal());
        String ret = "edit succeeded";
        int id = byteBuf.readInt();
        Columns columns = Columns.values()[byteBuf.readInt()];
        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);
        try {
            JDBCUtils.edit(id, columns, new String(bytes));
        }
        catch (Exception e) {
            logger.catching(e);
            ret = "edit failed";
        }
        response.writeInt(ret.getBytes().length);
        response.writeBytes(ret.getBytes());
        return response;
    }
}
