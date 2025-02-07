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
package org.eclipse.ditto.services.concierge.enforcement.placeholders.strategies;

import static java.util.Objects.requireNonNull;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.DittoHeadersSettable;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.services.concierge.enforcement.placeholders.HeaderBasedPlaceholderSubstitutionAlgorithm;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyEntry;

/**
 * Handles substitution for {@link org.eclipse.ditto.model.policies.SubjectId}
 * inside a {@link ModifyPolicyEntry} command.
 */
final class ModifyPolicyEntrySubstitutionStrategy extends AbstractTypedSubstitutionStrategy<ModifyPolicyEntry> {

    ModifyPolicyEntrySubstitutionStrategy() {
        super(ModifyPolicyEntry.class);
    }

    @Override
    public DittoHeadersSettable<?> apply(final ModifyPolicyEntry modifyPolicyEntry,
            final HeaderBasedPlaceholderSubstitutionAlgorithm substitutionAlgorithm) {
        requireNonNull(modifyPolicyEntry);
        requireNonNull(substitutionAlgorithm);

        final DittoHeaders dittoHeaders = modifyPolicyEntry.getDittoHeaders();
        final PolicyEntry existingPolicyEntry = modifyPolicyEntry.getPolicyEntry();
        final PolicyEntry resultEntry = substitutePolicyEntry(existingPolicyEntry, substitutionAlgorithm, dittoHeaders);

        if (existingPolicyEntry.equals(resultEntry)) {
            return modifyPolicyEntry;
        } else {
            return ModifyPolicyEntry.of(modifyPolicyEntry.getEntityId(), resultEntry, modifyPolicyEntry.getDittoHeaders());
        }
    }

}
