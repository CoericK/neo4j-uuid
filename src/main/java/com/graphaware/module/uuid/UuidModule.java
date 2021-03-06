/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.graphaware.module.uuid;

import com.graphaware.common.util.Change;
import com.graphaware.common.uuid.EaioUuidGenerator;
import com.graphaware.common.uuid.UuidGenerator;
import com.graphaware.module.uuid.index.LegacyIndexer;
import com.graphaware.module.uuid.index.UuidIndexer;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import com.graphaware.tx.executor.input.AllNodes;
import com.graphaware.tx.executor.input.AllRelationships;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * {@link com.graphaware.runtime.module.TxDrivenModule} that assigns UUID's to nodes in the graph.
 */
public class UuidModule extends BaseTxDrivenModule<Void> {

    public static final String DEFAULT_MODULE_ID = "UIDM";
    private static final int BATCH_SIZE = 1000;

    private final UuidGenerator uuidGenerator;
    private final UuidConfiguration uuidConfiguration;
    private final UuidIndexer uuidIndexer;

    /**
     * Construct a new UUID module.
     *
     * @param moduleId ID of the module.
     */
    public UuidModule(String moduleId, UuidConfiguration configuration, GraphDatabaseService database) {
        super(moduleId);
        this.uuidGenerator = new EaioUuidGenerator();
        this.uuidConfiguration = configuration;
        this.uuidIndexer = new LegacyIndexer(database, configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UuidConfiguration getConfiguration() {
        return uuidConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(GraphDatabaseService database) {
        new IterableInputBatchTransactionExecutor<>(
                database,
                BATCH_SIZE,
                new AllNodes(database, BATCH_SIZE),
                (db, node, batchNumber, stepNumber) -> {
                    if (getConfiguration().getInclusionPolicies().getNodeInclusionPolicy().include(node)) {
                        assignUuid(node);
                    }
                }
        ).execute();

        new IterableInputBatchTransactionExecutor<>(
                database,
                BATCH_SIZE,
                new AllRelationships(database, BATCH_SIZE),
                (db, rel, batchNumber, stepNumber) -> {
                    if (getConfiguration().getInclusionPolicies().getRelationshipInclusionPolicy().include(rel)) {
                        assignUuid(rel);
                    }
                }
        ).execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) throws DeliberateTransactionRollbackException {

        //Set the UUID on all created nodes
        for (Node node : transactionData.getAllCreatedNodes()) {
            assignUuid(node);
        }

        for (Node node : transactionData.getAllDeletedNodes()) {
            uuidIndexer.deleteNodeFromIndex(node);
        }

        //Check if the UUID has been modified or removed from the node and throw an error
        for (Change<Node> change : transactionData.getAllChangedNodes()) {
            if (!change.getCurrent().hasProperty(uuidConfiguration.getUuidProperty())) {
                throw new DeliberateTransactionRollbackException("You are not allowed to remove the " + uuidConfiguration.getUuidProperty() + " property");
            }

            if (!change.getPrevious().getProperty(uuidConfiguration.getUuidProperty()).equals(change.getCurrent().getProperty(uuidConfiguration.getUuidProperty()))) {
                throw new DeliberateTransactionRollbackException("You are not allowed to modify the " + uuidConfiguration.getUuidProperty() + " property");
            }
        }

        //Set the UUID on all created relationships
        for (Relationship relationship : transactionData.getAllCreatedRelationships()) {
            assignUuid(relationship);
        }

        for (Relationship rel : transactionData.getAllDeletedRelationships()) {
            uuidIndexer.deleteRelationshipFromIndex(rel);
        }

        //Check if the UUID has been modified or removed from the relationship and throw an error
        for (Change<Relationship> change : transactionData.getAllChangedRelationships()) {
            if (!change.getCurrent().hasProperty(uuidConfiguration.getUuidProperty())) {
                throw new DeliberateTransactionRollbackException("You are not allowed to remove the " + uuidConfiguration.getUuidProperty() + " property");
            }

            if (!change.getPrevious().getProperty(uuidConfiguration.getUuidProperty()).equals(change.getCurrent().getProperty(uuidConfiguration.getUuidProperty()))) {
                throw new DeliberateTransactionRollbackException("You are not allowed to modify the " + uuidConfiguration.getUuidProperty() + " property");
            }
        }

        return null;
    }

    private void assignUuid(Node node) {
        if (!node.hasProperty(uuidConfiguration.getUuidProperty())) {
            String uuid = uuidGenerator.generateUuid();
            node.setProperty(uuidConfiguration.getUuidProperty(), uuid);
        } else {
            Node existingNode = uuidIndexer.getNodeByUuid(node.getProperty(uuidConfiguration.getUuidProperty()).toString());
            if (existingNode != null && existingNode.getId() != node.getId()) {
                throw new DeliberateTransactionRollbackException("Another node with UUID " + node.getProperty(uuidConfiguration.getUuidProperty()) + " already exists (" + existingNode + ")!");
            }
        }
        uuidIndexer.indexNode(node);
    }

    private void assignUuid(Relationship relationship) {
        if (!relationship.hasProperty(uuidConfiguration.getUuidProperty())) {
            String uuid = uuidGenerator.generateUuid();
            relationship.setProperty(uuidConfiguration.getUuidProperty(), uuid);
        } else {
            Relationship existingRelationship = uuidIndexer.getRelationshipByUuid(relationship.getProperty(uuidConfiguration.getUuidProperty()).toString());
            if (existingRelationship != null && existingRelationship.getId() != relationship.getId()) {
                throw new DeliberateTransactionRollbackException("Another relationship with UUID " + relationship.getProperty(uuidConfiguration.getUuidProperty()) + " already exists (" + existingRelationship + ")!");
            }
        }
        uuidIndexer.indexRelationship(relationship);
    }
}
