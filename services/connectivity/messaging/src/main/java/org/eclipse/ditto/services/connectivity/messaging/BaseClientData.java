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
package org.eclipse.ditto.services.connectivity.messaging;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.connectivity.Connection;
import org.eclipse.ditto.model.connectivity.ConnectionId;
import org.eclipse.ditto.model.connectivity.ConnectivityStatus;
import org.eclipse.ditto.services.connectivity.messaging.tunnel.SshTunnelState;

import akka.actor.ActorRef;
import akka.japi.Pair;

/**
 * The data the {@link BaseClientActor} has in its different {@link org.eclipse.ditto.services.models.connectivity.BaseClientState States}.
 */
@Immutable
public final class BaseClientData {

    private final ConnectionId connectionId;
    private final Connection connection;
    private final ConnectivityStatus connectionStatus;
    private final ConnectivityStatus desiredConnectionStatus;
    @Nullable private final String connectionStatusDetails;
    private final Instant inConnectionStatusSince;
    private final List<Pair<ActorRef, DittoHeaders>> sessionSenders;
    private final SshTunnelState sshTunnelState;

    /**
     * Constructs new instance of BaseClientData, the data of the {@link BaseClientActor}.
     *
     * @param connectionId the ID of the {@link Connection}.
     * @param connection the optional {@link Connection}.
     * @param connectionStatus the current {@link ConnectivityStatus} of the Connection.
     * @param desiredConnectionStatus the desired {@link ConnectivityStatus} of the Connection.
     * @param connectionStatusDetails the optional details about the ConnectionStatus.
     * @param inConnectionStatusSince the instant since when the Client is in its current ConnectionStatus.
     */
    private BaseClientData(final ConnectionId connectionId, final Connection connection,
            final ConnectivityStatus connectionStatus,
            final ConnectivityStatus desiredConnectionStatus,
            @Nullable final String connectionStatusDetails,
            final Instant inConnectionStatusSince,
            final List<Pair<ActorRef, DittoHeaders>> sessionSenders,
            final SshTunnelState sshTunnelState) {
        this.connectionId = connectionId;
        this.connection = connection;
        this.connectionStatus = connectionStatus;
        this.desiredConnectionStatus = desiredConnectionStatus;
        this.connectionStatusDetails = connectionStatusDetails;
        this.inConnectionStatusSince = inConnectionStatusSince;
        this.sessionSenders = List.copyOf(sessionSenders);
        this.sshTunnelState = sshTunnelState;
    }

    /**
     * @return the ID of the Connection
     */
    public ConnectionId getConnectionId() {
        return connectionId;
    }

    /**
     * @return the managed Connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return the current connection status
     */
    public ConnectivityStatus getConnectionStatus() {
        return connectionStatus;
    }

    /**
     * @return the desired connection status
     */
    ConnectivityStatus getDesiredConnectionStatus() {
        return desiredConnectionStatus;
    }

    /**
     * @return the details description about the current connection status
     */
    Optional<String> getConnectionStatusDetails() {
        return Optional.ofNullable(connectionStatusDetails);
    }

    /**
     * @return the time since when the connection is in the current status
     */
    Instant getInConnectionStatusSince() {
        return inConnectionStatusSince;
    }

    /**
     * @return the Pairs of session senders (including DittoHeaders per sender ActorRef)
     */
    List<Pair<ActorRef, DittoHeaders>> getSessionSenders() {
        return sessionSenders;
    }

    /**
     * @return the holder for the state of the ssh tunnel (enabled, established, failed, ...)
     */
    SshTunnelState getSshTunnelState() {
        return sshTunnelState;
    }

    BaseClientData setSshTunnelState(final SshTunnelState sshTunnelState) {
        return BaseClientDataBuilder.from(this).setSshTunnelState(sshTunnelState).build();
    }

    /**
     * Updates the managed connection returning a new instance of BaseClientData.
     *
     * @param connection the new connection to use
     * @return the new instance of BaseClientData
     */
    public BaseClientData setConnection(final Connection connection) {
        return BaseClientDataBuilder.from(this).setConnection(connection).build();
    }

    /**
     * Updates the current connection status returning a new instance of BaseClientData.
     *
     * @param connectionStatus the new connection status to use
     * @return the new instance of BaseClientData
     */
    public BaseClientData setConnectionStatus(final ConnectivityStatus connectionStatus) {
        return BaseClientDataBuilder.from(this).setConnectionStatus(connectionStatus).build();
    }

    /**
     * Updates the desired connection status returning a new instance of BaseClientData.
     *
     * @param desiredConnectionStatus the new desired connection status to use
     * @return the new instance of BaseClientData
     */
    BaseClientData setDesiredConnectionStatus(final ConnectivityStatus desiredConnectionStatus) {
        return BaseClientDataBuilder.from(this).setDesiredConnectionStatus(desiredConnectionStatus).build();
    }

    public BaseClientData setConnectionStatusDetails(@Nullable final String connectionStatusDetails) {
        return BaseClientDataBuilder.from(this).setConnectionStatusDetails(connectionStatusDetails).build();
    }

    /**
     * Adds the passed {@code origin} sender with the passed {@code dittoHeaders} to the managed {@code sessionSenders}
     * returning a new instance of BaseClientData.
     *
     * @param origin the sender to add
     * @param dittoHeaders the DittoHeaders to add for the passed sender
     * @return the new instance of BaseClientData
     */
    BaseClientData addSessionSender(@Nullable final ActorRef origin, final DittoHeaders dittoHeaders) {
        if (origin != null) {
            final List<Pair<ActorRef, DittoHeaders>> newSessionSenders = new ArrayList<>(sessionSenders);
            newSessionSenders.add(Pair.create(origin, dittoHeaders));
            return BaseClientDataBuilder.from(this).setSessionSenders(newSessionSenders).build();
        } else {
            return this;
        }
    }

