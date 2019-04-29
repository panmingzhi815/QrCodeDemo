package org.pan;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.pan.CommonUtil.*;

public class QrCoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(QrCoder.class);

    public enum UserType{
        固定用户,临时用户,访客
    }

    public enum CardType{
        通卡,员工卡
    }

    public enum ValidModel{
        读头验证,云平台验证,控制器验证;
        public final byte[] ValidModelMapping = {(byte)0x80,(byte)0x40,(byte)0x20};
    }

    public String userId;
    public Date date;
    public UserType userType;
    public CardType cardType;
    public Integer validMinute;
    public Integer projectId;
    public ValidModel validModel;
    //不管为什么类型的二维码，该列表不能为空
    public List<Integer> deviceIds;

    public QrCoder(String userId, Date date, UserType userType, CardType cardType, Integer validMinute, Integer projectId, ValidModel validModel, List<Integer> deviceIds) {
        this.userId = userId;
        this.date = date;
        this.userType = userType;
        this.cardType = cardType;
        this.validMinute = validMinute;
        this.projectId = projectId;
        this.validModel = validModel;
        this.deviceIds = deviceIds;
    }

    public byte[] toBytes(byte[] key){
        ByteBuffer byteBuffer = ByteBuffer.allocate(512);
        byteBuffer.put(new byte[2]);

        byte[] userIdBytes = copyOf(hex2byte(userId),8);
        byteBuffer.put(userIdBytes);

        byte[] dateBytes = reverse(hex2byte(Long.toHexString(date.getTime() / 1000)));
        byteBuffer.put(dateBytes);

        byteBuffer.put((byte) userType.ordinal());
        byteBuffer.put((byte) cardType.ordinal());

        Preconditions.checkArgument(validMinute < 255 * 255,"二维码有效期最大不能超过:"+(255 * 255)+"分钟");
        byte[] validMinuteBytes = reverse(copyOf(hex2byte(Strings.padStart(Integer.toHexString(validMinute),4,'0')),2));
        byteBuffer.put(validMinuteBytes);

        Preconditions.checkArgument(projectId < 255 * 255,"项目最大不能超过:"+(255 * 255));
        byte[] projectIdBytes = reverse(copyOf(hex2byte(Strings.padStart(Integer.toHexString(projectId),4,'0')),2));
        byteBuffer.put(projectIdBytes);

        byteBuffer.put(validModel.ValidModelMapping[validModel.ordinal()]);

        byteBuffer.put(new byte[9]);

        Double virtualLength = Math.ceil(Collections.max(deviceIds)/128.0)*16;
        byte[] virtualLengthBytes = reverse(copyOf(hex2byte(Integer.toHexString(virtualLength.intValue())),2));
        byteBuffer.put(virtualLengthBytes);

        char[] virtualArr = new char[virtualLength.intValue() * 8];
        Arrays.fill(virtualArr,'0');
        deviceIds.forEach(deviceId -> virtualArr[deviceId-1] = '1');
        for (int i = 0; i < virtualLength; i++) {
            byteBuffer.put((byte) Integer.parseInt(arr2str(virtualArr,i*8,8),2));
        }

        byteBuffer.flip();
        byte[] result = new byte[byteBuffer.remaining()];
        byteBuffer.get(result);

        byte[] crc16Bytes = crc16(result, 2, result.length-2);
        System.arraycopy(crc16Bytes,0,result,0,2);

        byte[] aes256Encode = aes256Encode(Arrays.copyOfRange(result, 0, 32), key);
        System.arraycopy(aes256Encode,0,result,0,aes256Encode.length);

        return result;
    }


}
