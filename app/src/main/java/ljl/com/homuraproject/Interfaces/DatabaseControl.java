package ljl.com.homuraproject.Interfaces;

import java.io.File;

/**
 * Created by hzfd on 2016/9/8.
 */
public interface DatabaseControl {
    String query(File targetFile);
    void insert(String targetFilePath, String targetLyricPath);
    void update(String targetFilePath, String targetLyricPath);
    void close();
    void open();
}
