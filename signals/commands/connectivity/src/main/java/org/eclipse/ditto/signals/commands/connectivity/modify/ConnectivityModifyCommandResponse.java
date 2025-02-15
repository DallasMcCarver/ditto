/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.signals.commands.connectivity.modify;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.connectivity.WithConnectionId;
import org.eclipse.ditto.signals.base.SignalWithEntityId;
import org.eclipse.ditto.signals.base.WithOptionalEntity;
import org.eclipse.ditto.signals.commands.connectivity.ConnectivityCommandResponse;

/**
 * Aggregates all {@link ConnectivityCommandResponse} which respond to a
 * {@link ConnectivityModifyCommand}.
 *
 * @param <T> the type of the implementing class.
 */
public interface ConnectivityModifyCommandResponse<T extends ConnectivityModifyCommandResponse<T>>
        extends ConnectivityCommandResponse<T>, WithOptionalEntity, WithConnectionId, SignalWithEntityId<T> {

    @Override
    T setDittoHeaders(DittoHeaders dittoHeaders);

}