    /**
     * Remove info related to the last command. Should be called when entering a stable state (UNKNOWN, CONNECTED,
     * DISCONNECTED).
     *
     * @return data without info related to the last command.
     */
    BaseClientData resetSession() {
        return BaseClientDataBuilder.from(this).setSessionSenders(Collections.emptyList()).build();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {return true;}
        if (!(o instanceof BaseClientData)) {return false;}
        final BaseClientData that = (BaseClientData) o;
        return Objects.equals(connectionId, that.connectionId) &&
                Objects.equals(connection, that.connection) &&
                connectionStatus == that.connectionStatus &&
                desiredConnectionStatus == that.desiredConnectionStatus &&
                Objects.equals(connectionStatusDetails, that.connectionStatusDetails) &&
                Objects.equals(inConnectionStatusSince, that.inConnectionStatusSince) &&
                Objects.equals(sessionSenders, that.sessionSenders) &&
                Objects.equals(sshTunnelState, that.sshTunnelState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionId, connection, connectionStatus, desiredConnectionStatus,
                connectionStatusDetails, inConnectionStatusSince, sessionSenders, sshTunnelState);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "connectionId=" + connectionId +
                ", connection=" + connection +
                ", connectionStatus=" + connectionStatus +
                ", desiredConnectionStatus=" + desiredConnectionStatus +
                ", connectionStatusDetails=" + connectionStatusDetails +
                ", inConnectionStatusSince=" + inConnectionStatusSince +
                ", sessionSenders=" + sessionSenders +
                ", sshTunnelState=" + sshTunnelState +
                "]";
    }

    static final class BaseClientDataBuilder {

        private ConnectionId connectionId;
        private Connection connection;
        private ConnectivityStatus connectionStatus;
        private ConnectivityStatus desiredConnectionStatus;
        private String connectionStatusDetails;
        private Instant inConnectionStatusSince;
        private List<Pair<ActorRef, DittoHeaders>> sessionSenders = Collections.emptyList();
        private SshTunnelState sshTunnelState;

        static BaseClientDataBuilder from(final BaseClientData data) {
            return new BaseClientDataBuilder(data);
        }

        static BaseClientDataBuilder from(final ConnectionId connectionId, final Connection connection,
                final ConnectivityStatus connectionStatus,
                final ConnectivityStatus desiredConnectionStatus,
                @Nullable final String connectionStatusDetails,
                final Instant inConnectionStatusSince) {
            return new BaseClientDataBuilder(connectionId, connection, connectionStatus, desiredConnectionStatus, connectionStatusDetails, inConnectionStatusSince);
        }

        private BaseClientDataBuilder(final BaseClientData data) {
            connectionId = data.getConnectionId();
            connection = data.getConnection();
            connectionStatus = data.getConnectionStatus();
            connectionStatusDetails = data.getConnectionStatusDetails().orElse(null);
            desiredConnectionStatus = data.getDesiredConnectionStatus();
            inConnectionStatusSince = data.getInConnectionStatusSince();
            sessionSenders = data.getSessionSenders();
            sshTunnelState = data.getSshTunnelState();
        }

        private BaseClientDataBuilder(final ConnectionId connectionId, final Connection connection,
                final ConnectivityStatus connectionStatus,
                final ConnectivityStatus desiredConnectionStatus,
                @Nullable final String connectionStatusDetails,
                final Instant inConnectionStatusSince) {
            this.connectionId = connectionId;
            this.connection = connection;
            this.connectionStatus = connectionStatus;
            this.connectionStatusDetails = connectionStatusDetails;
            this.desiredConnectionStatus = desiredConnectionStatus;
            this.inConnectionStatusSince = inConnectionStatusSince;
            this.sshTunnelState = SshTunnelState.from(connection);
        }

        public BaseClientDataBuilder setConnectionId(final ConnectionId connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        public BaseClientDataBuilder setConnection(final Connection connection) {
            this.connection = connection;
            return this;
        }

        public BaseClientDataBuilder setConnectionStatus(final ConnectivityStatus connectionStatus) {
            this.connectionStatus = connectionStatus;
            return this;
        }

        public BaseClientDataBuilder setDesiredConnectionStatus(final ConnectivityStatus desiredConnectionStatus) {
            this.desiredConnectionStatus = desiredConnectionStatus;
            return this;
        }

        public BaseClientDataBuilder setConnectionStatusDetails(final String connectionStatusDetails) {
            this.connectionStatusDetails = connectionStatusDetails;
            return this;
        }

        public BaseClientDataBuilder setInConnectionStatusSince(final Instant inConnectionStatusSince) {
            this.inConnectionStatusSince = inConnectionStatusSince;
            return this;
        }

        public BaseClientDataBuilder setSessionSenders(final List<Pair<ActorRef, DittoHeaders>> sessionSenders) {
            this.sessionSenders = sessionSenders;
            return this;
        }

        public BaseClientDataBuilder setSshTunnelState(final SshTunnelState sshTunnelState) {
            this.sshTunnelState = sshTunnelState;
            return this;
        }

        public BaseClientData build() {
            return new BaseClientData(connectionId, connection, connectionStatus, desiredConnectionStatus,
                    connectionStatusDetails, inConnectionStatusSince, sessionSenders, sshTunnelState);
        }
    }

}

