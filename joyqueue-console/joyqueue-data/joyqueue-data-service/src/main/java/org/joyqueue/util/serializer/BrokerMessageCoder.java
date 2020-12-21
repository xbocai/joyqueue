/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.util.serializer;

import org.joyqueue.message.JoyQueueLog;
import org.joyqueue.message.BrokerMessage;
import org.joyqueue.message.Message;
import org.joyqueue.network.serializer.JoyQueueMapTools;
import org.joyqueue.toolkit.io.Compressors;
import org.joyqueue.toolkit.io.Zip;
import org.joyqueue.toolkit.io.ZipUtil;
import com.google.common.base.Charsets;
import org.joyqueue.toolkit.serialize.AbstractSerializer;
import io.netty.buffer.ByteBuf;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

public class BrokerMessageCoder extends AbstractSerializer {

    private static final byte BYTE_SIZE = 1;
    private static final byte SHORT_SIZE = 2;
    private static final byte INT_SIZE = 4;

    private static final int fixBodyLength = 4 // size
            + 2 // partition
            + 8 // index
            + 4
            + 2 // magic code
            + 1 // sys code
            + 1 // priority
            + 8 // send time
            + 4 // store time
            + 8; // crc
    //    public static final byte LONG_SIZE = 8;
//    public static final byte STRING_SIZE = 9;

    @Deprecated
    public static ByteBuf serialize(JoyQueueLog log, ByteBuf out) throws Exception {

        BrokerMessage message = (BrokerMessage) log;
        write(message, out);

        return out;
    }

    public static ByteBuffer serialize(JoyQueueLog log, ByteBuffer out, int size) throws Exception {

        BrokerMessage message = (BrokerMessage) log;
        write(message, out, size);
        return out;
    }

    public static int sizeOf(BrokerMessage msg) {

        int bodyLength = fixBodyLength;

        bodyLength += msg.getClientIp() == null ? 16 : msg.getClientIp().length;
        if (msg.getClientIp().length < 7){
            bodyLength += 10;
        }

        ByteBuffer buffer = msg.getBody();
        int length = buffer == null ? 0 : buffer.remaining();
        bodyLength += 4;
        bodyLength += length;

        byte[] bytes = getBytes(msg.getTopic(), Charsets.UTF_8);
        bodyLength += 1;
        bodyLength += bytes.length;
        bytes = getBytes(msg.getApp(), Charsets.UTF_8);
        bodyLength += 1;
        bodyLength += bytes.length;

        bytes = getBytes(msg.getBusinessId(), Charsets.UTF_8);
        bodyLength += 1;
        bodyLength += bytes.length;

        bytes = getBytes(msg.getTxId(), Charsets.UTF_8);
        bodyLength += 2;
        bodyLength += bytes.length;

        bytes = getBytes(toProperties(msg.getAttributes()), Charsets.UTF_8);
        bodyLength += 2;
        bodyLength += bytes.length;

        bytes = msg.getExtension();
        bodyLength += 4;
        if (bytes != null) {
            bodyLength += bytes.length;
        }

        return bodyLength;
    }

