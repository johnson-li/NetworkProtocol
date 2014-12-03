import io.netty.buffer.ByteBuf;

/**
 * Created by johnson on 12/1/14.
 */
public interface OrderHandler {
    boolean check(ByteBuf byteBuf);

    ByteBuf operate(ByteBuf byteBuf);
}
