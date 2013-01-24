/*
 * Copyright (c) 2013 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2013 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.data.common.enums;

import com.evolveum.midpoint.repo.sql.data.common.RObjectReference;
import com.evolveum.midpoint.repo.sql.data.common.type.*;
import org.apache.commons.lang.Validate;

/**
 * This is just helper enumeration for different types of reference entities
 * used in many relationships.
 *
 * @author lazyman
 */
public enum RReferenceOwner {

    /**
     * this constant also have to be changed in
     * {@link com.evolveum.midpoint.repo.sql.data.common.type.RParentOrgRef}
     */
    OBJECT_PARENT_ORG(RParentOrgRef.DISCRIMINATOR),
    /**
     * this constant also have to be changed in
     * {@link com.evolveum.midpoint.repo.sql.data.common.type.RAccountRef}
     */
    USER_ACCOUNT(RAccountRef.DISCRIMINATOR),
    /**
     * this constant also have to be changed in
     * {@link com.evolveum.midpoint.repo.sql.data.common.type.RResourceApproverRef}
     */
    RESOURCE_BUSINESS_CONFIGURATON_APPROVER(RResourceApproverRef.DISCRIMINATOR),
    /**
     * this constant also have to be changed in
     * {@link com.evolveum.midpoint.repo.sql.data.common.type.RTargetRef}
     */
    EXCLUSION_TARGET(RTargetRef.DISCRIMINATOR),             //todo maybe can be embedded
    /**
     * this constant also have to be changed in
     * {@link com.evolveum.midpoint.repo.sql.data.common.type.RConnectorHostRef}
     */
    CONNECTOR_CONNECTOR_HOST(RConnectorHostRef.DISCRIMINATOR),       //todo maybe can be embedded
    /**
     * this constant also have to be changed in
     * {@link com.evolveum.midpoint.repo.sql.data.common.type.RResourceRef}
     */
    RESOURCE_OBJECT_SHADOW_RESOURCE(RResourceRef.DISCRIMINATOR),    //todo maybe can be embedded
    /**
     * this constant also have to be changed in
     * {@link com.evolveum.midpoint.repo.sql.data.common.type.RRoleApproverRef}
     */
    ROLE_APPROVER(RRoleApproverRef.DISCRIMINATOR),
    /**
     * this constant also have to be changed in
     * {@link com.evolveum.midpoint.repo.sql.data.common.type.ROrgRootRef}
     */
    SYSTEM_CONFIGURATION_ORG_ROOT(ROrgRootRef.DISCRIMINATOR);

    private String discriminator;

    private RReferenceOwner(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public static RObjectReference createObjectReference(RReferenceOwner owner) {
        Validate.notNull(owner, "Reference owner must not be null.");

        switch (owner) {
            case OBJECT_PARENT_ORG:
                return new RParentOrgRef();
            case RESOURCE_OBJECT_SHADOW_RESOURCE:
                return new RResourceRef();
            case ROLE_APPROVER:
                return new RRoleApproverRef();
            case SYSTEM_CONFIGURATION_ORG_ROOT:
                return new ROrgRootRef();
            case USER_ACCOUNT:
                return new RAccountRef();
            case CONNECTOR_CONNECTOR_HOST:
                return new RConnectorHostRef();
            case EXCLUSION_TARGET:
                return new RTargetRef();
            case RESOURCE_BUSINESS_CONFIGURATON_APPROVER:
                return new RResourceApproverRef();
            default:
                throw new IllegalArgumentException("This is unknown reference owner: " + owner);
        }
    }
}
