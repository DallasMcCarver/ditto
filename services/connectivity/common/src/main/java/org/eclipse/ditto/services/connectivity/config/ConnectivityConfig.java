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
package org.eclipse.ditto.services.connectivity.config;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.services.base.config.ServiceSpecificConfig;
import org.eclipse.ditto.services.connectivity.config.mapping.MappingConfig;
import org.eclipse.ditto.services.models.acks.config.AcknowledgementConfig;
import org.eclipse.ditto.services.models.signalenrichment.SignalEnrichmentConfig;
import org.eclipse.ditto.services.utils.health.config.WithHealthCheckConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.config.WithMongoDbConfig;
import org.eclipse.ditto.services.utils.persistence.operations.WithPersistenceOperationsConfig;
import org.eclipse.ditto.services.utils.persistentactors.config.PingConfig;
import org.eclipse.ditto.services.utils.protocol.config.WithProtocolConfig;

/**
 * Provides the configuration settings of the Connectivity service.
 */
@Immutable
public interface ConnectivityConfig extends ServiceSpecificConfig, WithHealthCheckConfig,
        WithPersistenceOperationsConfig, WithMongoDbConfig, WithProtocolConfig {

    /**
     * Returns the config of connections.
     *
     * @return the config.
     */
    ConnectionConfig getConnectionConfig();

    /**
     * Returns the config for Connectivity service's reconnect (wakeup for connection persistence actors) behaviour.
     *
     * @return the config.
     */
    PingConfig getPingConfig();

    /**
     * Returns the config for Connectivity service's behaviour for retrieval of connection ids.
     *
     * @return the config.
     */
    ConnectionIdsRetrievalConfig getConnectionIdsRetrievalConfig();

    /**
     * Returns the config for the Connectivity service's client.
     *
     * @return the config.
     */
    ClientConfig getClientConfig();

    /**
     * Returns the config for the Connectivity service's monitoring features (connection logs and metrics).
     *
     * @return the config.
     */
    MonitoringConfig getMonitoringConfig();

    /**
     * Returns the config for Connectivity service's message mapping.
     *
     * @return the config.
     */
    MappingConfig getMappingConfig();

    /**
     * Returns the configuration for signal enrichment.
     *
     * @return the config.
     */
    SignalEnrichmentConfig getSignalEnrichmentConfig();

    /**
     * Returns the configuration for acknowledgement handling.
     *
     * @return the config.
     */
    AcknowledgementConfig getAcknowledgementConfig();

    /**
     * Returns the configuration for ssh tunneling.
     *
     * @return the config.
     */
    TunnelConfig getTunnelConfig();
}
