package org.pan;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

import static org.pan.CommonUtil.hex2byte;


public class QrCoderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QrCoderTest.class);
    public static final String DONGLU_QRCODE_KEY = "dongyun@#512809@AES#dlsv1.0";

    @Test
    public void test(){

        QrCoder qrCoder = new QrCoder("0000000046BE23F2", new Date(), QrCoder.UserType.固定用户, QrCoder.CardType.员工卡, 5, 1001, QrCoder.ValidModel.读头验证, Arrays.asList(1));

        ByteBuffer allocate = ByteBuffer.allocate(32);
        allocate.put(DONGLU_QRCODE_KEY.getBytes());
        byte[] bytes = qrCoder.toBytes(allocate.array());
        LOGGER.info("二维码内容:{}", hex2byte(bytes).toUpperCase());
    }

}