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
package org.eclipse.ditto.services.policies.persistence.actors.resolvers;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.policies.PolicyEntry;

/**
 * A placeholder requiring a {@link org.eclipse.ditto.model.policies.PolicyEntry} able to resolve information of that
 * policy entry.
 */
@Immutable
final class ImmutablePolicyEntryPlaceholder implements PolicyEntryPlaceholder {

    /**
     * Singleton instance of the ImmutablePolicyEntryPlaceholder.
     */
    static final ImmutablePolicyEntryPlaceholder INSTANCE = new ImmutablePolicyEntryPlaceholder();

    private static final String PREFIX = "policy-entry";
    private static final String LABEL = "label";

    private static final List<String> SUPPORTED_PLACEHOLDERS = Collections.singletonList(LABEL);

    private ImmutablePolicyEntryPlaceholder() {
        // no-op
    }

    @Override
    public Optional<String> resolve(final PolicyEntry policyEntry, final String name) {
        if (LABEL.equals(name)) {
            return Optional.of(policyEntry.getLabel().toString());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public List<String> getSupportedNames() {
        return SUPPORTED_PLACEHOLDERS;
    }

    @Override
    public boolean supports(final String name) {
        return SUPPORTED_PLACEHOLDERS.contains(name);
    }
}
