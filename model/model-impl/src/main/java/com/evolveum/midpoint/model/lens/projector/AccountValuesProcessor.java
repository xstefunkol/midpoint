/**
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.model.lens.projector;

import com.evolveum.midpoint.common.QueryUtil;
import com.evolveum.midpoint.common.refinery.RefinedAccountDefinition;
import com.evolveum.midpoint.common.refinery.RefinedAttributeDefinition;
import com.evolveum.midpoint.model.api.PolicyViolationException;
import com.evolveum.midpoint.model.api.context.SynchronizationPolicyDecision;
import com.evolveum.midpoint.model.lens.LensContext;
import com.evolveum.midpoint.model.lens.LensFocusContext;
import com.evolveum.midpoint.model.lens.LensProjectionContext;
import com.evolveum.midpoint.model.lens.LensUtil;
import com.evolveum.midpoint.model.lens.ShadowConstraintsChecker;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismProperty;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.repo.api.RepositoryService;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.processor.ResourceAttribute;
import com.evolveum.midpoint.schema.processor.ResourceAttributeContainer;
import com.evolveum.midpoint.schema.processor.ResourceAttributeDefinition;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ResourceObjectShadowUtil;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.AccountShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.IterationSpecificationType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ResourceAccountTypeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ResourceObjectShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ResourceType;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.UserType;
import com.evolveum.prism.xml.ns._public.query_2.QueryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.evolveum.midpoint.common.CompiletimeConfig.CONSISTENCY_CHECKS;

/**
 * Processor that determines values of account attributes. It does so by taking the pre-processed information left
 * behind by the assignment processor. It also does some checks, such as check of identifier uniqueness. It tries to
 * do several iterations over the value computations if a conflict is found (and this feature is enabled).
 * 
 * @author Radovan Semancik
 */
@Component
public class AccountValuesProcessor {
	
	private static final Trace LOGGER = TraceManager.getTrace(AccountValuesProcessor.class);
	
	@Autowired(required = true)
    private OutboundProcessor outboundProcessor;
	
	@Autowired(required = true)
    private ConsolidationProcessor consolidationProcessor;
	
	@Autowired(required = true)
    private AssignmentProcessor assignmentProcessor;
	
	@Autowired(required = true)
	@Qualifier("cacheRepositoryService")
	RepositoryService repositoryService;
	
	@Autowired(required = true)
	private PrismContext prismContext;

	
	public <F extends ObjectType, P extends ObjectType> void process(LensContext<F,P> context, 
			LensProjectionContext<P> projectionContext, String activityDescription, OperationResult result) 
			throws SchemaException, ExpressionEvaluationException, ObjectNotFoundException, ObjectAlreadyExistsException, 
			CommunicationException, ConfigurationException, SecurityViolationException, PolicyViolationException {
		LensFocusContext<F> focusContext = context.getFocusContext();
    	if (focusContext == null) {
    		return;
    	}
    	if (focusContext.getObjectTypeClass() != UserType.class) {
    		// We can do this only for user.
    		return;
    	}
    	processAccounts((LensContext<UserType,AccountShadowType>) context, (LensProjectionContext<AccountShadowType>)projectionContext, 
    			activityDescription, result);
	}
	
	public void processAccounts(LensContext<UserType,AccountShadowType> context, 
			LensProjectionContext<AccountShadowType> accountContext, String activityDescription, OperationResult result) 
			throws SchemaException, ExpressionEvaluationException, ObjectNotFoundException, ObjectAlreadyExistsException,
			CommunicationException, ConfigurationException, SecurityViolationException, PolicyViolationException {
		
		checkSchemaAndPolicies(context, accountContext, activityDescription, result);
		
		SynchronizationPolicyDecision policyDecision = accountContext.getSynchronizationPolicyDecision();
		if (policyDecision != null && policyDecision == SynchronizationPolicyDecision.UNLINK) {
			// We will not update accounts that are being unlinked.
			// we cannot skip deleted accounts here as the delete delta will be skipped as well
			return;
		}
		
		if (CONSISTENCY_CHECKS) context.checkConsistence();
		
		int maxIterations = determineMaxIterations(accountContext);
		int iteration = 0;
		while (true) {
			
			accountContext.setIteration(iteration);
			String iterationToken = formatIterationToken(iteration);
			accountContext.setIterationToken(iterationToken);			
			if (CONSISTENCY_CHECKS) context.checkConsistence();
			
			// Re-evaluates the values in the account constructions (including roles)
			assignmentProcessor.processAssignmentsAccountValues(accountContext, result);
			
			context.recompute();
			if (CONSISTENCY_CHECKS) context.checkConsistence();
			
			// Evaluates the values in outbound mappings
			outboundProcessor.processOutbound(context, accountContext, result);
			
			context.recompute();
			if (CONSISTENCY_CHECKS) context.checkConsistence();
			
			// Merges the values together, processing exclusions and strong/weak mappings are needed
			consolidationProcessor.consolidateValues(context, accountContext, result);
			
			if (CONSISTENCY_CHECKS) context.checkConsistence();
	        context.recompute();
	        if (CONSISTENCY_CHECKS) context.checkConsistence();
	
	        // Too noisy for now
//	        LensUtil.traceContext(LOGGER, activityDescription, "values", context, true);
	        
	        if (policyDecision != null && policyDecision == SynchronizationPolicyDecision.DELETE) {
	        	// No need to play the iterative game if the account is deleted
	        	break;
	        }
	        
	        // Check constraints
	        ShadowConstraintsChecker checker = new ShadowConstraintsChecker(accountContext);
	        checker.setPrismContext(prismContext);
	        checker.setContext(context);
	        checker.setRepositoryService(repositoryService);
	        checker.check(result);
	        if (checker.isSatisfiesConstraints()) {
	        	break;
	        }
	        
	        iteration++;
	        if (iteration > maxIterations) {
	        	StringBuilder sb = new StringBuilder();
	        	if (iteration == 1) {
	        		sb.append("Error processing ");
	        	} else {
	        		sb.append("Too many iterations ("+iteration+") for ");
	        	}
	        	sb.append(accountContext.getHumanReadableName());
	        	if (iteration == 1) {
	        		sb.append(": constraint violation: ");
	        	} else {
	        		sb.append(": cannot determine values that satisfy constraints: ");
	        	}
	        	sb.append(checker.getMessages());
	        	throw new ObjectAlreadyExistsException(sb.toString());
	        }
	        
	        cleanupContext(accountContext);
	        if (CONSISTENCY_CHECKS) context.checkConsistence();
		} 
		
		if (CONSISTENCY_CHECKS) context.checkConsistence();
					
	}
	
