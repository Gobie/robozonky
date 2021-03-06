/*
 * Copyright 2019 The RoboZonky Project
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

package com.github.robozonky.api.remote.enums;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.function.Function;

abstract class AbstractDeserializer<T extends Enum<T>> extends JsonDeserializer<T> {

    protected final Logger logger = LogManager.getLogger(getClass());
    private final Function<String, T> parser;
    private final T defaultValue;
    private final boolean nullAllowed;

    protected AbstractDeserializer(final Function<String, T> parser, final T defaultValue) {
        this(parser, defaultValue, false);
    }

    protected AbstractDeserializer(final Function<String, T> parser, final T defaultValue, final boolean nullAllowed) {
        this.parser = parser;
        this.defaultValue = defaultValue;
        this.nullAllowed = nullAllowed;
    }

    @Override
    public T deserialize(final JsonParser jsonParser, final DeserializationContext ctx) throws IOException {
        var id = jsonParser.getText();
        try {
            return parser.apply(id);
        } catch (final Exception ex) {
            logger.warn("Received unknown value from Zonky: '{}'. This may be a problem, but we continue.", id);
            return defaultValue;
        }
    }

    @Override
    public T getNullValue(final DeserializationContext ctx) {
        if (nullAllowed) {
            return null;
        }
        logger.warn("Unexpectedly received null value from Zonky. This may be a problem, but we continue.");
        return defaultValue;
    }
}
