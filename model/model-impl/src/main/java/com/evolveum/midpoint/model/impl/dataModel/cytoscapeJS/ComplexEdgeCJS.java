package com.evolveum.midpoint.model.impl.dataModel.cytoscapeJS;

import com.evolveum.midpoint.model.impl.dataModel.model.MappingRelation;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.prism.xnode.XNode;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AsIsExpressionEvaluatorType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ExpressionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ScriptExpressionEvaluatorType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBElement;

/**
 * Created by Lubos on 23. 4. 2017.
 */
public class ComplexEdgeCJS extends EdgeCJS {
    private String label;
    private String strength = "NORMAL";
    private String mapping;
    private String style = "dashed";

    public ComplexEdgeCJS() {

    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String classes) {
        this.style = classes;
    }

    public void setStrengthStyle(MappingRelation relation) {
        if (relation.getMapping().getStrength() != null) {
            setStrength(relation.getMapping().getStrength().toString());
            if (getStrength().equals("STRONG")) {
                setStyle("solid");
            } else if (getStrength().equals("WEAK")) {
                setStyle("dotted");
            } else {
                setStyle("dashed");
            }
        }
    }

    public void setExpression(MappingRelation relation) {
        ExpressionType expression = relation.getMapping().getExpression();
        if (expression == null || expression.getExpressionEvaluator().isEmpty()) {
            setLabel("");
            setMapping("asIs");
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
                    setLabel("constant");
                    setMapping(str);
                } else if (eval instanceof AsIsExpressionEvaluatorType) {
                    setLabel("");
                    setMapping("asIs");
                } else if (eval instanceof ScriptExpressionEvaluatorType) {
                    ScriptExpressionEvaluatorType script = (ScriptExpressionEvaluatorType) eval;
                    if (script.getLanguage() == null) {
                        setLabel("groovy");
                    } else {
                        setLabel(StringUtils.substringAfter(script.getLanguage(), "#"));
                    }
                    setMapping(((ScriptExpressionEvaluatorType) eval).getCode());
                } else if (eval instanceof ItemPathType){
                    setLabel(evalElement.getName().getLocalPart());
                    setMapping(String.valueOf(((ItemPathType) eval).getItemPath()));
                } else {
                    setLabel(evalElement.getName().getLocalPart());
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
