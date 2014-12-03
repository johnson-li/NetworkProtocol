import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

/**
 * Created by johnson on 12/1/14.
 */
public class QueryOrderHandler implements OrderHandler{
    static Logger logger = LogManager.getLogger();
    static final byte[] HEADER = "query".getBytes();

    @Override
    public boolean check(ByteBuf byteBuf) {
        byte[] header = new byte[HEADER.length];
        byteBuf.getBytes(byteBuf.readerIndex(), header, 0, header.length);
        if (!Arrays.equals(HEADER, header)) {
            return false;
        }
        logger.info("order type: " + new String(header));
        byteBuf.readBytes(header);
        return true;
    }

    @Override
    public ByteBuf operate(ByteBuf byteBuf) {
        ByteBuf response = Unpooled.buffer();
        int id = byteBuf.readInt();
        Columns columns = Columns.values()[byteBuf.readInt()];
        String ret;
        try {
            ret = JDBCUtils.get(id, columns);
        }
        catch (Exception e) {
            e.printStackTrace();
            response.writeInt(MediaType.String.ordinal());
            String error = "column error";
            response.writeInt(error.getBytes().length);
            response.writeBytes(error.getBytes());
            return response;
        }
        if (columns.equals(Columns.picture)) {
            response.writeInt(MediaType.File.ordinal());
        }
        else {
            response.writeInt(MediaType.String.ordinal());
        }
        byte[] bytes = ret.getBytes();
        response.writeInt(bytes.length);
        response.writeBytes(bytes);
        return response;
    }
}
