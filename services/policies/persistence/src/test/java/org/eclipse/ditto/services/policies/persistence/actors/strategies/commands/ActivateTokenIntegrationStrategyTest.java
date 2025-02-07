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
package org.eclipse.ditto.services.policies.persistence.actors.strategies.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.services.policies.persistence.TestConstants.Policy.LABEL;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import org.eclipse.ditto.model.base.common.DittoDuration;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.services.models.placeholders.UnresolvedPlaceholderException;
import org.eclipse.ditto.model.policies.Label;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.model.policies.ResourceKey;
import org.eclipse.ditto.model.policies.SubjectAnnouncement;
import org.eclipse.ditto.model.policies.SubjectExpiry;
import org.eclipse.ditto.model.policies.SubjectId;
import org.eclipse.ditto.model.policies.SubjectIdInvalidException;
import org.eclipse.ditto.model.policies.SubjectIssuer;
import org.eclipse.ditto.services.models.policies.Permission;
import org.eclipse.ditto.services.policies.common.config.DefaultPolicyConfig;
import org.eclipse.ditto.services.policies.persistence.TestConstants;
import org.eclipse.ditto.services.utils.persistentactors.commands.CommandStrategy;
import org.eclipse.ditto.signals.commands.policies.actions.ActivateTokenIntegration;
import org.eclipse.ditto.signals.commands.policies.actions.ActivateTokenIntegrationResponse;
import org.eclipse.ditto.signals.events.policies.SubjectCreated;
import org.junit.Before;
import org.junit.Test;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;

/**
 * Unit test for {@link ActivateTokenIntegrationStrategy}.
 */
public final class ActivateTokenIntegrationStrategyTest extends AbstractPolicyCommandStrategyTest {

    private ActivateTokenIntegrationStrategy underTest;

    @Before
    public void setUp() {
        underTest = new ActivateTokenIntegrationStrategy(
                DefaultPolicyConfig.of(ConfigFactory.load("activate-token-integration-test")),
                ActorSystem.create("test"));
    }

    @Test
    public void assertImmutability() {
        assertInstancesOf(ActivateTokenIntegrationStrategy.class, areImmutable());
    }

    @Test
    public void activateTokenIntegration() {
        final CommandStrategy.Context<PolicyId> context = getDefaultContext();
        final Instant expiry = Instant.now().plus(Duration.ofDays(1L));
        final SubjectId subjectId =
                SubjectId.newInstance(SubjectIssuer.INTEGRATION, "{{policy-entry:label}}:this-is-me");
        final SubjectId expectedSubjectId = SubjectId.newInstance(SubjectIssuer.INTEGRATION, LABEL + ":this-is-me");
        final DittoHeaders dittoHeaders = buildActivateTokenIntegrationHeaders();
        final ActivateTokenIntegration command =
                ActivateTokenIntegration.of(context.getState(), LABEL, Collections.singleton(subjectId), expiry,
                        dittoHeaders);
        assertModificationResult(underTest, TestConstants.Policy.POLICY, command,
                SubjectCreated.class,
                ActivateTokenIntegrationResponse.of(context.getState(), LABEL, Collections.singleton(expectedSubjectId),
                        dittoHeaders));
    }

    @Test
    public void roundUpNotifyBeforeDuration() {
        final CommandStrategy.Context<PolicyId> context = getDefaultContext();
        final Instant expiry = Instant.now().plus(Duration.ofDays(1L));
        final DittoDuration duration = DittoDuration.parseDuration("4s");
        final DittoDuration roundedUpDuration = DittoDuration.parseDuration("6s");
        final SubjectId subjectId = SubjectId.newInstance(SubjectIssuer.INTEGRATION, LABEL + ":this-is-me");
        final DittoHeaders dittoHeaders = buildActivateTokenIntegrationHeaders();

        // announcement duration is rounded up if it is not a multiple of the configured granularity (3s)
        final ActivateTokenIntegration commandToRoundUp =
                ActivateTokenIntegration.of(context.getState(), LABEL, Collections.singleton(subjectId),
                        SubjectExpiry.newInstance(expiry), SubjectAnnouncement.of(duration, false), dittoHeaders);
        final SubjectCreated event = (SubjectCreated) getEvent(
                applyStrategy(underTest, context, TestConstants.Policy.POLICY, commandToRoundUp));
        assertThat(event.getSubject().getAnnouncement().orElseThrow().getBeforeExpiry()).contains(roundedUpDuration);

        // announcement duration is not rounded up if it is a multiple of the configured granularity (3s)
        final ActivateTokenIntegration commandToNotRoundUp =
                ActivateTokenIntegration.of(context.getState(), LABEL, Collections.singleton(subjectId),
                        SubjectExpiry.newInstance(expiry), SubjectAnnouncement.of(roundedUpDuration, false),
                        dittoHeaders);
        final SubjectCreated notRoundedUpEvent = (SubjectCreated) getEvent(
                applyStrategy(underTest, context, TestConstants.Policy.POLICY, commandToNotRoundUp));
        assertThat(notRoundedUpEvent.getSubject().getAnnouncement().orElseThrow().getBeforeExpiry())
                .contains(roundedUpDuration);
    }

