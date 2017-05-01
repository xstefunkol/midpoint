package com.evolveum.midpoint.model.impl.dataModel.cytoscapeJS;

import com.evolveum.midpoint.model.impl.dataModel.model.MappingRelation;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AsIsExpressionEvaluatorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ExpressionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ScriptExpressionEvaluatorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SearchObjectRefExpressionEvaluatorType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBElement;

/**
 * Created by Lubos on 24. 4. 2017.
 */
public class ExpNodeCJS extends NodeCJS {
    private String mapping = "";

    public ExpNodeCJS() {}

    public String getMapping() { return mapping; }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public void setExpression(MappingRelation relation) {
        ExpressionType expression = relation.getMapping().getExpression();

        if (expression == null || expression.getExpressionEvaluator().isEmpty()) {
            setName("No expression");
            setMapping("No mapping");
        }
        else {
            if (expression.getExpressionEvaluator().size() > 1) {
                throw new AssertionError("Unsupported very rare expression type for visualization");
            }
            else {
                JAXBElement<?> evalElement = expression.getExpressionEvaluator().get(0);
                Object eval = evalElement.getValue();
                if (QNameUtil.match(evalElement.getName(), SchemaConstants.C_VALUE)) {
                    String str = getStringConstant(eval);
                    setName("constant");
                    setMapping(str);
                    setClasses("ConstantEXP");
                } else if (eval instanceof AsIsExpressionEvaluatorType) {
                    setName("asIs");
                    setClasses("AsIsEXP");
                    setMapping("asIs");
                } else if (eval instanceof ScriptExpressionEvaluatorType) {
                    ScriptExpressionEvaluatorType script = (ScriptExpressionEvaluatorType) eval;
                    if (script.getLanguage() == null) {
                        setName("groovy");
                    } else {
                        setName(StringUtils.substringAfter(script.getLanguage(), "#"));
                    }
                    setMapping(((ScriptExpressionEvaluatorType) eval).getCode());
                    setClasses("ScriptEXP");
                } else if (eval instanceof ItemPathType){
                    setName(evalElement.getName().getLocalPart());
                    setMapping(String.valueOf(((ItemPathType) eval).getItemPath()));
                    setClasses("ItemPathEXP");
                } else if (eval instanceof SearchObjectRefExpressionEvaluatorType){
                    setName("assignmentTargetSearch");
                    setMapping(((SearchObjectRefExpressionEvaluatorType) eval).getDescription());
                    setClasses("AssignmentEXP");
                } else {
                    setName(evalElement.getName().getLocalPart());
                    setMapping("");
                }
            }
        }
    }

    private String getStringConstant(Object eval) {
        if (eval instanceof RawType) {
            XNode xnode = ((RawType) eval).getXnode();
            if (xnode instanceof PrimitiveXNode) {
                eval = ((PrimitiveXNode) xnode).getStringValue();
            } else {
                eval = xnode.toString();
            }
        }
        return String.valueOf(eval);
    }
}
