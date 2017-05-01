package com.evolveum.midpoint.model.impl.dataModel.cytoscapeJS;

/**
 * Created by Lubos on 22. 4. 2017.
 */
public class ResourceDataItemCJS extends NodeCJS {
    private String kind;
    private String intent;
    private String typeName;

    public ResourceDataItemCJS(String id, String name, String parent, String classes, String kind, String intent, String typeName) {
        super(id, name, parent, classes);
        this.kind = kind;
        this.intent = intent;
        this.typeName = typeName;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

}