    @Test
    public void activateInvalidTokenIntegration() {
        final CommandStrategy.Context<PolicyId> context = getDefaultContext();
        final Instant expiry = Instant.now().plus(Duration.ofDays(1L));
        final SubjectId subjectId = SubjectId.newInstance("{{policy-entry:label}}");
        final DittoHeaders dittoHeaders = buildActivateTokenIntegrationHeaders();
        final ActivateTokenIntegration
                command =
                ActivateTokenIntegration.of(context.getState(), LABEL, Collections.singleton(subjectId), expiry,
                        dittoHeaders);
        assertErrorResult(underTest, TestConstants.Policy.POLICY, command,
                SubjectIdInvalidException.newBuilder(LABEL).build());
    }

    @Test
    public void activateUnresolvableTokenIntegration() {
        final CommandStrategy.Context<PolicyId> context = getDefaultContext();
        final Instant expiry = Instant.now().plus(Duration.ofDays(1L));
        final SubjectId subjectId = SubjectId.newInstance(SubjectIssuer.INTEGRATION, "{{fn:delete()}}");
        final DittoHeaders dittoHeaders = buildActivateTokenIntegrationHeaders();
        final ActivateTokenIntegration
                command =
                ActivateTokenIntegration.of(context.getState(), LABEL, Collections.singleton(subjectId), expiry,
                        dittoHeaders);
        assertErrorResult(underTest, TestConstants.Policy.POLICY, command,
                UnresolvedPlaceholderException.newBuilder("integration:{{fn:delete()}}").build());
    }

    @Test
    public void activateTokenIntegrationWithUnsupportedPlaceholder() {
        final CommandStrategy.Context<PolicyId> context = getDefaultContext();
        final Instant expiry = Instant.now().plus(Duration.ofDays(1L));
        final SubjectId subjectId = SubjectId.newInstance("{{request:subjectId}}");
        final DittoHeaders dittoHeaders = buildActivateTokenIntegrationHeaders();
        final ActivateTokenIntegration
                command =
                ActivateTokenIntegration.of(context.getState(), LABEL, Collections.singleton(subjectId), expiry,
                        dittoHeaders);
        assertErrorResult(underTest, TestConstants.Policy.POLICY, command,
                UnresolvedPlaceholderException.newBuilder("{{request:subjectId}}").build());
    }

    @Test
    public void rejectEntryWithoutAuthContextContainingPolicyEntrySubject() {
        final CommandStrategy.Context<PolicyId> context = getDefaultContext();
        final Instant expiry = Instant.now().plus(Duration.ofDays(1L));
        final SubjectId subjectId = SubjectId.newInstance("integration:this-is-me");
        final DittoHeaders dittoHeaders = DittoHeaders.empty();
        final ActivateTokenIntegration command =
                ActivateTokenIntegration.of(context.getState(), LABEL, Collections.singleton(subjectId), expiry,
                        dittoHeaders);
        assertErrorResult(underTest, TestConstants.Policy.POLICY, command,
                command.getNotApplicableException(dittoHeaders));
    }

    @Test
    public void rejectEntryWithoutThingReadPermission() {
        final CommandStrategy.Context<PolicyId> context = getDefaultContext();
        final Label label = Label.of("empty-entry");
        final Instant expiry = Instant.now().plus(Duration.ofDays(1L));
        final SubjectId subjectId = SubjectId.newInstance("integration:this-is-me");
        final DittoHeaders dittoHeaders = buildActivateTokenIntegrationHeaders();
        final ActivateTokenIntegration command =
                ActivateTokenIntegration.of(context.getState(), label, Collections.singleton(subjectId), expiry,
                        dittoHeaders);
        final Policy policy = TestConstants.Policy.POLICY.toBuilder()
                .forLabel(label)
                .setSubject(TestConstants.Policy.SUPPORT_SUBJECT)
                .setGrantedPermissions(ResourceKey.newInstance("policy:/"), Permission.READ)
                .build();
        assertErrorResult(underTest, policy, command, command.getNotApplicableException(dittoHeaders));
    }
}
