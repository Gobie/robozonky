/*
 * Copyright 2018 The RoboZonky Project
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

package com.github.robozonky.app.events;

import java.util.concurrent.CompletableFuture;

import com.github.robozonky.api.remote.entities.sanitized.Investment;
import com.github.robozonky.api.remote.entities.sanitized.Loan;
import com.github.robozonky.api.strategies.PortfolioOverview;
import com.github.robozonky.app.events.impl.EventFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AllSessionEventsTest {

    @Test
    void lazyFireReturnsFuture() {
        final Loan l = Loan.custom().build();
        final Investment i = Investment.fresh(l, 200).build();
        final CompletableFuture<Void> result = AllSessionEvents.get()
                .fire(EventFactory.loanRepaidLazy(() -> EventFactory.loanRepaid(i, l, mock(PortfolioOverview.class))));
        assertThat(result).isNotNull().isNotCancelled();

    }

    @Test
    void fireReturnsFuture() {
        final Loan l = Loan.custom().build();
        final Investment i = Investment.fresh(l, 200).build();
        final CompletableFuture<Void> result = AllSessionEvents.get()
                .fire(EventFactory.loanRepaid(i, l, mock(PortfolioOverview.class)));
        assertThat(result).isNotNull().isNotCancelled();

    }

}
