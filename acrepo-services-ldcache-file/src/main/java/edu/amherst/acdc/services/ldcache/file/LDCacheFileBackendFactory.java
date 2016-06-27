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
package edu.amherst.acdc.services.ldcache.file;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.apache.marmotta.ldcache.api.LDCachingBackend;
import org.apache.marmotta.ldcache.backend.file.LDCachingFileBackend;
import org.slf4j.Logger;

/**
 * @author acoburn
 * @since 6/27/16
 */
public class LDCacheFileBackendFactory {

    private static Logger LOGGER = getLogger(LDCacheFileBackendFactory.class);

    /**
     * Create a File-based LDCachingBackend
     * @param storageDir the location where files will be stored
     * @return a backend for linked data caching
     */
    public static LDCachingBackend createBackend(final String storageDir) {
        try {
            final LDCachingBackend backend = new LDCachingFileBackend(new File(storageDir));
            backend.initialize();
            return backend;
        } catch (final Exception ex) {
            LOGGER.error("Error creating LDCachingBackend: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private LDCacheFileBackendFactory() {
        // prevent instantiation
    }
}

