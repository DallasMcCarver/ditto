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
package org.eclipse.ditto.services.utils.persistence.mongo.streaming;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.eclipse.ditto.services.utils.config.DefaultScopedConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.DittoMongoClient;
import org.eclipse.ditto.services.utils.persistence.mongo.MongoClientWrapper;
import org.eclipse.ditto.services.utils.persistence.mongo.config.DefaultMongoDbConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.config.MongoDbConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.indices.Index;
import org.eclipse.ditto.services.utils.persistence.mongo.indices.IndexFactory;
import org.eclipse.ditto.services.utils.persistence.mongo.indices.IndexInitializer;
import org.eclipse.ditto.utils.jsr305.annotations.AllValuesAreNonnullByDefault;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.typesafe.config.Config;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.contrib.persistence.mongodb.JournallingFieldNames$;
import akka.contrib.persistence.mongodb.SnapshottingFieldNames$;
import akka.japi.Pair;
import akka.stream.Attributes;
import akka.stream.Materializer;
import akka.stream.RestartSettings;
import akka.stream.SystemMaterializer;
import akka.stream.javadsl.RestartSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

/**
 * Reads the event journal of com.github.scullxbones.akka-persistence-mongo plugin.
 * In the Akka system configuration,
 * <ul>
 * <li>
 * {@code akka.persistence.journal.auto-start-journals} must contain exactly 1 configuration key {@code
 * <JOURNAL_KEY>},
 * </li>
 * <li>
 * {@code <JOURNAL_KEY>.overrides.journal-collection} must be defined and equal to the name of the event journal
 * collection.
 * </li>
 * </ul>
 */
@AllValuesAreNonnullByDefault
public class MongoReadJournal {
    // not a final class to test with Mockito

    /**
     * ID field of documents delivered by the read journal.
     */
    public static final String J_ID = JournallingFieldNames$.MODULE$.ID();
    public static final String S_ID = J_ID;

    /**
     * Prefix of the priority tag which is used in
     * {@link #getJournalPidsWithTagOrderedByPriorityTag(String, java.time.Duration)}
     * for sorting/ordering by.
     */
    public static final String PRIORITY_TAG_PREFIX = "priority-";

    private static final String AKKA_PERSISTENCE_JOURNAL_AUTO_START =
            "akka.persistence.journal.auto-start-journals";
    private static final String AKKA_PERSISTENCE_SNAPS_AUTO_START =
            "akka.persistence.snapshot-store.auto-start-snapshot-stores";

    private static final String JOURNAL_COLLECTION_NAME_KEY = "overrides.journal-collection";
    private static final String SNAPS_COLLECTION_NAME_KEY = "overrides.snaps-collection";

    private static final String J_PROCESSOR_ID = JournallingFieldNames$.MODULE$.PROCESSOR_ID();
    private static final String J_TO = JournallingFieldNames$.MODULE$.TO();
    private static final String J_TAGS = JournallingFieldNames$.MODULE$.TAGS();
    private static final String S_PROCESSOR_ID = SnapshottingFieldNames$.MODULE$.PROCESSOR_ID();
    private static final String S_SN = SnapshottingFieldNames$.MODULE$.SEQUENCE_NUMBER();

    // Not working: SnapshottingFieldNames.V2$.MODULE$.SERIALIZED()
    private static final String S_SERIALIZED_SNAPSHOT = "s2";
    private static final String LIFECYCLE = "__lifecycle";

    private static final String J_EVENT = JournallingFieldNames$.MODULE$.EVENTS();
    public static final String J_EVENT_PID = JournallingFieldNames$.MODULE$.PROCESSOR_ID();
    public static final String J_EVENT_MANIFEST = JournallingFieldNames$.MODULE$.MANIFEST();
    public static final String J_EVENT_SN = JournallingFieldNames$.MODULE$.SEQUENCE_NUMBER();

    private static final Duration MAX_BACK_OFF_DURATION = Duration.ofSeconds(128L);

