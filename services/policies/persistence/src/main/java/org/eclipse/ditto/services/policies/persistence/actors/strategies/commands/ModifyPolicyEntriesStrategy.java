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

import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonArray;
import org.eclipse.ditto.json.JsonCollectors;
import org.eclipse.ditto.model.base.entity.metadata.Metadata;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.DittoHeadersBuilder;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.PolicyTooLargeException;
import org.eclipse.ditto.services.models.policies.PoliciesValidator;
import org.eclipse.ditto.services.policies.common.config.PolicyConfig;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandSizeValidator;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyEntries;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyEntriesResponse;
import org.eclipse.ditto.signals.events.policies.PolicyEntriesModified;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.policies.modify.ModifyPolicyEntries} command.
 */
final class ModifyPolicyEntriesStrategy extends AbstractPolicyCommandStrategy<ModifyPolicyEntries, PolicyEvent<?>> {

    ModifyPolicyEntriesStrategy(final PolicyConfig policyConfig) {
        super(ModifyPolicyEntries.class, policyConfig);
    }

    @Override
    protected Result<PolicyEvent<?>> doApply(final Context<PolicyId> context,
            @Nullable final Policy entity,
            final long nextRevision,
            final ModifyPolicyEntries command,
            @Nullable final Metadata metadata) {

        final Iterable<PolicyEntry> policyEntries = command.getPolicyEntries();
        final JsonArray policyEntriesJsonArray = StreamSupport.stream(policyEntries.spliterator(), false)
                .map(PolicyEntry::toJson)
                .collect(JsonCollectors.valuesToArray());

        try {
            PolicyCommandSizeValidator.getInstance().ensureValidSize(
                    policyEntriesJsonArray::getUpperBoundForStringSize,
                    () -> policyEntriesJsonArray.toString().length(),
                    command::getDittoHeaders);
        } catch (final PolicyTooLargeException e) {
            return ResultFactory.newErrorResult(e, command);
        }

        final DittoHeadersBuilder<?, ?> adjustedHeadersBuilder = command.getDittoHeaders().toBuilder();
        final Set<PolicyEntry> adjustedEntries = potentiallyAdjustPolicyEntries(policyEntries);
        final DittoHeaders adjustedHeaders = adjustedHeadersBuilder.build();
        final ModifyPolicyEntries adjustedCommand = ModifyPolicyEntries.of(command.getEntityId(), adjustedEntries,
                adjustedHeaders);

        final Optional<Result<PolicyEvent<?>>> alreadyExpiredSubject =
                checkForAlreadyExpiredSubject(policyEntries, adjustedHeaders, command);
        if (alreadyExpiredSubject.isPresent()) {
            return alreadyExpiredSubject.get();
        }

        final PoliciesValidator validator = PoliciesValidator.newInstance(adjustedEntries);

        if (validator.isValid()) {
            final PolicyId policyId = context.getState();
            final PolicyEntriesModified policyEntriesModified = PolicyEntriesModified.of(policyId, adjustedEntries,
                    nextRevision, getEventTimestamp(), adjustedHeaders, metadata);
            final WithDittoHeaders response = appendETagHeaderIfProvided(adjustedCommand,
                    ModifyPolicyEntriesResponse.of(policyId, adjustedHeaders), entity);

            return ResultFactory.newMutationResult(adjustedCommand, policyEntriesModified, response);
        } else {
            return ResultFactory.newErrorResult(
                    policyInvalid(context.getState(), validator.getReason().orElse(null), adjustedHeaders),
                    command);
        }
    }

    @Override
    public Optional<EntityTag> previousEntityTag(final ModifyPolicyEntries command,
            @Nullable final Policy previousEntity) {
        return Optional.ofNullable(previousEntity).map(Policy::getEntriesSet).flatMap(EntityTag::fromEntity);
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final ModifyPolicyEntries command, @Nullable final Policy newEntity) {
        return Optional.of(command.getPolicyEntries()).flatMap(EntityTag::fromEntity);
    }
}
