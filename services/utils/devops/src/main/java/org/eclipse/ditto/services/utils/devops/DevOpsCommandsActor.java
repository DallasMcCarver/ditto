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
package org.eclipse.ditto.services.utils.devops;

import static akka.cluster.pubsub.DistributedPubSubMediator.Publish;
import static akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.eclipse.ditto.json.JsonFactory;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.model.base.common.HttpStatusCode;
import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.base.json.Jsonifiable;
import org.eclipse.ditto.model.devops.LoggerConfig;
import org.eclipse.ditto.model.devops.LoggingFacade;
import org.eclipse.ditto.services.utils.akka.LogUtil;
import org.eclipse.ditto.services.utils.akka.actors.RetrieveConfigBehavior;
import org.eclipse.ditto.services.utils.akka.logging.DittoDiagnosticLoggingAdapter;
import org.eclipse.ditto.services.utils.akka.logging.DittoLoggerFactory;
import org.eclipse.ditto.services.utils.cluster.DistPubSubAccess;
import org.eclipse.ditto.services.utils.cluster.MappingStrategies;
import org.eclipse.ditto.signals.base.JsonParsable;
import org.eclipse.ditto.signals.base.JsonTypeNotParsableException;
import org.eclipse.ditto.signals.commands.base.Command;
import org.eclipse.ditto.signals.commands.base.CommandResponse;
import org.eclipse.ditto.signals.commands.base.exceptions.GatewayInternalErrorException;
import org.eclipse.ditto.signals.commands.devops.AggregatedDevOpsCommandResponse;
import org.eclipse.ditto.signals.commands.devops.ChangeLogLevel;
import org.eclipse.ditto.signals.commands.devops.ChangeLogLevelResponse;
import org.eclipse.ditto.signals.commands.devops.DevOpsCommand;
import org.eclipse.ditto.signals.commands.devops.DevOpsCommandResponse;
import org.eclipse.ditto.signals.commands.devops.DevOpsErrorResponse;
import org.eclipse.ditto.signals.commands.devops.ExecutePiggybackCommand;
import org.eclipse.ditto.signals.commands.devops.RetrieveLoggerConfig;
import org.eclipse.ditto.signals.commands.devops.RetrieveLoggerConfigResponse;

import com.typesafe.config.Config;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.cluster.pubsub.DistributedPubSub;
import akka.event.DiagnosticLoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

/**
 * An actor to consume {@link org.eclipse.ditto.signals.commands.devops.DevOpsCommand}s and reply appropriately.
 */
public final class DevOpsCommandsActor extends AbstractActor implements RetrieveConfigBehavior {

    /**
     * The name of this Actor in the ActorSystem.
     */
    public static final String ACTOR_NAME = "devOpsCommandsActor";

    /**
     * Ditto header to turn aggregation on and off.
     */
    public static final String AGGREGATE_HEADER = "aggregate";

    private static final String UNKNOWN_MESSAGE_TEMPLATE = "Unknown message: {}";
    private static final String TOPIC_HEADER = "topic";
    private static final String IS_GROUP_TOPIC_HEADER = "is-group-topic";

    private final DittoDiagnosticLoggingAdapter logger = DittoLoggerFactory.getDiagnosticLoggingAdapter(this);

    private final LoggingFacade loggingFacade;

    private final String serviceName;
    private final String instance;
    private final ActorRef pubSubMediator;
    private final MappingStrategies serviceMappingStrategy;

    @SuppressWarnings("unused")
    private DevOpsCommandsActor(final LoggingFacade loggingFacade, final String serviceName, final String instance) {
        this.loggingFacade = loggingFacade;
        this.serviceName = serviceName;
        this.instance = instance;

        pubSubMediator = DistributedPubSub.get(getContext().system()).mediator();
        serviceMappingStrategy = MappingStrategies.loadMappingStrategies(getContext().getSystem());
        getContext().actorOf(
                PubSubSubscriberActor.props(pubSubMediator, serviceName, instance,
                        RetrieveLoggerConfig.TYPE,
                        ChangeLogLevel.TYPE,
                        ExecutePiggybackCommand.TYPE
                ),
                "pubSubSubscriber");
    }

