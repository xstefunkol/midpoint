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
package com.evolveum.midpoint.web.component.dialog;

import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.EqualFilter;
import com.evolveum.midpoint.prism.query.NotFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.util.LoadableModel;
import com.evolveum.midpoint.web.page.PageBase;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OrgType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowKindType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author shood
 */
public class DeleteAllDialog extends Modal<DeleteAllDto> {

    private static final Trace LOGGER = TraceManager.getTrace(DeleteAllDialog.class);

    private static final String DOT_CLASS = DeleteAllDialog.class.getName() + ".";
    private static final String OPERATION_SEARCH_ITERATIVE_TASK = DOT_CLASS + "searchIterativeTask";
    private static final String OPERATION_COUNT_TASK = DOT_CLASS + "countObjectsTask";

    private static final String ID_CHB_USERS = "checkboxUsers";
    private static final String ID_CHB_ORG = "checkboxOrg";
    private static final String ID_CHB_ACCOUNT_SHADOW = "checkboxAccountShadow";
    private static final String ID_CHB_NON_ACCOUNT_SHADOW = "checkboxNonAccountShadow";
    private static final String ID_TEXT_USERS = "confirmTextUsers";
    private static final String ID_TEXT_ORGS = "confirmTextOrgUnits";
    private static final String ID_TEXT_ACC_SHADOWS = "confirmTextAccountShadow";
    private static final String ID_TEXT_NON_ACC_SHADOW = "confirmTextNonAccountShadows";
    private static final String ID_TOTAL = "totalCountLabel";

    public DeleteAllDialog(String id, IModel<String> title) {
        super(id);

        if (title != null) {
            header(title);
        } else {
            setHeaderVisible(false);
        }

        setModel(new Model<>(new DeleteAllDto()));

        initLayout();
    }

    private void updateLabelModel(AjaxRequestTarget target, String labelID) {
        LoadableModel<String> model = (LoadableModel<String>) get(labelID).getDefaultModel();
        model.reset();

        model = (LoadableModel<String>) get(ID_TOTAL).getDefaultModel();
        model.reset();

        target.add(get(labelID), get(ID_TOTAL));
    }

