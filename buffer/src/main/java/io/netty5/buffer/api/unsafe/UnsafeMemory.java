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
package io.netty5.buffer.api.unsafe;

import io.netty5.util.internal.PlatformDependent;

class UnsafeMemory {
    final Object base;
    final long address;
    final int size;

    UnsafeMemory(Object base, long address, int size) {
        this.base = base;
        this.address = address;
        this.size = size;
    }

    public UnsafeMemory slice(int offset, int length) {
        return new UnsafeMemory(base, address + offset, length);
    }

    public void clearMemory() {
        PlatformDependent.setMemory(base, address, size, (byte) 0);
    }
}
