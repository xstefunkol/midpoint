package com.evolveum.midpoint.model.impl.dataModel.cytoscapeJS;

import com.evolveum.midpoint.common.refinery.RefinedObjectClassDefinition;
import com.evolveum.midpoint.common.refinery.RefinedResourceSchema;
import com.evolveum.midpoint.model.impl.dataModel.DataModel;
import com.evolveum.midpoint.model.impl.dataModel.model.*;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lubos on 10. 4. 2017.
 */
public class CJSFactory {
   public void createGraph(DataModel dataModel) throws IOException {
       JSONObject finalFile = new JSONObject();
       JSONArray nodesJSON = new JSONArray();
       JSONArray edgesJSON = new JSONArray();

       Map<DataItem, NodeCJS> nodes = initMap(dataModel);
       ArrayList<ObjectTNodeCJS> objectDefinitions = new ArrayList<>();

       int resourceCluster = 1;
       int counter = 1;
       for (PrismObject<ResourceType> resource : dataModel.getResources().values()) {
           ResourceNodeCJS resourceNodeCJS = new ResourceNodeCJS("r" + Integer.toString(resourceCluster),
                   resource.getName().toString(), "", "Resource", resource.getOid());
           JSONObject resourceNode = new JSONObject();
           JSONObject resourceData = new JSONObject();

           resourceData.put("id", resourceNodeCJS.getId());
           resourceData.put("oid", resourceNodeCJS.getOid());
           resourceData.put("name", resourceNodeCJS.getName());
           resourceNode.put("data", resourceData);
           resourceNode.put("classes", resourceNodeCJS.getClasses());
           nodesJSON.add(resourceNode);
           counter++;

           RefinedResourceSchema schema = dataModel.getRefinedResourceSchema(resource.getOid());
           for (RefinedObjectClassDefinition def : schema.getRefinedDefinitions()) {
               ObjectTNodeCJS objectClass = new ObjectTNodeCJS();
               objectClass.initializeOTN(def, "o" + Integer.toString(counter), "r" +
                       Integer.toString(resourceCluster), resource.getOid());
               objectDefinitions.add(objectClass);
               JSONObject objectCNode = new JSONObject();
               JSONObject objectCData = new JSONObject();

               objectCData.put("id", objectClass.getId());
               objectCData.put("name", objectClass.getName());
               objectCData.put("parent", objectClass.getParent());
               objectCData.put("resourceOid", objectClass.getResourceOid());
               objectCData.put("kind", ResourceTypeUtil.fillDefault(def.getKind()).toString());
               objectCData.put("intent", objectClass.getIntent());
               objectCData.put("typeName", def.getObjectClassDefinition().getTypeName().getLocalPart());
               objectCNode.put("data", objectCData);
               objectCNode.put("classes", objectClass.getClasses());
               nodesJSON.add(objectCNode);
               counter++;
           }
           resourceCluster++;
       }

       ArrayList<String> createdNodes = new ArrayList<>();
       setParents(nodes, objectDefinitions);

       int mapC = 1;
       int edgeC = 1;
       for (Relation relation : dataModel.getRelations()) {
           if (relation.getSources().size() == 0) {
               ExpNodeCJS mapNode = new ExpNodeCJS();
               mapNode.setId("m" + Integer.toString(mapC));
               mapNode.setExpression((MappingRelation) relation);

               String sourceId = mapNode.getId();
               String targetId = getDataItemNode(nodes, relation.getTarget()).getId();

               ComplexEdgeCJS edge = new ComplexEdgeCJS();
               edge.setId("e" + Integer.toString(edgeC));
               edge.setSource(sourceId);
               edge.setTarget(targetId);
               edge.setStrengthStyle((MappingRelation)relation);
               edge.setExpression((MappingRelation) relation);

               createNodeRel(relation.getTarget(), nodesJSON, nodes);

               JSONObject nodeCJS = new JSONObject();
               JSONObject nodeDataCJS = new JSONObject();
               nodeDataCJS.put("id", mapNode.getId());
               nodeDataCJS.put("name", mapNode.getName());
               nodeDataCJS.put("parent", mapNode.getParent());
               nodeDataCJS.put("mapping", mapNode.getMapping());
               nodeCJS.put("data", nodeDataCJS);
               nodeCJS.put("classes", mapNode.getClasses());
               nodesJSON.add(nodeCJS);

               JSONObject edgeJSON = new JSONObject();
               JSONObject edgeDataJSON = new JSONObject();
               edgeDataJSON.put("id", edge.getId());
               edgeDataJSON.put("source", edge.getSource());
               edgeDataJSON.put("target", edge.getTarget());
               edgeDataJSON.put("label", edge.getLabel());
               edgeDataJSON.put("strength", edge.getStrength());
               edgeDataJSON.put("mapping", edge.getMapping());
               edgeDataJSON.put("style", edge.getStyle());
               edgeJSON.put("data", edgeDataJSON);
               edgesJSON.add(edgeJSON);
               edgeC++;
               mapC++;
           }
           else {
               for (DataItem item : relation.getSources()) {
                   if (!(isNodeCreated(nodes, item, createdNodes))) {
                       createNodeRel(item, nodesJSON, nodes);
                   }
                   if (!(isNodeCreated(nodes, relation.getTarget(), createdNodes))) {
                       createNodeRel(relation.getTarget(), nodesJSON, nodes);
                   }

                   JSONObject edgeJSON = new JSONObject();
                   JSONObject edgeDataJSON = new JSONObject();

                   String sourceId = getDataItemNode(nodes, item).getId();
                   String targetId = getDataItemNode(nodes, relation.getTarget()).getId();

                   ComplexEdgeCJS edge = new ComplexEdgeCJS();
                   edge.setId("e" + Integer.toString(edgeC));
                   edge.setSource(sourceId);
                   edge.setTarget(targetId);
                   edge.setStrengthStyle((MappingRelation) relation);
                   edge.setExpression((MappingRelation) relation);

                   edgeDataJSON.put("id", edge.getId());
                   edgeDataJSON.put("source", edge.getSource());
                   edgeDataJSON.put("target", edge.getTarget());
                   edgeDataJSON.put("label", edge.getLabel());
                   edgeDataJSON.put("strength", edge.getStrength());
                   edgeDataJSON.put("mapping", edge.getMapping());
                   edgeDataJSON.put("style", edge.getStyle());
                   edgeJSON.put("data", edgeDataJSON);
                   edgesJSON.add(edgeJSON);
                   edgeC++;
               }
          }
       }

       finalFile.put("nodes", nodesJSON);
       finalFile.put("edges", edgesJSON);
       createJSONFile(finalFile);
   }

