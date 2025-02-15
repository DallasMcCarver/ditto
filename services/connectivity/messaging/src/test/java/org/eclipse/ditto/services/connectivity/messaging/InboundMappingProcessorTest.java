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
package org.eclipse.ditto.services.connectivity.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.headers.DittoHeaderDefinition;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.headers.WithDittoHeaders;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.model.connectivity.ConnectionType;
import org.eclipse.ditto.model.connectivity.ConnectivityModelFactory;
import org.eclipse.ditto.model.connectivity.MappingContext;
import org.eclipse.ditto.model.connectivity.PayloadMappingDefinition;
import org.eclipse.ditto.services.connectivity.config.ConnectivityConfig;
import org.eclipse.ditto.services.connectivity.config.DittoConnectivityConfig;
import org.eclipse.ditto.services.connectivity.mapping.DittoMessageMapper;
import org.eclipse.ditto.services.connectivity.mapping.MessageMapperConfiguration;
import org.eclipse.ditto.services.connectivity.messaging.mappingoutcome.MappingOutcome;
import org.eclipse.ditto.services.models.connectivity.ExternalMessage;
import org.eclipse.ditto.services.models.connectivity.ExternalMessageFactory;
import org.eclipse.ditto.services.models.connectivity.MappedInboundExternalMessage;
import org.eclipse.ditto.services.utils.akka.logging.ThreadSafeDittoLoggingAdapter;
import org.eclipse.ditto.services.utils.config.DefaultScopedConfig;
import org.eclipse.ditto.services.utils.protocol.DittoProtocolAdapterProvider;
import org.eclipse.ditto.services.utils.protocol.ProtocolAdapterProvider;
import org.eclipse.ditto.services.utils.protocol.config.ProtocolConfig;
import org.eclipse.ditto.signals.base.SignalWithEntityId;
import org.eclipse.ditto.signals.commands.things.modify.ModifyThing;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

/**
 * Tests {@link InboundMappingProcessor}.
 */
public final class InboundMappingProcessorTest {

    private InboundMappingProcessor underTest;

    private static final String DITTO_MAPPER = "ditto";
    private static final Map<String, String> DITTO_MAPPER_CONDITIONS = Map.of(
            "testCondition", "fn:filter(header:correlation-id,'ne','testCor')",
            "testCondition2", "fn:filter(header:correlation-id,'ne','testCor2')");
    private static final String DITTO_MAPPER_BY_ALIAS = "ditto-by-alias";
    private static final String DITTO_MAPPER_CUSTOM_HEADER_BLOCKLIST = "ditto-cust-header";
    private static final String DROPPING_MAPPER = "dropping";
    private static final String FAILING_MAPPER = "faulty";
    private static final String DUPLICATING_MAPPER = "duplicating";

    private static ActorSystem actorSystem;
    private static ConnectivityConfig connectivityConfig;
    private static ProtocolAdapterProvider protocolAdapterProvider;
    private static ThreadSafeDittoLoggingAdapter logger;

    @BeforeClass
    public static void setUp() {
        actorSystem = ActorSystem.create("AkkaTestSystem", TestConstants.CONFIG);

        logger = Mockito.mock(ThreadSafeDittoLoggingAdapter.class);
        when(logger.withMdcEntry(Mockito.any(CharSequence.class), Mockito.nullable(CharSequence.class)))
                .thenReturn(logger);
        when(logger.withCorrelationId(Mockito.nullable(String.class))).thenReturn(logger);
        when(logger.withCorrelationId(Mockito.nullable(WithDittoHeaders.class))).thenReturn(logger);
        when(logger.withCorrelationId(Mockito.nullable(DittoHeaders.class))).thenReturn(logger);
        when(logger.withCorrelationId(Mockito.nullable(CharSequence.class))).thenReturn(logger);

        connectivityConfig =
                DittoConnectivityConfig.of(DefaultScopedConfig.dittoScoped(actorSystem.settings().config()));
        protocolAdapterProvider = new DittoProtocolAdapterProvider(Mockito.mock(ProtocolConfig.class));
    }

