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

package com.github.robozonky.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UrlUtilTest {

    @Test
    void toUrlValid() {
        final URL url = UrlUtil.toURL("http://" + UUID.randomUUID());
        assertThat(url).hasProtocol("http");
    }

    @Test
    void toUrlInvalid() {
        final URL url = UrlUtil.toURL("noproto://" + UUID.randomUUID());
        assertThat(url).hasProtocol("file");
    }

    @Test
    void openWrongUrl() {
        assertThatThrownBy(() -> UrlUtil.open(new URL("http://" + UUID.randomUUID()))).isInstanceOf(IOException.class);
    }

    @Test
    void openCorrectUrl() throws IOException {
        try (final InputStream s = UrlUtil.open(new URL("http://www.google.com"))) {
            assertThat(s).isNotNull();
        }
    }

}
