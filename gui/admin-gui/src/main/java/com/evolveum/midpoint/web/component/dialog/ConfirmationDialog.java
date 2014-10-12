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

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import org.apache.commons.lang.Validate;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * @author lazyman
 */
public class ConfirmationDialog extends Modal {

    private static final String ID_CONFIRM_TEXT = "confirmText";

    private IModel<String> noLabel = new StringResourceModel("confirmationDialog.no", this, null);
    private IModel<String> yesLabel = new StringResourceModel("confirmationDialog.yes", this, null);

    private int confirmType;

    public ConfirmationDialog(String id, IModel<String> title, IModel<String> message) {
        super(id);
        Validate.notNull(message, "Message model must not be null.");

        header(title);
        add(new Label(ID_CONFIRM_TEXT, message));
        addButton(new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Buttons.Type.Default) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                noPerformed(target);
            }
        }.setLabel(noLabel));

        addButton(new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Buttons.Type.Primary) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                yesPerformed(target);
            }
        }.setLabel(yesLabel));
    }

    public ConfirmationDialog setYesLabel(IModel<String> yesLabel) {
        Validate.notNull(yesLabel, "Yes label model must not be null.");
        this.yesLabel = yesLabel;
        return this;
    }

    public ConfirmationDialog setNoLabel(IModel<String> noLabel) {
        Validate.notNull(noLabel, "No label model must not be null.");
        this.noLabel = noLabel;
        return this;
    }

    public void yesPerformed(AjaxRequestTarget target) {

    }

    public void noPerformed(AjaxRequestTarget target) {
        close(target);
    }

    /**
     * @return confirmation type identifier
     */
    public int getConfirmType() {
        return confirmType;
    }

    /**
     * This method provides solution for reusing one confirmation dialog for more messages/actions
     * by using confirmType identifier. See for example {@link com.evolveum.midpoint.web.page.admin.users.component.TreeTablePanel}
     *
     * @param confirmType
     */
    public void setConfirmType(int confirmType) {
        this.confirmType = confirmType;
    }

    @Override
    public Modal show(AjaxRequestTarget target) {
        target.add(this);
        return super.show(target);
    }
}
