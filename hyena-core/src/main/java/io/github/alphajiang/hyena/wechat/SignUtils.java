package io.github.alphajiang.hyena.wechat;



import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author shiyue
 */
public class SignUtils {

    /**
     * @param characterEncoding 编码格式 utf-8
     * @param apiKey  APIKey for the merchant
     * @return  生成的签名
     * */
    public static String creatSign(String characterEncoding,
                                   SortedMap<Object, Object> parameters, String apiKey) {
        StringBuffer sb = new StringBuffer();
        Set es = parameters.entrySet();
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            Object v = entry.getValue();
            if(null != v && !"".equals(v)
                    && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        //需要把该常量改成自己商务号的key值。原因是Api规定了签名必须加上自己的key值哦
        sb.append("key=" + apiKey);
//        String sign = MD5Utils.MD5Encode(sb.toString(), characterEncoding).toUpperCase();
        String sign = MD5Util.md5(sb.toString()).toUpperCase();
        System.out.println(sign);
        return sign;
    }
}
