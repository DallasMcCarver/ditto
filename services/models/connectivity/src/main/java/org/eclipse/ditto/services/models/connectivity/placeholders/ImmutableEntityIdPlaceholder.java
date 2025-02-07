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
package org.eclipse.ditto.services.models.connectivity.placeholders;

import static org.eclipse.ditto.model.base.common.ConditionChecker.argumentNotEmpty;
import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Optional;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.entity.id.EntityId;
import org.eclipse.ditto.model.base.entity.id.NamespacedEntityId;
import org.eclipse.ditto.model.base.entity.id.NamespacedEntityIdInvalidException;

/**
 * Placeholder implementation that replaces {@code entity:id}, {@code entity:namespace} and {@code entity:name}.
 * The input value is a String and must be a valid Entity ID.
 */
@Immutable
final class ImmutableEntityIdPlaceholder extends AbstractEntityIdPlaceholder<NamespacedEntityId> {

    /**
     * Singleton instance of the ImmutableEntityPlaceholder.
     */
    static final ImmutableEntityIdPlaceholder INSTANCE = new ImmutableEntityIdPlaceholder();

    @Override
    public String getPrefix() {
        return "entity";
    }

    @Override
    public Optional<String> resolve(final EntityId entityId, final String placeholder) {
        argumentNotEmpty(placeholder, "placeholder");
        checkNotNull(entityId, "Entity ID");
        try {
            final NamespacedEntityId namespacedEntityId = NamespacedEntityId.of(entityId.getEntityType(), entityId);
            return doResolve(namespacedEntityId, placeholder);
        } catch (final NamespacedEntityIdInvalidException e) {
            // not a namespaced entity ID; does not resolve.
            return Optional.empty();
        }
    }
}
