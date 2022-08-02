/*
 * Copyright 2022 The Netty Project
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
package io.netty5.channel.socket.nio;

import io.netty5.channel.socket.DomainSocketAddress;
import io.netty5.channel.socket.SocketProtocolFamily;
import io.netty5.util.internal.PlatformDependent;
import io.netty5.util.internal.logging.InternalLogger;
import io.netty5.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ProtocolFamily;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.channels.Channel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Path;

final class NioChannelUtil {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioChannelUtil.class);

    private static final MethodHandle OF_METHOD_HANDLE;
    private static final MethodHandle GET_PATH_METHOD_HANDLE;

    static {
        MethodHandle ofMethodHandle = null;
        MethodHandle getPathMethodHandle = null;

        if (PlatformDependent.javaVersion() >= 16) {
            try {
                Class<?> clazz = Class.forName("java.net.UnixDomainSocketAddress", false,
                        PlatformDependent.getClassLoader(NioChannelUtil.class));
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                MethodType type = MethodType.methodType(clazz, String.class);
                ofMethodHandle = lookup.findStatic(clazz, "of", type);

                type = MethodType.methodType(Path.class);
                getPathMethodHandle = lookup.findVirtual(clazz, "getPath", type);
            } catch (ClassNotFoundException t) {
                ofMethodHandle = null;
                getPathMethodHandle = null;
                logger.debug(
                        "java.net.UnixDomainSocketAddress not found", t);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                ofMethodHandle = null;
                getPathMethodHandle = null;
                logger.debug(
                        "Could not access methods of java.net.UnixDomainSocketAddress", e);
            }
        }
        OF_METHOD_HANDLE = ofMethodHandle;
        GET_PATH_METHOD_HANDLE = getPathMethodHandle;
    }

    static boolean isDomainSocket(ProtocolFamily family) {
        if (family instanceof StandardProtocolFamily) {
            return "UNIX".equals(family.name());
        }
        if (family instanceof SocketProtocolFamily) {
            return family == SocketProtocolFamily.UNIX;
        }
        return false;
    }

    static SocketAddress toDomainSocketAddress(SocketAddress address) {
        if (GET_PATH_METHOD_HANDLE != null) {
            try {
                Path path = (Path) GET_PATH_METHOD_HANDLE.invoke(address);
                return new DomainSocketAddress(path.toFile());
            } catch (RuntimeException e) {
                throw e;
            } catch (Throwable cause) {
                return null;
            }
        }
        return address;
    }

    static SocketAddress toUnixDomainSocketAddress(SocketAddress address) {
        if (OF_METHOD_HANDLE != null) {
            if (address instanceof DomainSocketAddress) {
                try {
                    return (SocketAddress) OF_METHOD_HANDLE.invoke(((DomainSocketAddress) address).path());
                } catch (RuntimeException e) {
                    throw e;
                } catch (Throwable cause) {
                    return null;
                }
            }
        }
        return address;
    }

    static Method findOpenMethod(String methodName) {
        if (PlatformDependent.javaVersion() >= 15) {
            try {
                return SelectorProvider.class.getMethod(methodName, ProtocolFamily.class);
            } catch (Throwable e) {
                logger.debug("SelectorProvider.{}(ProtocolFamily) not available, will use default", methodName, e);
            }
        }
        return null;
    }

    static <C extends Channel> C newChannel(Method method, SelectorProvider provider,
                                            ProtocolFamily family) throws IOException {
        /*
         *  Use the {@link SelectorProvider} to open {@link SocketChannel} and so remove condition in
         *  {@link SelectorProvider#provider()} which is called by each SocketChannel.open() otherwise.
         *
         *  See <a href="https://github.com/netty/netty/issues/2308">#2308</a>.
         */
        if (family != null && method != null) {
            try {
                @SuppressWarnings("unchecked")
                C channel = (C) method.invoke(
                        provider, family);
                return channel;
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new IOException(e);
            }
        }
        return null;
    }

    static ProtocolFamily toJdkFamily(ProtocolFamily family) {
        if (family instanceof SocketProtocolFamily) {
            return ((SocketProtocolFamily) family).toJdkFamily();
        }
        return family;
    }

    private NioChannelUtil() { }
}
