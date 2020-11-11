package io.github.alphajiang.hyena.wechat;


import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class WechatPayConnector {

    private static final Logger logger = LoggerFactory.getLogger(WechatPayConnector.class);

    private static String defaultUserPassword = "123456";

    @Value("${spring.wechat.mchAppid}")
    private String mchAppid;

    @Value("${spring.wechat.mchId}")
    private String mchId;

    @Value("${spring.wechat.spbillCreateIp}")
    private String spbillCreateIp;

    @Value("${spring.wechat.apiKey}")
    private String apiKey;

    @Value("${spring.wechat.certPath}")
    private String certPath;

    private static String payURl = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";


    public WechatPayConnector() {


    }

    @PostConstruct
    public void init() {


    }

    /**
     * Do not force name check, re_user_name not filled;
     *
     * @param openId openId 从微信中获得
     * @param amount 提现金额，以分为单位
     * @return 提现结果
     * @throws Exception 通用的错误
     */
    public String withdrawCash(String openId, String amount) throws Exception {

        SortedMap<Object, Object> params = new TreeMap<Object, Object>();
        params.put("mch_appid", mchAppid);
        params.put("mchid", mchId);
        params.put("nonce_str", generateUUID32());
        params.put("partner_trade_no", generateUUID32());
        params.put("openid", openId);
        params.put("check_name", "NO_CHECK");
        params.put("amount", amount);
        params.put("desc", "提现");
        params.put("spbill_create_ip", spbillCreateIp);

        String str = SignUtils.creatSign("utf-8", params, apiKey);
        params.put("sign", str);

        String xml = MapToXmlUtil.getRequestXml(params);

        String returnStr = doRefund(payURl, xml);
        System.out.println("=======================================");
        System.out.println(returnStr);

        return returnStr;
    }


    /**
     * @return UUID 32 digits, no "-"
     */
    public static String generateUUID32() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    public String doRefund(String url, String data) throws Exception {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource resource = resolver.getResource(certPath);
        InputStream instream = resource.getInputStream();

        /**
         * 注意PKCS12证书 是从微信商户平台-》账户设置-》 API安全 中下载的
         */

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        /**
         *此处要改
         *wxconfig.SSLCERT_PATH : 指向你的证书的绝对路径，带着证书去访问
         */

        try {
            /**
             * 此处要改
             *
             * 下载证书时的密码、默认密码是你的MCHID mch_id  1495130402
             * */

            keyStore.load(instream, mchId.toCharArray());//这里写密码
        } finally {
            instream.close();
        }

        // Trust own CA and all self-signed certs
        /**
         * 此处要改
         * 下载证书时的密码、默认密码是你的MCHID mch_id
         * */

        SSLContext sslcontext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, mchId.toCharArray())//这里也是写密码的
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        try {
            HttpPost httpost = new HttpPost(url); // 设置响应头信息
            httpost.addHeader("Connection", "keep-alive");
            httpost.addHeader("Accept", "*/*");
            httpost.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            httpost.addHeader("Host", "api.mch.weixin.qq.com");
            httpost.addHeader("X-Requested-With", "XMLHttpRequest");
            httpost.addHeader("Cache-Control", "max-age=0");
            httpost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
            httpost.setEntity(new StringEntity(data, "UTF-8"));
            CloseableHttpResponse response = httpclient.execute(httpost);
            try {
                HttpEntity entity = response.getEntity();

                String jsonStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                EntityUtils.consume(entity);
                return jsonStr;
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }


}
