
package cn.linksign.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

/**
 * 令牌处理器
 * 
 * 
 * 
 */
public class TokenProcessor {
	private static TokenProcessor instance = new TokenProcessor();

	private long previous;

	protected TokenProcessor() {
	}

	public static TokenProcessor getInstance() {
		return instance;
	}

	public synchronized String generateToken() {
		String value = "XIAOXINRANDOM" + System.currentTimeMillis() + new Random().nextInt() + "";
		// 获取数据指纹，指纹是唯一的
		try {
			MessageDigest md = MessageDigest.getInstance("md5");
			byte[] b = md.digest(value.getBytes());// 产生数据的指纹
			// Base64编码

			return toHex(b);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public synchronized String generateToken(String msg, boolean timeChange) {
		try {

			long current = System.currentTimeMillis();
			if (current == previous)
				current++;
			previous = current;
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(msg.getBytes());
			if (timeChange) {
				// byte now[] = (current+"").toString().getBytes();
				byte now[] = (new Long(current)).toString().getBytes();
				md.update(now);
			}
			return toHex(md.digest());
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	private String toHex(byte buffer[]) {
		StringBuffer sb = new StringBuffer(buffer.length * 2);
		for (int i = 0; i < buffer.length; i++) {
			sb.append(Character.forDigit((buffer[i] & 240) >> 4, 16));
			sb.append(Character.forDigit(buffer[i] & 15, 16));
		}

		return sb.toString();
	}

	public static String getUUID() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replaceAll("-", "");
		return uuid;
	}

	public static void main(String[] args) {
		System.out.println(TokenProcessor.getInstance().generateToken());
		System.out.println(getUUID());
	}

}