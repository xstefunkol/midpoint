package com.evolveum.midpoint.repo.sql.data.common.id;

import com.evolveum.midpoint.repo.sql.data.common.RContainerType;

import java.io.Serializable;

/**
 * @author lazyman
 */
public class RAnyClobId implements Serializable {

    private String ownerOid;
    private Long ownerId;
    private RContainerType ownerType;
    private String checksum;

    public String getOwnerOid() {
        return ownerOid;
    }

    public void setOwnerOid(String ownerOid) {
        this.ownerOid = ownerOid;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public RContainerType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(RContainerType ownerType) {
        this.ownerType = ownerType;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