    private static final Index TAG_PID_INDEX =
            IndexFactory.newInstance("ditto_tag_pid", List.of(J_TAGS, J_PROCESSOR_ID), false, true);

    private final String journalCollection;
    private final String snapsCollection;
    private final DittoMongoClient mongoClient;
    private final IndexInitializer indexInitializer;

    private MongoReadJournal(final String journalCollection, final String snapsCollection,
            final DittoMongoClient mongoClient, final ActorSystem actorSystem) {
        this.journalCollection = journalCollection;
        this.snapsCollection = snapsCollection;
        this.mongoClient = mongoClient;
        final var materializer = SystemMaterializer.get(actorSystem).materializer();
        indexInitializer = IndexInitializer.of(mongoClient.getDefaultDatabase(), materializer);
    }

    /**
     * Create a read journal for an actor system with a persistence plugin having a unique auto-start journal.
     *
     * @param system the actor system.
     * @return the read journal.
     */
    public static MongoReadJournal newInstance(final ActorSystem system) {
        final Config config = system.settings().config();
        final MongoDbConfig mongoDbConfig =
                DefaultMongoDbConfig.of(DefaultScopedConfig.dittoScoped(config));
        return newInstance(config, MongoClientWrapper.newInstance(mongoDbConfig), system);
    }

    /**
     * Creates a new {@code MongoReadJournal}.
     *
     * @param config The Akka system configuration.
     * @param mongoClient The Mongo client wrapper.
     * @return A {@code MongoReadJournal} object.
     */
    public static MongoReadJournal newInstance(final Config config, final DittoMongoClient mongoClient,
            final ActorSystem actorSystem) {
        final String autoStartJournalKey = extractAutoStartConfigKey(config, AKKA_PERSISTENCE_JOURNAL_AUTO_START);
        final String autoStartSnapsKey = extractAutoStartConfigKey(config, AKKA_PERSISTENCE_SNAPS_AUTO_START);
        final String journalCollection =
                getOverrideCollectionName(config.getConfig(autoStartJournalKey), JOURNAL_COLLECTION_NAME_KEY);
        final String snapshotCollection =
                getOverrideCollectionName(config.getConfig(autoStartSnapsKey), SNAPS_COLLECTION_NAME_KEY);
        return new MongoReadJournal(journalCollection, snapshotCollection, mongoClient, actorSystem);
    }

    /**
     * Ensure a compound index exists for journal PID streaming based on tags.
     *
     * @return a future that completes after index creation completes or fails when index creation fails.
     */
    public CompletionStage<Done> ensureTagPidIndex() {
        return indexInitializer.createNonExistingIndices(journalCollection, List.of(TAG_PID_INDEX));
    }

    /**
     * Retrieve all unique PIDs in journals. Does its best not to create long-living cursors on the database by reading
     * {@code batchSize} events per query.
     *
     * @param batchSize how many events to read in one query.
     * @param maxIdleTime how long the stream is allowed to idle without sending any element. Bounds the number of
     * retries with exponential back-off.
     * @param mat the actor materializer to run the query streams.
     * @return Source of all persistence IDs such that each element contains the persistence IDs in {@code batchSize}
     * events that do not occur in prior buckets.
     */
    public Source<String, NotUsed> getJournalPids(final int batchSize, final Duration maxIdleTime,
            final Materializer mat) {

        final int maxRestarts = computeMaxRestarts(maxIdleTime);
        return getJournal().withAttributes(Attributes.inputBuffer(1, 1))
                .flatMapConcat(journal ->
                        listPidsInJournal(journal, "", "", batchSize, mat, MAX_BACK_OFF_DURATION, maxRestarts)
                )
                .mapConcat(pids -> pids);
    }

