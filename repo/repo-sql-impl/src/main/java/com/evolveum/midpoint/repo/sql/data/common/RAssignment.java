/*
 * Copyright (c) 2012 Evolveum
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
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.data.common;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.data.common.embedded.RActivation;
import com.evolveum.midpoint.repo.sql.data.common.embedded.REmbeddedReference;
import com.evolveum.midpoint.repo.sql.data.common.other.RAssignmentOwner;
import com.evolveum.midpoint.repo.sql.data.common.other.RContainerType;
import com.evolveum.midpoint.repo.sql.util.ContainerIdGenerator;
import com.evolveum.midpoint.repo.sql.util.DtoTranslationException;
import com.evolveum.midpoint.repo.sql.util.RUtil;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ConstructionType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.AssignmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ExtensionType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectType;
import org.apache.commons.lang.Validate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import javax.persistence.*;

/**
 * @author lazyman
 */
@Entity
@org.hibernate.annotations.Table(appliesTo = "m_assignment",
        indexes = {@Index(name = "iAssignmentAdministrative", columnNames = "administrativeStatus")})
@ForeignKey(name = "fk_assignment")
public class RAssignment extends RContainer implements ROwnable {

    public static final String F_OWNER = "owner";
    /**
     * enum identifier of object class which owns this assignment. It's used because we have to
     * distinguish between assignments and inducements (all of them are the same kind) in {@link RAbstractRole}.
     */
    public static final String F_ASSIGNMENT_OWNER = "assignmentOwner";

    private static final Trace LOGGER = TraceManager.getTrace(RAssignment.class);

    //owner
    private RObject owner;
    private String ownerOid;
    private Long ownerId;
    private RAssignmentOwner assignmentOwner;
    //extension
    private RAnyContainer extension;
    //assignment fields
    private String description;
    private RActivation activation;
    private String accountConstruction;
    private REmbeddedReference targetRef;
    private RMetadata metadata;

    public RAssignment() {
        this(null, null);
    }

    public RAssignment(RObject owner, RAssignmentOwner assignmentOwner) {
        this.owner = owner;
        this.assignmentOwner = assignmentOwner;
    }

    @Enumerated(EnumType.ORDINAL)
    public RAssignmentOwner getAssignmentOwner() {
        return assignmentOwner;
    }

    @OneToOne(mappedBy = RMetadata.F_OWNER, optional = true, orphanRemoval = true)
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    public RMetadata getMetadata() {
        return metadata;
    }

