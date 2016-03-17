/**
 * MD5加密算法
 */
package org.esec.mcg.androidu2fsimulator.token;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MD5Encrypt {

	/** 编码 */
	private static String ENCODE = "UTF-8";
	
	/**
	 * MD5签名
	 * @param signSrc 排序后需签名的数据
	 * @param md5Key 签名的key
	 * @return MD5签名数据
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String sign(String signSrc, String md5Key) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		/********MD5签名*********/
		String signmd5Src = signSrc + "&KEY="+md5Key;
		String signmd5 = MD5Encrypt.getMessageDigest(signmd5Src);
		return signmd5;
	}
	
	
	/**
	 * 功能：MD5加密
	 * @param strSrc 加密的源字符串
	 * @return 加密串 长度32位(hex串)
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String getMessageDigest(String strSrc) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = null;
		String strDes = "";
		final String ALGO_MD5 = "MD5";

		byte[] bt = strSrc.getBytes(ENCODE);
		md = MessageDigest.getInstance(ALGO_MD5);
		md.update(bt);
		strDes = byte2hex(md.digest());
		return strDes;
	}
	
	/**
	 * 将字节数组转为HEX字符串(16进制串)
	 * 
	 * @param b 要转换的字节数组
	 * @return 转换后的HEX串
	 */
	public static String byte2hex(byte[] b) {
		char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] out = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			byte c = b[i];
			out[i * 2] = digit[(c >>> 4) & 0X0F];
			out[i * 2 + 1] = digit[c & 0X0F];
		}

		return new String(out);
	}

	
	// 参数签名（排序）(第一步）
	/**
	 * 
	 * @param str
	 *            需要排序的数据
	 * @return
	 */
	public static String signJsonStringSort(String str) {
		JSONArray ja = null;
		String endString = "";
		ArrayList<String> array = new ArrayList<String>();
		try {

			JSONObject newJo = new JSONObject(str);
			ja = newJo.names();

			for (int i = 0; i < ja.length(); i++) {
				array.add(ja.getString(i));
			}
			Collections.sort(array, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					return o1.compareToIgnoreCase(o2);
				}
			});

			for (int j = 0; j < array.size(); j++) {
				endString += array.get(j) + "=" + newJo.getString(array.get(j))
						+ "&";
				if (j == array.size() - 1) {
					endString = endString.substring(0,
							endString.lastIndexOf("&"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return endString;

	}
	
}