    /**
     * Retrieve latest journal entries for each distinct PID.
     * Does its best not to create long-living cursors on the database by reading {@code batchSize} events per query.
     *
     * @param batchSize how many events to read in one query.
     * @param maxIdleTime how long the stream is allowed to idle without sending any element. Bounds the number of
     * retries with exponential back-off.
     * @param mat the actor materializer to run the query streams.
     * @return Source of all latest journal entries per pid such that each element contains the persistence IDs in
     * {@code batchSize} events that do not occur in prior buckets.
     */
    public Source<Document, NotUsed> getLatestJournalEntries(final int batchSize, final Duration maxIdleTime,
            final Materializer mat) {

        final int maxRestarts = computeMaxRestarts(maxIdleTime);
        return getJournal().withAttributes(Attributes.inputBuffer(1, 1))
                .flatMapConcat(
                        journal -> listLatestJournalEntries(journal, "", "", batchSize, MAX_BACK_OFF_DURATION, mat,
                                maxRestarts, J_EVENT_PID, J_EVENT_SN, J_EVENT_MANIFEST))
                .mapConcat(pids -> pids);
    }

    private Source<List<Document>, NotUsed> listLatestJournalEntries(final MongoCollection<Document> journal,
            final String lowerBoundPid,
            final String tag,
            final int batchSize,
            final Duration maxBackoff,
            final Materializer mat,
            final int maxRestarts,
            final String... journalFields) {

        return this.unfoldBatchedSource(lowerBoundPid,
                mat,
                document -> document.getString(J_ID),
                actualStartPid -> listLatestJournalEntries(journal, actualStartPid, tag, batchSize, maxBackoff,
                        maxRestarts, journalFields));
    }

    /**
     * Retrieve all unique PIDs in journals selected by a provided {@code tag}.
     * Does its best not to create long-living cursors on the database by reading {@code batchSize} events per query.
     *
     * @param tag the Tag name the journal entries have to contain in order to be selected, or an empty string to select
     * all journal entries.
     * @param batchSize how many events to read in one query.
     * @param maxIdleTime how long the stream is allowed to idle without sending any element. Bounds the number of
     * retries with exponential back-off.
     * @param mat the actor materializer to run the query streams.
     * @return Source of all persistence IDs such that each element contains the persistence IDs in {@code batchSize}
     * events that do not occur in prior buckets.
     */
    public Source<String, NotUsed> getJournalPidsWithTag(final String tag, final int batchSize,
            final Duration maxIdleTime,
            final Materializer mat) {

        final int maxRestarts = computeMaxRestarts(maxIdleTime);
        return getJournal().withAttributes(Attributes.inputBuffer(1, 1))
                .flatMapConcat(journal ->
                        listPidsInJournal(journal, "", tag, batchSize, mat, MAX_BACK_OFF_DURATION, maxRestarts)
                )
                .mapConcat(pids -> pids);
    }

    /**
     * Retrieve all unique PIDs in journals selected by a provided {@code tag}.
     * The PIDs are ordered based on the {@link #PRIORITY_TAG_PREFIX} tags of the events: Descending by the appended
     * priority (an integer value).
     *
     * @param tag the Tag name the journal entries have to contain in order to be selected, or an empty string to select
     * all journal entries.
     * @param maxIdleTime how long the stream is allowed to idle without sending any element. Bounds the number of
     * retries with exponential back-off.
     * @return Source of all persistence IDs tagged with the provided {@code tag}, sorted ascending by the value of an
     * additional {@link #PRIORITY_TAG_PREFIX} tag.
     */
    public Source<String, NotUsed> getJournalPidsWithTagOrderedByPriorityTag(final String tag,
            final Duration maxIdleTime) {

        final int maxRestarts = computeMaxRestarts(maxIdleTime);
        return getJournal().withAttributes(Attributes.inputBuffer(1, 1))
                .flatMapConcat(journal ->
                        listPidsInJournalOrderedByPriorityTag(journal, tag, MAX_BACK_OFF_DURATION, maxRestarts)
                );
    }

