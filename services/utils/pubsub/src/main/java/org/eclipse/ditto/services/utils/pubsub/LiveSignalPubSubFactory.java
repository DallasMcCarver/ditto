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
package org.eclipse.ditto.services.utils.pubsub;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.ditto.model.things.ThingId;
import org.eclipse.ditto.services.utils.pubsub.extractors.AckExtractor;
import org.eclipse.ditto.services.utils.pubsub.extractors.PubSubTopicExtractor;
import org.eclipse.ditto.services.utils.pubsub.extractors.ReadSubjectExtractor;
import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.base.SignalWithEntityId;

import akka.actor.ActorContext;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;

/**
 * Pub-sub factory for live signals.
 */
final class LiveSignalPubSubFactory extends AbstractPubSubFactory<SignalWithEntityId<?>> {

    private static final AckExtractor<SignalWithEntityId<?>> ACK_EXTRACTOR =
            AckExtractor.of(LiveSignalPubSubFactory::getThingId, Signal::getDittoHeaders);

    private static final DDataProvider PROVIDER = DDataProvider.of("live-signal-aware");

    @SuppressWarnings("unchecked")
    private LiveSignalPubSubFactory(final ActorRefFactory actorRefFactory,
            final ActorSystem actorSystem,
            final PubSubTopicExtractor<SignalWithEntityId<?>> topicExtractor,
            final DistributedAcks distributedAcks) {
        super(actorRefFactory, actorSystem, (Class<SignalWithEntityId<?>>) (Object) Signal.class, topicExtractor, PROVIDER,
                ACK_EXTRACTOR, distributedAcks);
    }

    /**
     * Create a pubsub factory for live signals from an actor context.
     *
     * @param context context of the actor under which the publisher and subscriber actors are started.
     * @param distributedAcks the distributed acks interface.
     * @return the thing
     */
    public static LiveSignalPubSubFactory of(final ActorContext context, final DistributedAcks distributedAcks) {
        return new LiveSignalPubSubFactory(context, context.system(), topicExtractor(), distributedAcks);
    }

    /**
     * Create a pubsub factory for live signals from an actor system.
     *
     * @param system the actor system.
     * @param distributedAcks the distributed acks interface.
     * @return the thing
     */
    public static LiveSignalPubSubFactory of(final ActorSystem system, final DistributedAcks distributedAcks) {
        return new LiveSignalPubSubFactory(system, system, topicExtractor(), distributedAcks);
    }

    private static Collection<String> getStreamingTypeTopic(final Signal<?> signal) {
        return StreamingType.fromSignal(signal)
                .map(StreamingType::getDistributedPubSubTopic)
                .map(Collections::singleton)
                .orElse(Collections.emptySet());
    }

    private static PubSubTopicExtractor<SignalWithEntityId<?>> topicExtractor() {
        return ReadSubjectExtractor.<SignalWithEntityId<?>>of().with(LiveSignalPubSubFactory::getStreamingTypeTopic);
    }

    // precondition: all live signals are thing signals.
    private static ThingId getThingId(final SignalWithEntityId<?> signal) {
        return ThingId.of(signal.getEntityId());
    }
}
