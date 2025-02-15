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
package org.eclipse.ditto.protocoladapter;

import static org.eclipse.ditto.protocoladapter.TopicPath.Channel.LIVE;
import static org.eclipse.ditto.protocoladapter.TopicPath.Channel.NONE;
import static org.eclipse.ditto.protocoladapter.TopicPath.Channel.TWIN;

import org.eclipse.ditto.signals.base.Signal;
import org.eclipse.ditto.signals.commands.messages.MessageCommand;
import org.eclipse.ditto.signals.commands.messages.MessageCommandResponse;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;
import org.eclipse.ditto.signals.commands.policies.PolicyCommandResponse;

/**
 * A protocol adapter provides methods for mapping {@link Signal} instances to an {@link Adaptable}.
 */
public interface ProtocolAdapter {

    /**
     * Maps the given {@code Adaptable} to the corresponding {@code Signal}, which can be a {@code Command},
     * {@code CommandResponse} or an {@code Event}.
     *
     * @param adaptable the adaptable.
     * @return the Signal.
     */
    Signal<?> fromAdaptable(Adaptable adaptable);

    /**
     * Maps the given {@code Signal} to an {@code Adaptable}.
     *
     * @param signal the signal.
     * @return the adaptable.
     * @throws UnknownSignalException if the passed Signal was not supported by the ProtocolAdapter
     */
    Adaptable toAdaptable(Signal<?> signal);

    /**
     * Maps the given {@code Signal} to an {@code Adaptable}.
     *
     * @param signal the signal.
     * @param channel the channel to use when converting toAdaptable. This will overwrite any channel header in {@code signal}.
     * @return the adaptable.
     * @throws UnknownSignalException if the passed Signal was not supported by the ProtocolAdapter
     * @since 1.1.0
     */
    Adaptable toAdaptable(Signal<?> signal, TopicPath.Channel channel);

    /**
     * Retrieve the header translator responsible for this protocol adapter.
     *
     * @return the header translator.
     */
    HeaderTranslator headerTranslator();

    /**
     * Test whether a signal belongs to the live channel.
     *
     * @param signal the signal.
     * @return whether it is a live signal.
     */
    static boolean isLiveSignal(final Signal<?> signal) {
        return signal.getDittoHeaders()
                .getChannel()
                .filter(TopicPath.Channel.LIVE.getName()::equals)
                .isPresent();
    }

    /**
     * Determine the channel of the processed {@link Signal}. First the DittoHeaders are checked for the
     * {@link org.eclipse.ditto.model.base.headers.DittoHeaderDefinition#CHANNEL} header. If not given the default
     * channel is determined by the type of the {@link Signal}.
     *
     * @param signal the processed signal
     * @return the channel determined from the signal
     */
    static TopicPath.Channel determineChannel(final Signal<?> signal) {
        // internally a twin command/event and live command/event are distinguished only  by the channel header i.e.
        // a twin and live command "look the same" except for the channel header
        final boolean isLiveSignal = isLiveSignal(signal);
        return isLiveSignal ? LIVE  // live signals (live commands/events) use the live channel
                : determineDefaultChannel(signal); // use default for other commands
    }

    /**
     * Determines the default channel of the processed {@link Signal} by signal type.
     *
     * @param signal the processed signal
     * @return the default channel determined from the signal
     */
    static TopicPath.Channel determineDefaultChannel(final Signal<?> signal) {
        if (signal instanceof PolicyCommand || signal instanceof PolicyCommandResponse) {
            return NONE;
        } else if (signal instanceof MessageCommand || signal instanceof MessageCommandResponse) {
            return LIVE;
        } else {
            return TWIN;
        }
    }
}