    /**
     * Retrieve all unique PIDs in journals above a lower bound. Does not limit database access in any way.
     *
     * @param lowerBoundPid the lower-bound PID.
     * @param batchSize how many events to read in 1 query.
     * @param mat the materializer.
     * @return all unique PIDs in journals above a lower bound.
     */
    public Source<String, NotUsed> getJournalPidsAbove(final String lowerBoundPid, final int batchSize,
            final Materializer mat) {

        return getJournal()
                .withAttributes(Attributes.inputBuffer(1, 1))
                .flatMapConcat(journal ->
                        listPidsInJournal(journal, lowerBoundPid, "", batchSize, mat, MAX_BACK_OFF_DURATION, 0)
                )
                .mapConcat(pids -> pids);
    }

    /**
     * Retrieve all unique PIDs in journals selected by a provided {@code tag} above a lower bound.
     * Does its best not to create long-living cursors on the database by reading {@code batchSize} events per query.
     *
     * @param lowerBoundPid the lower-bound PID.
     * @param tag the Tag name the journal entries have to contain in order to be selected.
     * @param batchSize how many events to read in one query.
     * @param mat the actor materializer to run the query streams.
     * @return Source of all persistence IDs such that each element contains the persistence IDs in {@code batchSize}
     * events that do not occur in prior buckets.
     */
    public Source<String, NotUsed> getJournalPidsAboveWithTag(final String lowerBoundPid, final String tag,
            final int batchSize, final Materializer mat) {

        return getJournal().withAttributes(Attributes.inputBuffer(1, 1))
                .flatMapConcat(journal ->
                        listPidsInJournal(journal, lowerBoundPid, tag, batchSize, mat, MAX_BACK_OFF_DURATION, 0)
                )
                .mapConcat(pids -> pids);
    }

    /**
     * Retrieve all latest snapshots with unique PIDs in snapshot store above a lower bound.
     * Does not limit database access in any way.
     *
     * @param lowerBoundPid the lower-bound PID.
     * @param batchSize how many snapshots to read in 1 query.
     * @param mat the materializer.
     * @param snapshotFields snapshot fields to project out.
     * @return source of newest snapshots with unique PIDs.
     */
    public Source<Document, NotUsed> getNewestSnapshotsAbove(final String lowerBoundPid,
            final int batchSize,
            final Materializer mat,
            final String... snapshotFields) {

        return getSnapshotStore()
                .withAttributes(Attributes.inputBuffer(1, 1))
                .flatMapConcat(snapshotStore ->
                        listNewestSnapshots(snapshotStore, lowerBoundPid, batchSize, mat,
                                snapshotFields)
                )
                .mapConcat(pids -> pids);
    }

    private Source<List<String>, NotUsed> listPidsInJournal(final MongoCollection<Document> journal,
            final String lowerBoundPid, final String tag,
            final int batchSize, final Materializer mat, final Duration maxBackOff, final int maxRestarts) {

        return unfoldBatchedSource(lowerBoundPid, mat, Function.identity(), actualStartPid ->
                listJournalPidsAbove(journal, actualStartPid, tag, batchSize, maxBackOff, maxRestarts)
        );
    }

    private Source<String, NotUsed> listJournalPidsAbove(final MongoCollection<Document> journal, final String startPid,
            final String tag, final int batchSize, final Duration maxBackOff, final int maxRestarts) {

        return listLatestJournalEntries(journal, startPid, tag, batchSize, maxBackOff, maxRestarts, J_EVENT_PID)
                .flatMapConcat(document -> {
                    final Object pid = document.get(J_EVENT_PID);
                    if (pid instanceof CharSequence) {
                        return Source.single(pid.toString());
                    } else {
                        return Source.empty();
                    }
                });
    }

    private Source<List<Document>, NotUsed> listNewestSnapshots(final MongoCollection<Document> snapshotStore,
            final String lowerBoundPid,
            final int batchSize,
            final Materializer mat,
            final String... snapshotFields) {

        return this.unfoldBatchedSource(lowerBoundPid,
                mat,
                SnapshotBatch::getMaxPid,
                actualStartPid -> listNewestActiveSnapshotsByBatch(snapshotStore, actualStartPid, batchSize,
                        snapshotFields))
                .mapConcat(x -> x)
                .map(SnapshotBatch::getItems);
    }

