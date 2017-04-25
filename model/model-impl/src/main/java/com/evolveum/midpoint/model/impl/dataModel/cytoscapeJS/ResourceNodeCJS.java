package com.evolveum.midpoint.model.impl.dataModel.cytoscapeJS;

/**
 * Created by Lubos on 21. 4. 2017.
 */
public class ResourceNodeCJS extends NodeCJS {
    private String oid;

    public ResourceNodeCJS(String id, String name, String parent, String classes, String oid) {
        super(id, name, parent, classes);
        this.oid = oid;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
