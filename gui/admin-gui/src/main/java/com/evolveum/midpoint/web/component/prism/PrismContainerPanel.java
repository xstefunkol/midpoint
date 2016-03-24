/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.component.prism;

import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.objectdetails.FocusDetailsTabPanel;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author lazyman
 */
public class PrismContainerPanel extends Panel {

	private static final Trace LOGGER = TraceManager.getTrace(PrismContainerPanel.class);
    private static final String ID_SHOW_EMPTY_FIELDS = "showEmptyFields";
    private static final String ID_SORT_PROPERTIES = "sortProperties";
    private static final String STRIPED_CLASS = "striped";

    private boolean showHeader;
    private PageBase pageBase;

    public PrismContainerPanel(String id, IModel<ContainerWrapper> model, Form form) {
        this(id, model, true, form, null);
    }

    public PrismContainerPanel(String id, final IModel<ContainerWrapper> model, boolean showHeader, Form form, PageBase pageBase) {
        super(id);
        this.showHeader = showHeader;
        this.pageBase = pageBase;

        LOGGER.trace("Creating container panel for {}", model.getObject());
        
        add(new AttributeAppender("class", new Model<>("attributeComponent"), " "));
        add(new VisibleEnableBehaviour() {

            @Override
            public boolean isVisible() {
                ContainerWrapper<? extends PrismContainer> containerWrapper = model.getObject();
                PrismContainer prismContainer = containerWrapper.getItem();
                if (prismContainer.getDefinition().isOperational()) {
                    return false;
                }

                // HACK HACK HACK
                if (ShadowType.F_ASSOCIATION.equals(prismContainer.getElementName())) {
                	return true;
                }

                boolean isVisible = false;
                for (ItemWrapper item : containerWrapper.getItems()) {
                    if (containerWrapper.isItemVisible(item)) {
                        isVisible = true;
                        break;
                    }
                }

                return !containerWrapper.getItems().isEmpty() && isVisible;
            }
        });

        initLayout(model, form);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        StringBuilder sb = new StringBuilder();
        sb.append("fixStripingOnPrismForm('").append(getMarkupId()).append("', '").append(STRIPED_CLASS).append("');");
        response.render(OnDomReadyHeaderItem.forScript(sb.toString()));
    }

