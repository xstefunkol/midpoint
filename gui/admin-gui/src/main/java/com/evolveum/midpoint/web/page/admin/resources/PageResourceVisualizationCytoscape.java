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

package com.evolveum.midpoint.web.page.admin.resources;

import com.evolveum.midpoint.model.api.DataModelVisualizer;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskManager;
import com.evolveum.midpoint.util.exception.CommonException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.application.AuthorizationAction;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.page.admin.PageAdmin;
import com.evolveum.midpoint.web.security.MidPointApplication;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceRequestHandler;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mederly
 */
@PageDescriptor(url = "/admin/resources/visualizationCytoscape", action = {
		@AuthorizationAction(actionUri = PageAdminResources.AUTH_RESOURCE_ALL,
				label = PageAdminResources.AUTH_RESOURCE_ALL_LABEL,
				description = PageAdminResources.AUTH_RESOURCE_ALL_DESCRIPTION),
		@AuthorizationAction(actionUri = AuthorizationConstants.AUTZ_UI_RESOURCE_EDIT_URL,
				label = "PageResourceWizard.auth.resource.label",
				description = "PageResourceWizard.auth.resource.description")})
//public class PageResourceVisualizationCytoscape extends PageAdmin {
public class PageResourceVisualizationCytoscape extends WebPage {

	private static final Trace LOGGER = TraceManager.getTrace(PageResourceVisualizationCytoscape.class);

	public PageResourceVisualizationCytoscape() {
	}

	public PageResourceVisualizationCytoscape(@NotNull PrismObject<ResourceType> resourceObject) {
		initLayout(resourceObject);
	}

	private AbstractAjaxBehavior behave;

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		Map<String, Object> map = new HashMap<>();
		String callbackUrl = behave.getCallbackUrl().toString();
		map.put("callbackUrl", callbackUrl);

		PackageTextTemplate ptt = new PackageTextTemplate( PageResourceVisualizationCytoscape.class, "get-and-process-cytoscape-data.js" );
		String javaScript = ptt.asString(map);
		System.out.println("Javascript = " + javaScript);
		response.render(JavaScriptHeaderItem.forScript(javaScript, null));
	}

	private void initLayout(PrismObject<ResourceType> resourceObject) {
		behave = new AbstractAjaxBehavior() {
			@Override
			public void onRequest() {
				System.out.println("onRequest starting");
				RequestCycle requestCycle = getRequestCycle();
				requestCycle.scheduleRequestHandlerAfterCurrent(null);

				MidPointApplication app = (MidPointApplication) MidPointApplication.get();
				TaskManager taskManager = app.getTaskManager();
				Task task = taskManager.createTaskInstance(PageResourceVisualizationCytoscape.class + ".onRequest");

				String jsonData;
				try {
					jsonData = app.getModelDiagnosticService()
							.exportDataModel(resourceObject.asObjectable(), DataModelVisualizer.Target.CYTOSCAPE, task,
									task.getResult());
					System.out.println("JSON Cytoscape Data:\n" + jsonData);
				} catch (CommonException|RuntimeException e) {
					LoggingUtils.logUnexpectedException(LOGGER, "Couldn't visualize resource {}", e, resourceObject);
					jsonData = "{\"nodes\":[], \"edges\":[]}";		// TODO better error handling
				}

				IResource jsonResource = new ByteArrayResource("text/plain", jsonData.getBytes());
				IRequestHandler requestHandler = new ResourceRequestHandler(jsonResource, null);
				requestHandler.respond(requestCycle);
				System.out.println("Response written.");
			}
		};
		add(behave);
	}
}