    /**
     * Creates Akka configuration object Props for this Actor.
     *
     * @param loggingFacade a facade providing logging functionality.
     * @param serviceName name of the microservice.
     * @param instance instance number of the microservice instance.
     * @return the Akka configuration Props object.
     */
    public static Props props(final LoggingFacade loggingFacade, final String serviceName, final String instance) {

        return Props.create(DevOpsCommandsActor.class, loggingFacade, serviceName, instance);
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(DevOpsCommand.class, this::handleInitialDevOpsCommand)
                .match(DevOpsCommandViaPubSub.class, this::handleDevOpsCommandViaPubSub)
                .build()
                .orElse(retrieveConfigBehavior())
                .orElse(matchAnyUnhandled());
    }

    private Receive matchAnyUnhandled() {
        return ReceiveBuilder.create()
                .matchAny(m -> {
                    logger.warning(UNKNOWN_MESSAGE_TEMPLATE, m);
                    unhandled(m);
                }).build();
    }

    @Override
    public Config getConfig() {
        return getContext().getSystem().settings().config();
    }

    /**
     * DevOps commands issued via the HTTP DevopsRoute are handled here on the (gateway) cluster node which got the HTTP
     * request. The job now is to:
     * <ul>
     * <li>publish the command in the cluster (so that all services which should react on that command get it)</li>
     * <li>start aggregation of responses</li>
     * </ul>
     *
     * @param command the initial DevOpsCommand to handle
     */
    private void handleInitialDevOpsCommand(final DevOpsCommand<?> command) {
        logger.setCorrelationId(command);

        final Supplier<ActorRef> responseCorrelationActor = () -> getContext().actorOf(
                DevOpsCommandResponseCorrelationActor.props(getSender(), command),
                command.getDittoHeaders().getCorrelationId()
                        .orElseThrow(() -> new IllegalArgumentException("Missing correlation-id for DevOpsCommand!")));

        if (isExecutePiggybackCommandToPubSubMediator(command)) {
            tryInterpretAsDirectPublication(command, publish -> {
                logger.info("Publishing <{}> into cluster on topic <{}> with sendOneMessageToEachGroup=<{}>",
                        publish.msg().getClass().getCanonicalName(),
                        publish.topic(),
                        publish.sendOneMessageToEachGroup());
                pubSubMediator.tell(publish, responseCorrelationActor.get());
            }, devOpsErrorResponse -> {
                logger.warning("Dropping publishing command <{}>. Reason: <{}>", command,
                        devOpsErrorResponse.getDittoRuntimeException());
                getSender().tell(devOpsErrorResponse, getSelf());
            });
        } else {
            final String topic;
            final Optional<String> commandServiceNameOpt = command.getServiceName();
            if (commandServiceNameOpt.isPresent()) {
                final String commandServiceName = commandServiceNameOpt.get();
                final String commandInstance = command.getInstance().orElse(null);
                if (commandInstance != null) {
                    topic = command.getType() + ":" + commandServiceName + ":" + commandInstance;
                } else {
                    topic = command.getType() + ":" + commandServiceName;
                }
            } else {
                topic = command.getType();
            }
            final Publish msg;
            if (isGroupTopic(command.getDittoHeaders())) {
                msg = DistPubSubAccess.publishViaGroup(topic, command);
            } else {
                msg = DistPubSubAccess.publish(topic, command);
            }
            logger.info("Publishing DevOpsCommand <{}> into cluster on topic <{}> with " +
                    "sendOneMessageToEachGroup=<{}>", command.getType(), msg.topic(), msg.sendOneMessageToEachGroup());
            pubSubMediator.tell(msg, responseCorrelationActor.get());
        }
        logger.discardCorrelationId();
    }

    private boolean isExecutePiggybackCommandToPubSubMediator(final DevOpsCommand<?> command) {
        if (command instanceof ExecutePiggybackCommand) {
            final ExecutePiggybackCommand executePiggyback = (ExecutePiggybackCommand) command;
            return Objects.equals(executePiggyback.getTargetActorSelection(),
                    pubSubMediator.path().toStringWithoutAddress());
        }
        return false;
    }

