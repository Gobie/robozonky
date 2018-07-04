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

package com.github.robozonky.api.remote.entities.sanitized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.robozonky.api.remote.entities.MyInvestment;
import com.github.robozonky.api.remote.entities.RawLoan;
import com.github.robozonky.api.remote.enums.MainIncomeType;
import com.github.robozonky.api.remote.enums.Purpose;
import com.github.robozonky.api.remote.enums.Rating;
import com.github.robozonky.api.remote.enums.Region;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LoanTest {

    @Mock
    private RawLoan mocked;

    @Test
    @DisplayName("Sanitization works.")
    void sanitizing() {
        assertThat(Loan.sanitized(mocked)).isNotNull();
    }

    @Nested
    @DisplayName("Existing Loan")
    class LoanUpdatingTest {

        private Loan loan;

        @BeforeEach
        void prepareLoan() {
            loan = Loan.sanitize(mocked)
                    .build();
        }

        @Test
        @DisplayName("is properly updated.")
        void updatesValuesThatMayChangeInTheMarketplace() {
            final MarketplaceLoan l = MarketplaceLoan.sanitize(mocked)
                    .setAmount(100000) // will not be updated
                    .setRemainingInvestment(1000) // will be updated
                    .build();
            Loan.updateFromMarketplace(loan, l);
            assertSoftly(softly -> {
                softly.assertThat(loan.getAmount()).isNotEqualTo(l.getAmount());
                softly.assertThat(loan.getRemainingInvestment()).isEqualTo(l.getRemainingInvestment());
            });
        }
    }

    @Nested
    @DisplayName("Setters for ")
    class SetterTest {

        private final LoanBuilder b = Loan.custom();

        private <T> void standard(final LoanBuilder builder, final Function<T, LoanBuilder> setter,
                                  final Supplier<T> getter, final T value) {
            assertThat(getter.get()).as("Null before setting.").isNull();
            final LoanBuilder newBuilder = setter.apply(value);
            assertSoftly(softly -> {
                softly.assertThat(newBuilder).as("Setter returned itself.").isSameAs(builder);
                softly.assertThat(getter.get()).as("Correct value was set.").isEqualTo(value);
            });
        }

        private <T> void bool(final LoanBuilder builder, final Function<Boolean, LoanBuilder> setter,
                              final Supplier<Boolean> getter) {
            assertThat(getter.get()).as("False before setting.").isFalse();
            final LoanBuilder newBuilder = setter.apply(true);
            assertSoftly(softly -> {
                softly.assertThat(newBuilder).as("Setter returned itself.").isSameAs(builder);
                softly.assertThat(getter.get()).as("Correct value was set.").isTrue();
            });
        }

        private <T> void integer(final LoanBuilder builder, final Function<Integer, LoanBuilder> setter,
                                 final Supplier<Integer> getter, final int value) {
            assertThat(getter.get()).as("False before setting.").isLessThanOrEqualTo(0);
            final LoanBuilder newBuilder = setter.apply(value);
            assertSoftly(softly -> {
                softly.assertThat(newBuilder).as("Setter returned itself.").isSameAs(builder);
                softly.assertThat(getter.get()).as("Correct value was set.").isEqualTo(value);
            });
        }

        private <T> void optional(final LoanBuilder builder, final Function<T, LoanBuilder> setter,
                                  final Supplier<Optional<T>> getter, final T value) {
            assertThat(getter.get()).isEmpty();
            final LoanBuilder newBuilder = setter.apply(value);
            assertSoftly(softly -> {
                softly.assertThat(newBuilder).as("Setter returned itself.").isSameAs(builder);
                softly.assertThat(getter.get()).as("Correct value was set.").contains(value);
            });
        }

        @Test
        void remainingPrincipalToLoan() {
            optional(b, b::setRemainingPrincipalToLoan, b::getRemainingPrincipalToLoan, BigDecimal.TEN);
        }

        @Test
        void remainingPrincipalToBorrower() {
            optional(b, b::setRemainingPrincipalToBorrower, b::getRemainingPrincipalToBorrower, BigDecimal.TEN);
        }

        @Test
        void totalPrincipalToLoan() {
            optional(b, b::setTotalPrincipalToLoan, b::getTotalPrincipalToLoan, BigDecimal.TEN);
        }

        @Test
        void totalPrincipalToBorrower() {
            optional(b, b::setTotalPrincipalToBorrower, b::getTotalPrincipalToBorrower, BigDecimal.TEN);
        }

        @Test
        void myInvestment() {
            optional(b, b::setMyInvestment, b::getMyInvestment, mock(MyInvestment.class));
        }

        @Test
        void mainIncomeType() {
            standard(b, b::setMainIncomeType, b::getMainIncomeType, MainIncomeType.EMPLOYMENT);
        }

        @Test
        void region() {
            standard(b, b::setRegion, b::getRegion, Region.JIHOCESKY);
        }

        @Test
        void purpose() {
            standard(b, b::setPurpose, b::getPurpose, Purpose.AUTO_MOTO);
        }

        @Test
        void rating() {
            standard(b, b::setRating, b::getRating, Rating.D);
        }

        @Test
        void name() {
            standard(b, b::setName, b::getName, "Something");
        }

        @Test
        void story() {
            standard(b, b::setStory, b::getStory, "Something");
        }

        @Test
        void nickname() {
            standard(b, b::setNickName, b::getNickName, "Something");
        }

        @Test
        void investmentRate() {
            standard(b, b::setInvestmentRate, b::getInvestmentRate, BigDecimal.ONE);
        }

        @Test
        void interestRate() {
            standard(b, b::setInterestRate, b::getInterestRate, BigDecimal.ONE);
        }

        @Test
        void datePublished() {
            standard(b, b::setDatePublished, b::getDatePublished, OffsetDateTime.now());
        }

        @Test
        void deadline() {
            standard(b, b::setDeadline, b::getDeadline, OffsetDateTime.now());
        }

        @Test
        void covered() {
            bool(b, b::setCovered, b::isCovered);
        }

        @Test
        void topped() {
            bool(b, b::setTopped, b::isTopped);
        }

        @Test
        void published() {
            bool(b, b::setPublished, b::isPublished);
        }

        @Test
        void questionsAllowed() {
            bool(b, b::setQuestionsAllowed, b::isQuestionsAllowed);
        }

        @Test
        void insuranceActive() {
            bool(b, b::setInsuranceActive, b::isInsuranceActive);
        }

        @Test
        void termInMonths() {
            integer(b, b::setTermInMonths, b::getTermInMonths, 1);
        }

        @Test
        void amount() {
            integer(b, b::setAmount, b::getAmount, 1);
        }

        @Test
        void remainingInvestment() {
            integer(b, b::setRemainingInvestment, b::getRemainingInvestment, 1);
        }

        @Test
        void investmentsCount() {
            integer(b, b::setInvestmentsCount, b::getInvestmentsCount, 1);
        }

        @Test
        void activeLoansCount() {
            integer(b, b::setActiveLoansCount, b::getActiveLoansCount, 1);
        }

        @Test
        void questionsCount() {
            integer(b, b::setQuestionsCount, b::getQuestionsCount, 1);
        }

        @Test
        void userId() {
            integer(b, b::setUserId, b::getUserId, 1);
        }
    }
}