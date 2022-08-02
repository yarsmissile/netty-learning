/*
 * Copyright 2012 The Netty Project
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

import io.netty5.buffer.api.BufferAllocator;
import io.netty5.util.AbstractConstant;
import io.netty5.util.ConstantPool;

import java.net.InetAddress;
import java.net.NetworkInterface;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ChannelOption} allows to configure a {@link Channel} in a type-safe
 * way. Which {@link ChannelOption} is supported depends on the actual implementation
 * of {@link Channel} and may depend on the nature of the transport it belongs
 * to.
 *
 * @param <T>   the type of the value which is valid for the {@link ChannelOption}
 */
public class ChannelOption<T> extends AbstractConstant<ChannelOption<T>> {

    private static final ConstantPool<ChannelOption<Object>> pool = new ConstantPool<>() {
        @Override
        protected ChannelOption<Object> newConstant(int id, String name) {
            return new ChannelOption<>(id, name);
        }
    };

    /**
     * Returns the {@link ChannelOption} of the specified name.
     */
    @SuppressWarnings("unchecked")
    public static <T> ChannelOption<T> valueOf(String name) {
        return (ChannelOption<T>) pool.valueOf(name);
    }

    /**
     * Shortcut of {@link #valueOf(String) valueOf(firstNameComponent.getName() + "#" + secondNameComponent)}.
     */
    @SuppressWarnings("unchecked")
    public static <T> ChannelOption<T> valueOf(Class<?> firstNameComponent, String secondNameComponent) {
        return (ChannelOption<T>) pool.valueOf(firstNameComponent, secondNameComponent);
    }

    /**
     * Returns {@code true} if a {@link ChannelOption} exists for the given {@code name}.
     */
    public static boolean exists(String name) {
        return pool.exists(name);
    }

    /**
     * Creates a new {@link ChannelOption} for the given {@code name} or fail with an
     * {@link IllegalArgumentException} if a {@link ChannelOption} for the given {@code name} exists.
     *
     * @deprecated use {@link #valueOf(String)}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> ChannelOption<T> newInstance(String name) {
        return (ChannelOption<T>) pool.newInstance(name);
    }

    public static final ChannelOption<BufferAllocator> BUFFER_ALLOCATOR = valueOf("BUFFER_ALLOCATOR");
    public static final ChannelOption<RecvBufferAllocator> RCVBUFFER_ALLOCATOR = valueOf("RCVBUFFER_ALLOCATOR");
    public static final ChannelOption<MessageSizeEstimator> MESSAGE_SIZE_ESTIMATOR = valueOf("MESSAGE_SIZE_ESTIMATOR");

    public static final ChannelOption<Integer> CONNECT_TIMEOUT_MILLIS = valueOf("CONNECT_TIMEOUT_MILLIS");
    /**
     * @deprecated Use {@link MaxMessagesRecvBufferAllocator}
     * and {@link MaxMessagesRecvBufferAllocator#maxMessagesPerRead(int)}.
     */
    @Deprecated
    public static final ChannelOption<Integer> MAX_MESSAGES_PER_READ = valueOf("MAX_MESSAGES_PER_READ");
    public static final ChannelOption<Integer> MAX_MESSAGES_PER_WRITE = valueOf("MAX_MESSAGES_PER_WRITE");

    public static final ChannelOption<Integer> WRITE_SPIN_COUNT = valueOf("WRITE_SPIN_COUNT");

    public static final ChannelOption<WriteBufferWaterMark> WRITE_BUFFER_WATER_MARK =
            valueOf("WRITE_BUFFER_WATER_MARK");

    public static final ChannelOption<Boolean> ALLOW_HALF_CLOSURE = valueOf("ALLOW_HALF_CLOSURE");
    public static final ChannelOption<Boolean> AUTO_READ = valueOf("AUTO_READ");

    /**
     * If {@code true} then the {@link Channel} is closed automatically and immediately on write failure.
     * The default value is {@code true}.
     */
    public static final ChannelOption<Boolean> AUTO_CLOSE = valueOf("AUTO_CLOSE");

    public static final ChannelOption<Boolean> SO_BROADCAST = valueOf("SO_BROADCAST");
    public static final ChannelOption<Boolean> SO_KEEPALIVE = valueOf("SO_KEEPALIVE");
    public static final ChannelOption<Integer> SO_SNDBUF = valueOf("SO_SNDBUF");
    public static final ChannelOption<Integer> SO_RCVBUF = valueOf("SO_RCVBUF");
    public static final ChannelOption<Boolean> SO_REUSEADDR = valueOf("SO_REUSEADDR");
    public static final ChannelOption<Integer> SO_LINGER = valueOf("SO_LINGER");
    public static final ChannelOption<Integer> SO_BACKLOG = valueOf("SO_BACKLOG");
    public static final ChannelOption<Integer> SO_TIMEOUT = valueOf("SO_TIMEOUT");

    public static final ChannelOption<Integer> IP_TOS = valueOf("IP_TOS");
    public static final ChannelOption<NetworkInterface> IP_MULTICAST_IF = valueOf("IP_MULTICAST_IF");
    public static final ChannelOption<Integer> IP_MULTICAST_TTL = valueOf("IP_MULTICAST_TTL");
    public static final ChannelOption<Boolean> IP_MULTICAST_LOOP_DISABLED = valueOf("IP_MULTICAST_LOOP_DISABLED");

    public static final ChannelOption<Boolean> TCP_NODELAY = valueOf("TCP_NODELAY");
    /**
     * Client-side TCP FastOpen. Sending data with the initial TCP handshake.
     */
    public static final ChannelOption<Boolean> TCP_FASTOPEN_CONNECT = valueOf("TCP_FASTOPEN_CONNECT");

    /**
     * Server-side TCP FastOpen. Configures the maximum number of outstanding (waiting to be accepted) TFO connections.
     */
    public static final ChannelOption<Integer> TCP_FASTOPEN = valueOf(ChannelOption.class, "TCP_FASTOPEN");

    public static final ChannelOption<Boolean> DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION =
            valueOf("DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION");

    /**
     * Creates a new {@link ChannelOption} with the specified unique {@code name}.
     */
    private ChannelOption(int id, String name) {
        super(id, name);
    }

    @Deprecated
    protected ChannelOption(String name) {
        this(pool.nextId(), name);
    }

    /**
     * Validate the value which is set for the {@link ChannelOption}. Sub-classes
     * may override this for special checks.
     */
    public void validate(T value) {
        requireNonNull(value, "value");
    }
}
