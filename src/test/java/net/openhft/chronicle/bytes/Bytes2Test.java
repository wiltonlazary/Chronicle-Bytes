/*
 * Copyright 2016 higherfrequencytrading.com
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

package net.openhft.chronicle.bytes;

import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.core.threads.ThreadDump;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static net.openhft.chronicle.bytes.Allocator.HEAP;
import static net.openhft.chronicle.bytes.Allocator.NATIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class Bytes2Test {

    private Allocator alloc1;
    private Allocator alloc2;
    private ThreadDump threadDump;

    public Bytes2Test(Allocator alloc1, Allocator alloc2) {
        this.alloc1 = alloc1;
        this.alloc2 = alloc2;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {NATIVE, NATIVE}, {HEAP, NATIVE}, {NATIVE, HEAP}, {HEAP, HEAP}
        });
    }

    @Before
    public void threadDump() {
        threadDump = new ThreadDump();
    }

    @After
    public void checkThreadDump() {
        threadDump.assertNoNewThreads();
    }

    @Test
    public void testPartialWrite() {
        Bytes from = alloc1.elasticBytes(1);
        Bytes to = alloc2.fixedBytes(6);

        try {
            from.write("Hello World");

            to.writeSome(from);
            assertEquals("World", from.toString());
        } finally {
            from.release();
            to.release();
        }
    }

    @Test
    public void testPartialWrite64plus() {
        Bytes from = alloc1.elasticBytes(1);
        Bytes to = alloc2.fixedBytes(6);

        from.write("Hello World 0123456789012345678901234567890123456789012345678901234567890123456789");

        try {
            to.writeSome(from);
            assertTrue("from: " + from, from.toString().startsWith("World "));
        } finally {
            from.release();
            to.release();
        }
    }

    @Test
    public void testParseToBytes() throws IORuntimeException {
        Bytes from = alloc1.fixedBytes(64);
        Bytes to = alloc2.fixedBytes(32);
        try {
            from.append8bit("0123456789 aaaaaaaaaa 0123456789 0123456789");

            for (int i = 0; i < 4; i++) {
                from.parse8bit(to, StopCharTesters.SPACE_STOP);
                assertEquals(10, to.readRemaining());
            }
            assertEquals(0, from.readRemaining());
        } finally {
            from.release();
            to.release();
        }
    }
}