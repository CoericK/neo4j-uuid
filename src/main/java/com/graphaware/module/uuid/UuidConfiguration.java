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

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.common.policy.none.IncludeNoRelationships;
import com.graphaware.runtime.config.BaseTxDrivenModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;


/**
 * {@link BaseTxDrivenModuleConfiguration} for {@link com.graphaware.module.uuid.UuidModule}.
 */
public class UuidConfiguration extends BaseTxDrivenModuleConfiguration<UuidConfiguration> {

    private static final String DEFAULT_UUID_PROPERTY = Properties.UUID;
    private static final String DEFAULT_UUID_NODEX_INDEX = Indexes.UUID_NODE_INDEX;
    private static final String DEFAULT_UUID_REL_INDEX = Indexes.UUID_REL_INDEX;

    private final String uuidProperty;
    private final String uuidIndex;
    private final String uuidRelationshipIndex;

    private UuidConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil, String uuidProperty, String uuidIndex, String uuidRelationshipIndex) {
        super(inclusionPolicies, initializeUntil);
        this.uuidProperty = uuidProperty;
        this.uuidIndex = uuidIndex;
        this.uuidRelationshipIndex = uuidRelationshipIndex;
    }

    /**
     * Create a default configuration with default uuid property = {@link #DEFAULT_UUID_PROPERTY},
     * uuid index = {@link #DEFAULT_UUID_NODEX_INDEX}
     * inclusion policies = {@link InclusionPoliciesFactory#allBusiness()} with {@link IncludeNoRelationships},
     * and initialize until = {@link #ALWAYS}.
     * <p/>
     * Change this by calling {@link #withUuidProperty(String)}, with* other inclusion strategies
     * on the object, always using the returned object (this is a fluent interface).
     */
    public static UuidConfiguration defaultConfiguration() {
        return new UuidConfiguration(InclusionPoliciesFactory
                .allBusiness()
                .with(IncludeNoRelationships.getInstance())
                , ALWAYS, DEFAULT_UUID_PROPERTY, DEFAULT_UUID_NODEX_INDEX, DEFAULT_UUID_REL_INDEX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UuidConfiguration newInstance(InclusionPolicies inclusionPolicies, long initializeUntil) {
        return new UuidConfiguration(inclusionPolicies, initializeUntil, getUuidProperty(), getUuidIndex(), getUuidRelationshipIndex());
    }

    public String getUuidProperty() {
        return uuidProperty;
    }

    public String getUuidIndex() {
        return uuidIndex;
    }

    public String getUuidRelationshipIndex() {
        return uuidRelationshipIndex;
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid property.
     *
     * @param uuidProperty of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidProperty(String uuidProperty) {
        return new UuidConfiguration(getInclusionPolicies(), initializeUntil(), uuidProperty, getUuidIndex(), getUuidRelationshipIndex());
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid index.
     *
     * @param uuidIndex of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidIndex(String uuidIndex) {
        return new UuidConfiguration(getInclusionPolicies(), initializeUntil(), getUuidProperty(), uuidIndex, getUuidRelationshipIndex());
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid relationship index.
     *
     * @param uuidRelationshipIndex of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidRelationshipIndex(String uuidRelationshipIndex) {
        return new UuidConfiguration(getInclusionPolicies(), initializeUntil(), getUuidProperty(), getUuidIndex(), uuidRelationshipIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        UuidConfiguration that = (UuidConfiguration) o;

        if (!uuidProperty.equals(that.uuidProperty)) {
            return false;
        }
        if (!uuidIndex.equals(that.uuidIndex)) {
            return false;
        }

        if (uuidRelationshipIndex.equals(that.uuidRelationshipIndex)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + uuidProperty.hashCode();
        result = 31 * result + uuidIndex.hashCode();
        result = 31 * result + uuidRelationshipIndex.hashCode();
        return result;
    }
}
