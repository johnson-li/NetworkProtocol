import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by johnson on 12/1/14.
 */
public class AddOrderHandler implements OrderHandler{
    static Logger logger = LogManager.getLogger();
    static final byte[] HEADER = "add".getBytes();

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
        int fields = byteBuf.readInt();
        Map<Columns, String> map = new HashMap<Columns, String>();
        for (int i = 0; i < fields; i++) {
            Columns columns = Columns.values()[byteBuf.readInt()];
            int length = byteBuf.readInt();
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);
            map.put(columns, new String(bytes));
        }
        String ret;
        try {
            JDBCUtils.add(map);
            ret = "insert succeeded";
        }
        catch (Exception e) {
            e.printStackTrace();
            ret = "insert failed";
        }
        response.writeInt(MediaType.String.ordinal());
        response.writeInt(ret.getBytes().length);
        response.writeBytes(ret.getBytes());
        return response;
    }
}