    private <T> Source<List<T>, NotUsed> unfoldBatchedSource(
            final String lowerBoundPid,
            final Materializer mat,
            final Function<T, String> seedCreator,
            final Function<String, Source<T, ?>> sourceCreator) {

        return Source.unfoldAsync("",
                startPid -> {
                    final String actualStart = lowerBoundPid.compareTo(startPid) >= 0 ? lowerBoundPid : startPid;
                    return sourceCreator.apply(actualStart)
                            .runWith(Sink.seq(), mat)
                            .thenApply(list -> {
                                if (list.isEmpty()) {
                                    return Optional.empty();
                                } else {
                                    return Optional.of(Pair.create(seedCreator.apply(list.get(list.size() - 1)), list));
                                }
                            });
                })
                .withAttributes(Attributes.inputBuffer(1, 1));
    }

    private Source<String, NotUsed> listPidsInJournalOrderedByPriorityTag(final MongoCollection<Document> journal,
            final String tag, final Duration maxBackOff, final int maxRestarts) {

        final List<Bson> pipeline = new ArrayList<>(4);
        // optional match stages: consecutive match stages are optimized together ($match + $match coalescence)
        if (!tag.isEmpty()) {
            pipeline.add(Aggregates.match(Filters.eq(J_TAGS, tag)));
        }

        // group stage. We can assume that the $last element ist also new latest event because of the insert order.
        pipeline.add(Aggregates.group("$" + J_PROCESSOR_ID, Accumulators.last(J_TAGS, "$" + J_TAGS)));

        // Filter irrelevant tags for priority ordering.
        final BsonDocument arrayFilter = BsonDocument.parse(
                "{\n" +
                        "    $filter: {\n" +
                        "        input: \"$" + J_TAGS + "\",\n" +
                        "        as: \"tags\",\n" +
                        "        cond: {\n" +
                        "            $eq: [\n" +
                        "                {\n" +
                        "                    $substrCP: [\"$$tags\", 0, " + PRIORITY_TAG_PREFIX.length() + "]\n" +
                        "                }\n," +
                        "                \"" + PRIORITY_TAG_PREFIX + "\"\n" +
                        "            ]\n" +
                        "        }\n" +
                        "    }\n" +
                        "}"
        );
        pipeline.add(Aggregates.project(Projections.computed(J_TAGS, arrayFilter)));

        // sort stage 2 -- order after group stage is not defined
        pipeline.add(Aggregates.sort(Sorts.orderBy(Sorts.descending(J_TAGS))));

        final Duration minBackOff = Duration.ofSeconds(1L);
        final double randomFactor = 0.1;

        final RestartSettings restartSettings = RestartSettings.create(minBackOff, maxBackOff, randomFactor)
                .withMaxRestarts(maxRestarts, minBackOff);
        return RestartSource.onFailuresWithBackoff(restartSettings, () ->
                Source.fromPublisher(journal.aggregate(pipeline)
                        .collation(Collation.builder().locale("en_US").numericOrdering(true).build()))
                        .flatMapConcat(document -> {
                            final Object pid = document.get(J_ID);
                            if (pid instanceof CharSequence) {
                                return Source.single(pid.toString());
                            } else {
                                return Source.empty();
                            }
                        })
        );
    }

