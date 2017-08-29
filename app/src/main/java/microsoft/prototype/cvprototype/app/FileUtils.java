package microsoft.prototype.cvprototype.app;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();

    public static final String EYEGLASSES_FILE = "haarcascade_eye_tree_eyeglasses.xml";
    public static final String FACE_FILE = "haarcascade_frontalface_alt.xml";

    public static String getFilePath(Context context, String targetFile) {
        String filePath = "";
        Log.d(TAG, "targetFile: " + targetFile);

        try {
            String rootDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Temp";
            File rootDir = new File(rootDirPath);
            if (!rootDir.exists()) {
                rootDir.mkdir();
                Log.d(TAG, "rootDir created");
            }

            Log.d(TAG, "File rootDir getAbsolutePath: " + rootDirPath);

            File file = new File(rootDir, targetFile);
            if (!file.exists()) {
                file.createNewFile();
                Log.d(TAG, "File created");

                // write to file
                InputStream is = context.getAssets().open(targetFile);
                OutputStream os = new FileOutputStream(file, false);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = is.read(bytes)) != -1) {
                    os.write(bytes, 0, read);
                }
            }

            filePath = file.getAbsolutePath();
            Log.d(TAG, "Write file finished");
            Log.i(TAG, "File getAbsolutePath: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }
}
