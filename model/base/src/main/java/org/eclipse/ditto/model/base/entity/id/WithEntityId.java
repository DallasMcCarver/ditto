/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.model.base.entity.id;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Implementations of this interface are associated to an entity identified by the value
 * returned from {@link #getEntityId()}.
 */
public interface WithEntityId {

    /**
     * Returns the identifier of the entity.
     *
     * @return the identifier of the entity.
     */
    EntityId getEntityId();

    /**
     * Extracts the entity ID of the given type out of the given object in case the object is an instance of
     * {@link WithEntityId}.
     *
     * @param theClass The class type of the ID that should be extracted.
     * @param object The object of which the ID should be extracted.
     * @param <I> The type of the ID that should be extracted.
     * @return An optional which holds the entity ID of type I if the given object implements
     * {@link WithEntityId} and the entity ID is of type I.
     */
    static <I extends EntityId> Optional<I> getEntityIdOfType(final Class<I> theClass, @Nullable final Object object) {
        return Optional.ofNullable(object)
                .filter(WithEntityId.class::isInstance)
                .map(WithEntityId.class::cast)
                .map(WithEntityId::getEntityId)
                .filter(theClass::isInstance)
                .map(theClass::cast);
    }

}
