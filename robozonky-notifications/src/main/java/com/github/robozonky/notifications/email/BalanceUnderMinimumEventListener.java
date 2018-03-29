/*
 * Copyright 2017 The RoboZonky Project
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

package com.github.robozonky.notifications.email;

import java.util.Map;
import java.util.OptionalInt;

import com.github.robozonky.api.notifications.ExecutionStartedEvent;
import com.github.robozonky.api.notifications.SessionInfo;
import org.eclipse.collections.impl.map.mutable.UnifiedMap;

class BalanceUnderMinimumEventListener extends AbstractEmailingListener<ExecutionStartedEvent> {

    private final int minimumBalance;

    public BalanceUnderMinimumEventListener(final ListenerSpecificNotificationProperties properties) {
        super(properties);
        this.minimumBalance = properties.getListenerSpecificIntProperty("minimumBalance", 200);
    }

    @Override
    String getSubject(final ExecutionStartedEvent event) {
        return "Zůstatek na Zonky účtu klesl pod " + this.minimumBalance + ",- Kč";
    }

    @Override
    String getTemplateFileName() {
        return "under-minimum-balance.ftl";
    }

    @Override
    protected Map<String, Object> getData(final ExecutionStartedEvent event) {
        return new UnifiedMap<String, Object>(super.getData(event)) {{
            put("minimumBalance", minimumBalance);
        }};
    }

    @Override
    public void handle(final ExecutionStartedEvent event, final SessionInfo sessionInfo) {
        // figure out whether or not the balance is over the threshold
        final OptionalInt lastKnownBalance = BalanceTracker.INSTANCE.getLastKnownBalance();
        final int newBalance = event.getPortfolioOverview().getCzkAvailable();
        final boolean balanceNowUnder = newBalance < minimumBalance;
        final boolean wasFineLastTime = !lastKnownBalance.isPresent() || lastKnownBalance.getAsInt() >= minimumBalance;
        if (balanceNowUnder && wasFineLastTime) {
            // and continue with event-processing, possibly eventually sending the e-mail
            super.handle(event, sessionInfo);
        } else {
            LOGGER.debug("Will not send e-mail.");
        }
    }
}
