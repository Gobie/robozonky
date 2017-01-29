/*
 * Copyright 2017 Lukáš Petrovický
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

package com.github.triceo.robozonky.app.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.github.triceo.robozonky.api.strategies.InvestmentStrategy;
import com.github.triceo.robozonky.api.strategies.InvestmentStrategyService;
import com.github.triceo.robozonky.app.util.ExtensionsManager;
import com.github.triceo.robozonky.internal.api.Defaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements Java's {@link ServiceLoader} to provide suitable {@link InvestmentStrategy} implementations.
 */
class InvestmentStrategyLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvestmentStrategyLoader.class);
    private static final ServiceLoader<InvestmentStrategyService> STRATEGY_LOADER =
            ExtensionsManager.INSTANCE.getServiceLoader(InvestmentStrategyService.class);

    static Optional<InvestmentStrategy> load(final String strategy) {
        return StreamSupport.stream(InvestmentStrategyLoader.STRATEGY_LOADER.spliterator(), true)
                .map(iss -> {
                    InvestmentStrategyLoader.LOGGER.debug("Re-reading strategy.");
                    try (final InputStream stream = new ByteArrayInputStream(strategy.getBytes(Defaults.CHARSET))) {
                        return iss.parse(stream);
                    } catch (final IOException ex) {
                        InvestmentStrategyLoader.LOGGER.error("Failed reading strategy.", ex);
                        return Optional.<InvestmentStrategy>empty();
                    }
                })
                .flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty())
                .findFirst();
    }

}