    @AfterClass
    public static void tearDown() {
        if (actorSystem != null) {
            TestKit.shutdownActorSystem(actorSystem);
        }
    }

    @Before
    public void init() {
        final Map<String, MappingContext> mappings = new HashMap<>();
        mappings.put(DITTO_MAPPER, DittoMessageMapper.CONTEXT);
        mappings.put(DITTO_MAPPER_BY_ALIAS, ConnectivityModelFactory.newMappingContext("Ditto", JsonObject.empty(),
                DITTO_MAPPER_CONDITIONS, Collections.emptyMap()));

        final Map<String, String> dittoCustomMapperHeaders = new HashMap<>();
        dittoCustomMapperHeaders.put(MessageMapperConfiguration.CONTENT_TYPE_BLOCKLIST, "foo/bar");
        final MappingContext dittoCustomMappingContext =
                ConnectivityModelFactory.newMappingContext("Ditto", dittoCustomMapperHeaders);
        mappings.put(DITTO_MAPPER_CUSTOM_HEADER_BLOCKLIST, dittoCustomMappingContext);
        mappings.put(FAILING_MAPPER, FaultyMessageMapper.CONTEXT);
        mappings.put(DROPPING_MAPPER, DroppingMessageMapper.CONTEXT);

        final Map<String, String> duplicatingMapperHeaders = new HashMap<>();
        duplicatingMapperHeaders.put(
                MessageMapperConfiguration.CONTENT_TYPE_BLOCKLIST,
                "text/custom-plain,application/custom-json"
        );
        final MappingContext duplicatingMappingContext =
                ConnectivityModelFactory.newMappingContext(DuplicatingMessageMapper.ALIAS, duplicatingMapperHeaders);
        mappings.put(DUPLICATING_MAPPER, duplicatingMappingContext);

        final PayloadMappingDefinition payloadMappingDefinition =
                ConnectivityModelFactory.newPayloadMappingDefinition(mappings);

        underTest = InboundMappingProcessor.of(ConnectionId.of("theConnection"), ConnectionType.AMQP_10,
                payloadMappingDefinition, actorSystem,
                connectivityConfig, protocolAdapterProvider.getProtocolAdapter(null), logger);
    }

    @Test
    public void testInboundMessageMapped() {
        testInbound(1, 0, 0);
    }

    @Test
    public void testInboundMessageWithCondition() {
        testInboundWithCor(0, 1, 0, DITTO_MAPPER_BY_ALIAS);
    }

    @Test
    public void testInboundMessageDropped() {
        testInbound(0, 1, 0, DROPPING_MAPPER);
    }

    @Test
    public void testInboundMessageDuplicated() {
        testInbound(2, 0, 0, DUPLICATING_MAPPER);
    }

    @Test
    public void testInboundMappingFails() {
        testInbound(0, 0, 1, FAILING_MAPPER);
    }

    @Test
    public void testInboundMessageDroppedFailedMappedDuplicated() {
        testInbound(2 /* duplicated */ + 1 /* mapped */, 1, 1,
                DROPPING_MAPPER, FAILING_MAPPER, DITTO_MAPPER, DUPLICATING_MAPPER);
    }

