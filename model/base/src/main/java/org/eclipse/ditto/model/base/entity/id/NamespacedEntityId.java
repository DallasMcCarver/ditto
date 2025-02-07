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
package org.eclipse.ditto.model.base.entity.id;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.entity.type.EntityType;

/**
 * Interface for all entity IDs that contain a namespace in their string representation.
 * Every implementation of this interface needs to ensure that name and namespace are valid according to
 * {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#ENTITY_NAME_REGEX} and
 * {@link org.eclipse.ditto.model.base.entity.id.RegexPatterns#NAMESPACE_REGEX}.
 * Every implementation must ensure immutability.
 */
@Immutable
public interface NamespacedEntityId extends EntityId {

    /**
     * Instantiates a {@link NamespacedEntityId} based on the given entity type and entity ID.
     *
     * @param entityType The type of the entity which is identified by the given ID.
     * @param entityId The ID of the entity.
     * @return the instance.
     * @since 2.0.0
     */
    static NamespacedEntityId of(final EntityType entityType, final CharSequence entityId) {
        return EntityIds.getNamespacedEntityId(entityType, entityId);
    }

    /**
     * Gets the name part of this entity ID.
     *
     * @return the name if the entity.
     */
    String getName();

    /**
     * Gets the namespace part of this entity ID.
     *
     * @return the namespace o the entity.
     */
    String getNamespace();

}
