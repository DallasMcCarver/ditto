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
package org.eclipse.ditto.services.policies.persistence.actors.strategies.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.ditto.services.policies.persistence.TestConstants.Policy.NEW_ATTRIBUTE_RESOURCE_KEY;
import static org.eclipse.ditto.services.policies.persistence.TestConstants.Policy.READ_GRANTED;
import static org.eclipse.ditto.services.policies.persistence.TestConstants.Policy.SUPPORT_LABEL;
import static org.eclipse.ditto.services.policies.persistence.TestConstants.Policy.SUPPORT_SUBJECT_ID;

import java.time.Instant;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.services.policies.persistence.TestConstants;
import org.eclipse.ditto.signals.events.policies.ResourceCreated;

/**
 * Tests {@link ResourceCreatedStrategy}.
 */
public class ResourceCreatedStrategyTest extends AbstractPolicyEventStrategyTest<ResourceCreated> {

    @Override
    ResourceCreatedStrategy getStrategyUnderTest() {
        return new ResourceCreatedStrategy();
    }

    @Override
    ResourceCreated getPolicyEvent(final Instant instant, final Policy policy) {
        final PolicyId policyId = policy.getEntityId().orElseThrow();
        return ResourceCreated.of(policyId, SUPPORT_LABEL, TestConstants.Policy.NEW_ATTRIBUTE_RESOURCE,
                10L, instant, DittoHeaders.empty(), METADATA);
    }

    @Override
    protected void additionalAssertions(final Policy policyWithEventApplied) {
        assertThat(policyWithEventApplied.getEffectedPermissionsFor(SUPPORT_LABEL, SUPPORT_SUBJECT_ID,
                NEW_ATTRIBUTE_RESOURCE_KEY)).contains(READ_GRANTED);
    }
}