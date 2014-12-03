import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

/**
 * Created by johnson on 12/3/14.
 */
public class JDBCTest {
    static Logger logger = LogManager.getLogger();
    public static void main(String args[]) {
        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
//                        logger.debug(JDBCUtils.getPicturePath(1));
                        JDBCUtils.setPicture(1, Paths.get("/home/johnson/Pictures/sell.png"));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }
}
