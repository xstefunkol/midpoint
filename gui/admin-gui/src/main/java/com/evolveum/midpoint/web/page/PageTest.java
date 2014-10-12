/*
 * Copyright (c) 2010-2014 Evolveum
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

package com.evolveum.midpoint.web.page;

import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.web.application.AuthorizationAction;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.component.AjaxSubmitButton;
import com.evolveum.midpoint.web.page.admin.home.PageAdminHome;
import com.evolveum.midpoint.web.page.admin.users.PageUsers;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Bytes;

/**
 * @author lazyman
 */
@PageDescriptor(url = "/admin/test", action = {@AuthorizationAction(actionUri = AuthorizationConstants.AUTZ_UI_PERMIT_ALL)})
public class PageTest extends PageBase {

    public PageTest() {
        initLayout();
    }

    private void initLayout() {
        final Modal modal = new Modal("modal")
                .header(new Model("Delete user"))
                .addButton(new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Buttons.Type.Default) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        System.out.println("cancel clicked");
                    }
                }.setLabel(new Model("Cancel")))
                .addButton(new BootstrapAjaxLink(Modal.BUTTON_MARKUP_ID, Buttons.Type.Primary) {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        System.out.println("delete clicked");
                    }
                }.setLabel(new Model("Delete")))
                .addCloseButton(new Model("Close me"));
        add(modal);

        BootstrapAjaxLink button = new BootstrapAjaxLink("button", Buttons.Type.Primary) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                modal.show(target);
            }
        };
        button.setLabel(new Model("show"));
        add(button);
    }
}
