package com.evolveum.midpoint.model.impl.dataModel.cytoscapeJS;

/**
 * Created by Lubos on 10. 4. 2017.
 */
public class NodeCJS {
    private String id;
    private String name = "";
    private String parent = "";
    private String classes = "";

    public NodeCJS() {

    }

    public NodeCJS(String id, String name, String parent, String classes) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.classes = classes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent() { return parent; }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }
}
