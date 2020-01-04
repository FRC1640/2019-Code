package frc.utilities;

import java.io.File;
import java.nio.file.Files;

public class FileUtil {

	public static String readFile (File file) {
		try { 
			return new String(Files.readAllBytes(file.toPath()), "utf-8");
		} catch (Exception e) { 
			LogUtil.error("FileUtil.readFile()", e.getMessage());
			e.printStackTrace();
			return "";
		}
	}

	public static String readFile (String path) {
		return readFile(new File(path));
	}

}