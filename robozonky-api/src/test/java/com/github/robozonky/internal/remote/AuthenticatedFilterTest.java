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

package com.github.robozonky.internal.remote;

import com.github.robozonky.api.remote.entities.ZonkyApiToken;
import com.github.robozonky.internal.Defaults;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.*;

class AuthenticatedFilterTest extends AbstractCommonFilterTest {

    static final ZonkyApiToken TOKEN = new ZonkyApiToken(UUID.randomUUID().toString(),
                                                         UUID.randomUUID().toString(), 300);

    private static InputStream c(final ZonkyApiToken token) {
        final String error = "{\"error\":\"invalid_token\",\"error_description\":\"Invalid access token: "
                + Arrays.toString(token.getAccessToken()) + "\"}";
        return new ByteArrayInputStream(error.getBytes(Defaults.CHARSET));
    }

    @Override
    protected RoboZonkyFilter getTestedFilter() {
        return new AuthenticatedFilter(() -> AuthenticatedFilterTest.TOKEN);
    }

    @Test
    void hasToken() throws URISyntaxException {
        final ClientRequestContext crc = mockClientRequestContext();
        getTestedFilter().filter(crc);
        verify(crc.getHeaders()).putSingle(eq("Authorization"), eq("Bearer " + String.valueOf(TOKEN.getAccessToken())));
    }

    @Test
    void changes400to401() throws IOException, URISyntaxException {
        final int expectedCode = 400;
        final ClientRequestContext ctx = mockClientRequestContext();
        final ClientResponseContext ctx2 = mockClientResponseContext();
        when(ctx2.hasEntity()).thenReturn(true);
        when(ctx2.getEntityStream()).thenReturn(c(TOKEN));
        when(ctx2.getStatusInfo()).thenReturn(Response.Status.fromStatusCode(expectedCode));
        when(ctx2.getStatus()).thenReturn(expectedCode);
        final RoboZonkyFilter filter = getTestedFilter();
        filter.filter(ctx, ctx2);
        verify(ctx2, times(1)).setStatus(401);
    }
}