	private int determineMaxIterations(LensProjectionContext<AccountShadowType> accountContext) {
		ResourceAccountTypeDefinitionType accDef = accountContext.getResourceAccountTypeDefinitionType();
		if (accDef != null) {
			IterationSpecificationType iteration = accDef.getIteration();
			if (iteration != null) {
				return iteration.getMaxIterations();
			}
		}
		return 0;
	}

	private String formatIterationToken(int iteration) {
		// TODO: flexible token format (MID-1102)
		if (iteration == 0) {
			return "";
		}
		return Integer.toString(iteration);
	}


	private boolean isInDelta(PrismProperty<?> attr, ObjectDelta<AccountShadowType> delta) {
		if (delta == null) {
			return false;
		}
		return delta.hasItemDelta(new ItemPath(ResourceObjectShadowType.F_ATTRIBUTES, attr.getName()));
	}

	/**
	 * Check that the primary deltas do not violate schema and policies
	 * TODO: implement schema check 
	 */
	public void checkSchemaAndPolicies(LensContext<UserType,AccountShadowType> context, 
			LensProjectionContext<AccountShadowType> accountContext, String activityDescription, OperationResult result) throws SchemaException, PolicyViolationException {
		ObjectDelta<AccountShadowType> primaryDelta = accountContext.getPrimaryDelta();
		if (primaryDelta == null || primaryDelta.isDelete()) {
			return;
		}
		
		RefinedAccountDefinition rAccountDef = accountContext.getRefinedAccountDefinition();
		if (rAccountDef == null) {
			throw new SchemaException("No definition for account type '"
					+accountContext.getResourceShadowDiscriminator().getIntent()+"' in "+accountContext.getResource());
		}
		
		if (primaryDelta.isAdd()) {
			PrismObject<AccountShadowType> accountToAdd = primaryDelta.getObjectToAdd();
			ResourceAttributeContainer attributesContainer = ResourceObjectShadowUtil.getAttributesContainer(accountToAdd);
			if (attributesContainer != null) {
				for (ResourceAttribute<?> attribute: attributesContainer.getAttributes()) {
					RefinedAttributeDefinition rAttrDef = rAccountDef.findAttributeDefinition(attribute.getName());
					if (!rAttrDef.isTolerant()) {
						throw new PolicyViolationException("Attempt to add object with non-tolerant attribute "+attribute.getName()+" in "+
								"account "+accountContext.getResourceShadowDiscriminator()+" during "+activityDescription);
					}
				}
			}
		} else if (primaryDelta.isModify()) {
			for(ItemDelta<?> modification: primaryDelta.getModifications()) {
				if (modification.getParentPath().equals(SchemaConstants.PATH_ATTRIBUTES)) {
					PropertyDelta<?> attrDelta = (PropertyDelta<?>) modification;
					RefinedAttributeDefinition rAttrDef = rAccountDef.findAttributeDefinition(attrDelta.getName());
					if (!rAttrDef.isTolerant()) {
						throw new PolicyViolationException("Attempt to modify non-tolerant attribute "+attrDelta.getName()+" in "+
								"account "+accountContext.getResourceShadowDiscriminator()+" during "+activityDescription);
					}
				}
			}
		} else {
			throw new IllegalStateException("Whoops!");
		}
	}
	
	/**
	 * Remove the intermediate results of values processing such as secondary deltas.
	 */
	private void cleanupContext(LensProjectionContext<AccountShadowType> accountContext) throws SchemaException {
		accountContext.setSecondaryDelta(null);
		accountContext.clearIntermediateResults();
		accountContext.recompute();
	}

	
}