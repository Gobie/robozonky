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

package com.github.triceo.robozonky.app;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;

import com.github.triceo.robozonky.api.Refreshable;
import com.github.triceo.robozonky.api.ReturnCode;
import com.github.triceo.robozonky.api.marketplaces.ExpectedTreatment;
import com.github.triceo.robozonky.api.marketplaces.Marketplace;
import com.github.triceo.robozonky.api.remote.ZonkyApi;
import com.github.triceo.robozonky.api.remote.ZonkyOAuthApi;
import com.github.triceo.robozonky.api.remote.entities.Loan;
import com.github.triceo.robozonky.api.remote.entities.Wallet;
import com.github.triceo.robozonky.api.strategies.InvestmentStrategy;
import com.github.triceo.robozonky.app.authentication.ApiProvider;
import com.github.triceo.robozonky.app.authentication.AuthenticationHandler;
import com.github.triceo.robozonky.app.authentication.SecretProvider;
import com.github.triceo.robozonky.app.investing.DirectInvestmentMode;
import com.github.triceo.robozonky.app.investing.SingleShotInvestmentMode;
import com.github.triceo.robozonky.app.investing.ZonkyProxy;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

public class AppTest extends AbstractStateLeveragingTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Test
    public void notWellFormedCli() {
        exit.expectSystemExitWithStatus(ReturnCode.ERROR_WRONG_PARAMETERS.getCode());
        App.main();
    }

    @Test
    public void wrongKeyStore() {
        exit.expectSystemExitWithStatus(ReturnCode.ERROR_WRONG_PARAMETERS.getCode());
        App.main("-g", "some.random.file", "-p", "password", "single", "-l", "1", "-a", "1000");
    }

    @Test
    public void handleUnexpectedError() {
        exit.expectSystemExitWithStatus(ReturnCode.ERROR_UNEXPECTED.getCode());
        App.handleUnexpectedException(null);
    }

    @Test
    public void handleMaintenanceError() {
        exit.expectSystemExitWithStatus(ReturnCode.ERROR_DOWN.getCode());
        App.handleZonkyMaintenanceError(null, false);
    }

    @Test
    public void handleMaintenanceErrorFaultTolerant() {
        exit.expectSystemExitWithStatus(ReturnCode.OK.getCode());
        App.handleZonkyMaintenanceError(null, true);
    }

    @Test
    public void handleProcessingExceptionWithoutCase() {
        exit.expectSystemExitWithStatus(ReturnCode.ERROR_UNEXPECTED.getCode());
        App.handleException(new ProcessingException("No cause."), false);
    }

    @Test
    public void handleProcessingExceptionOkCausedBySocket() {
        exit.expectSystemExitWithStatus(ReturnCode.OK.getCode());
        App.handleException(new ProcessingException(new SocketException()), true);
    }

    @Test
    public void handleProcessingExceptionOkCausedByHost() {
        exit.expectSystemExitWithStatus(ReturnCode.OK.getCode());
        App.handleException(new ProcessingException(new UnknownHostException()), true);
    }

    @Test
    public void handleProcessingExceptionDownCausedBySocket() {
        exit.expectSystemExitWithStatus(ReturnCode.ERROR_DOWN.getCode());
        App.handleException(new ProcessingException(new SocketException()), false);
    }

    @Test
    public void handleProcessingExceptionDownCausedByHost() {
        exit.expectSystemExitWithStatus(ReturnCode.ERROR_DOWN.getCode());
        App.handleException(new ProcessingException(new UnknownHostException()), false);
    }

    @Test
    public void handleException() {
        exit.expectSystemExitWithStatus(ReturnCode.ERROR_REMOTE.getCode());
        App.handleException(new WebApplicationException());
    }

    @Test
    public void singleInvestmentExecutionFailingLogin() {
        // a lot of mocking to exercise the basic path all the way through to the core
        final SecretProvider secret = Mockito.mock(SecretProvider.class);
        Mockito.when(secret.getPassword()).thenReturn("".toCharArray());
        final AuthenticationHandler auth = Mockito.mock(AuthenticationHandler.class);
        Mockito.doThrow(IllegalStateException.class).when(auth).execute(ArgumentMatchers.any(), ArgumentMatchers.any());
        final ApiProvider api = Mockito.mock(ApiProvider.class);
        Mockito.when(api.oauth()).thenReturn(Mockito.mock(ZonkyOAuthApi.class));
        final Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getDatePublished()).thenReturn(OffsetDateTime.now());
        final ZonkyApi zonky = Mockito.mock(ZonkyApi.class);
        Mockito.when(zonky.getLoan(ArgumentMatchers.anyInt())).thenReturn(loan);
        Mockito.when(zonky.getWallet()).thenReturn(Mockito.mock(Wallet.class));
        Mockito.when(api.authenticated(ArgumentMatchers.any())).thenReturn(zonky);
        // and now test
        final ReturnCode rc = App.execute(new DirectInvestmentMode(auth, new ZonkyProxy.Builder().asDryRun(),
                false, 1, 1000));
        Assertions.assertThat(rc).isEqualTo(ReturnCode.ERROR_SETUP);
    }

    @Test
    public void strategyBasedExecutionInvestingNothing() {
        // a lot of mocking to exercise the basic path all the way through to the core
        final SecretProvider secret = Mockito.mock(SecretProvider.class);
        Mockito.when(secret.getPassword()).thenReturn("".toCharArray());
        final AuthenticationHandler auth = Mockito.mock(AuthenticationHandler.class);
        Mockito.when(auth.execute(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        final ApiProvider api = Mockito.mock(ApiProvider.class);
        Mockito.when(api.oauth()).thenReturn(Mockito.mock(ZonkyOAuthApi.class));
        final Loan loan = Mockito.mock(Loan.class);
        Mockito.when(loan.getDatePublished()).thenReturn(OffsetDateTime.now());
        final ZonkyApi zonky = Mockito.mock(ZonkyApi.class);
        Mockito.when(zonky.getLoan(ArgumentMatchers.anyInt())).thenReturn(loan);
        Mockito.when(zonky.getWallet()).thenReturn(Mockito.mock(Wallet.class));
        Mockito.when(api.authenticated(ArgumentMatchers.any())).thenReturn(zonky);
        final InvestmentStrategy strategyMock = Mockito.mock(InvestmentStrategy.class);
        final Refreshable<InvestmentStrategy> refreshable = Mockito.mock(Refreshable.class);
        Mockito.when(refreshable.getLatest()).thenReturn(Optional.of(strategyMock));
        final Marketplace marketplace = Mockito.mock(Marketplace.class);
        Mockito.when(marketplace.specifyExpectedTreatment()).thenReturn(ExpectedTreatment.POLLING);
        // and now test
        final ReturnCode rc = App.execute(new SingleShotInvestmentMode(auth, new ZonkyProxy.Builder().asDryRun(),
                false, marketplace, refreshable));
        Assertions.assertThat(rc).isEqualTo(ReturnCode.OK);
    }

}
