/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.connectivity.messaging.persistence.strategies.commands;

import static org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionAction.RETRIEVE_CONNECTION_METRICS;

import org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionAction;
import org.eclipse.ditto.signals.commands.connectivity.query.RetrieveConnectionMetrics;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.connectivity.query.RetrieveConnectionMetrics}
 * command.
 */
final class RetrieveConnectionMetricsStrategy extends AbstractSingleActionStrategy<RetrieveConnectionMetrics> {

    RetrieveConnectionMetricsStrategy() {
        super(RetrieveConnectionMetrics.class);
    }

    @Override
    ConnectionAction getAction() {
        return RETRIEVE_CONNECTION_METRICS;
    }
}
