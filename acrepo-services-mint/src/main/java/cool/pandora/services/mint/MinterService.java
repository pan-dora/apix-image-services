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

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import java.util.function.Supplier;

/**
 * @author acoburn
 * @since 9/14/15
 */
public class MinterService implements Supplier {

    private int length;

    /**
     * Set the minter length property
     *
     * @param length the length of the ID
     */
    public MinterService(final int length) {
        this.length = length;
    }

    /**
     * Generate an ID
     *
     * @return the new ID
     */
    public String get() {
        return randomAlphanumeric(length).toLowerCase();
    }

}
