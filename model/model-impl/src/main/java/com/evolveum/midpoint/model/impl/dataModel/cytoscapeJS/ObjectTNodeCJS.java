package com.evolveum.midpoint.model.impl.dataModel.cytoscapeJS;

import com.evolveum.midpoint.common.refinery.RefinedObjectClassDefinition;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;


/**
 * Created by Lubos on 20. 4. 2017.
 */
public class ObjectTNodeCJS extends NodeCJS {
    private String resourceOid;
    private String kind;
    private String intent;
    private String typeName;

    public ObjectTNodeCJS() {

    }

    public String getResourceOid() {
        return resourceOid;
    }

    public void setResourceOid(String resourceOid) {
        this.resourceOid = resourceOid;
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

    public void initializeOTN(RefinedObjectClassDefinition refinedObjectClassDefinition, String id, String parent, String resourceOid) {
        if (refinedObjectClassDefinition != null) {
            String name = "";
            if (refinedObjectClassDefinition.getDisplayName() != null) {
                name = refinedObjectClassDefinition.getDisplayName();
            }
            name += "\n" + String.valueOf(ResourceTypeUtil.fillDefault(refinedObjectClassDefinition.getKind())) + "\n" +
                    ResourceTypeUtil.fillDefault(refinedObjectClassDefinition.getIntent()) + "\n" +
                    refinedObjectClassDefinition.getObjectClassDefinition().getTypeName().getLocalPart();
            setName(name);
            setId(id);
            setClasses("ObjectTNode");
            setResourceOid(resourceOid);
            setParent(parent);
            setKind(String.valueOf(ResourceTypeUtil.fillDefault(refinedObjectClassDefinition.getKind())));
            setIntent(ResourceTypeUtil.fillDefault(refinedObjectClassDefinition.getIntent()));
            setTypeName(refinedObjectClassDefinition.getObjectClassDefinition().getTypeName().getLocalPart());
        }
    }
}
