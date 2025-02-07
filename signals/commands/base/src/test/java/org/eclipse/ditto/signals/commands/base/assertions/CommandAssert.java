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
package org.eclipse.ditto.signals.commands.base.assertions;

import org.assertj.core.api.Assertions;
import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.entity.id.WithEntityId;
import org.eclipse.ditto.signals.commands.base.Command;

/**
 * An Assert for {@link Command}s.
 */
public class CommandAssert extends AbstractCommandAssert<CommandAssert, Command<?>> {

    /**
     * Constructs a new {@code AbstractThingCommandAssert} object.
     *
     * @param actual the command to be checked.
     */
    protected CommandAssert(final Command actual) {
        super(actual, CommandAssert.class);
    }

    public CommandAssert withId(final EntityId expectedId) {
        Assertions.assertThat(actual).isInstanceOf(WithEntityId.class);
        final WithEntityId actualWithEntityId = (WithEntityId) this.actual;
        return assertThatEquals(actualWithEntityId.getEntityId(), expectedId, "id");
    }

}