    private Source<Document, NotUsed> listLatestJournalEntries(final MongoCollection<Document> journal,
            final String startPid, final String tag, final int batchSize, final Duration maxBackOff,
            final int maxRestarts, final String... fieldNames) {

        final List<Bson> pipeline = new ArrayList<>(6);
        // optional match stages: consecutive match stages are optimized together ($match + $match coalescence)
        if (!tag.isEmpty()) {
            pipeline.add(Aggregates.match(Filters.eq(J_TAGS, tag)));
        }
        if (!startPid.isEmpty()) {
            pipeline.add(Aggregates.match(Filters.gt(J_PROCESSOR_ID, startPid)));
        }

        // sort stage
        pipeline.add(Aggregates.sort(Sorts.orderBy(Sorts.ascending(J_PROCESSOR_ID), Sorts.descending(J_TO))));

        // limit stage. It should come before group stage or MongoDB would scan the entire journal collection.
        pipeline.add(Aggregates.limit(batchSize));

        // group stage
        pipeline.add(Aggregates.group("$" + J_PROCESSOR_ID, toFirstJournalEntryFields(fieldNames)));

        // sort stage 2 -- order after group stage is not defined
        pipeline.add(Aggregates.sort(Sorts.ascending(J_ID)));

        final Duration minBackOff = Duration.ofSeconds(1L);
        final double randomFactor = 0.1;

        final RestartSettings restartSettings = RestartSettings.create(minBackOff, maxBackOff, randomFactor)
                .withMaxRestarts(maxRestarts, minBackOff);
        return RestartSource.onFailuresWithBackoff(restartSettings, () ->
                Source.fromPublisher(journal.aggregate(pipeline))
        );
    }

    private List<BsonField> toFirstJournalEntryFields(final String... journalFields) {
        return Arrays.stream(journalFields)
                .map(fieldName -> {
                    final String serializedFieldName = String.format("$%s.%s", J_EVENT, fieldName);
                    final BsonArray bsonArray =
                            new BsonArray(List.of(new BsonString(serializedFieldName), new BsonInt32(0)));
                    return Accumulators.first(fieldName, new BsonDocument().append("$arrayElemAt", bsonArray));
                })
                .collect(Collectors.toList());
    }

    private int computeMaxRestarts(final Duration maxDuration) {
        if (MAX_BACK_OFF_DURATION.minus(maxDuration).isNegative()) {
            // maxBackOff < maxDuration: backOff at least 7 times (1+2+4+8+16+32+64=127s)
            return Math.max(7, 6 + (int) (maxDuration.toMillis() / MAX_BACK_OFF_DURATION.toMillis()));
        } else {
            // maxBackOff >= maxDuration: maxRestarts = log2 of maxDuration in seconds
            final int log2MaxDuration = 63 - Long.numberOfLeadingZeros(maxDuration.getSeconds());
            return Math.max(0, log2MaxDuration);
        }
    }

    private Source<SnapshotBatch, NotUsed> listNewestActiveSnapshotsByBatch(
            final MongoCollection<Document> snapshotStore,
            final String startPid,
            final int batchSize,
            final String... snapshotFields) {

        final List<Bson> pipeline = new ArrayList<>(5);
        // optional match stage
        if (!startPid.isEmpty()) {
            pipeline.add(Aggregates.match(Filters.gt(S_PROCESSOR_ID, startPid)));
        }

        // sort stage
        pipeline.add(Aggregates.sort(Sorts.orderBy(Sorts.ascending(S_PROCESSOR_ID), Sorts.descending(S_SN))));

        // limit stage. It should come before group stage or MongoDB would scan the entire journal collection.
        pipeline.add(Aggregates.limit(batchSize));

        // group stage 1: by PID. PID is from now on in field _id (S_ID)
        pipeline.add(Aggregates.group("$" + S_PROCESSOR_ID, asFirstSnapshotBsonFields(snapshotFields)));

        // sort stage 2 -- order after group stage is not defined
        pipeline.add(Aggregates.sort(Sorts.ascending(S_ID)));

        // group stage 2: filter out pids whose latest snapshot is a deleted snapshot, but retain max encountered pid
        final String maxPid = "m";
        final String items = "i";
        pipeline.add(Aggregates.group(null,
                Accumulators.max(maxPid, "$" + S_ID),
                Accumulators.push(
                        items,
                        new Document().append("$cond", new Document()
                                .append("if", new Document().append("$ne", Arrays.asList("$" + LIFECYCLE, "DELETED")))
                                .append("then", "$$CURRENT")
                                .append("else", null)
                        ))
        ));

        // remove null entries by projection
        pipeline.add(Aggregates.project(new Document()
                .append(maxPid, 1)
                .append(items, new Document()
                        .append("$setDifference", Arrays.asList("$" + items, Collections.singletonList(null)))
                )
        ));

        return Source.fromPublisher(snapshotStore.aggregate(pipeline))
                .flatMapConcat(document -> {
                    final String theMaxPid = document.getString(maxPid);
                    if (theMaxPid == null) {
                        return Source.empty();
                    } else {
                        return Source.single(new SnapshotBatch(theMaxPid, document.getList(items, Document.class)));
                    }
                });
    }

