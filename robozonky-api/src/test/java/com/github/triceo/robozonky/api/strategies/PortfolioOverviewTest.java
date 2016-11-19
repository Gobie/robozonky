/*
 * Copyright 2016 Lukáš Petrovický
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

package com.github.triceo.robozonky.api.strategies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.triceo.robozonky.api.remote.entities.Investment;
import com.github.triceo.robozonky.api.remote.entities.Rating;
import com.github.triceo.robozonky.api.remote.entities.RiskPortfolio;
import com.github.triceo.robozonky.api.remote.entities.Statistics;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.mockito.Mockito;

public class PortfolioOverviewTest {

    private static void assertProperRatingShare(final PortfolioOverview result, final Rating r, final int amount,
                                                final int total) {
        final BigDecimal expectedShare =
                BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_EVEN);
        Assertions.assertThat(result.getShareOnInvestment(r)).isEqualTo(expectedShare);
    }

    private static List<Investment> getMockInvestmentWithBalance(final int loanAmount) {
        final Investment i = Mockito.mock(Investment.class);
        Mockito.when(i.getAmount()).thenReturn(loanAmount);
        return Collections.singletonList(i);
    }

    @Test
    public void emptyPortfolio() {
        final Statistics s = Mockito.mock(Statistics.class);
        final PortfolioOverview o = PortfolioOverview.calculate(BigDecimal.valueOf(5000.0), s, Collections.emptyList());
        final SoftAssertions softly = new SoftAssertions();
        for (final Rating r: Rating.values()) {
            softly.assertThat(o.getShareOnInvestment(r)).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Test
    public void properRatingShareCalculation() {
        // mock necessary structures
        final int amountAA = 300, amountB = 200, amountD = 100;
        final int totalPie = amountAA + amountB + amountD;
        final RiskPortfolio riskAA = new RiskPortfolio(Rating.AA, 0, amountAA, 0);
        final RiskPortfolio riskB = new RiskPortfolio(Rating.B, 0, amountB, 0);
        final RiskPortfolio riskD = new RiskPortfolio(Rating.D, 0, amountD, 0);
        final Statistics stats = Mockito.mock(Statistics.class);
        Mockito.when(stats.getRiskPortfolio()).thenReturn(Arrays.asList(riskAA, riskB, riskD));

        // check standard operation
        final BigDecimal balance = BigDecimal.TEN;
        PortfolioOverview result = PortfolioOverview.calculate(balance, stats, Collections.emptyList());
        Assertions.assertThat(result.getCzkAvailable()).isEqualTo(balance.intValue());
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.AA, amountAA, totalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.B, amountB, totalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.D, amountD, totalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.AAAAA, 0, totalPie); // test other ratings included
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.AAAA, 0, totalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.AAA, 0, totalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.A, 0, totalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.C, 0, totalPie);
        // check operation with offline investments
        final int increment = 200, newTotalPie = totalPie + increment;
        final List<Investment> investments = PortfolioOverviewTest.getMockInvestmentWithBalance(increment);
        final Investment i = investments.get(0);
        Mockito.when(i.getRating()).thenReturn(Rating.A);
        result = PortfolioOverview.calculate(balance, stats, investments);
        Assertions.assertThat(result.getCzkAvailable()).isEqualTo(balance.intValue());
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.AA, amountAA, newTotalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.B, amountB, newTotalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.D, amountD, newTotalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.AAAAA, 0, newTotalPie); // test other ratings included
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.AAAA, 0, newTotalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.AAA, 0, newTotalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.A, increment, newTotalPie);
        PortfolioOverviewTest.assertProperRatingShare(result, Rating.C, 0, newTotalPie);
    }


}
