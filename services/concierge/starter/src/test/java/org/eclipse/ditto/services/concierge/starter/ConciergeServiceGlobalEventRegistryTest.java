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
package org.eclipse.ditto.services.concierge.starter;

import org.eclipse.ditto.services.models.things.ThingSnapshotTaken;
import org.eclipse.ditto.services.utils.test.GlobalEventRegistryTestCases;
import org.eclipse.ditto.signals.events.connectivity.ConnectionCreated;
import org.eclipse.ditto.signals.events.policies.ResourceDeleted;
import org.eclipse.ditto.signals.events.things.FeatureDeleted;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionComplete;

public final class ConciergeServiceGlobalEventRegistryTest extends GlobalEventRegistryTestCases {

    public ConciergeServiceGlobalEventRegistryTest() {
        super(
                ConnectionCreated.class,
                ResourceDeleted.class,
                SubscriptionComplete.class,
                FeatureDeleted.class,
                ThingSnapshotTaken.class
        );
    }

}
