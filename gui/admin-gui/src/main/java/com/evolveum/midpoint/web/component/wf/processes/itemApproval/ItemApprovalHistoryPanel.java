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

package com.evolveum.midpoint.web.component.wf.processes.itemApproval;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.wf.DecisionsPanel;
import com.evolveum.midpoint.web.page.admin.workflow.dto.DecisionDto;
import com.evolveum.midpoint.xml.ns._public.common.common_3.DecisionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ItemApprovalProcessStateType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.WfContextType;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mederly
 */
public class ItemApprovalHistoryPanel extends BasePanel<WfContextType> {

    private static final Trace LOGGER = TraceManager.getTrace(ItemApprovalHistoryPanel.class);

    private static final String ID_DECISIONS_DONE = "decisionsDone";

    public ItemApprovalHistoryPanel(String id, IModel<WfContextType> model) {
        super(id, model);
        initLayout();
    }

    private void initLayout() {

        add(new DecisionsPanel(ID_DECISIONS_DONE, new AbstractReadOnlyModel<List<DecisionDto>>() {
            @Override
            public List<DecisionDto> getObject() {
                List<DecisionDto> rv = new ArrayList<>();
                WfContextType wfContextType = getModelObject();
				if (wfContextType == null) {
					return rv;
				}
                ItemApprovalProcessStateType instanceState = (ItemApprovalProcessStateType) wfContextType.getProcessSpecificState();
                List<DecisionType> allDecisions = instanceState.getDecisions();
                if (allDecisions == null) {
					return rv;
				}
				for (DecisionType decision : allDecisions) {
					rv.add(new DecisionDto(decision));
				}
                return rv;
            }
        }));

    }
}