    private void tryInterpretAsDirectPublication(final DevOpsCommand<?> command, final Consumer<Publish> onSuccess,
            final Consumer<DevOpsErrorResponse> onError) {

        if (command instanceof ExecutePiggybackCommand) {
            final ExecutePiggybackCommand executePiggyback = (ExecutePiggybackCommand) command;
            final DittoHeaders dittoHeaders = executePiggyback.getDittoHeaders();
            deserializePiggybackCommand(executePiggyback,
                    jsonifiable -> {
                        final Optional<String> topic =
                                Optional.ofNullable(dittoHeaders.get(TOPIC_HEADER))
                                        .map(Optional::of)
                                        .orElseGet(() -> executePiggyback.getPiggybackCommand()
                                                .getValue(Command.JsonFields.TYPE));
                        if (topic.isPresent()) {
                            if (isGroupTopic(dittoHeaders)) {
                                onSuccess.accept(DistPubSubAccess.publishViaGroup(topic.get(), jsonifiable));
                            } else {
                                onSuccess.accept(DistPubSubAccess.publish(topic.get(), jsonifiable));
                            }
                        } else {
                            onError.accept(getErrorResponse(command));
                        }
                    },
                    dittoRuntimeException -> onError.accept(getErrorResponse(command, dittoRuntimeException.toJson())));

        } else {
            // this should not happen
            final JsonObject error =
                    GatewayInternalErrorException.newBuilder().dittoHeaders(command.getDittoHeaders()).build().toJson();
            onError.accept(getErrorResponse(command, error));
        }
    }

    private static boolean isGroupTopic(final DittoHeaders dittoHeaders) {
        final String isGroupTopicValue = dittoHeaders.get(IS_GROUP_TOPIC_HEADER);
        return isGroupTopicValue != null && !"false".equalsIgnoreCase(isGroupTopicValue);
    }

    private void handleDevOpsCommandViaPubSub(final DevOpsCommandViaPubSub devOpsCommandViaPubSub) {
        final DevOpsCommand wrappedCommand = devOpsCommandViaPubSub.wrappedCommand;
        if (wrappedCommand instanceof ChangeLogLevel) {
            handleChangeLogLevel((ChangeLogLevel) wrappedCommand);
        } else if (wrappedCommand instanceof RetrieveLoggerConfig) {
            handleRetrieveLoggerConfig((RetrieveLoggerConfig) wrappedCommand);
        } else if (wrappedCommand instanceof ExecutePiggybackCommand) {
            handleExecutePiggyBack((ExecutePiggybackCommand) wrappedCommand);
        }
    }

    private void handleChangeLogLevel(final ChangeLogLevel command) {
        final Boolean isApplied = loggingFacade.setLogLevel(command.getLoggerConfig());
        final ChangeLogLevelResponse changeLogLevelResponse =
                ChangeLogLevelResponse.of(serviceName, instance, isApplied, command.getDittoHeaders());
        getSender().tell(changeLogLevelResponse, getSelf());
    }

    private void handleRetrieveLoggerConfig(final RetrieveLoggerConfig command) {
        final List<LoggerConfig> loggerConfigs;

        if (command.isAllKnownLoggers()) {
            loggerConfigs = loggingFacade.getLoggerConfig();
        } else {
            loggerConfigs = loggingFacade.getLoggerConfig(command.getSpecificLoggers());
        }

        final RetrieveLoggerConfigResponse retrieveLoggerConfigResponse =
                RetrieveLoggerConfigResponse.of(serviceName, instance, loggerConfigs, command.getDittoHeaders());
        getSender().tell(retrieveLoggerConfigResponse, getSelf());
    }

    private void handleExecutePiggyBack(final ExecutePiggybackCommand command) {
        deserializePiggybackCommand(command,
                jsonifiable -> {
                    logger.withCorrelationId(command)
                            .info("Received PiggybackCommand: <{}> - telling to: <{}>", jsonifiable,
                                    command.getTargetActorSelection());
                    getContext().actorSelection(command.getTargetActorSelection()).forward(jsonifiable, getContext());
                },
                dittoRuntimeException -> getSender().tell(dittoRuntimeException, getSelf()));
    }

    private void deserializePiggybackCommand(final ExecutePiggybackCommand command,
            final Consumer<Jsonifiable<?>> onSuccess, final Consumer<DittoRuntimeException> onError) {

        final JsonObject piggybackCommandJson = command.getPiggybackCommand();
        @Nullable final String piggybackCommandType = piggybackCommandJson.getValue(Command.JsonFields.TYPE)
                .orElse(null);
        final Consumer<JsonParsable<Jsonifiable<?>>> action = mappingStrategy -> {
            try {
                onSuccess.accept(mappingStrategy.parse(piggybackCommandJson, command.getDittoHeaders()));
            } catch (final DittoRuntimeException e) {
                logger.withCorrelationId(command)
                        .warning("Got DittoRuntimeException while parsing PiggybackCommand <{}>: {}!",
                                piggybackCommandType, e);
                onError.accept(e);
            }
        };
        final Runnable emptyAction = () -> {
            final String msgPattern = "ExecutePiggybackCommand with PiggybackCommand <%s> cannot be executed by this"
                    + " service as there is no mapping strategy for it!";
            final String message = String.format(msgPattern, piggybackCommandType);
            logger.withCorrelationId(command).warning(message);
            onError.accept(JsonTypeNotParsableException.fromMessage(message, command.getDittoHeaders()));
        };
        serviceMappingStrategy.getMappingStrategy(piggybackCommandType)
                .ifPresentOrElse(action, emptyAction);
    }