    /**
     * 写入存储消息
     *
     * @param message 存储消息
     * @param out     输出缓冲区
     * @throws Exception 序列化异常
     */
    public static void write(final BrokerMessage message, final ByteBuffer out, int size) throws Exception {

//        int size;
        if (out == null || message == null) {
            return;
        }
        // 记录写入的起始位置
        int begin = out.position();
        // 4个字节的消息长度需要计算出来
        out.putInt(size);
        // 分区
        out.putShort(message.getPartition());
        //消息序号
        out.putLong(message.getMsgIndexNo());
        // 任期
        out.putInt(message.getTerm());
        // 2个字节的魔法标识
        out.putShort(BrokerMessage.MAGIC_CODE);

        //   | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 |
        //   1个字节的系统字段 1-1：压缩标识 2-2：顺序消息 3-4: 消息来源，包括Jmq，kafka，mqtt 5-5：压缩算法 6-8：其他,预留未用
        byte sysCode = (byte) (message.isCompressed() ? 1 : 0);
        sysCode |= ((message.isOrdered() ? 1 : 0) << 1) & 0x3;

        sysCode |= (message.getSource() << 2) & 12;
        // compressor
        if (message.isCompressed()) {
            sysCode |= (message.getCompressionType().getType() << 4) & 48;
        }
        if (message.getClientIp().length < 7) {
            sysCode |= (1 << 5);
        }
        out.put(sysCode);
        // 1字节优先级
        out.put(message.getPriority());
        // 16字节的客户端地址
        out.put(message.getClientIp() == null ? new byte[16] : message.getClientIp());
        if (message.getClientIp().length < 7){
            out.put(new byte[10]);
        }
        // 8字节发送时间
        out.putLong(message.getStartTime());
        // 4字节存储时间（相对发送时间的偏移）
        out.putInt(0);
        // 8字节消息体CRC
        out.putLong(message.getBodyCRC());

        // 4字节消息体大小
        // 消息体（字节数组）
        if (message.getByteBody() != null) {
            write(message.getBody(), out, true);
        } else {
            out.putInt(0);
        }

        // 1字节主题长度
        // 主题（字节数组）
        write(message.getTopic(), out);
        // 1字节应用长度
        // 应用（字节数组）
        write(message.getApp(), out);
        // 1字节业务ID长度
        // 业务ID（字节数组）
        write(message.getBusinessId(), out);
        write(message.getTxId(), out, SHORT_SIZE);
        // 2字节属性长度
        // 属性（字节数组）
        write(toProperties(message.getAttributes()), out, 2);
        // 4字节扩展字段大小
        // 扩展字段（字节数组）
        write(message.getExtension(), out, true);

        // 重写总长度
//        int end = out.position();
//        size = end - begin;
        message.setSize(size);
//        out.position(begin);
//        out.putInt(size);
        out.flip();
//        out.position(end);
    }

    /**
     * 写入存储消息
     *
     * @param messages 存储消息
     * @param out      输出缓冲区
     * @throws java.lang.Exception
     */
    public static void write(final BrokerMessage[] messages, final ByteBuf out) throws Exception {
        if (out == null) {
            return;
        }

        int count = messages == null ? 0 : messages.length;
        out.writeShort(count);

        for (int i = 0; i < count; i++) {
            write(messages[i], out);
        }
    }



    /**
     * 写入存储消息
     *
     * @param message 存储消息
     * @param out     输出缓冲区
     * @throws Exception 序列化异常
     */
    @Deprecated
    public static void write(final BrokerMessage message, final ByteBuf out) throws Exception {
        int size;
        if (out == null || message == null) {
            return;
        }
        // 记录写入的起始位置
        int begin = out.writerIndex();
        // 4个字节的消息长度需要计算出来
        out.writeInt(0);
        // 2个字节的魔法标识
        out.writeShort(message.getPartition());
        //消息序号
        out.writeLong(message.getMsgIndexNo());
        out.writeInt(message.getTerm());
        out.writeShort(BrokerMessage.MAGIC_CODE);

        //   | 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 |
        //   1个字节的系统字段 1-1：压缩标识 2-2：顺序消息 3-4: 消息来源，包括Jmq，kafka，mqtt 5-5：压缩算法 6-8：其他,预留未用
        byte sysCode = (byte) (message.isCompressed() ? 1 : 0);
        sysCode |= ((message.isOrdered() ? 1 : 0) << 1) & 0x3;


        sysCode |= (message.getSource() << 2) & 12;
        // compressor
        if (message.isCompressed()) {
            sysCode |= (message.getCompressionType().getType() << 4) & 48;
        }
        out.writeByte(sysCode);
        // 1字节优先级
        out.writeByte(message.getPriority());
        // 6字节的客户端地址
        byte[] clientIp = message.getClientIp();
        if (clientIp != null) {
            out.writeBytes(message.getClientIp());
            if (message.getClientIp().length == 6){
                out.writeBytes(new byte[10]);
            }
        } else {
            out.writeBytes(new byte[16]);
        }

        // 8字节发送时间
        out.writeLong(message.getStartTime());
        // 4字节存储时间（相对发送时间的偏移）
        out.writeInt(0);
        // 8字节消息体CRC
        out.writeLong(message.getBodyCRC());

        // 4字节消息体大小
        // 消息体（字节数组）
        if (message.getByteBody() != null) {
            write(message.getBody(), out);
        } else {
            out.writeInt(0);
        }

        // 1字节主题长度
        // 主题（字节数组）
        write(message.getTopic(), out);
        // 1字节应用长度
        // 应用（字节数组）
        write(message.getApp(), out);
        // 1字节业务ID长度
        // 业务ID（字节数组）
        write(message.getBusinessId(), out);
        write(message.getTxId(), out, SHORT_SIZE);
        // 2字节属性长度
        // 属性（字节数组）
        write(toProperties(message.getAttributes()), out, 2);
        // 4字节扩展字段大小
        // 扩展字段（字节数组）
        write(message.getExtension(), out);

        // 重写总长度
        int end = out.writerIndex();
        size = end - begin;
        message.setSize(size);
        out.writerIndex(begin);
        out.writeInt(size);
        out.writerIndex(end);
    }

