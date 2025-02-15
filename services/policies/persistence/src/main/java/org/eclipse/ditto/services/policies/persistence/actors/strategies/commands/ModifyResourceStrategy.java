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
import org.eclipse.ditto.model.base.headers.entitytag.EntityTag;
import org.eclipse.ditto.model.policies.Label;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyEntry;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.Resource;
import org.eclipse.ditto.services.models.policies.PoliciesValidator;
import org.eclipse.ditto.services.policies.common.config.PolicyConfig;
import org.eclipse.ditto.services.utils.persistentactors.results.Result;
import org.eclipse.ditto.services.utils.persistentactors.results.ResultFactory;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyResource;
import org.eclipse.ditto.signals.commands.policies.modify.ModifyResourceResponse;
import org.eclipse.ditto.signals.events.policies.PolicyEvent;
import org.eclipse.ditto.signals.events.policies.ResourceCreated;
import org.eclipse.ditto.signals.events.policies.ResourceModified;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.commands.policies.modify.ModifyResource} command.
 */
final class ModifyResourceStrategy extends AbstractPolicyCommandStrategy<ModifyResource, PolicyEvent<?>> {

    ModifyResourceStrategy(final PolicyConfig policyConfig) {
        super(ModifyResource.class, policyConfig);
    }

    @Override
    protected Result<PolicyEvent<?>> doApply(final Context<PolicyId> context,
            @Nullable final Policy policy,
            final long nextRevision,
            final ModifyResource command,
            @Nullable final Metadata metadata) {

        final Policy nonNullPolicy = checkNotNull(policy, "policy");
        final PolicyId policyId = context.getState();
        final Label label = command.getLabel();
        final Resource resource = command.getResource();
        final DittoHeaders dittoHeaders = command.getDittoHeaders();

        final Optional<PolicyEntry> optionalEntry = nonNullPolicy.getEntryFor(label);
        if (optionalEntry.isPresent()) {
            final PoliciesValidator validator =
                    PoliciesValidator.newInstance(nonNullPolicy.setResourceFor(label, resource));

            if (validator.isValid()) {
                final PolicyEntry policyEntry = optionalEntry.get();
                final PolicyEvent<?> eventToPersist;
                final ModifyResourceResponse rawResponse;

                if (policyEntry.getResources().getResource(resource.getResourceKey()).isPresent()) {
                    rawResponse =
                            ModifyResourceResponse.modified(policyId, label, resource.getResourceKey(), dittoHeaders);
                    eventToPersist = ResourceModified.of(policyId, label, resource, nextRevision, getEventTimestamp(),
                            dittoHeaders, metadata);
                } else {
                    rawResponse = ModifyResourceResponse.created(policyId, label, resource, dittoHeaders);
                    eventToPersist = ResourceCreated.of(policyId, label, resource, nextRevision, getEventTimestamp(),
                            dittoHeaders, metadata);
                }

                return ResultFactory.newMutationResult(command, eventToPersist,
                        appendETagHeaderIfProvided(command, rawResponse, nonNullPolicy));
            } else {
                return ResultFactory.newErrorResult(
                        policyEntryInvalid(policyId, label, validator.getReason().orElse(null), dittoHeaders), command);
            }
        } else {
            return ResultFactory.newErrorResult(policyEntryNotFound(policyId, label, dittoHeaders), command);
        }
    }

    @Override
    public Optional<EntityTag> previousEntityTag(final ModifyResource command, @Nullable final Policy previousEntity) {
        return Optional.ofNullable(previousEntity)
                .flatMap(p -> p.getEntryFor(command.getLabel()))
                .map(PolicyEntry::getResources)
                .flatMap(r -> r.getResource(command.getResource().getResourceKey()))
                .flatMap(EntityTag::fromEntity);
    }

    @Override
    public Optional<EntityTag> nextEntityTag(final ModifyResource command, @Nullable final Policy newEntity) {
        return Optional.of(command.getResource()).flatMap(EntityTag::fromEntity);
    }
}
