package io.github.alphajiang.hyena.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class ResourceResolverDemo {

    private static final Logger logger = LoggerFactory.getLogger(HyenaMain.class);

    public static void main(String[] args) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        logger.info("starting......");

        String certPath = "apiclient_cert.p12";
        String mchId = "1564749331";
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
        logger.info("started");
    }
}