    @Test
    public void testInboundMessageDroppedForHonoEmptyNotificationMessagesWithDefaultMapper() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(ExternalMessage.CONTENT_TYPE_HEADER, "application/vnd.eclipse-hono-empty-notification");
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withPayloadMapping(ConnectivityModelFactory.newPayloadMapping())
                .build();
        testInbound(message, 0, 1, 0);
    }

    @Test
    public void testInboundMessageDroppedForHonoEmptyNotificationMessagesWithDittoMapper() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(ExternalMessage.CONTENT_TYPE_HEADER, "application/vnd.eclipse-hono-empty-notification");
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withPayloadMapping(ConnectivityModelFactory.newPayloadMapping(DITTO_MAPPER))
                .build();
        testInbound(message, 0, 1, 0);
    }

    @Test
    public void testInboundMessageDroppedForHonoEmptyNotificationMessagesWithDittoByAliasMapper() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(ExternalMessage.CONTENT_TYPE_HEADER, "application/vnd.eclipse-hono-empty-notification");
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withPayloadMapping(ConnectivityModelFactory.newPayloadMapping(DITTO_MAPPER_BY_ALIAS))
                .build();
        testInbound(message, 0, 1, 0);
    }

    @Test
    public void testInboundFailedForHonoEmptyNotificationMessagesWithCustomDittoMapper() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(ExternalMessage.CONTENT_TYPE_HEADER, "application/vnd.eclipse-hono-empty-notification");
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withPayloadMapping(ConnectivityModelFactory.newPayloadMapping(DITTO_MAPPER_CUSTOM_HEADER_BLOCKLIST))
                .build();
        // should fail because no payload was present:
        testInbound(message, 0, 0, 1);
    }

    @Test
    public void testInboundMessageDroppedForCustomContentType() {
        final Map<String, String> headers = new HashMap<>();
        headers.put(ExternalMessage.CONTENT_TYPE_HEADER, "application/custom-json");
        final ExternalMessage message = ExternalMessageFactory.newExternalMessageBuilder(headers)
                .withPayloadMapping(ConnectivityModelFactory.newPayloadMapping(DUPLICATING_MAPPER))
                .build();
        testInbound(message, 0, 1, 0);
    }

    private void testInbound(final int mapped, final int dropped, final int failed, final String... mappers) {
        final ExternalMessage externalMessage = ExternalMessageFactory
                .newExternalMessageBuilder(Collections.emptyMap())
                .withText(TestConstants.modifyThing())
                .withPayloadMapping(ConnectivityModelFactory.newPayloadMapping(mappers))
                .build();
        testInbound(externalMessage, mapped, dropped, failed);
    }

    private void testInboundWithCor(final int mapped,
            final int dropped,
            final int failed,
            final String... mappers) {

        final ExternalMessage externalMessage = ExternalMessageFactory
                .newExternalMessageBuilder(Collections.emptyMap())
                .withText(TestConstants.modifyThing())
                .withPayloadMapping(ConnectivityModelFactory.newPayloadMapping(mappers))
                .withAdditionalHeaders(DittoHeaders.newBuilder().correlationId("testCor").build())
                .build();
        testInbound(externalMessage, mapped, dropped, failed);
    }

    @SuppressWarnings("unchecked")
    private void testInbound(final ExternalMessage externalMessage,
            final int mapped,
            final int dropped,
            final int failed) {

        new TestKit(actorSystem) {{
            final MappingOutcome.Visitor<MappedInboundExternalMessage, Void> mock =
                    Mockito.mock(MappingOutcome.Visitor.class);
            underTest.process(externalMessage).forEach(x -> x.accept(mock));
            final ArgumentCaptor<MappedInboundExternalMessage> captor =
                    ArgumentCaptor.forClass(MappedInboundExternalMessage.class);
            verify(mock, times(mapped)).onMapped(any(String.class), captor.capture());
            verify(mock, times(failed)).onError(any(String.class), any(Exception.class), any(), any());
            verify(mock, times(dropped)).onDropped(any(String.class), any());

            assertThat(captor.getAllValues()).allSatisfy(mapped -> {
                assertThat(mapped.getSignal().getDittoHeaders()).containsEntry(
                        DittoHeaderDefinition.CORRELATION_ID.getKey(),
                        TestConstants.CORRELATION_ID);
                assertThat(mapped.getSignal()).isInstanceOf(SignalWithEntityId.class);
                Object actualId = ((SignalWithEntityId<?>) mapped.getSignal()).getEntityId();
                assertThat(actualId).isEqualTo(TestConstants.Things.THING_ID);
                assertThat(mapped.getSignal()).isInstanceOf(ModifyThing.class);
            });
        }};
    }

}
