package kr.selfcontrol.selfwebfilter.util;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by owner on 2015-12-24.
 */
public class SelfControlUtil {
    public static String md5(String str){
        String MD5 = "";
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte byteData[] = md.digest();
            StringBuffer sb = new StringBuffer();
            for(int i = 0 ; i < byteData.length ; i++){
                sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
            }
            MD5 = sb.toString();

        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
            MD5 = null;
        }
        return MD5;
    }
    static String IV                = "1234567890123456"; //16bit

    public static String encode(String str)  {
        try {
            SecretKeySpec skeySpec=new SecretKeySpec(IV.getBytes(),"AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
            String enStr = new String(Base64.encode(encrypted, Base64.DEFAULT));
            return enStr;
        }catch(Exception exc){exc.printStackTrace();}
        return "ErrorError";
    }

    public static String decode(String str){
        try {
            SecretKeySpec skeySpec=new SecretKeySpec(IV.getBytes(),"AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, skeySpec);

            byte[] byteStr = Base64.decode(str.getBytes(), Base64.DEFAULT);
            return new String(c.doFinal(byteStr),"UTF-8");
        }catch(Exception exc){exc.printStackTrace();}
        return "ErrorError";
    }

    public static String remainOnlyKoreanAndEnglish(String str){
        StringBuffer sb=new StringBuffer();
        for(int i=0 ; i<str.length(); i++){
            char c=str.charAt(i);
            if((c>='\uAC00' && c<='\uD7AF') || (c>='\u1100' && c<='\u11FF') || (c>='\u3130' && c<='\u318F')){
                sb.append(c);
            } else if((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9') || (c=='=')){
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public static boolean isHangulHanjaJapaness(String str)
    {
        for(int i = 0 ; i < str.length() ; i++)
        {
            char ch = str.charAt(i);
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);
            if( //Character.UnicodeBlock.HANGUL_SYLLABLES.equals(unicodeBlock) ||
                //      Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO.equals(unicodeBlock) ||
                //        Character.UnicodeBlock.HANGUL_JAMO.equals(unicodeBlock) ||
                    Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock) ||
                            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(unicodeBlock) ||
                            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B.equals(unicodeBlock) ||
                            Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(unicodeBlock) ||
                            Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT.equals(unicodeBlock) ||
                            Character.UnicodeBlock.HIRAGANA.equals(unicodeBlock) ||
                            Character.UnicodeBlock.KATAKANA.equals(unicodeBlock) ||
                            Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS.equals(unicodeBlock)
                    ) return true;
        }
        return false;
    }
}
