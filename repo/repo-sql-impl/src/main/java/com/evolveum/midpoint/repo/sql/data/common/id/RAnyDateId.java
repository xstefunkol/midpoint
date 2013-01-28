package com.evolveum.midpoint.repo.sql.data.common.id;

import com.evolveum.midpoint.repo.sql.data.common.RContainerType;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author lazyman
 */
public class RAnyDateId implements Serializable {

    private String ownerOid;
    private Long ownerId;
    private RContainerType ownerType;
    private Timestamp value;

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

    public Timestamp getValue() {
        return value;
    }

    public void setValue(Timestamp value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RAnyDateId that = (RAnyDateId) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (ownerId != null ? !ownerId.equals(that.ownerId) : that.ownerId != null) return false;
        if (ownerOid != null ? !ownerOid.equals(that.ownerOid) : that.ownerOid != null) return false;
        if (ownerType != that.ownerType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ownerOid != null ? ownerOid.hashCode() : 0;
        result = 31 * result + (ownerId != null ? ownerId.hashCode() : 0);
        result = 31 * result + (ownerType != null ? ownerType.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RAnyDateId[" + ownerOid + "," + ownerId + "," + ownerType + "," + value + "]";
    }
}