    /**
     * 读取存储的消息
     *
     * @param in 输入缓冲区
     */
    public static BrokerMessage readBrokerMessage(final ByteBuf in) throws Exception {
        if (in == null) {
            return null;
        }
        BrokerMessage message = new BrokerMessage();

        // 4个字节的消息长度
        int totalLength = in.readInt();

        message.setPartition(in.readShort());
        message.setMsgIndexNo(in.readLong());
        message.setTerm(in.readInt());
        in.readShort();

        byte sysCode = in.readByte();

        message.setCompressed(((sysCode & 0x1) > 0));
        message.setOrdered(((sysCode & 0x2) > 0));
        message.setSource((byte) (sysCode >> 2 & 0x3));
        message.setCompressionType(Message.CompressionType.valueOf(sysCode >> 4 & 0x3));

        message.setPriority(in.readByte());
        if ((sysCode | 1 << 5) > 0){
            message.setClientIp(readBytes(in, 6));
            readBytes(in, 10);
        }else {
            message.setClientIp(readBytes(in, 16));
        }


        message.setStartTime(in.readLong());
        message.setStoreTime(in.readInt());
        message.setBodyCRC(in.readLong());

        int bodyLength = in.readInt();
        message.setBody(readBytes(in, bodyLength));
        message.setTopic(readString(in));
        message.setApp(readString(in));
        message.setBusinessId(readString(in));
        message.setTxId(readString(in, SHORT_SIZE));

        message.setAttributes(toStringMap(readString(in, 2)));

        int extensionLength = in.readInt();
        message.setExtension(readBytes(in, extensionLength));


        return message;
    }


    /**
     * 读取存储的消息
     *
     * @param in 输入缓冲区
     */
    public static BrokerMessage readBrokerMessage(final ByteBuffer in) throws Exception {
        if (in == null) {
            return null;
        }
        BrokerMessage message = new BrokerMessage();

        // 4个字节的消息长度
        int totalLength = in.getInt();

        message.setPartition(in.getShort());
        message.setMsgIndexNo(in.getLong());
        message.setTerm(in.getInt());
        in.getShort();

        byte sysCode = in.get();

        message.setCompressed(((sysCode & 0x1) > 0));
        message.setOrdered(((sysCode & 0x2) > 0));
        message.setSource((byte) (sysCode >> 2 & 0x3));
        message.setCompressionType(Message.CompressionType.valueOf(sysCode >> 4 & 0x3));

        message.setPriority(in.get());
        if ((sysCode | 1 << 5) > 0){
            message.setClientIp(readBytes(in, 6));
            readBytes(in, 10);
        }else {
            message.setClientIp(readBytes(in, 16));
        }


        message.setStartTime(in.getLong());
        message.setStoreTime(in.getInt());
        message.setBodyCRC(in.getLong());

        int bodyLength = in.getInt();
        message.setBody(readBytes(in, bodyLength));
        message.setTopic(readString(in));
        message.setApp(readString(in));
        message.setBusinessId(readString(in));
        message.setTxId(readString(in, SHORT_SIZE));

        message.setAttributes(toStringMap(readString(in, 2)));

        int extensionLength = in.getInt();
        message.setExtension(readBytes(in, extensionLength));

        return message;
    }


