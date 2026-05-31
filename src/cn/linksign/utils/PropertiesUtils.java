package cn.linksign.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

/**
 * @author wz
 *
 */
public class PropertiesUtils {
	private static Map<String, Properties> map = new HashMap<>();
	private static Map<String, Map<String, String>> promap = new HashMap<>();

	public static Properties loadProperties(String fileName) {
		if (map.containsKey(fileName)) {
			return map.get(fileName);
		}
		return readProperties(fileName);
	}

	public static Map<String, String> loadPropertiesToMap(String fileName) {
		if (promap.containsKey(fileName)) {
			return promap.get(fileName);
		}
		return readPropertiesToMap(fileName);
	}

	private synchronized static Map<String, String> readPropertiesToMap(String fileName) {
		Properties props = new Properties();
		FileInputStream fileInputStream = null;
		try {
			String path = PropertiesUtils.class.getClassLoader().getResource(fileName).getPath();
			path = path.replace("%20", " ");// 如果你的文件路径中包含空格，是必定会报错的
			File file = new File(path);
			fileInputStream = new FileInputStream(file);
			props.load(fileInputStream);
			Map<String, String> tempMap = new HashMap<>();
			for (Entry<Object, Object> entry : props.entrySet()) {
				tempMap.put(entry.getKey().toString(), entry.getValue().toString());
			}

			promap.put(fileName, tempMap);
			return tempMap;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
		return null;

	}

	private synchronized static Properties readProperties(String fileName) {
		Properties props = new Properties();
		FileInputStream fileInputStream = null;
		try {
			String path = PropertiesUtils.class.getClassLoader().getResource(fileName).getPath();
			path = path.replace("%20", " ");// 如果你的文件路径中包含空格，是必定会报错的
			File file = new File(path);
			fileInputStream = new FileInputStream(file);
			props.load(fileInputStream);
			map.put(fileName, props);
			return props;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fileInputStream);
		}
		return props;
	}

}
