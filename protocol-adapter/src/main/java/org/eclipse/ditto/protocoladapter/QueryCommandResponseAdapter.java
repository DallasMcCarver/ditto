/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.protocoladapter;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.ditto.signals.commands.base.CommandResponse;

/**
 * An {@code Adapter} mixin for retrieve command responses.
 *
 * @param <T> the type mapped by this {@code Adapter}.
 * @since 1.1.0
 */
public interface QueryCommandResponseAdapter<T extends CommandResponse<?>> extends Adapter<T> {

    @Override
    default Set<TopicPath.Criterion> getCriteria() {
        return EnumSet.of(TopicPath.Criterion.COMMANDS);
    }

    @Override
    default Set<TopicPath.Action> getActions() {
        return EnumSet.of(TopicPath.Action.RETRIEVE);
    }

    @Override
    default boolean isForResponses() {
        return true;
    }
}