    @ForeignKey(name = "fk_assignment_owner")
    @MapsId("owner")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "owner_oid", referencedColumnName = "oid"),
            @JoinColumn(name = "owner_id", referencedColumnName = "id")
    })
    public RObject getOwner() {
        return owner;
    }

    @Embedded
    public REmbeddedReference getTargetRef() {
        return targetRef;
    }

    @Column(name = "owner_id", nullable = false)
    public Long getOwnerId() {
        if (ownerId == null && owner != null) {
            ownerId = owner.getId();
        }
        return ownerId;
    }

    @Column(name = "owner_oid", length = 36, nullable = false)
    public String getOwnerOid() {
        if (ownerOid == null && owner != null) {
            ownerOid = owner.getOid();
        }
        return ownerOid;
    }

    @com.evolveum.midpoint.repo.sql.query.definition.Any(jaxbNameLocalPart = "extension")
    @OneToOne(optional = true, orphanRemoval = true)
    @ForeignKey(name = "none")
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinColumns({
            @JoinColumn(name = "extOid", referencedColumnName = "owner_oid"),
            @JoinColumn(name = "extId", referencedColumnName = "owner_id"),
            @JoinColumn(name = "extType", referencedColumnName = "owner_type")
    })
    public RAnyContainer getExtension() {
        return extension;
    }

    @Embedded
    public RActivation getActivation() {
        return activation;
    }

    @Lob
    @Type(type = RUtil.LOB_STRING_TYPE)
    public String getAccountConstruction() {
        return accountConstruction;
    }

    @Lob
    @Type(type = RUtil.LOB_STRING_TYPE)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActivation(RActivation activation) {
        this.activation = activation;
    }

    public void setExtension(RAnyContainer extension) {
        this.extension = extension;
        if (this.extension != null) {
            this.extension.setOwnerType(RContainerType.ASSIGNMENT);
        }
    }

    public void setAccountConstruction(String accountConstruction) {
        this.accountConstruction = accountConstruction;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerOid(String ownerOid) {
        this.ownerOid = ownerOid;
    }

    public void setTargetRef(REmbeddedReference targetRef) {
        this.targetRef = targetRef;
    }

    public void setOwner(RObject owner) {
        this.owner = owner;
    }

    public void setMetadata(RMetadata metadata) {
        this.metadata = metadata;
    }

    public void setAssignmentOwner(RAssignmentOwner assignmentOwner) {
        this.assignmentOwner = assignmentOwner;
    }

    @Transient
    @Override
    public RContainer getContainerOwner() {
        return getOwner();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RAssignment that = (RAssignment) o;

        if (accountConstruction != null ? !accountConstruction.equals(that.accountConstruction) : that.accountConstruction != null)
            return false;
        if (activation != null ? !activation.equals(that.activation) : that.activation != null) return false;
        if (extension != null ? !extension.equals(that.extension) : that.extension != null) return false;
        if (targetRef != null ? !targetRef.equals(that.targetRef) : that.targetRef != null) return false;
        if (assignmentOwner != null ? !assignmentOwner.equals(that.assignmentOwner) : that.assignmentOwner != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (activation != null ? activation.hashCode() : 0);
        result = 31 * result + (accountConstruction != null ? accountConstruction.hashCode() : 0);
        return result;
    }

    public static void copyToJAXB(RAssignment repo, AssignmentType jaxb, PrismContext prismContext) throws
            DtoTranslationException {
        Validate.notNull(repo, "Repo object must not be null.");
        Validate.notNull(jaxb, "JAXB object must not be null.");

        jaxb.setId(repo.getId());
        jaxb.setDescription(repo.getDescription());

        if (repo.getExtension() != null) {
            ExtensionType extension = new ExtensionType();
            jaxb.setExtension(extension);
            RAnyContainer.copyToJAXB(repo.getExtension(), extension, prismContext);
        }
        if (repo.getActivation() != null) {
            jaxb.setActivation(repo.getActivation().toJAXB(prismContext));
        }

        try {
            jaxb.setAccountConstruction(RUtil.toJAXB(repo.getAccountConstruction(), ConstructionType.class, prismContext));
        } catch (Exception ex) {
            throw new DtoTranslationException(ex.getMessage(), ex);
        }

        if (repo.getTargetRef() != null) {
            jaxb.setTargetRef(repo.getTargetRef().toJAXB(prismContext));
        }

        if (repo.getMetadata() != null) {
            jaxb.setMetadata(repo.getMetadata().toJAXB(prismContext));
        }
    }

    public static void copyFromJAXB(AssignmentType jaxb, RAssignment repo, ObjectType parent, PrismContext prismContext)
            throws DtoTranslationException {
        Validate.notNull(repo, "Repo object must not be null.");
        Validate.notNull(jaxb, "JAXB object must not be null.");

        repo.setOid(parent.getOid());
        repo.setId(jaxb.getId());
        repo.setDescription(jaxb.getDescription());

        if (jaxb.getExtension() != null) {
            RAnyContainer extension = new RAnyContainer();
            extension.setOwner(repo);

            repo.setExtension(extension);
            RAnyContainer.copyFromJAXB(jaxb.getExtension(), extension, prismContext);
        }

        if (jaxb.getActivation() != null) {
            RActivation activation = new RActivation();
            RActivation.copyFromJAXB(jaxb.getActivation(), activation, prismContext);
            repo.setActivation(activation);
        }

        try {
            repo.setAccountConstruction(RUtil.toRepo(jaxb.getAccountConstruction(), prismContext));
        } catch (Exception ex) {
            throw new DtoTranslationException(ex.getMessage(), ex);
        }

        if (jaxb.getTarget() != null) {
            LOGGER.warn("Target from assignment type won't be saved. It should be translated to target reference.");
        }

        repo.setTargetRef(RUtil.jaxbRefToEmbeddedRepoRef(jaxb.getTargetRef(), prismContext));

        if (jaxb.getMetadata() != null) {
            RMetadata metadata = new RMetadata();
            metadata.setOwner(repo);
            RMetadata.copyFromJAXB(jaxb.getMetadata(), metadata, prismContext);
            repo.setMetadata(metadata);
        }

        ContainerIdGenerator gen = new ContainerIdGenerator();
        repo.setId((Long) gen.generate(null, repo));
    }

    public AssignmentType toJAXB(PrismContext prismContext) throws DtoTranslationException {
        AssignmentType object = new AssignmentType();
        RAssignment.copyToJAXB(this, object, prismContext);
        return object;
    }
}
