package dlmj.hideseek.Util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Utils {

	public static String encode(InputStream in) {
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");
			byte[] bytes = new byte[8192];
			int byteCount;
			while ((byteCount = in.read(bytes)) > 0) {
				digester.update(bytes, 0, byteCount);
			}
			byte[] digest = digester.digest();

			// 将byte 转化为string

			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				int i = b & 0xff;// 16进制数据
				// 将16进制数据转化为string
				String hex = Integer.toHexString(i);

				// 1--->1---> 01
				// 16--> 10

				// 补位
				if (hex.length() == 1) {
					hex = 0 + hex;
				}

				sb.append(hex);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				in = null;
			}
		}
		return null;
	}

	public static String encode(String pwd) {
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");

			byte[] digest = digester.digest(pwd.getBytes());

			// 将byte 转化为string

			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				int i = b & 0xff;// 16进制数据
				// 将16进制数据转化为string
				String hex = Integer.toHexString(i);

				// 1--->1---> 01
				// 16--> 10

				// 补位
				if (hex.length() == 1) {
					hex = 0 + hex;
				}

				sb.append(hex);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String encodeU(String pwd) {
		// 0-9 + abc ---.52
		try {
			MessageDigest digester = MessageDigest.getInstance("MD5");

			byte[] digest = digester.digest(pwd.getBytes());

			// 将byte 转化为string

			StringBuffer sb = new StringBuffer();
			for (byte b : digest) {
				// byte -256 -- 255
				// 0 + 15
				int i = (b + 1) & 0xff ;// 16进制数据
				// 将16进制数据转化为string
				String hex = Integer.toHexString(i);

				// 1--->1---> 01
				// 16--> 10

				// 补位
				if (hex.length() == 1) {
					hex = 0 + hex;
				}

				sb.append(hex);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
