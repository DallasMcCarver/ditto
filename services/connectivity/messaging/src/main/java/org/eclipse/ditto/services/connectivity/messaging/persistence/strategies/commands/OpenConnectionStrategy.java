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

import static org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionAction.OPEN_CONNECTION;
import static org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionAction.PERSIST_AND_APPLY_EVENT;
import static org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionAction.SEND_RESPONSE;
import static org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionAction.UPDATE_SUBSCRIPTIONS;
import static org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory.newErrorResult;
import static org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory.newMutationResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionAction;
import org.eclipse.ditto.services.connectivity.messaging.persistence.stages.ConnectionState;
import org.eclipse.ditto.services.connectivity.messaging.persistence.stages.StagedCommand;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.signals.commands.connectivity.modify.OpenConnection;
import org.eclipse.ditto.signals.commands.connectivity.modify.OpenConnectionResponse;
import org.eclipse.ditto.signals.events.connectivity.ConnectionOpened;
import org.eclipse.ditto.signals.events.connectivity.ConnectivityEvent;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.connectivity.modify.OpenConnection} command.
 */
final class OpenConnectionStrategy extends AbstractConnectivityCommandStrategy<OpenConnection> {

    OpenConnectionStrategy() {
        super(OpenConnection.class);
    }

    @Override
    protected Result<ConnectivityEvent<?>> doApply(final Context<ConnectionState> context,
            @Nullable final Connection connection,
            final long nextRevision,
            final OpenConnection command,
            @Nullable final Metadata metadata) {

        final Optional<DittoRuntimeException> validationError = validate(context, command, connection);
        if (validationError.isPresent()) {
            return newErrorResult(validationError.get(), command);
        } else {
            final ConnectivityEvent<?> event = ConnectionOpened.of(context.getState().id(), nextRevision,
                    getEventTimestamp(), command.getDittoHeaders(), metadata);
            final WithDittoHeaders response =
                    OpenConnectionResponse.of(context.getState().id(), command.getDittoHeaders());
            final List<ConnectionAction> actions =
                    Arrays.asList(PERSIST_AND_APPLY_EVENT, OPEN_CONNECTION, UPDATE_SUBSCRIPTIONS, SEND_RESPONSE);
            return newMutationResult(StagedCommand.of(command, event, response, actions), event, response);
        }
    }
}
