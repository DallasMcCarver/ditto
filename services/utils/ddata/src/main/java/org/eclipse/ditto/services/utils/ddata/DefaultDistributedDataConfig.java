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
package org.eclipse.ditto.services.utils.ddata;

import java.time.Duration;
import java.util.Objects;

import org.eclipse.ditto.services.utils.config.ConfigWithFallback;

import com.typesafe.config.Config;

import akka.cluster.ddata.Replicator;

/**
 * This class is the default implementation of the distributed data config.
 */
class DefaultDistributedDataConfig implements DistributedDataConfig {

    private static final String CONFIG_PATH = "ddata";

    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final Replicator.WriteConsistency subscriptionWriteConsistency;
    private final Duration subscriptionDelay;
    private final AkkaReplicatorConfig akkaReplicatorConfig;
    private final int numberOfShards;

    private DefaultDistributedDataConfig(final Config configWithFallback) {
        readTimeout = configWithFallback.getDuration(DistributedDataConfigValue.READ_TIMEOUT.getConfigPath());
        writeTimeout = configWithFallback.getDuration(DistributedDataConfigValue.WRITE_TIMEOUT.getConfigPath());
        akkaReplicatorConfig = DefaultAkkaReplicatorConfig.of(configWithFallback);
        subscriptionWriteConsistency = toWriteConsistency(configWithFallback.getString(
                DistributedDataConfigValue.SUBSCRIPTION_WRITE_CONSISTENCY.getConfigPath()),
                configWithFallback.getDuration(DistributedDataConfigValue.WRITE_TIMEOUT.getConfigPath()));
        subscriptionDelay =
                configWithFallback.getDuration(DistributedDataConfigValue.SUBSCRIPTION_DELAY.getConfigPath());
        numberOfShards = configWithFallback.getInt(DistributedDataConfigValue.NUMBER_OF_SHARDS.getConfigPath());
    }

    private DefaultDistributedDataConfig(final Config configWithFallback,
            final CharSequence replicatorName,
            final CharSequence replicatorRole) {
        readTimeout = configWithFallback.getDuration(DistributedDataConfigValue.READ_TIMEOUT.getConfigPath());
        writeTimeout = configWithFallback.getDuration(DistributedDataConfigValue.WRITE_TIMEOUT.getConfigPath());
        akkaReplicatorConfig = DefaultAkkaReplicatorConfig.of(configWithFallback, replicatorName, replicatorRole);
        subscriptionWriteConsistency = toWriteConsistency(configWithFallback.getString(
                DistributedDataConfigValue.SUBSCRIPTION_WRITE_CONSISTENCY.getConfigPath()),
                configWithFallback.getDuration(DistributedDataConfigValue.WRITE_TIMEOUT.getConfigPath()));
        subscriptionDelay =
                configWithFallback.getDuration(DistributedDataConfigValue.SUBSCRIPTION_DELAY.getConfigPath());
        numberOfShards = configWithFallback.getInt(DistributedDataConfigValue.NUMBER_OF_SHARDS.getConfigPath());
    }

    /**
     * Returns an instance of {@code DefaultDistributedDataConfig} based on the settings of the specified Config.
     *
     * @param config is supposed to provide the settings of the ddata config at {@value #CONFIG_PATH}.
     * @return the instance.
     * @throws org.eclipse.ditto.services.utils.config.DittoConfigError if {@code config} is invalid.
     */
    public static DefaultDistributedDataConfig of(final Config config) {

        return new DefaultDistributedDataConfig(
                ConfigWithFallback.newInstance(config, CONFIG_PATH, DistributedDataConfigValue.values()));
    }

    /**
     * Returns an instance of {@code DefaultDistributedDataConfig} based on the settings of the specified Config.
     *
     * @param replicatorName the name of the replicator.
     * @param replicatorRole the cluster role of members with replicas of the distributed collection.
     * @param config is supposed to provide the settings of the ddata config at {@value #CONFIG_PATH}.
     * @return the instance.
     * @throws org.eclipse.ditto.services.utils.config.DittoConfigError if {@code config} is invalid.
     */
    public static DefaultDistributedDataConfig of(final Config config, final CharSequence replicatorName,
            final CharSequence replicatorRole) {

        final ConfigWithFallback configWithFallback =
                ConfigWithFallback.newInstance(config, CONFIG_PATH, DistributedDataConfigValue.values());

        return new DefaultDistributedDataConfig(configWithFallback, replicatorName, replicatorRole);
    }

    @Override
    public Duration getReadTimeout() {
        return readTimeout;
    }

    @Override
    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    @Override
    public Replicator.WriteConsistency getSubscriptionWriteConsistency() {
        return subscriptionWriteConsistency;
    }

    @Override
    public Duration getSubscriptionDelay() {
        return subscriptionDelay;
    }

    @Override
    public AkkaReplicatorConfig getAkkaReplicatorConfig() {
        return akkaReplicatorConfig;
    }

    @Override
    public int getNumberOfShards() {
        return numberOfShards;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefaultDistributedDataConfig that = (DefaultDistributedDataConfig) o;
        return numberOfShards == that.numberOfShards && Objects.equals(readTimeout, that.readTimeout) &&
                Objects.equals(writeTimeout, that.writeTimeout) &&
                Objects.equals(subscriptionWriteConsistency, that.subscriptionWriteConsistency) &&
                Objects.equals(subscriptionDelay, that.subscriptionDelay) &&
                Objects.equals(akkaReplicatorConfig, that.akkaReplicatorConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(readTimeout, writeTimeout, akkaReplicatorConfig, subscriptionWriteConsistency,
                subscriptionDelay, numberOfShards);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "readTimeout=" + readTimeout +
                ", writeTimeout=" + writeTimeout +
                ", subscriptionWriteConsistency=" + subscriptionWriteConsistency +
                ", subscriptionDelay" + subscriptionDelay +
                ", akkaReplicatorConfig=" + akkaReplicatorConfig +
                ", numberOfShards=" + numberOfShards +
                "]";
    }

    private static Replicator.WriteConsistency toWriteConsistency(final String configuredWriteConsistency,
            final Duration writeTimeout) {
        final Object writeConsistency;
        switch (configuredWriteConsistency) {
            case "local":
                writeConsistency = Replicator.writeLocal();
                break;
            case "majority":
                writeConsistency = new Replicator.WriteMajority(writeTimeout);
                break;
            default: // default is "all"
                writeConsistency = new Replicator.WriteAll(writeTimeout);
        }
        return (Replicator.WriteConsistency) writeConsistency;
    }
}
