package com.evolveum.midpoint.model.impl.dataModel.cytoscapeJS;

/**
 * Created by Lubos on 10. 4. 2017.
 */
public class EdgeCJS {
    private String id;
    private String source;
    private String target;

    public EdgeCJS() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
