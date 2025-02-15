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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.ditto.services.models.placeholders.ExpressionResolver;
import org.eclipse.ditto.services.models.placeholders.PlaceholderFactory;
import org.eclipse.ditto.services.models.placeholders.UnresolvedPlaceholderException;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.SubjectId;
import org.eclipse.ditto.signals.commands.policies.actions.PolicyActionCommand;

/**
 * Default subject ID resolver utilizing the {@link PolicyEntryPlaceholder}.
 */
@SuppressWarnings("unused") // called by reflection
public final class DefaultSubjectIdFromActionResolver implements SubjectIdFromActionResolver {

    public DefaultSubjectIdFromActionResolver() {
        // no-op
    }

    @Override
    public Set<SubjectId> resolveSubjectIds(final PolicyEntry entry, final PolicyActionCommand<?> command) {
        return resolveSubjectId(entry, command.getSubjectIds());
    }

    static Set<SubjectId> resolveSubjectId(final PolicyEntry entry,
            final Collection<SubjectId> subjectIdsWithPlaceholder) {
        final ExpressionResolver expressionResolver = PlaceholderFactory.newExpressionResolver(
                PlaceholderFactory.newPlaceholderResolver(PoliciesPlaceholders.newPolicyEntryPlaceholder(), entry)
        );
        return subjectIdsWithPlaceholder.stream()
                .map(subjectId -> {
                    try {
                        return expressionResolver
                                .resolve(subjectId.toString())
                                .toOptional()
                                .map(SubjectId::newInstance)
                                .orElseThrow(
                                        () -> UnresolvedPlaceholderException.newBuilder(subjectId.toString()).build());
                    } catch (final UnresolvedPlaceholderException e) {
                        // Possible encounter of malformed UnresolvedPlaceholderException from expressionResolver.
                        throw UnresolvedPlaceholderException.newBuilder(subjectId.toString()).build();
                    }
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