    private static DevOpsErrorResponse getErrorResponse(final DevOpsCommand<?> command, final JsonObject error) {
        final String serviceName = command.getServiceName().orElse(null);
        final String instance = command.getInstance().map(String::valueOf).orElse(null);
        return DevOpsErrorResponse.of(serviceName, instance, error, command.getDittoHeaders());
    }

    private static DevOpsErrorResponse getErrorResponse(final DevOpsCommand<?> command) {
        final JsonObject error = JsonFactory.newObjectBuilder()
                .set(DittoRuntimeException.JsonFields.STATUS, HttpStatusCode.BAD_REQUEST.toInt())
                .set(DittoRuntimeException.JsonFields.MESSAGE,
                        "No topic found for publishing. Did you set the <topic> header?")
                .build();

        return getErrorResponse(command, error);
    }

    /**
     * Child actor handling the distributed pub/sub subscriptions of DevOpsCommands in the cluster.
     */
    private static final class PubSubSubscriberActor extends AbstractActor {

        private final DiagnosticLoggingAdapter log = LogUtil.obtain(this);

        @SuppressWarnings("unused")
        private PubSubSubscriberActor(final ActorRef pubSubMediator, final String serviceName, final String instance,
                final String... pubSubTopicsToSubscribeTo) {

            Arrays.stream(pubSubTopicsToSubscribeTo).forEach(topic ->
                    subscribeToDevOpsTopic(pubSubMediator, topic, serviceName, instance));
        }

        /**
         * @return the Akka configuration Props object.
         */
        static Props props(final ActorRef pubSubMediator,
                final String serviceName,
                final String instance,
                final String... pubSubTopicsToSubscribeTo) {

            return Props.create(PubSubSubscriberActor.class, pubSubMediator, serviceName, instance,
                    pubSubTopicsToSubscribeTo);
        }

        private void subscribeToDevOpsTopic(final ActorRef pubSubMediator,
                final String topic,
                final String serviceName,
                final String instance) {

            pubSubMediator.tell(DistPubSubAccess.subscribe(topic, getSelf()), getSelf());
            pubSubMediator.tell(DistPubSubAccess.subscribe(String.join(":", topic, serviceName), getSelf()), getSelf());
            pubSubMediator.tell(
                    DistPubSubAccess.subscribeViaGroup(String.join(":", topic, serviceName), serviceName, getSelf()), getSelf());
            pubSubMediator.tell(DistPubSubAccess.subscribe(String.join(":", topic, serviceName, instance), getSelf()), getSelf());
        }

        @Override
        public Receive createReceive() {
            return ReceiveBuilder.create()
                    .match(DevOpsCommand.class, command -> getContext().getParent().forward(
                            new DevOpsCommandViaPubSub(command), getContext()
                    ))
                    .match(SubscribeAck.class, this::handleSubscribeAck)
                    .matchAny(m -> {
                        log.warning(UNKNOWN_MESSAGE_TEMPLATE, m);
                        unhandled(m);
                    }).build();
        }

        private void handleSubscribeAck(final SubscribeAck subscribeAck) {
            log.info("Successfully subscribed to distributed pub/sub on topic <{}>.", subscribeAck.subscribe().topic());
        }
    }

    private static final class DevOpsCommandViaPubSub {

        private final DevOpsCommand wrappedCommand;

        private DevOpsCommandViaPubSub(final DevOpsCommand wrappedCommand) {
            this.wrappedCommand = wrappedCommand;
        }

    }

    /**
     * Child actor correlating the {@link DevOpsCommandResponse}s.
     * Waits for and collects responses until time runs out, then forwards all collected responses to the sender of the
     * original DevOps command.
     */
    private static final class DevOpsCommandResponseCorrelationActor extends AbstractActor {

        private static final Duration DEFAULT_RECEIVE_TIMEOUT = Duration.ofMillis(5000);
        private static final boolean DEFAULT_AGGREGATE = true;

        /**
         * @return the Akka configuration Props object.
         */
        static Props props(final ActorRef devOpsCommandSender, final DevOpsCommand<?> devOpsCommand) {
            return Props.create(DevOpsCommandResponseCorrelationActor.class, devOpsCommandSender, devOpsCommand);
        }

