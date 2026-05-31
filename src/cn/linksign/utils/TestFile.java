package cn.linksign.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;

public class TestFile {

	private static final int ENCRYPT = 0;
	private static final int DECRYPT = 1;
	private static final String key = "linksign867089d49f23488f9307012f6c1063d0";

	/**
	 * 文件处理方法 code为加密或者解密的判断条件 key 加密密钥
	 */
	private static void doFile(int code, File source, File target) throws Exception {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
		byte[] bytIn = new byte[(int) source.length()];
		bis.read(bytIn);
		bis.close();
		// AES加密
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, new SecureRandom(key.getBytes()));
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
		if (0 == code) {
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		} else if (1 == code) {
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		}
		// 写文件
		byte[] bytOut = cipher.doFinal(bytIn);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(target));
		bos.write(bytOut);
		bos.close();
	}

	public static File encryptFile(File sourceFile, String fileType) {
		File target = null;
		try {
			target = File.createTempFile("temp_", "." + fileType);
			doFile(ENCRYPT, sourceFile, target);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return target;

	}

	public static File decryptFile(File sourceFile, String fileType) {
		File target = null;
		try {
			target = File.createTempFile("temp_", "." + fileType);
			doFile(DECRYPT, sourceFile, target);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return target;
	}

	public static void main(String[] args) {
		try {
			File file = encryptFile(new File("E:/test/test.txt"), "txt");
			FileUtils.copyFile(file, new File("E:\\test\\tt1.txt"));

			File file2 = decryptFile(new File("E:/test/tt1.txt"), "txt");
			FileUtils.copyFile(file2, new File("E:\\test\\tt2.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