    /**
     * 写字符串
     *
     * @param value      字符串
     * @param out        输出缓冲区
     * @param lengthSize 长度字节数
     * @throws Exception 序列化异常
     */
    public static void write(final String value, final ByteBuffer out, final int lengthSize) throws Exception {
        write(value, out, lengthSize, false);
    }

    /**
     * 写字符串
     *
     * @param value      字符串
     * @param out        输出缓冲区
     * @param lengthSize 长度字节数
     * @throws Exception 序列化异常
     */
    @Deprecated
    public static void write(final String value, final ByteBuf out, final int lengthSize) throws Exception {
        write(value, out, lengthSize, false);
    }

    /**
     * 写字符串(长度<=255)
     *
     * @param value 字符串
     * @param out   输出缓冲区
     * @throws Exception 序列化异常
     */
    @Deprecated
    public static void write(final String value, final ByteBuf out) throws Exception {
        write(value, out, 1, false);
    }

    public static void write(final String value, final ByteBuffer out) throws Exception {
        write(value, out, 1, false);
    }

    /**
     * 写字符串
     *
     * @param value      字符串
     * @param out        输出缓冲区
     * @param lengthSize 长度字节数
     * @param compressed 是否进行压缩
     * @throws Exception 序列化异常
     */
    @Deprecated
    public static void write(final String value, final ByteBuf out, final int lengthSize,
                             final boolean compressed) throws Exception {
        if (out == null) {
            return;
        }
        if (value != null && !value.isEmpty()) {
            byte[] bytes = getBytes(value, Charsets.UTF_8);
            if (compressed) {
                bytes = Compressors.compress(bytes, 0, bytes.length, Zip.INSTANCE);
            }
            write(bytes.length, out, lengthSize);
            out.writeBytes(bytes);
        } else {
            write(0, out, lengthSize);
        }
    }

    /**
     * 写字符串
     *
     * @param value      字符串
     * @param out        输出缓冲区
     * @param lengthSize 长度字节数
     * @param compressed 是否进行压缩
     * @throws Exception 序列化异常
     */
    public static void write(final String value, final ByteBuffer out, final int lengthSize,
                             final boolean compressed) throws Exception {
        if (out == null) {
            return;
        }
        if (value != null && !value.isEmpty()) {
            byte[] bytes = getBytes(value, Charsets.UTF_8);
            if (compressed) {
                bytes = Compressors.compress(bytes, 0, bytes.length, Zip.INSTANCE);
            }
            write(bytes.length, out, lengthSize);
            out.put(bytes);
        } else {
            write(0, out, lengthSize);
        }
    }

    /**
     * 写整数
     *
     * @param value      整数
     * @param out        输出
     * @param lengthSize 长度字节数
     */
    @Deprecated
    public static void write(final int value, final ByteBuf out, final int lengthSize) {
        if (out == null) {
            return;
        }
        switch (lengthSize) {
            case BYTE_SIZE:
                out.writeByte(value);
                break;
            case SHORT_SIZE:
                out.writeShort(value);
                break;
            case INT_SIZE:
                out.writeInt(value);
                break;
        }
    }

    /**
     * 写整数
     *
     * @param value      整数
     * @param out        输出
     * @param lengthSize 长度字节数
     */
    public static void write(final int value, final ByteBuffer out, final int lengthSize) {
        if (out == null) {
            return;
        }
        switch (lengthSize) {
            case BYTE_SIZE:
                out.put((byte) value);
                break;
            case SHORT_SIZE:
                out.putShort((short) value);
                break;
            case INT_SIZE:
                out.putInt(value);
                break;
        }
    }

    /**
     * 写数据
     *
     * @param value 数据源
     * @param out   输出缓冲区
     */
    public static void write(final byte[] value, final ByteBuf out) {
        ByteBuffer wrap = ByteBuffer.wrap(value);
        write(wrap, out, true);
    }

    /**
     * 写数据
     *
     * @param value 数据源
     * @param out   输出缓冲区
     */
    public static void write(final ByteBuffer value, final ByteBuf out) {
        write(value, out, true);
    }

