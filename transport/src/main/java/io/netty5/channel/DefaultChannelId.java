/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty5.channel;

import io.netty5.buffer.BufferUtil;
import io.netty5.util.internal.MacAddressUtil;
import io.netty5.util.internal.SystemPropertyUtil;
import io.netty5.util.internal.logging.InternalLogger;
import io.netty5.util.internal.logging.InternalLoggerFactory;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty5.util.internal.MacAddressUtil.defaultMachineId;
import static io.netty5.util.internal.MacAddressUtil.parseMAC;

/**
 * The default {@link ChannelId} implementation.
 */
public final class DefaultChannelId implements ChannelId {

    private static final long serialVersionUID = 3884076183504074063L;

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultChannelId.class);
    private static final byte[] MACHINE_ID;
    private static final int PROCESS_ID_LEN = 4;
    private static final int PROCESS_ID;
    private static final int SEQUENCE_LEN = 4;
    private static final int TIMESTAMP_LEN = 8;
    private static final int RANDOM_LEN = 4;

    private static final AtomicInteger nextSequence = new AtomicInteger();

    /**
     * Returns a new {@link DefaultChannelId} instance.
     */
    public static DefaultChannelId newInstance() {
        return new DefaultChannelId();
    }

    static {
        int processId = -1;
        String customProcessId = SystemPropertyUtil.get("io.netty5.processId");
        if (customProcessId != null) {
            try {
                processId = Integer.parseInt(customProcessId);
            } catch (NumberFormatException e) {
                // Malformed input.
            }

            if (processId < 0) {
                processId = -1;
                logger.warn("-Dio.netty5.processId: {} (malformed)", customProcessId);
            } else if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty5.processId: {} (user-set)", processId);
            }
        }

        if (processId < 0) {
            processId = defaultProcessId();
            if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty5.processId: {} (auto-detected)", processId);
            }
        }

        PROCESS_ID = processId;

        byte[] machineId = null;
        String customMachineId = SystemPropertyUtil.get("io.netty5.machineId");
        if (customMachineId != null) {
            try {
                machineId = parseMAC(customMachineId);
            } catch (Exception e) {
                logger.warn("-Dio.netty5.machineId: {} (malformed)", customMachineId, e);
            }
            if (machineId != null) {
                logger.debug("-Dio.netty5.machineId: {} (user-set)", customMachineId);
            }
        }

        if (machineId == null) {
            machineId = defaultMachineId();
            if (logger.isDebugEnabled()) {
                logger.debug("-Dio.netty5.machineId: {} (auto-detected)", MacAddressUtil.formatAddress(machineId));
            }
        }

        MACHINE_ID = machineId;
    }

    private static int defaultProcessId() {
        long pid = ProcessHandle.current().pid();
        if (pid > Integer.MAX_VALUE || pid < Integer.MIN_VALUE) {
            logger.warn("Current process ID exceeds int range: " + pid);
            pid = ThreadLocalRandom.current().nextInt();
        }
        return (int) pid;
    }

    private final byte[] data;
    private final int hashCode;

    private transient String shortValue;
    private transient String longValue;

    private DefaultChannelId() {
        data = new byte[MACHINE_ID.length + PROCESS_ID_LEN + SEQUENCE_LEN + TIMESTAMP_LEN + RANDOM_LEN];
        int i = 0;

        // machineId
        System.arraycopy(MACHINE_ID, 0, data, i, MACHINE_ID.length);
        i += MACHINE_ID.length;

        // processId
        i = writeInt(i, PROCESS_ID);

        // sequence
        i = writeInt(i, nextSequence.getAndIncrement());

        // timestamp (kind of)
        i = writeLong(i, Long.reverse(System.nanoTime()) ^ System.currentTimeMillis());

        // random
        int random = ThreadLocalRandom.current().nextInt();
        i = writeInt(i, random);
        assert i == data.length;

        hashCode = Arrays.hashCode(data);
    }

    private int writeInt(int i, int value) {
        data[i ++] = (byte) (value >>> 24);
        data[i ++] = (byte) (value >>> 16);
        data[i ++] = (byte) (value >>> 8);
        data[i ++] = (byte) value;
        return i;
    }

    private int writeLong(int i, long value) {
        data[i ++] = (byte) (value >>> 56);
        data[i ++] = (byte) (value >>> 48);
        data[i ++] = (byte) (value >>> 40);
        data[i ++] = (byte) (value >>> 32);
        data[i ++] = (byte) (value >>> 24);
        data[i ++] = (byte) (value >>> 16);
        data[i ++] = (byte) (value >>> 8);
        data[i ++] = (byte) value;
        return i;
    }

    @Override
    public String asShortText() {
        String shortValue = this.shortValue;
        if (shortValue == null) {
            this.shortValue = shortValue = BufferUtil.hexDump(data, data.length - RANDOM_LEN, RANDOM_LEN);
        }
        return shortValue;
    }

    @Override
    public String asLongText() {
        String longValue = this.longValue;
        if (longValue == null) {
            this.longValue = longValue = newLongValue();
        }
        return longValue;
    }

    private String newLongValue() {
        StringBuilder buf = new StringBuilder(2 * data.length + 5);
        int i = 0;
        i = appendHexDumpField(buf, i, MACHINE_ID.length);
        i = appendHexDumpField(buf, i, PROCESS_ID_LEN);
        i = appendHexDumpField(buf, i, SEQUENCE_LEN);
        i = appendHexDumpField(buf, i, TIMESTAMP_LEN);
        i = appendHexDumpField(buf, i, RANDOM_LEN);
        assert i == data.length;
        return buf.substring(0, buf.length() - 1);
    }

    private int appendHexDumpField(StringBuilder buf, int i, int length) {
        buf.append(BufferUtil.hexDump(data, i, length));
        buf.append('-');
        i += length;
        return i;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public int compareTo(final ChannelId o) {
        if (this == o) {
            // short circuit
            return 0;
        }
        if (o instanceof DefaultChannelId) {
            // lexicographic comparison
            final byte[] otherData = ((DefaultChannelId) o).data;
            int len1 = data.length;
            int len2 = otherData.length;
            int len = Math.min(len1, len2);

            for (int k = 0; k < len; k++) {
                byte x = data[k];
                byte y = otherData[k];
                if (x != y) {
                    // treat these as unsigned bytes for comparison
                    return (x & 0xff) - (y & 0xff);
                }
            }
            return len1 - len2;
        }

        return asLongText().compareTo(o.asLongText());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DefaultChannelId)) {
            return false;
        }
        DefaultChannelId other = (DefaultChannelId) obj;
        return hashCode == other.hashCode && Arrays.equals(data, other.data);
    }

    @Override
    public String toString() {
        return asShortText();
    }
}
