/*
 * Copyright (c) 2010-2013 Evolveum
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

package com.evolveum.midpoint.web.page.admin.users.component;

import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.web.component.data.ObjectDataProvider;
import com.evolveum.midpoint.web.component.data.TablePanel;
import com.evolveum.midpoint.web.component.data.column.CheckBoxHeaderColumn;
import com.evolveum.midpoint.web.component.data.column.LinkColumn;
import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shood
 */
public class OrgUnitAddDeletePopup extends Modal {

    public enum ActionState {
        DELETE,
        ADD
    }

    private static final String ID_TABLE = "table";

    private static final String DEFAULT_SORTABLE_PROPERTY = null;

    private boolean initialized;

    private ActionState state = ActionState.ADD;

    public OrgUnitAddDeletePopup(String id) {
        super(id);
        setOutputMarkupId(true);
        header(new StringResourceModel("orgUnitAddDeletePopup.title", this, null));
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        if (initialized) {
            return;
        }

        initLayout();
        initialized = true;
    }

    public void initLayout() {
        List<IColumn<SelectableBean<ObjectType>, String>> columns = initColumns();

        ObjectDataProvider provider = new ObjectDataProvider(getPageBase(), OrgType.class);
        provider.setQuery(getDataProviderQuery());

        TablePanel table = new TablePanel<>(ID_TABLE, provider, columns);
        table.setOutputMarkupId(true);
        add(table);

        addButton(new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Buttons.Type.Default) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                cancelPerformed(target);
            }
        }.setLabel(new StringResourceModel("orgUnitAddDeletePopup.button.cancel", this, null)));

        BootstrapAjaxLink link = new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Buttons.Type.Primary) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                addPerformed(target, null);
            }
        }.setLabel(createStringResource("orgUnitAddDeletePopup.button.add"));
        link.add(new VisibleEnableBehaviour() {

            @Override
            public boolean isVisible() {
                return state == ActionState.ADD;
            }
        });
        addButton(link);

        link = new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Buttons.Type.Danger) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                removePerformed(target, null);
            }
        }.setLabel(createStringResource("orgUnitAddDeletePopup.button.remove"));
        link.add(new VisibleEnableBehaviour() {

            @Override
            public boolean isVisible() {
                return state == ActionState.DELETE;
            }
        });
        addButton(link);
    }

    private List<IColumn<SelectableBean<ObjectType>, String>> initColumns() {
        List<IColumn<SelectableBean<ObjectType>, String>> columns = new ArrayList<IColumn<SelectableBean<ObjectType>, String>>();

        IColumn column = new CheckBoxHeaderColumn<OrgType>();
        columns.add(column);

        column = new LinkColumn<SelectableBean<OrgType>>(createStringResource("orgUnitAddDeletePopup.column.name"),
                getSortableProperty(), "value.name") {

            @Override
            public void onClick(AjaxRequestTarget target, IModel<SelectableBean<OrgType>> rowModel) {
                OrgType org = rowModel.getObject().getValue();
                chooseOperationPerformed(target, org);
            }

        };
        columns.add(column);

        return columns;
    }

    public ActionState getState() {
        return state;
    }

    public void setState(ActionState state, AjaxRequestTarget target) {
        this.state = state;

        TablePanel panel = getTable();
        DataTable table = panel.getDataTable();
        ObjectDataProvider provider = (ObjectDataProvider) table.getDataProvider();
        provider.setQuery(getDataProviderQuery());

        target.add(this);
    }

    private TablePanel getTable() {
        return (TablePanel) get(ID_TABLE);
    }

    private ObjectQuery getDataProviderQuery() {
        if (state == ActionState.ADD) {
            return getAddProviderQuery();
        } else {
            return getRemoveProviderQuery();
        }
    }

    public ObjectQuery getAddProviderQuery() {
        return null;
    }

    public ObjectQuery getRemoveProviderQuery() {
        return null;
    }

    public StringResourceModel createStringResource(String resourceKey, Object... objects) {
        return new StringResourceModel(resourceKey, this, null, resourceKey, objects);
    }

    public String getSortableProperty() {
        return DEFAULT_SORTABLE_PROPERTY;
    }

    private PageBase getPageBase() {
        return (PageBase) getPage();
    }

    private void cancelPerformed(AjaxRequestTarget target) {
        close(target);
    }

    private void chooseOperationPerformed(AjaxRequestTarget target, OrgType org) {
        if (state == ActionState.ADD) {
            addPerformed(target, org);
        } else {
            removePerformed(target, org);
        }
    }

    public void addPerformed(AjaxRequestTarget target, OrgType selected) {
    }

    public void removePerformed(AjaxRequestTarget target, OrgType selected) {
    }
}