   private boolean isNodeCreated(Map<DataItem, NodeCJS> array, DataItem item, ArrayList<String> createdNodes) {
       for (String id : createdNodes) {
           if (id.equals(getDataItemNode(array, item).getId())) {
               return true;
           }
       }
       createdNodes.add(getDataItemNode(array, item).getId());
       return false;
   }

   private void createNodeRel(DataItem item, JSONArray nodesJSON, Map<DataItem, NodeCJS> nodes) {
       JSONObject node = new JSONObject();
       JSONObject nodeData = new JSONObject();

       if (item instanceof ResourceDataItem) {
           nodeData.put("id", getDataItemNode(nodes, item).getId());
           nodeData.put("name", getDataItemNode(nodes, item).getName());
           nodeData.put("parent", getDataItemNode(nodes, item).getParent());
           nodeData.put("kind", ((ResourceDataItemCJS)getDataItemNode(nodes, item)).getKind());
           nodeData.put("intent", ((ResourceDataItemCJS)getDataItemNode(nodes, item)).getIntent());
           nodeData.put("typeName", ((ResourceDataItemCJS)getDataItemNode(nodes, item)).getTypeName());
           node.put("data", nodeData);
           node.put("classes", getDataItemNode(nodes, item).getClasses());
       } else if (item instanceof RepositoryDataItem) {
           nodeData.put("id", getDataItemNode(nodes, item).getId());
           nodeData.put("name", getDataItemNode(nodes, item).getName());
           nodeData.put("parent", getDataItemNode(nodes, item).getParent());
           node.put("data", nodeData);
           node.put("classes", getDataItemNode(nodes, item).getClasses());
       } else if (item instanceof AdHocDataItem) {
           nodeData.put("id", getDataItemNode(nodes, item).getId());
           nodeData.put("name", getDataItemNode(nodes, item).getName());
           nodeData.put("parent", getDataItemNode(nodes, item).getParent());
           node.put("data", nodeData);
           node.put("classes", getDataItemNode(nodes, item).getClasses());
       } else {
           throw new AssertionError("Unsupported dataItem for visualization" + item);
       }
       nodesJSON.add(node);
   }