    /**
     * 写数据
     *
     * @param value       数据源
     * @param out         输出缓冲区
     * @param writeLength 是否写长度
     */
    public static void write(final ByteBuffer value, final ByteBuf out, final boolean writeLength) {
        int length = value == null ? 0 : value.remaining();
        if (writeLength) {
            out.writeInt(length);
        }
        if (length > 0) {
            if (value.hasArray()) {
                out.writeBytes(value.array(), value.arrayOffset() + value.position(), value.remaining());
            } else {
                out.writeBytes(value.slice());
            }
        }
    }

    /**
     * 写数据
     *
     * @param value       数据源
     * @param out         输出缓冲区
     * @param writeLength 是否写长度
     */
    public static void write(final byte[] value, final ByteBuffer out, final boolean writeLength) {
        ByteBuffer wrap = null;
        if (value != null) {
            wrap = ByteBuffer.wrap(value);
        }
        write(wrap, out, writeLength);
    }

    /**
     * 写数据
     *
     * @param value       数据源
     * @param out         输出缓冲区
     * @param writeLength 是否写长度
     */
    public static void write(final ByteBuffer value, final ByteBuffer out, final boolean writeLength) {
        int length = value == null ? 0 : value.remaining();
        if (writeLength) {
            out.putInt(length);
        }
        if (length > 0) {
            if (value.hasArray()) {
                out.put(value.array(), value.arrayOffset() + value.position(), value.remaining());
            } else {
                out.put(value.slice());
            }
        }
    }

    /**
     * 写入map数据
     *
     * @param hashMap map对象
     * @param out     序列化流
     * @throws Exception 序列化/反序列化错误
     */
    @Deprecated
    public static <K, V> void write(final Map<K, V> hashMap, ByteBuf out) throws Exception {
        JoyQueueMapTools.write(hashMap, out);
    }


    /**
     * 读取字符串，字符长度<=255
     *
     * @param in 输入缓冲区
     * @return 返回值 字符串
     * @throws Exception 序列化异常
     */
    @Deprecated
    public static String readString(final ByteBuf in) throws Exception {
        return readString(in, 1, false);
    }

    /**
     * 读取字符串，前面有一个字符串长度字节
     *
     * @param in         输入缓冲区
     * @param lengthSize 长度大小
     * @param compressed 压缩标示
     * @return 返回值 字符串
     * @throws Exception 序列化异常
     */
    @Deprecated
    public static String readString(final ByteBuf in, final int lengthSize, final boolean compressed) throws Exception {
        int length;
        if (lengthSize == 1) {
            length = in.readUnsignedByte();
        } else if (lengthSize == 2) {
            length = in.readUnsignedShort();
        } else {
            length = in.readInt();
        }
        return read(in, length, compressed, "UTF-8");
    }

    /**
     * 读取字符串
     *
     * @param in         输入缓冲区
     * @param length     长度
     * @param compressed 压缩
     * @param charset    字符集
     * @return 返回值 字符串
     * @throws Exception 序列化/反序列化错误
     */
    @Deprecated
    public static String read(final ByteBuf in, final int length, final boolean compressed, String charset) throws
            Exception {
        if (length <= 0) {
            return null;
        }

        byte[] bytes = readBytes(in, length);
        try {
            if (compressed) {
                bytes = ZipUtil.decompressByZlib(bytes, 0, bytes.length);
            }

            if (charset == null || charset.isEmpty()) {
                charset = "UTF-8";
            }
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    /**
     * 读取字节数
     *
     * @param in     输入缓冲区
     * @param length 长度
     */
    @Deprecated
    private static byte[] readBytes(final ByteBuf in, final int length) {
        if (in == null || length <= 0) {
            return new byte[0];
        }
        int len = in.readableBytes();
        if (len == 0) {
            return new byte[0];
        }
        if (length < len) {
            len = length;
        }

        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        return bytes;
    }

    /**
     * 读取字符串
     *
     * @param in         输入缓冲区
     * @param lengthSize 长度大小
     * @return 返回值 字符串
     * @throws Exception 序列化异常
     */
    @Deprecated
    public static String readString(final ByteBuf in, final int lengthSize) throws Exception {
        return readString(in, lengthSize, false);
    }

}
