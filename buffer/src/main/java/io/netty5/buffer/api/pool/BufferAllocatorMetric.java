/*
 * Copyright 2021 The Netty Project
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
package io.netty5.buffer.api.pool;

import io.netty5.buffer.api.BufferAllocator;

import java.util.List;

public interface BufferAllocatorMetric {

    /**
     * Return the number of arenas.
     */
    int numArenas();

    /**
     * Return a {@link List} of all {@link PoolArenaMetric}s that are provided by this pool.
     */
    List<PoolArenaMetric> arenaMetrics();

    /**
     * Return the number of thread local caches used by this {@link PooledBufferAllocator}.
     */
    int numThreadLocalCaches();

    /**
     * Return the size of the small cache.
     */
    int smallCacheSize();

    /**
     * Return the size of the normal cache.
     */
    int normalCacheSize();

    /**
     * Return the chunk size for an arena.
     */
    int chunkSize();

    /**
     * Returns the number of bytes of heap memory used by a {@link BufferAllocator} or {@code -1} if unknown.
     */
    long usedMemory();

    /**
     * Returns the number of bytes of memory that is currently pinned to the buffers allocated by a
     * {@link BufferAllocator}, or {@code -1} if unknown.
     */
    long pinnedMemory();
}
