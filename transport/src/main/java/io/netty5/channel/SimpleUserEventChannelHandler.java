/*
 * Copyright 2018 The Netty Project
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

import io.netty5.util.Resource;
import io.netty5.util.internal.TypeParameterMatcher;

/**
 * {@link ChannelHandler} which allows to conveniently only handle a specific type of user events.
 *
 * For example, here is an implementation which only handle {@link String} user events.
 *
 * <pre>
 *     public class StringEventHandler extends
 *             {@link SimpleUserEventChannelHandler}&lt;{@link String}&gt; {
 *
 *         {@code @Override}
 *         protected void eventReceived({@link ChannelHandlerContext} ctx, {@link String} evt)
 *                 throws {@link Exception} {
 *             System.out.println(evt);
 *         }
 *     }
 * </pre>
 *
 * Be aware that depending of the constructor parameters it will release all handled events by passing them to
 * {@link Resource#dispose(Object)}.
 */
public abstract class SimpleUserEventChannelHandler<I> implements ChannelHandler {

    private final TypeParameterMatcher matcher;
    private final boolean autoRelease;

    /**
     * see {@link #SimpleUserEventChannelHandler(boolean)} with {@code true} as boolean parameter.
     */
    protected SimpleUserEventChannelHandler() {
        this(true);
    }

    /**
     * Create a new instance which will try to detect the types to match out of the type parameter of the class.
     *
     * @param autoRelease   {@code true} if handled events should be released automatically by passing them to
     *                      {@link Resource#dispose(Object)}.
     */
    protected SimpleUserEventChannelHandler(boolean autoRelease) {
        matcher = TypeParameterMatcher.find(this, SimpleUserEventChannelHandler.class, "I");
        this.autoRelease = autoRelease;
    }

    /**
     * see {@link #SimpleUserEventChannelHandler(Class, boolean)} with {@code true} as boolean value.
     */
    protected SimpleUserEventChannelHandler(Class<? extends I> eventType) {
        this(eventType, true);
    }

    /**
     * Create a new instance
     *
     * @param eventType      The type of events to match
     * @param autoRelease    {@code true} if handled events should be released automatically by passing them to
     *                       {@link Resource#dispose(Object)}.
     */
    protected SimpleUserEventChannelHandler(Class<? extends I> eventType, boolean autoRelease) {
        matcher = TypeParameterMatcher.get(eventType);
        this.autoRelease = autoRelease;
    }

    /**
     * Returns {@code true} if the given user event should be handled. If {@code false} it will be passed to the next
     * {@link ChannelHandler} in the {@link ChannelPipeline}.
     */
    protected boolean acceptEvent(Object evt) throws Exception {
        return matcher.match(evt);
    }

    @Override
    public final void channelInboundEvent(ChannelHandlerContext ctx, Object evt) throws Exception {
        boolean release = true;
        try {
            if (acceptEvent(evt)) {
                @SuppressWarnings("unchecked")
                I ievt = (I) evt;
                eventReceived(ctx, ievt);
            } else {
                release = false;
                ctx.fireChannelInboundEvent(evt);
            }
        } catch (Throwable throwable) {
            if (autoRelease && release) {
                try {
                    Resource.dispose(evt);
                } catch (Exception e) {
                    throwable.addSuppressed(e);
                }
            }
            throw throwable;
        }
        if (autoRelease && release) {
            Resource.dispose(evt);
        }
    }

    /**
     * Is called for each user event triggered of type {@link I}.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link SimpleUserEventChannelHandler} belongs to
     * @param evt the user event to handle
     *
     * @throws Exception is thrown if an error occurred
     */
    protected abstract void eventReceived(ChannelHandlerContext ctx, I evt) throws Exception;
}
