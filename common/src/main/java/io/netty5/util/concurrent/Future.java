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
package io.netty5.util.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * The result of an asynchronous operation.
 * <p>
 * An asynchronous operation is one that might be completed outside a given thread of execution. The operation can
 * either be performing computation, or I/O, or both.
 * <p>
 * All I/O operations in Netty are asynchronous. It means any I/O calls will return immediately with no guarantee that
 * the requested I/O operation has been completed at the end of the call. Instead, you will be returned with a {@link
 * Future} instance which gives you the information about the result or status of the I/O operation.
 * <p>
 * A {@link Future} is either <em>uncompleted</em> or <em>completed</em>. When an I/O operation begins, a new future
 * object is created. The new future is uncompleted initially - it is neither succeeded, failed, nor cancelled because
 * the I/O operation is not finished yet. If the I/O operation is finished either successfully, with failure, or by
 * cancellation, the future is marked as completed with more specific information, such as the cause of the failure.
 * Please note that even failure and cancellation belong to the completed state.
 * <pre>
 *                                      +---------------------------+
 *                                      | Completed successfully    |
 *                                      +---------------------------+
 *                                 +---->      isDone() = true      |
 * +--------------------------+    |    |   isSuccess() = true      |
 * |        Uncompleted       |    |    +===========================+
 * +--------------------------+    |    | Completed with failure    |
 * |      isDone() = false    |    |    +---------------------------+
 * |   isSuccess() = false    |----+---->      isDone() = true      |
 * | isCancelled() = false    |    |    |       cause() = non-null  |
 * |       cause() = throws   |    |    +===========================+
 * |      getNow() = throws   |    |    | Completed by cancellation |
 * +--------------------------+    |    +---------------------------+
 *                                 +---->      isDone() = true      |
 *                                      | isCancelled() = true      |
 *                                      +---------------------------+
 * </pre>
 * <p>
 * Various methods are provided to let you check if the I/O operation has been completed, wait for the completion, and
 * retrieve the result of the I/O operation. It also allows you to add {@link FutureListener}s so you can get notified
 * when the I/O operation is completed.
 *
 * <h3>Prefer {@link #addListener(FutureListener)} to {@link FutureCompletionStage#await()}</h3>
 * <p>
 * It is recommended to prefer {@link #addListener(FutureListener)}, or {@link #addListener(Object,
 * FutureContextListener)}, to {@link FutureCompletionStage#await()} wherever possible to get notified when an I/O
 * operation is done and to do any follow-up tasks.
 * <p>
 * The {@link #addListener(FutureListener)} method is non-blocking. It simply adds the specified {@link FutureListener}
 * to the {@link Future}, and the I/O thread will notify the listeners when the I/O operation associated with the future
 * is done. The {@link FutureListener} and {@link FutureContextListener} callbacks yield the best performance and
 * resource utilization because it does not block at all, but it could be tricky to implement a sequential logic if you
 * are not used to event-driven programming.
 * <p>
 * By contrast, {@link FutureCompletionStage#await()} is a blocking operation. Once called, the caller thread blocks
 * until the operation is done. It is easier to implement a sequential logic with {@link FutureCompletionStage#await()},
 * but the caller thread blocks unnecessarily until the I/O operation is done and there's relatively expensive cost of
 * inter-thread notification. Moreover, there's a chance of deadlock in a particular circumstance, which is
 * described below.
 *
 * <h3>Do not call {@link FutureCompletionStage#await()} inside a {@link io.netty5.channel.ChannelHandler}</h3>
 * <p>
 * The event handler methods in {@link io.netty5.channel.ChannelHandler} are usually called by an I/O thread.
 * If {@link FutureCompletionStage#await()} is called by an event handler method, which is called by the I/O thread,
 * the I/O operation it is waiting for might never complete because {@link FutureCompletionStage#await()} can block
 * the I/O operation it is waiting for, which is a deadlock.
 * <pre>
 * // BAD - NEVER DO THIS
 * {@code @Override}
 * public void channelRead({@link io.netty5.channel.ChannelHandlerContext} ctx, Object msg) {
 *     {@link Future} future = ctx.channel().close();
 *     future.asStage().await();
 *     // Perform post-closure operation
 *     // ...
 * }
 *
 * // GOOD
 * {@code @Override}
 * public void channelRead({@link io.netty5.channel.ChannelHandlerContext} ctx, Object msg) {
 *     {@link Future} future = ctx.channel().close();
 *     future.addListener(new {@link FutureListener}() {
 *         public void operationComplete({@link Future} future) {
 *             // Perform post-closure operation
 *             // ...
 *         }
 *     });
 * }
 * </pre>
 * <p>
 * In spite of the disadvantages mentioned above, there are certainly the cases where it is more convenient to call
 * {@link FutureCompletionStage#await()}. In such a case, please make sure you do not call
 * {@link FutureCompletionStage#await()} in an I/O thread. Otherwise, {@link BlockingOperationException} will be
 * raised to prevent a deadlock.
 *
 * <h3>Do not confuse I/O timeout and await timeout</h3>
 * <p>
 * The timeout value you specify with {@link FutureCompletionStage#await(long, TimeUnit)} is not related to the
 * I/O timeout at all.
 * If an I/O operation times out, the future will be marked as 'completed with failure,' as depicted in the
 * diagram above.  For example, connect timeout should be configured via a transport-specific option:
 * <pre>
 * // BAD - NEVER DO THIS
 * {@link io.netty5.bootstrap.Bootstrap} b = ...;
 * {@link Future} f = b.connect(...);
 * f.asStage().await(10, TimeUnit.SECONDS);
 * if (f.isCancelled()) {
 *     // Connection attempt cancelled by user
 * } else if (!f.isSuccess()) {
 *     // You might get a NullPointerException here because the future
 *     // might not be completed yet.
 *     f.cause().printStackTrace();
 * } else {
 *     // Connection established successfully
 * }
 *
 * // GOOD
 * {@link io.netty5.bootstrap.Bootstrap} b = ...;
 * // Configure the connect timeout option.
 * <b>b.option({@link io.netty5.channel.ChannelOption}.CONNECT_TIMEOUT_MILLIS, 10000);</b>
 * {@link Future} f = b.connect(...);
 * f.asStage().await();
 *
 * // Now we are sure the future is completed.
 * assert f.isDone();
 *
 * if (f.isCancelled()) {
 *     // Connection attempt cancelled by user
 * } else if (!f.isSuccess()) {
 *     f.cause().printStackTrace();
 * } else {
 *     // Connection established successfully
 * }
 * </pre>
 */
public interface Future<V> extends AsynchronousResult<V> {
    /**
     * Adds the specified listener to this future. The specified listener is notified when this future is {@linkplain
     * #isDone() done}. If this future is already completed, the specified listener is notified immediately.
     *
     * @param listener The listener to be called when this future completes. The listener will be passed this future as
     *                 an argument.
     * @return this future object.
     */
    Future<V> addListener(FutureListener<? super V> listener);

    /**
     * Adds the specified listener to this future. The specified listener is notified when this future is {@linkplain
     * #isDone() done}. If this future is already completed, the specified listener is notified immediately.
     *
     * @param context  The context object that will be passed to the listener when this future completes.
     * @param listener The listener to be called when this future completes. The listener will be passed the given
     *                 context, and this future.
     * @return this future object.
     */
    <C> Future<V> addListener(C context, FutureContextListener<? super C, ? super V> listener);

    /**
     * Returns a {@link FutureCompletionStage} that reflects the state of this {@link Future} and so will receive all
     * updates as well.
     * <p>
     * The returned {@link FutureCompletionStage} also implements the JDK {@link java.util.concurrent.Future},
     * and has blocking methods not found on the Netty {@code Future} interface, for awaiting the completion.
     */
    FutureCompletionStage<V> asStage();

    /**
     * Creates a <strong>new</strong> {@link Future} that will complete with the result of this {@link Future} mapped
     * through the given mapper function.
     * <p>
     * If this future fails, then the returned future will fail as well, with the same exception. Cancellation of either
     * future will cancel the other. If the mapper function throws, the returned future will fail, but this future will
     * be unaffected.
     *
     * @param mapper The function that will convert the result of this future into the result of the returned future.
     * @param <R>    The result type of the mapper function, and of the returned future.
     * @return A new future instance that will complete with the mapped result of this future.
     */
    default <R> Future<R> map(Function<V, R> mapper) {
        return Futures.map(this, mapper);
    }

    /**
     * Creates a <strong>new</strong> {@link Future} that will complete with the result of this {@link Future}
     * flat-mapped through the given mapper function.
     * <p>
     * The "flat" in "flat-map" means the given mapper function produces a result that itself is a future-of-R, yet this
     * method also returns a future-of-R, rather than a future-of-future-of-R. In other words, if the same mapper
     * function was used with the {@link #map(Function)} method, you would get back a {@code Future<Future<R>>}. These
     * nested futures are "flattened" into a {@code Future<R>} by this method.
     * <p>
     * Effectively, this method behaves similar to this serial code, except asynchronously and with proper exception and
     * cancellation handling:
     * <pre>{@code
     * V x = future.sync().getNow();
     * Future<R> y = mapper.apply(x);
     * R result = y.sync().getNow();
     * }</pre>
     * <p>
     * If the given future fails, then the returned future will fail as well, with the same exception. Cancellation of
     * either future will cancel the other. If the mapper function throws, the returned future will fail, but this
     * future will be unaffected.
     *
     * @param mapper The function that will convert the result of this future into the result of the returned future.
     * @param <R>    The result type of the mapper function, and of the returned future.
     * @return A new future instance that will complete with the mapped result of this future.
     */
    default <R> Future<R> flatMap(Function<V, Future<R>> mapper) {
        return Futures.flatMap(this, mapper);
    }

    /**
     * Link the {@link Future} and {@link Promise} such that if the {@link Future} completes the {@link Promise}
     * will be notified. Cancellation is propagated both ways such that if the {@link Future} is cancelled
     * the {@link Promise} is cancelled and vice-versa.
     *
     * @param promise   the {@link Promise} which will be notified
     * @return          itself
     */
    default Future<V> cascadeTo(final Promise<? super V> promise) {
        Futures.cascade(this, promise);
        return this;
    }
}
