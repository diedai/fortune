import com.kangyonggan.app.fortune.biz.service.FpayHelper;
import com.kangyonggan.app.fortune.common.util.FpayUtil;
import com.kangyonggan.app.fortune.model.constants.Resp;
import lombok.extern.log4j.Log4j2;

import java.io.OutputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author kangyonggan
 * @since 2017/5/6 0006
 */
@Log4j2
public class SocketTest {

    /**
     * 商户号
     */
    private static String merchCo = "201705050000001";

    /**
     * 对方公钥路径
     */
    private static String publicKeyPath = "E:/data/fpay/fpay/fpay_rsa_public_key_2048.pem";

    /**
     * 己方私钥路径
     */
    private static String privateKeyPath = "E:/data/fpay/merch/merch_pkcs8_rsa_private_key_2048.pem";

    public static void main(String[] args) throws Exception {
        // 公钥
        PublicKey publicKey = FpayUtil.getPublicKey(publicKeyPath);

        // 私钥
        PrivateKey privateKey = FpayUtil.getPrivateKey(privateKeyPath);

        // 报文
        String plain = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        log.info("请求报文明文:{}", plain);

        // 签名
        byte[] signBytes = FpayUtil.sign(plain, privateKey);
        log.info("请求报文签名数据长度:{}", signBytes.length);

        // 加密
        byte[] encryptedBytes = FpayUtil.encrypt(plain, publicKey);
        log.info("请求报文密文长度{}", encryptedBytes.length);

        byte bytes[] = FpayUtil.build("201705050000001", "K011", signBytes, encryptedBytes);

        Socket socket = new Socket("127.0.0.1", 8888);
        OutputStream out = socket.getOutputStream();

        out.write(bytes);

        out.flush();
        log.info("请求完毕");

        Map<String, Object> resMap = FpayUtil.parse(socket.getInputStream());
        // 解密
        String reqXml;
        try {
            reqXml = FpayUtil.decrypt((byte[]) resMap.get("encryptedBytes"), privateKey);
            log.info("报文解密后:\n{}", reqXml);
        } catch (Exception e) {
            log.warn(e.getMessage());
            return;
        }
    }

}
