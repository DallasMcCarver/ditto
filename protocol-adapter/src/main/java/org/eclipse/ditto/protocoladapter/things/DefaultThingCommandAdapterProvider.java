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
package org.eclipse.ditto.protocoladapter.things;

import java.util.Arrays;
import java.util.List;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.protocoladapter.Adapter;
import org.eclipse.ditto.protocoladapter.HeaderTranslator;
import org.eclipse.ditto.protocoladapter.provider.ThingCommandAdapterProvider;
import org.eclipse.ditto.signals.base.ErrorRegistry;
import org.eclipse.ditto.signals.commands.messages.MessageCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommandResponse;
import org.eclipse.ditto.signals.commands.things.ThingErrorResponse;
import org.eclipse.ditto.signals.commands.things.modify.MergeThing;
import org.eclipse.ditto.signals.commands.things.modify.MergeThingResponse;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommand;
import org.eclipse.ditto.signals.commands.things.modify.ThingModifyCommandResponse;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThings;
import org.eclipse.ditto.signals.commands.things.query.RetrieveThingsResponse;
import org.eclipse.ditto.signals.commands.things.query.ThingQueryCommand;
import org.eclipse.ditto.signals.commands.things.query.ThingQueryCommandResponse;
import org.eclipse.ditto.signals.commands.thingsearch.ThingSearchCommand;
import org.eclipse.ditto.signals.events.things.ThingEvent;
import org.eclipse.ditto.signals.events.things.ThingMerged;
import org.eclipse.ditto.signals.events.thingsearch.SubscriptionEvent;


/**
 * Instantiates and provides {@link Adapter}s used to process Thing commands, responses, messages, events and errors.
 */
public class DefaultThingCommandAdapterProvider implements ThingCommandAdapterProvider {

    private final ThingQueryCommandAdapter queryCommandAdapter;
    private final ThingModifyCommandAdapter modifyCommandAdapter;
    private final ThingMergeCommandAdapter mergeCommandAdapter;
    private final ThingQueryCommandResponseAdapter queryCommandResponseAdapter;
    private final ThingModifyCommandResponseAdapter modifyCommandResponseAdapter;
    private final ThingMergeCommandResponseAdapter mergeCommandResponseAdapter;
    private final ThingSearchCommandAdapter searchCommandAdapter;
    private final MessageCommandAdapter messageCommandAdapter;
    private final MessageCommandResponseAdapter messageCommandResponseAdapter;
    private final ThingEventAdapter thingEventAdapter;
    private final ThingMergedEventAdapter thingMergedEventAdapter;
    private final SubscriptionEventAdapter subscriptionEventAdapter;
    private final ThingErrorResponseAdapter errorResponseAdapter;
    private final RetrieveThingsCommandAdapter retrieveThingsCommandAdapter;
    private final RetrieveThingsCommandResponseAdapter retrieveThingsCommandResponseAdapter;

    public DefaultThingCommandAdapterProvider(final ErrorRegistry<DittoRuntimeException> errorRegistry,
            final HeaderTranslator headerTranslator) {
        this.queryCommandAdapter = ThingQueryCommandAdapter.of(headerTranslator);
        this.retrieveThingsCommandAdapter = RetrieveThingsCommandAdapter.of(headerTranslator);
        this.retrieveThingsCommandResponseAdapter = RetrieveThingsCommandResponseAdapter.of(headerTranslator);
        this.modifyCommandAdapter = ThingModifyCommandAdapter.of(headerTranslator);
        this.mergeCommandAdapter = ThingMergeCommandAdapter.of(headerTranslator);
        this.queryCommandResponseAdapter = ThingQueryCommandResponseAdapter.of(headerTranslator);
        this.modifyCommandResponseAdapter = ThingModifyCommandResponseAdapter.of(headerTranslator);
        this.mergeCommandResponseAdapter = ThingMergeCommandResponseAdapter.of(headerTranslator);
        this.searchCommandAdapter = ThingSearchCommandAdapter.of(headerTranslator);
        this.messageCommandAdapter = MessageCommandAdapter.of(headerTranslator);
        this.messageCommandResponseAdapter = MessageCommandResponseAdapter.of(headerTranslator);
        this.thingEventAdapter = ThingEventAdapter.of(headerTranslator);
        this.thingMergedEventAdapter = ThingMergedEventAdapter.of(headerTranslator);
        this.subscriptionEventAdapter = SubscriptionEventAdapter.of(headerTranslator, errorRegistry);
        this.errorResponseAdapter = ThingErrorResponseAdapter.of(headerTranslator, errorRegistry);
    }

    @Override
    public List<Adapter<?>> getAdapters() {
        return Arrays.asList(
                queryCommandAdapter,
                retrieveThingsCommandAdapter,
                retrieveThingsCommandResponseAdapter,
                modifyCommandAdapter,
                mergeCommandAdapter,
                queryCommandResponseAdapter,
                modifyCommandResponseAdapter,
                mergeCommandResponseAdapter,
                messageCommandAdapter,
                messageCommandResponseAdapter,
                thingEventAdapter,
                thingMergedEventAdapter,
                searchCommandAdapter,
                subscriptionEventAdapter,
                errorResponseAdapter
        );
    }

    @Override
    public Adapter<ThingErrorResponse> getErrorResponseAdapter() {
        return errorResponseAdapter;
    }

    @Override
    public Adapter<ThingEvent<?>> getEventAdapter() {
        return thingEventAdapter;
    }

    @Override
    public Adapter<ThingMerged> getMergedEventAdapter() {
        return thingMergedEventAdapter;
    }

    @Override
    public Adapter<SubscriptionEvent<?>> getSubscriptionEventAdapter() {
        return subscriptionEventAdapter;
    }

    @Override
    public Adapter<ThingModifyCommand<?>> getModifyCommandAdapter() {
        return modifyCommandAdapter;
    }

    @Override
    public Adapter<MergeThing> getMergeCommandAdapter() {
        return mergeCommandAdapter;
    }

    @Override
    public Adapter<ThingModifyCommandResponse<?>> getModifyCommandResponseAdapter() {
        return modifyCommandResponseAdapter;
    }

    @Override
    public Adapter<MergeThingResponse> getMergeCommandResponseAdapter() {
        return mergeCommandResponseAdapter;
    }

    @Override
    public Adapter<ThingQueryCommand<?>> getQueryCommandAdapter() {
        return queryCommandAdapter;
    }

    @Override
    public Adapter<ThingQueryCommandResponse<?>> getQueryCommandResponseAdapter() {
        return queryCommandResponseAdapter;
    }

    @Override
    public Adapter<MessageCommand<?, ?>> getMessageCommandAdapter() {
        return messageCommandAdapter;
    }

    @Override
    public Adapter<MessageCommandResponse<?, ?>> getMessageCommandResponseAdapter() {
        return messageCommandResponseAdapter;
    }

    @Override
    public Adapter<ThingSearchCommand<?>> getSearchCommandAdapter() {
        return searchCommandAdapter;
    }

    @Override
    public Adapter<RetrieveThings> getRetrieveThingsCommandAdapter() {
        return retrieveThingsCommandAdapter;
    }

    @Override
    public Adapter<RetrieveThingsResponse> getRetrieveThingsCommandResponseAdapter() {
        return retrieveThingsCommandResponseAdapter;
    }
}
