/*
 * Copyright 2016 Amherst College
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
package cool.pandora.services.mint;

import static java.util.regex.Pattern.compile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author acoburn
 * @since 9/14/15
 */
public class MinterServiceTest {

    private static final String ID_PATTERN = "^[a-z0-9]+$";

    @Test
    public void testMintId() {
        final String id7 = new MinterService(7).get();
        final String id8 = new MinterService(8).get();
        final String id9 = new MinterService(9).get();

        assertEquals(7, id7.length());
        assertTrue(compile(ID_PATTERN).matcher(id7).find());

        assertEquals(8, id8.length());
        assertTrue(compile(ID_PATTERN).matcher(id8).find());

        assertEquals(9, id9.length());
        assertTrue(compile(ID_PATTERN).matcher(id9).find());
    }
}