    private void initLayout(final IModel<ContainerWrapper> model, final Form form) {
        WebMarkupContainer header = new WebMarkupContainer("header");
        header.add(new VisibleEnableBehaviour() {

            @Override
            public boolean isVisible() {
                //
                return true;
            }
        });


        AjaxLink showEmptyFieldsButton = new AjaxLink(ID_SHOW_EMPTY_FIELDS) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                ContainerWrapper containerWrapper = model.getObject();
                ObjectWrapper objectWrapper = containerWrapper.getObject();
                objectWrapper.setShowEmpty(!objectWrapper.isShowEmpty());
                target.add(PrismContainerPanel.this.findParent(PrismObjectPanel.class));
            }
        };
        header.add(showEmptyFieldsButton);

        AjaxLink sortProperties = new AjaxLink(ID_SORT_PROPERTIES) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                ContainerWrapper containerWrapper = model.getObject();
                ObjectWrapper objectWrapper = containerWrapper.getObject();
                objectWrapper.setSorted(!objectWrapper.isSorted());

                PropertyModel propertiesModel = new PropertyModel(model, "properties");
                List<PropertyOrReferenceWrapper> propertiesList = (List<PropertyOrReferenceWrapper>)propertiesModel.getObject();

                if (objectWrapper.isSorted()){
                    Collections.sort(propertiesList, new Comparator<PropertyOrReferenceWrapper>(){
                        @Override
                        public int compare(PropertyOrReferenceWrapper pw1, PropertyOrReferenceWrapper pw2) {
                            ItemDefinition id1 = pw1.getItemDefinition();
                            ItemDefinition id2 = pw2.getItemDefinition();
                            String str1 =(id1 != null ? (id1.getDisplayName() != null ? id1.getDisplayName() :
                                    (id1.getName() != null && id1.getName().getLocalPart() != null ? id1.getName().getLocalPart() : "")) : "");
                            String str2 =(id2 != null ? (id2.getDisplayName() != null ? id2.getDisplayName() :
                                    (id2.getName() != null && id2.getName().getLocalPart() != null ? id2.getName().getLocalPart() : "")) : "");
                            return str1.compareToIgnoreCase(str2);
                        }
                    });
                }
                else {
                    final int[] maxOrderArray = new int[3];
                    Collections.sort(propertiesList, new Comparator<PropertyOrReferenceWrapper>(){
                        @Override
                        public int compare(PropertyOrReferenceWrapper pw1, PropertyOrReferenceWrapper pw2) {
                            ItemDefinition id1 = pw1.getItemDefinition();
                            ItemDefinition id2 = pw2.getItemDefinition();

                            //we need to find out the value of the biggest displayOrder to put
                            //properties with null display order to the end of the list
                            int displayOrder1 = (id1 != null && id1.getDisplayOrder() != null) ? id1.getDisplayOrder() : 0;
                            int displayOrder2 = (id2 != null && id2.getDisplayOrder() != null) ? id2.getDisplayOrder() : 0;
                            if (maxOrderArray[0] == 0){
                                maxOrderArray[0] = displayOrder1 > displayOrder2 ? displayOrder1 + 1 : displayOrder2 + 1;
                            }
                            maxOrderArray[1] = displayOrder1;
                            maxOrderArray[2] = displayOrder2;

                            int maxDisplayOrder = NumberUtils.max(maxOrderArray);
                            maxOrderArray[0] = maxDisplayOrder + 1;

                            return Integer.compare(id1 != null  && id1.getDisplayOrder() != null ? id1.getDisplayOrder() : maxDisplayOrder,
                                    id2 != null && id2.getDisplayOrder() != null ? id2.getDisplayOrder() : maxDisplayOrder);
                        }
                    });
                }
                addOrReplaceProperties(model, form, true);
                target.add(PrismContainerPanel.this);
            }
        };
        header.add(sortProperties);
        add(header);

        IModel headerLabelModel;

        if (model.getObject().isMain()){
//            headerLabelModel = new StringResourceModel(resourceKey, this);
            ContainerWrapper wrappper = model.getObject();
            ObjectWrapper objwrapper = wrappper.getObject();
            final String key = objwrapper.getDisplayName();

            headerLabelModel = new IModel<String>() {
                @Override
                public String getObject() {
                    String displayName = PageBase.createStringResourceStatic(getPage(), key).getString();
                    if (displayName.equals(key)){
                        displayName = (new PropertyModel<String>(model, "displayName")).getObject();
                    }
                    return displayName;
                }

                @Override
                public void setObject(String o) {

                }

                @Override
                public void detach() {

                }
            };
        } else {
            headerLabelModel = new PropertyModel<>(model, "displayName");
        }
        header.add(new Label("label", headerLabelModel));

        addOrReplaceProperties(model, form, false);
    }

    public PageBase getPageBase(){
        return pageBase;
    }

    private IModel<String> createStyleClassModel(final IModel<ItemWrapper> wrapper) {
        return new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
            	ItemWrapper property = wrapper.getObject();
                return property.isVisible() ? "visible" : null;
            }
        };
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }

    private void addOrReplaceProperties(IModel<ContainerWrapper> model, final Form form, boolean isToBeReplaced){
        ListView<ItemWrapper> properties = new ListView<ItemWrapper>("properties",
                new PropertyModel(model, "properties")) {

            @Override
            protected void populateItem(ListItem<ItemWrapper> item) {
//            	if (item.getModel().getObject() instanceof PropertyWrapper){
                item.add(new PrismPropertyPanel("property", item.getModel(), form, pageBase));
                item.add(AttributeModifier.append("class", createStyleClassModel(item.getModel())));
//            	} else if (item.getModel().getObject() instanceof ReferenceWrapper){
//            		 item.add(new PrismReferencePanel("property", item.getModel(), form, pageBase));
// 	                item.add(AttributeModifier.append("class", createStyleClassModel(item.getModel())));
//            	}
            }
        };
        properties.setReuseItems(true);
        if (isToBeReplaced) {
            replace(properties);
        } else {
            add(properties);
        }
    }
}
