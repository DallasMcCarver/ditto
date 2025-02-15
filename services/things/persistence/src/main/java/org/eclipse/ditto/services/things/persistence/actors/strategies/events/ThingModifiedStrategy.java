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
package org.eclipse.ditto.services.things.persistence.actors.strategies.events;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingBuilder;
import org.eclipse.ditto.model.things.ThingLifecycle;
import org.eclipse.ditto.signals.events.things.ThingModified;

/**
 * This strategy handles the {@link org.eclipse.ditto.signals.events.things.ThingModified} event.
 */
@Immutable
final class ThingModifiedStrategy extends AbstractThingEventStrategy<ThingModified> {

    protected ThingModifiedStrategy() {
        super();
    }

    /**
     * Merges the modifications from {@code thingWithModifications} to {@code thingBuilder}.
     * Merge is implemented very simple: All first level fields of {@code thingWithModifications} overwrite the first
     * level fields of {@code thingBuilder}.
     * If a field does not exist in the event's Thing, a maybe existing field in {@code thingBuilder} remains
     * unchanged.
     */
    @Override
    protected ThingBuilder.FromCopy applyEvent(final ThingModified event, final ThingBuilder.FromCopy thingBuilder) {

        // we need to use the current thing as base otherwise we would loose its state
        thingBuilder.setLifecycle(ThingLifecycle.ACTIVE);

        final Thing thingWithModifications = event.getThing();
        thingWithModifications.getPolicyEntityId().ifPresent(thingBuilder::setPolicyId);
        thingWithModifications.getAttributes().ifPresent(thingBuilder::setAttributes);
        thingWithModifications.getDefinition().ifPresent(thingBuilder::setDefinition);
        thingWithModifications.getFeatures().ifPresent(thingBuilder::setFeatures);

        return thingBuilder;
    }

}
