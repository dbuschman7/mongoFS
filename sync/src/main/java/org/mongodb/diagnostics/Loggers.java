/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.diagnostics;

import static org.mongodb.assertions.Assertions.notNull;

import org.mongodb.diagnostics.logging.JULLogger;
import org.mongodb.diagnostics.logging.Logger;
import org.mongodb.diagnostics.logging.SLF4JLogger;

/**
 * This class is not part of the public API.
 * 
 * @since 3.0
 */
public final class Loggers {
    /**
     * The prefix for all logger names.
     */
    public static final String PREFIX = "org.mongodb.driver";

    private static final boolean USE_SLF4J = shouldUseSLF4J();

    /**
     * Gets a logger with the given suffix appended on to {@code PREFIX}, separated by a '.'.
     * 
     * @param suffix
     *            the suffix for the logger
     * @return the logger
     * @see Loggers#PREFIX
     */
    public static Logger getLogger(final String suffix) {
        notNull("suffix", suffix);
        if (suffix.startsWith(".") || suffix.endsWith(".")) {
            throw new IllegalArgumentException("The suffix can not start or end with a '.'");
        }

        String name = PREFIX + "." + suffix;

        if (USE_SLF4J) {
            return new SLF4JLogger(name);
        }
        else {
            return new JULLogger(name);
        }
    }

    private Loggers() {
    }

    private static boolean shouldUseSLF4J() {
        try {
            Class.forName("org.slf4j.LoggerFactory");
            // Don't use SLF4J unless a logging implementation has been configured for it
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