    /**
     * For $group stage of an aggregation pipeline over a snapshot collection: take the newest values of fields
     * of serialized snapshots. Always include the first snapshot lifecycle.
     *
     * @param snapshotFields fields of a serialized snapshot to project.
     * @return list of group stage field accumulators.
     */
    private List<BsonField> asFirstSnapshotBsonFields(final String... snapshotFields) {
        return Stream.concat(Stream.of(LIFECYCLE), Arrays.stream(snapshotFields))
                .map(fieldName -> {
                    final String serializedFieldName = String.format("$%s.%s", S_SERIALIZED_SNAPSHOT, fieldName);
                    return Accumulators.first(fieldName, serializedFieldName);
                })
                .collect(Collectors.toList());
    }

    private Source<MongoCollection<Document>, NotUsed> getJournal() {
        return Source.single(mongoClient.getDefaultDatabase().getCollection(journalCollection));
    }

    private Source<MongoCollection<Document>, NotUsed> getSnapshotStore() {
        return Source.single(mongoClient.getDefaultDatabase().getCollection(snapsCollection));
    }

    /**
     * Extract the auto-start journal/snaps config from the configuration of the actor system.
     * <p>
     * It assumes that in the Akka system configuration,
     * {@code akka.persistence.journal.auto-start-journals} or
     * {@code akka.persistence.snapshot-store.auto-start-snapshot-stores}
     * contains exactly 1 configuration key, which points to the configuration of the auto-start journal/snapshot-store.
     *
     * @param config the system configuration.
     * @param key either {@code akka.persistence.journal.auto-start-journals} or
     * {@code akka.persistence.snapshot-store.auto-start-snapshot-stores}.
     */
    private static String extractAutoStartConfigKey(final Config config, final String key) {
        final List<String> autoStartJournals = config.getStringList(key);
        if (autoStartJournals.size() != 1) {
            final String message = String.format("Expect %s to be a singleton list, but it is List(%s)",
                    AKKA_PERSISTENCE_JOURNAL_AUTO_START,
                    String.join(", ", autoStartJournals));
            throw new IllegalArgumentException(message);
        } else {
            return autoStartJournals.get(0);
        }
    }

    /**
     * Resolve event journal collection name (e.g. "things_journal") from the auto-start journal configuration.
     * <p>
     * It assumes that in the auto-start journal configuration,
     * {@code overrides.journal-collection} is defined and equal to the name of the event journal
     * collection.
     *
     * @param journalOrSnapsConfig The journal or snapshot-store configuration.
     * @param key Config key of the collection name.
     * @return The name of the event journal collection.
     * @throws IllegalArgumentException if {@code akka.persistence.journal.auto-start-journal} is not a singleton list.
     * @throws com.typesafe.config.ConfigException.Missing if a relevant config value is missing.
     * @throws com.typesafe.config.ConfigException.WrongType if a relevant config value has not the expected type.
     */
    private static String getOverrideCollectionName(final Config journalOrSnapsConfig, final String key) {
        return journalOrSnapsConfig.getString(key);
    }

    private static final class SnapshotBatch {

        private final String maxPid;
        private final List<Document> items;

        private SnapshotBatch(final String maxPid, final List<Document> items) {
            this.maxPid = maxPid;
            this.items = items;
        }

        private String getMaxPid() {
            return maxPid;
        }

        private List<Document> getItems() {
            return items;
        }
    }

}