    private void initLayout() {
        CheckBox deleteUsersCheckbox = new CheckBox(ID_CHB_USERS,
                new PropertyModel<Boolean>(getModel(), DeleteAllDto.F_USERS));
        deleteUsersCheckbox.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateLabelModel(target, ID_TEXT_USERS);
            }
        });
        add(deleteUsersCheckbox);

        CheckBox deleteOrgsCheckbox = new CheckBox(ID_CHB_ORG,
                new PropertyModel<Boolean>(getModel(), DeleteAllDto.F_ORGS));
        deleteOrgsCheckbox.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateLabelModel(target, ID_TEXT_ORGS);
            }
        });
        add(deleteOrgsCheckbox);

        CheckBox deleteAccountShadowsCheckbox = new CheckBox(ID_CHB_ACCOUNT_SHADOW,
                new PropertyModel<Boolean>(getModel(), DeleteAllDto.F_ACC_SHADOW));
        deleteAccountShadowsCheckbox.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateLabelModel(target, ID_TEXT_ACC_SHADOWS);
            }
        });
        add(deleteAccountShadowsCheckbox);

        CheckBox deleteNonAccountShadowsCheckbox = new CheckBox(ID_CHB_NON_ACCOUNT_SHADOW,
                new PropertyModel<Boolean>(getModel(), DeleteAllDto.F_NON_ACC_SHADOW));
        deleteNonAccountShadowsCheckbox.add(new OnChangeAjaxBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateLabelModel(target, ID_TEXT_NON_ACC_SHADOW);
            }
        });
        add(deleteNonAccountShadowsCheckbox);

        Label usersLabel = new Label(ID_TEXT_USERS, new LoadableModel<String>() {
            @Override
            protected String load() {
                return createDeleteUsersMessage();
            }
        });
        usersLabel.setOutputMarkupId(true);
        add(usersLabel);

        Label orgsLabel = new Label(ID_TEXT_ORGS, new LoadableModel<String>() {
            @Override
            protected String load() {
                return createDeleteOrgUnitsMessage();
            }
        });
        orgsLabel.setOutputMarkupId(true);
        add(orgsLabel);

        Label accShadowsLabel = new Label(ID_TEXT_ACC_SHADOWS, new LoadableModel<String>() {
            @Override
            protected String load() {
                return createDeleteAccountShadowsMessage();
            }
        });
        accShadowsLabel.setOutputMarkupId(true);
        add(accShadowsLabel);

        Label nonAccShadowsLabel = new Label(ID_TEXT_NON_ACC_SHADOW, new LoadableModel<String>() {

            @Override
            protected String load() {
                return createDeleteNonAccountShadowsMessage();
            }
        });
        nonAccShadowsLabel.setOutputMarkupId(true);
        add(nonAccShadowsLabel);

        Label countLabel = new Label(ID_TOTAL, new LoadableModel<String>() {
            @Override
            protected String load() {
                return createTotalMessage();
            }
        });
        countLabel.setOutputMarkupId(true);
        add(countLabel);

        addButton(new BootstrapAjaxLink(BUTTON_MARKUP_ID, Buttons.Type.Default) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                noPerformed(target);
            }
        }.setLabel(new StringResourceModel("deleteAllDialog.no", this, null)));

        addButton(new BootstrapAjaxLink(BUTTON_MARKUP_ID, Buttons.Type.Primary) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                yesPerformed(target);
            }
        }.setLabel(new StringResourceModel("deleteAllDialog.yes", this, null)));
    }

    public StringResourceModel createStringResource(String resourceKey, Object... objects) {
        return new StringResourceModel(resourceKey, this, new Model<String>(), resourceKey, objects);
    }

    private String createTotalMessage() {
        DeleteAllDto dto = getModel().getObject();
        dto.setObjectsToDelete(0);

        if (dto.getDeleteUsers()) {
            dto.setObjectsToDelete(dto.getObjectsToDelete() + dto.getUserCount());
        }
        if (dto.getDeleteOrgs()) {
            dto.setObjectsToDelete(dto.getObjectsToDelete() + dto.getOrgUnitCount());
        }
        if (dto.getDeleteAccountShadow()) {
            dto.setObjectsToDelete(dto.getObjectsToDelete() + dto.getAccountShadowCount());
        }
        if (dto.getDeleteNonAccountShadow()) {
            dto.setObjectsToDelete(dto.getObjectsToDelete() + dto.getNonAccountShadowCount());
        }

        return createStringResource("deleteAllDialog.label.totalToDelete", dto.getObjectsToDelete()).getString();
    }

    private String createDeleteUsersMessage() {
        if (!getModel().getObject().getDeleteUsers()) {
            return createStringResource("deleteAllDialog.label.usersDelete", 0).getString();
        }
        DeleteAllDto dto = getModel().getObject();
        Task task = getPagebase().createSimpleTask(OPERATION_COUNT_TASK);
        OperationResult result = new OperationResult(OPERATION_COUNT_TASK);

        Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
        GetOperationOptions opt = GetOperationOptions.createRaw();
        options.add(SelectorOptions.create(ItemPath.EMPTY_PATH, opt));

        try {
            dto.setUserCount(getPagebase().getModelService().countObjects(UserType.class, null, options, task, result));

            //We need to substract 1, because we are not deleting user 'Administrator'
            dto.setUserCount(dto.getUserCount() - 1);
            dto.setObjectsToDelete(dto.getObjectsToDelete() + dto.getUserCount());
        } catch (Exception ex) {
            result.computeStatus(getString("deleteAllDialog.message.countSearchProblem"));
            LoggingUtils.logException(LOGGER, getString("deleteAllDialog.message.countSearchProblem"), ex);
        }

        return createStringResource("deleteAllDialog.label.usersDelete", dto.getUserCount()).getString();
    }

    private String createDeleteOrgUnitsMessage() {
        if (!getModel().getObject().getDeleteOrgs()) {
            return createStringResource("deleteAllDialog.label.orgUnitsDelete", 0).getString();
        }

        DeleteAllDto dto = getModel().getObject();
        Task task = getPagebase().createSimpleTask(OPERATION_COUNT_TASK);
        OperationResult result = new OperationResult(OPERATION_COUNT_TASK);

        Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
        GetOperationOptions opt = GetOperationOptions.createRaw();
        options.add(SelectorOptions.create(ItemPath.EMPTY_PATH, opt));

        try {
            dto.setOrgUnitCount(getPagebase().getModelService().countObjects(OrgType.class, null, options, task, result));

            dto.setObjectsToDelete(dto.getObjectsToDelete() + dto.getOrgUnitCount());
        } catch (Exception ex) {
            result.computeStatus(getString("deleteAllDialog.message.countSearchProblem"));
            LoggingUtils.logException(LOGGER, getString("deleteAllDialog.message.countSearchProblem"), ex);
        }

        return createStringResource("deleteAllDialog.label.orgUnitsDelete", dto.getOrgUnitCount()).getString();
    }

    private void countShadows(boolean isAccountShadow) {
        DeleteAllDto dto = getModel().getObject();
        Task task = getPagebase().createSimpleTask(OPERATION_SEARCH_ITERATIVE_TASK);
        OperationResult result = new OperationResult(OPERATION_SEARCH_ITERATIVE_TASK);

        Collection<SelectorOptions<GetOperationOptions>> options = new ArrayList<>();
        GetOperationOptions opt = GetOperationOptions.createRaw();
        options.add(SelectorOptions.create(ItemPath.EMPTY_PATH, opt));

        try {
            ObjectFilter filter = EqualFilter.createEqual(ShadowType.F_KIND, ShadowType.class, getPagebase().getPrismContext(), null, ShadowKindType.ACCOUNT);
            if (isAccountShadow) {
                ObjectQuery query = ObjectQuery.createObjectQuery(filter);
                dto.setAccountShadowCount(getPagebase().getModelService().countObjects(ShadowType.class, query, options, task, result));
                dto.setObjectsToDelete(dto.getObjectsToDelete() + dto.getAccountShadowCount());
            } else {
                ObjectQuery query = ObjectQuery.createObjectQuery(NotFilter.createNot(filter));
                dto.setNonAccountShadowCount(getPagebase().getModelService().countObjects(ShadowType.class, query, options, task, result));
                dto.setObjectsToDelete(dto.getObjectsToDelete() + dto.getNonAccountShadowCount());
            }

        } catch (Exception ex) {
            result.computeStatus(getString("deleteAllDialog.message.countSearchProblem"));
            LoggingUtils.logException(LOGGER, getString("deleteAllDialog.message.countSearchProblem"), ex);
        }
    }

    private String createDeleteNonAccountShadowsMessage() {
        if (!getModel().getObject().getDeleteNonAccountShadow()) {
            return createStringResource("deleteAllDialog.label.nonAccountShadowsDelete", 0).getString();
        }
        DeleteAllDto dto = getModel().getObject();

        countShadows(false);

        return createStringResource("deleteAllDialog.label.nonAccountShadowsDelete", dto.getNonAccountShadowCount()).getString();
    }

    private String createDeleteAccountShadowsMessage() {
        if (!getModel().getObject().getDeleteAccountShadow()) {
            return createStringResource("deleteAllDialog.label.accountShadowsDelete", 0).getString();
        }

        DeleteAllDto dto = getModel().getObject();
        countShadows(true);

        return createStringResource("deleteAllDialog.label.accountShadowsDelete", dto.getAccountShadowCount()).getString();
    }

    public int getObjectsToDelete() {
        return getModel().getObject().getObjectsToDelete();
    }

    private PageBase getPagebase() {
        return (PageBase) getPage();
    }

    public void yesPerformed(AjaxRequestTarget target) {

    }

    public void noPerformed(AjaxRequestTarget target) {
        close(target);
    }
}
