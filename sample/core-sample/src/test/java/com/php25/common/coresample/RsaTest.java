package com.php25.common.coresample;

import com.php25.common.core.util.crypto.RSA;
import com.php25.common.core.util.crypto.constant.KeyType;
import com.php25.common.core.util.crypto.constant.RsaAlgorithm;
import com.php25.common.core.util.crypto.key.SecretKeyPair;
import com.php25.common.core.util.crypto.key.SecretKeyUtil;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;

/**
 * @author penghuiping
 * @date 2019/9/4 17:30
 */
public class RsaTest {

    private static final Logger log = LoggerFactory.getLogger(RsaTest.class);

    @Test
    public void test1() {
        testRSA(RsaAlgorithm.RSA_None);
        testRSA(RsaAlgorithm.RSA);
        testRSA(RsaAlgorithm.RSA_ECB_PKCS1);
    }


    private void testRSA(RsaAlgorithm rsaAlgorithm) {
        KeyPair pair = SecretKeyUtil.getRsaKey(rsaAlgorithm);
        Assert.assertNotNull(pair.getPrivate());
        Assert.assertNotNull(pair.getPublic());

        SecretKeyPair secretKeyPair = new SecretKeyPair(pair, rsaAlgorithm);
        log.info("publicKey:{}", secretKeyPair.getPublicKey());
        log.info("privateKey:{}", secretKeyPair.getPrivateKey());

        pair = secretKeyPair.toKeyPair();

        RSA rsa = new RSA(rsaAlgorithm, pair.getPrivate(), pair.getPublic());

        //私钥加密
        String secureInfo = rsa.encryptBase64("hello world，你好".getBytes(), KeyType.PrivateKey);

        //公钥解密
        String originInfo = rsa.decryptBase64Str(secureInfo, KeyType.PublicKey);

        log.info("origin_info:{}", originInfo);
        log.info("secure_info:{}", secureInfo);

        //公钥加密
        String secureInfo1 = rsa.encryptBase64("hello world，你好".getBytes(), KeyType.PublicKey);

        //私钥解密
        String originInfo1 = rsa.decryptBase64Str(secureInfo1, KeyType.PrivateKey);


        log.info("origin_info1:{}", originInfo1);
        log.info("secure_info1:{}", secureInfo1);
    }

}