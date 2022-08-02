/*
 * Copyright 2016 The Netty Project
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

public interface ChannelInboundInvoker {

    /**
     * A {@link Channel} was registered to its {@link EventLoop}.
     *
     * This will result in having the  {@link ChannelHandler#channelRegistered(ChannelHandlerContext)} method
     * called of the next  {@link ChannelHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelRegistered();

    /**
     * A {@link Channel} was unregistered from its {@link EventLoop}.
     *
     * This will result in having the  {@link ChannelHandler#channelUnregistered(ChannelHandlerContext)} method
     * called of the next  {@link ChannelHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelUnregistered();

    /**
     * A {@link Channel} is active now, which means it is connected.
     *
     * This will result in having the  {@link ChannelHandler#channelActive(ChannelHandlerContext)} method
     * called of the next  {@link ChannelHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelActive();

    /**
     * A {@link Channel} is inactive now, which means it is closed.
     *
     * This will result in having the  {@link ChannelHandler#channelInactive(ChannelHandlerContext)} method
     * called of the next  {@link ChannelHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelInactive();

    /**
     * A {@link Channel} was shutdown in a specific direction.
     *
     * This will result in having the
     * {@link ChannelHandler#channelShutdown(ChannelHandlerContext, ChannelShutdownDirection)}  method
     * called of the next  {@link ChannelHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelShutdown(ChannelShutdownDirection direction);

    /**
     * A {@link Channel} received an {@link Throwable} in one of its inbound operations.
     *
     * This will result in having the  {@link ChannelHandler#channelExceptionCaught(ChannelHandlerContext, Throwable)}
     * method  called of the next  {@link ChannelHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelExceptionCaught(Throwable cause);

    /**
     * A {@link Channel} received a custom defined inbound event.
     *
     * This will result in having the {@link ChannelHandler#channelInboundEvent(ChannelHandlerContext, Object)}
     * method  called of the next {@link ChannelHandler} contained in the {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelInboundEvent(Object event);

    /**
     * A {@link Channel} received a message.
     *
     * This will result in having the {@link ChannelHandler#channelRead(ChannelHandlerContext, Object)}
     * method  called of the next {@link ChannelHandler} contained in the  {@link ChannelPipeline} of the
     * {@link Channel}.
     */
    ChannelInboundInvoker fireChannelRead(Object msg);

    /**
     * Triggers an {@link ChannelHandler#channelReadComplete(ChannelHandlerContext)}
     * event to the next {@link ChannelHandler} in the {@link ChannelPipeline}.
     */
    ChannelInboundInvoker fireChannelReadComplete();

    /**
     * Triggers an {@link ChannelHandler#channelWritabilityChanged(ChannelHandlerContext)}
     * event to the next {@link ChannelHandler} in the {@link ChannelPipeline}.
     */
    ChannelInboundInvoker fireChannelWritabilityChanged();
}
