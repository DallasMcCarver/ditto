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
package org.eclipse.ditto.services.things.persistence.serializer;

import java.time.Instant;
import java.util.Optional;

import javax.annotation.concurrent.ThreadSafe;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.entity.Revision;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.model.things.ThingLifecycle;
import org.eclipse.ditto.model.things.ThingsModelFactory;
import org.eclipse.ditto.services.base.persistence.PersistenceLifecycle;
import org.eclipse.ditto.services.base.persistence.SnapshotTaken;
import org.eclipse.ditto.services.models.things.ThingSnapshotTaken;
import org.eclipse.ditto.services.utils.cluster.DistPubSubAccess;
import org.eclipse.ditto.services.utils.persistence.mongo.AbstractMongoSnapshotAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

/**
 * A {@link org.eclipse.ditto.services.utils.persistence.SnapshotAdapter} for snapshotting a
 * {@link org.eclipse.ditto.model.things.Thing}.
 */
@ThreadSafe
public final class ThingMongoSnapshotAdapter extends AbstractMongoSnapshotAdapter<Thing> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThingMongoSnapshotAdapter.class);

    private final ActorRef pubSubMediator;

    /**
     * Constructs a new {@code ThingMongoSnapshotAdapter}.
     *
     * @param pubSubMediator Akka pubsub mediator with which to publish snapshot events.
     */
    public ThingMongoSnapshotAdapter(final ActorRef pubSubMediator) {
        super(LOGGER);
        this.pubSubMediator = pubSubMediator;
    }

    @Override
    protected Thing createJsonifiableFrom(final JsonObject jsonObject) {
        return ThingsModelFactory.newThing(jsonObject);
    }

    @Override
    protected void onSnapshotStoreConversion(final Thing thing, final JsonObject thingJson) {
        final Optional<ThingId> thingId = thing.getEntityId();
        if (thingId.isPresent()) {
            final var thingSnapshotTaken = ThingSnapshotTaken.newBuilder(thingId.get(),
                    thing.getRevision().map(Revision::toLong).orElse(0L),
                    thing.getLifecycle()
                            .map(ThingLifecycle::name)
                            .flatMap(PersistenceLifecycle::forName)
                            .orElse(PersistenceLifecycle.ACTIVE),
                    thingJson)
                    .timestamp(Instant.now())
                    .build();
            publishThingSnapshotTaken(thingSnapshotTaken);
        } else {
            LOGGER.warn("Could not publish snapshot taken event for thing <{}>.", thing);
        }
    }

    private void publishThingSnapshotTaken(final SnapshotTaken<ThingSnapshotTaken> snapshotTakenEvent) {
        final var publish = DistPubSubAccess.publishViaGroup(snapshotTakenEvent.getPubSubTopic(), snapshotTakenEvent);
        pubSubMediator.tell(publish, ActorRef.noSender());
    }

}