    private void createJSONFile(JSONObject object) throws IOException {
        FileWriter file = new FileWriter(System.getProperty("user.home") + "/Desktop" + "/elements.json");
        try {
            file.write(object.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            file.flush();
            file.close();
        }
    }

    private void setParents(Map<DataItem, NodeCJS> array, ArrayList<ObjectTNodeCJS> objNodes) {
        for (Map.Entry<DataItem, NodeCJS> o : array.entrySet()) {
            DataItem dataItem = o.getKey();
            NodeCJS node = o.getValue();
            if (dataItem instanceof ResourceDataItem) {
                ObjectTNodeCJS oNode = new ObjectTNodeCJS();
                oNode.initializeOTN(((ResourceDataItem) dataItem).getRefinedObjectClassDefinition(), "", "", ((ResourceDataItem) dataItem).getResourceOid());
                for (ObjectTNodeCJS n : objNodes) {
                    if (n.getResourceOid().equals(oNode.getResourceOid()) && n.getName().equals(oNode.getName())
                            && n.getTypeName().equals(oNode.getTypeName()) && n.getIntent().equals(oNode.getIntent())
                            && n.getKind().equals(oNode.getKind())) {
                        node.setParent(n.getId());
                        break;
                    }
                }
            }
        }
    }

    private Map<DataItem, NodeCJS> initMap(DataModel dataModel) {
        Map<DataItem, NodeCJS> nodes = new HashMap<>();
        int counter = 0;
        for (DataItem dataItem : dataModel.getDataItems()) {
            NodeCJS node;
            if (dataItem instanceof RepositoryDataItem) {
                String classes = getTypeRep((RepositoryDataItem)dataItem);
                String entity = StringUtils.removeEnd(((RepositoryDataItem)dataItem).getTypeName().getLocalPart(),
                        "Type");
                String pathString = ((RepositoryDataItem)dataItem).getItemPath().toString();
                String name = entity + "\n" + pathString;
                node = new NodeCJS(Integer.toString(counter), name, "", classes);
            } else if (dataItem instanceof ResourceDataItem) {
                node = new ResourceDataItemCJS(Integer.toString(counter),
                        ((ResourceDataItem)dataItem).getLastItemName().getLocalPart(), "", "ResourceDataItem",
                        ResourceTypeUtil.fillDefault(((ResourceDataItem)dataItem).getRefinedObjectClassDefinition().getKind()).toString(),
                        ((ResourceDataItem)dataItem).getRefinedObjectClassDefinition().getIntent(),
                        ((ResourceDataItem)dataItem).getRefinedObjectClassDefinition().getTypeName().getLocalPart());
            } else if (dataItem instanceof AdHocDataItem) {
                node = new NodeCJS(Integer.toString(counter), String.valueOf(((AdHocDataItem) dataItem).getItemPath()),
                        "", "AdHocDataItem");
            } else {
                throw new AssertionError("Unsupported dataItem for visualization" + dataItem);
            }
            nodes.put(dataItem, node);
            counter++;
        }
        return nodes;
    }

    private String getTypeRep(RepositoryDataItem dataItem) {
        if (QNameUtil.match(UserType.COMPLEX_TYPE, dataItem.getTypeName())) {
            return "UserRep";
        } else if (QNameUtil.match(RoleType.COMPLEX_TYPE, dataItem.getTypeName())) {
            return "RoleRep";
        } else if (QNameUtil.match(OrgType.COMPLEX_TYPE, dataItem.getTypeName())) {
            return "OrgRep";
        } else {
            return "";
        }
    }

    private NodeCJS getDataItemNode(Map<DataItem, NodeCJS> array, DataItem item) {
        NodeCJS node = array.get(item);
        if (node != null) {
            return node;
        } else {
            throw new IllegalStateException("No match     " + item);
        }
    }
}
