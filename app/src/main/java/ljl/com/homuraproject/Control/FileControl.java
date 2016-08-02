package ljl.com.homuraproject.Control;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by hzfd on 2016/8/1.
 */
public class FileControl {
    public static String getEncoding(File file) {
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            String codeType;
            if (is.markSupported()) {
                is.mark(4);
                byte[] first3bytes = new byte[3];
                is.read(first3bytes);
                is.reset();
                if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                        && first3bytes[2] == (byte) 0xBF) {// utf-8
                    codeType = "utf-8";
                } else if (first3bytes[0] == (byte) 0xFF
                        && first3bytes[1] == (byte) 0xFE) {
                    codeType = "unicode";
                } else if (first3bytes[0] == (byte) 0xFE
                        && first3bytes[1] == (byte) 0xFF) {
                    codeType = "utf-16be";
                } else if (first3bytes[0] == (byte) 0xFF
                        && first3bytes[1] == (byte) 0xFF) {
                    codeType = "utf-16le";
                } else {
                    codeType = "GBK";
                }
            } else {
                codeType = "GBK";
            }
            return codeType;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