        private final DiagnosticLoggingAdapter log = LogUtil.obtain(this);

        private final ActorRef devOpsCommandSender;
        private final DevOpsCommand<?> devOpsCommand;
        private final List<CommandResponse<?>> commandResponses = new ArrayList<>();
        private final boolean aggregateResults;

        @SuppressWarnings("unused")
        private DevOpsCommandResponseCorrelationActor(final ActorRef devOpsCommandSender,
                final DevOpsCommand<?> devOpsCommand) {

            this.devOpsCommandSender = devOpsCommandSender;
            this.devOpsCommand = devOpsCommand;

            final DittoHeaders dittoHeaders = devOpsCommand.getDittoHeaders();
            aggregateResults = isAggregateResults(dittoHeaders);
            getContext().setReceiveTimeout(getReceiveTimeout(dittoHeaders));
        }

        private static boolean isAggregateResults(final DittoHeaders dittoHeaders) {
            boolean result = DEFAULT_AGGREGATE;
            final String aggregateHeaderValue = dittoHeaders.get(AGGREGATE_HEADER);
            if (null != aggregateHeaderValue) {
                result = Boolean.parseBoolean(aggregateHeaderValue);
            }
            return result;
        }

        private static Duration getReceiveTimeout(final DittoHeaders dittoHeaders) {
            final Duration timeout = dittoHeaders.getTimeout().orElse(DEFAULT_RECEIVE_TIMEOUT);
            return timeout.compareTo(DEFAULT_RECEIVE_TIMEOUT) > 0 ? timeout : DEFAULT_RECEIVE_TIMEOUT;
        }

        @Override
        public Receive createReceive() {
            return ReceiveBuilder.create()
                    .match(CommandResponse.class, this::handleCommandResponse)
                    .match(DittoRuntimeException.class, this::handleDittoRuntimeException)
                    .match(ReceiveTimeout.class, receiveTimeout -> {
                        LogUtil.enhanceLogWithCorrelationId(log, getSelf().path().name());
                        log.info("Got ReceiveTimeout, answering with all aggregated DevOpsCommandResponses and " +
                                "stopping ourselves ...");
                        sendCommandResponsesAndStop();
                    })
                    .matchAny(m -> {
                        LogUtil.enhanceLogWithCorrelationId(log, getSelf().path().name());
                        log.warning(UNKNOWN_MESSAGE_TEMPLATE, m);
                        unhandled(m);
                    }).build();
        }

        private void sendCommandResponsesAndStop() {
            devOpsCommandSender.tell(AggregatedDevOpsCommandResponse.of(commandResponses,
                    DevOpsCommandResponse.TYPE_PREFIX + devOpsCommand.getName(),
                    commandResponses.isEmpty() ? HttpStatusCode.REQUEST_TIMEOUT : HttpStatusCode.OK,
                    devOpsCommand.getDittoHeaders()),
                    getSelf());
            getContext().stop(getSelf());
        }

        private void handleCommandResponse(final CommandResponse<?> commandResponse) {
            LogUtil.enhanceLogWithCorrelationId(log, commandResponse);
            if (commandResponse instanceof DevOpsCommandResponse) {
                log.debug("Received DevOpsCommandResponse from service/instance <{}/{}>: {}",
                        ((DevOpsCommandResponse<?>) commandResponse).getServiceName().orElse("?"),
                        ((DevOpsCommandResponse<?>) commandResponse).getInstance().orElse("?"),
                        commandResponse.getType());
            } else {
                log.debug("Received DevOpsCommandResponse from service/instance <?/?>: {}", commandResponse.getType());
            }
            addCommandResponse(commandResponse);
        }

        private void handleDittoRuntimeException(final DittoRuntimeException dittoRuntimeException) {
            LogUtil.enhanceLogWithCorrelationId(log, dittoRuntimeException);

            log.warning("Received DittoRuntimeException <{}> from <{}>: <{}>!",
                    dittoRuntimeException.getClass().getName(), getSender(), dittoRuntimeException);

            addCommandResponse(DevOpsErrorResponse.of(null, null, dittoRuntimeException.toJson(),
                    dittoRuntimeException.getDittoHeaders()));
        }

        private void addCommandResponse(final CommandResponse<?> commandResponse) {
            commandResponses.add(commandResponse);
            if (!aggregateResults) {
                log.info("Do not aggregate sent response immediately.");
                sendCommandResponsesAndStop();
            }
        }

    }

}
