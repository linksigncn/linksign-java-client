package cn.linksign.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.crypto.stream.CryptoInputStream;
import org.apache.commons.crypto.stream.CryptoOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author wz
 *
 */
public class AESUtils {

	private static final SecretKeySpec key = new SecretKeySpec(getUTF8Bytes("linksign#%^*89d0"), "AES");
	private static final IvParameterSpec iv = new IvParameterSpec(getUTF8Bytes("linksign#%^*89d0"));
	private static final String transform = "AES/CBC/PKCS5Padding";

	/**
	 * 对文件进行AES加密
	 * 
	 * @param sourceFile
	 * @param fileType
	 * @param sKey
	 * @return
	 */
	public static File encryptFile(File sourceFile, String fileType) {
		Properties properties = new Properties();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try (CryptoOutputStream cos = new CryptoOutputStream(transform, properties, outputStream, key, iv)) {
			cos.write(getContent(sourceFile));
			cos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream fileOutputStream = null;
		try {
			File target = File.createTempFile("temp_", "." + fileType);
			fileOutputStream = new FileOutputStream(target);
			outputStream.writeTo(fileOutputStream);
			return target;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fileOutputStream);
			IOUtils.closeQuietly(outputStream);
		}
		return null;
	}

	/**
	 * AES方式解密文件
	 * 
	 * @param sourceFile
	 * @return
	 */
	public static File decryptFile(File sourceFile, String fileType) {
		Properties properties = new Properties();

		// Decryption with CryptoInputStream.
		InputStream inputStream = new ByteArrayInputStream(getContent(sourceFile));
		FileOutputStream fileOutputStream = null;

		try (CryptoInputStream cis = new CryptoInputStream(transform, properties, inputStream, key, iv)) {
			byte[] decryptedData = new byte[1024];
			int i;
			File target = File.createTempFile("temp_", "." + fileType);
			fileOutputStream = new FileOutputStream(target);
			while ((i = cis.read(decryptedData, 0, 1024)) > 0) {
				fileOutputStream.write(decryptedData, 0, i);
			}
			fileOutputStream.close();
			return target;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(fileOutputStream);
			IOUtils.closeQuietly(inputStream);
		}

		return null;

	}

	/**
	 * Converts String to UTF8 bytes
	 *
	 * @param input
	 *            the input string
	 * @return UTF8 bytes
	 */
	private static byte[] getUTF8Bytes(String input) {
		return input.getBytes(StandardCharsets.UTF_8);
	}

	private static byte[] getContent(File file) {
		try {
			long fileSize = file.length();
			FileInputStream fi = new FileInputStream(file);
			byte[] buffer = new byte[(int) fileSize];
			int offset = 0;
			int numRead = 0;
			while (offset < buffer.length && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
				offset += numRead;
			}
			fi.close();
			return buffer;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public static void main(String[] args) {
		File file = encryptFile(new File("E:\\test\\test.txt"), ".txt");
		try {
			FileUtils.copyFile(file, new File("E:\\test\\jami.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file2 = decryptFile(new File("E:\\test\\jami.txt"), ".txt");
		try {
			FileUtils.copyFile(file2, new File("E:\\test\\jiemi.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
