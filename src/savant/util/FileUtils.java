/*
 *    Copyright 2009-2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package savant.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility methods for manipulating general files.  Functions for manipulating Savant
 * files are in SavantFileUtils.
 *
 * @author mfiume, tarkvara
 */
public class FileUtils {
    public static void copyFile(File srcFile, File destFile) throws IOException {
        FileInputStream in = new FileInputStream(srcFile);

        //For Overwrite the file.
        FileOutputStream out = new FileOutputStream(destFile);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0 ){
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void copyDir(File srcDir, File destDir) throws IOException {
        File[] files = srcDir.listFiles();
        for (File f: files) {
            copyFile(f, new File(destDir, f.getName()));
        }
    }
}