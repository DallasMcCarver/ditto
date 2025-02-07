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
package org.eclipse.ditto.services.policies.persistence.actors.strategies.commands;

import static org.eclipse.ditto.model.base.common.ConditionChecker.checkNotNull;

import java.util.Optional;

import javax.annotation.Nullable;

import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.DittoHeadersBuilder;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.Label;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.Subjects;
import org.eclipse.ditto.services.models.policies.PoliciesValidator;
import org.eclipse.ditto.services.policies.common.config.PolicyConfig;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.policies.modify.ModifySubjects;
import org.eclipse.ditto.signals.commands.policies.modify.ModifySubjectsResponse;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;
import org.eclipse.ditto.signals.events.policies.SubjectsModified;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.policies.modify.ModifySubjects} command.
 */
final class ModifySubjectsStrategy extends AbstractPolicyCommandStrategy<ModifySubjects, PolicyEvent<?>> {

    ModifySubjectsStrategy(final PolicyConfig policyConfig) {
        super(ModifySubjects.class, policyConfig);
    }

    @Override
    protected Result<PolicyEvent<?>> doApply(final Context<PolicyId> context,
            @Nullable final Policy policy,
            final long nextRevision,
            final ModifySubjects command,
            @Nullable final Metadata metadata) {

        final Policy nonNullPolicy = checkNotNull(policy, "policy");
        final PolicyId policyId = context.getState();
        final Label label = command.getLabel();
        final Subjects subjects = command.getSubjects();

        if (nonNullPolicy.getEntryFor(label).isPresent()) {
            final DittoHeadersBuilder<?, ?> adjustedHeadersBuilder = command.getDittoHeaders().toBuilder();
            final Subjects adjustedSubjects = potentiallyAdjustSubjects(subjects);
            final DittoHeaders adjustedHeaders = adjustedHeadersBuilder.build();
            final ModifySubjects adjustedCommand = ModifySubjects.of(command.getEntityId(), command.getLabel(),
                    adjustedSubjects, adjustedHeaders);

            final Policy newPolicy = nonNullPolicy.setSubjectsFor(label, adjustedSubjects);

            final Optional<Result<PolicyEvent<?>>> alreadyExpiredSubject =
                    checkForAlreadyExpiredSubject(newPolicy, adjustedHeaders, command);
            if (alreadyExpiredSubject.isPresent()) {
                return alreadyExpiredSubject.get();
            }

            final PoliciesValidator validator = PoliciesValidator.newInstance(newPolicy);

            if (validator.isValid()) {
                final SubjectsModified subjectsModified =
                        SubjectsModified.of(policyId, label, adjustedSubjects, nextRevision, getEventTimestamp(),
                                adjustedHeaders, metadata);
                final WithDittoHeaders response = appendETagHeaderIfProvided(adjustedCommand,
                        ModifySubjectsResponse.of(policyId, label, adjustedHeaders), nonNullPolicy);
                return ResultFactory.newMutationResult(adjustedCommand, subjectsModified, response);
            } else {
                return ResultFactory.newErrorResult(
                        policyEntryInvalid(policyId, label, validator.getReason().orElse(null), adjustedHeaders),
                        command);
            }
        } else {
            return ResultFactory.newErrorResult(policyEntryNotFound(policyId, label, command.getDittoHeaders()), command);
        }
    }

    @Override
    public Optional<EntityTag> previousEntityTag(final ModifySubjects command, @Nullable final Policy previousEntity) {
        return Optional.ofNullable(previousEntity)
                .flatMap(p -> p.getEntryFor(command.getLabel()))
                .map(PolicyEntry::getSubjects)
                .flatMap(EntityTag::fromEntity);
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final ModifySubjects command, @Nullable final Policy newEntity) {
        return Optional.of(command.getSubjects()).flatMap(EntityTag::fromEntity);
    }
}
