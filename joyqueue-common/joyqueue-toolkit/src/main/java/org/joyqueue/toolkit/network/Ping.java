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
package org.joyqueue.toolkit.network;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络工具栏
 *
 * @author hexiaofeng
 */
public class Ping {

    /**
     * Linux的PING响应
     */
    public static final Pattern LINUX = Pattern.compile("icmp_seq=\\d+ ttl=\\d+ time=(.*?) ms");
    /**
     * Windows的PING响应
     */
    public static final Pattern WINDOWS = Pattern.compile("(bytes|字节)=\\d+ (time|时间)=(.*?)ms TTL=\\d+");

    // less than 1ms
    private static final Pattern WINDOWS_LESS = Pattern.compile("(bytes|字节)=\\d+ (time|时间)<(.*?)ms TTL=\\d+");

    /**
     * Ping IP
     *
     * @param ip    IP
     * @param count 次数
     * @return 平均响应时间
     * @throws IOException
     */
    public static double ping(final String ip, final int count) throws IOException {
        return ping(ip, count, -1, null);
    }

    /**
     * Ping IP,Linux普通用户时间间隔不能小于200毫秒
     *
     * @param ip       IP
     * @param count    次数
     * @param interval 间隔(毫秒)
     * @return 平均响应时间
     * @throws IOException
     */
    public static double ping(final String ip, final int count, final long interval) throws IOException {
        return ping(ip, count, interval, null);
    }

    /**
     * Ping IP,Linux普通用户时间间隔不能小于200毫秒
     *
     * @param ip       IP
     * @param count    次数
     * @param interval 间隔(毫秒)
     * @param output   输出信息
     * @return 平均响应时间
     * @throws IOException
     */
    public static double ping(final String ip, final int count, final long interval, final StringBuilder output) throws
            IOException {
        LineNumberReader input = null;
        double result = 0;
        int success = 0;
        try {
            // 根据操作系统构造ping命令
            String osName = System.getProperties().getProperty("os.name");
            String charset = System.getProperties().getProperty("sun.jnu.encoding");
            String pingCmd = null;
            boolean windows = osName.toUpperCase().startsWith("WINDOWS");
            if (windows) {
                pingCmd = "cmd /c ping -n {0} {1}";
                pingCmd = MessageFormat.format(pingCmd, count, ip);
            } else if (interval > 0) {
                pingCmd = "ping -c {0} -i {1} {2}";
                pingCmd = MessageFormat.format(pingCmd, count, interval * 1.0 / 1000, ip);
            } else {
                pingCmd = "ping -c {0} {1}";
                pingCmd = MessageFormat.format(pingCmd, count, ip);
            }
            // 执行ping命令
            Process process = Runtime.getRuntime().exec(pingCmd);
            // 读取输出
            input = new LineNumberReader(new InputStreamReader(process.getInputStream(), charset));
            String text;
            int lines = 0;
            // 循环读取输出
            while ((text = input.readLine()) != null) {
                // 输出
                if (output != null) {
                    if (lines++ > 0) {
                        output.append('\n');
                    }
                    output.append(text);
                }
                if (!text.isEmpty()) {
                    // 模式匹配
                    if (windows) {
                        Matcher matcher = WINDOWS.matcher(text);
                        if (matcher.find()) {
                            success++;
                            result += Double.valueOf(matcher.group(3));
                        } else {
                            matcher = WINDOWS_LESS.matcher(text);
                            if (matcher.find()) {
                                success++;
                                result += Double.valueOf(matcher.group(3));
                            }
                        }
                    } else {
                        Matcher matcher = LINUX.matcher(text);
                        if (matcher.find()) {
                            success++;
                            result += Double.valueOf(matcher.group(1));
                        }
                    }
                }
            }
            if (success > 0) {
                // 保留2位小数
                return Math.round(result * 100 / success) / 100.0;
                //return (double) Math.round(result * 100 / success) / 100; // or this
            }
            return -1;
        } finally {
            input.close();
        }
    }
}
