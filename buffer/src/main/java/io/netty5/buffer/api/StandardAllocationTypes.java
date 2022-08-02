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
package io.netty5.buffer.api;

/**
 * Standard implementations of {@link AllocationType} that all {@linkplain MemoryManager memory managers} should
 * support.
 */
public enum StandardAllocationTypes implements AllocationType {
    /**
     * The allocation should use Java heap memory.
     */
    ON_HEAP,
    /**
     * The allocation should use native (non-heap) memory.
     */
    OFF_HEAP;

    @Override
    public boolean isDirect() {
        return this == OFF_HEAP;
    }
}
