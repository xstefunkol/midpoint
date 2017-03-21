/*
 * Copyright (c) 2010-2017 Evolveum
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
package com.evolveum.midpoint.model.impl.lens;

import com.evolveum.midpoint.common.Clock;
import com.evolveum.midpoint.model.impl.lens.projector.AssignmentProcessor;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PlusMinusZero;
import com.evolveum.midpoint.prism.delta.builder.DeltaBuilder;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ActivationUtil;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.test.util.TestUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.bag.TreeBag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.evolveum.midpoint.test.IntegrationTestTools.display;
import static com.evolveum.midpoint.test.IntegrationTestTools.displayObjectTypeCollection;
import static com.evolveum.midpoint.test.util.TestUtil.assertSuccess;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Comprehensive test of assignment evaluator and processor.
 *
 *            MMR1 -----------I------------------------------*
 *             ^                                             |
 *             |                                             I
 *             |                                             V
 *            MR1 -----------I-------------*-----> MR3      MR4
 *             ^        MR2 --I---*        |        |        |
 *             |         ^        I        I        I        I
 *             |         |        V        V        V        V
 *             R1 --I--> R2       O3       R4       R5       R6
 *             ^
 *             |
 *             |
 *            jack
 *
 * Straight line means assignment.
 * Line marked with "I" means inducement.
 *
 * Orders of these inducements are given by the levels of participants, so that each induced role belongs to jack, and each
 * induced metarole belongs to some role. So,
 * - inducement Rx->Ry is of order 1
 * - inducement MRx->MRy is of order 1
 * - inducement MRx->Ry is of order 2
 * - inducement MMRx->MRy is of order 1
 *
 * Each role has an authorization, GUI config, constructions, focus mappings, focus policy rules and target policy rules.
 *
 * Each assignment and each role can be selectively enabled/disabled (via activation) and has its condition matched (none/old/new/old+new).
 *
 * @author mederly
 */
@SuppressWarnings({ "FieldCanBeLocal", "SameParameterValue" })
public class TestAssignmentProcessor2 extends AbstractLensTest {

	private static final int CONSTRUCTION_LEVELS = 5;
	private static final int FOCUS_MAPPING_LEVELS = 5;
	private static final int POLICY_RULES_LEVELS = 5;

	private static final boolean FIRST_PART = true;
	private static final boolean SECOND_PART = true;

	private static final File RESOURCE_DUMMY_EMPTY_FILE = new File(TEST_DIR, "resource-dummy-empty.xml");
	private static final String RESOURCE_DUMMY_EMPTY_OID = "10000000-0000-0000-0000-00000000EEE4";
	private static final String RESOURCE_DUMMY_EMPTY_INSTANCE_NAME = "empty";

	@Autowired private AssignmentProcessor assignmentProcessor;
    @Autowired private Clock clock;

    // first part
    private RoleType role1, role2, role4, role5, role6;
    private OrgType org3;
    private RoleType metarole1, metarole2, metarole3, metarole4;
    private RoleType metametarole1;

	// second part
	private RoleType role7, role8, role9;
	private RoleType metarole7, metarole8, metarole9;
	private RoleType metametarole7;

	private List<ObjectType> objects;

	private static final String ROLE_R1_OID = getRoleOid("R1");
	private static final String ROLE_R7_OID = getRoleOid("R7");
	private static final String ROLE_MR1_OID = getRoleOid("MR1");

	@Override
    public void initSystem(Task initTask, OperationResult initResult) throws Exception {
        super.initSystem(initTask, initResult);
		initDummyResourcePirate(RESOURCE_DUMMY_EMPTY_INSTANCE_NAME, RESOURCE_DUMMY_EMPTY_FILE,
				RESOURCE_DUMMY_EMPTY_OID, initTask, initResult);

		createObjectsInFirstPart(false, initTask, initResult, null);
	}

	@Test(enabled = FIRST_PART)
	public void test010AssignR1ToJack() throws Exception {
		final String TEST_NAME = "test010AssignR1ToJack";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		LensContext<UserType> context = createContextForRoleAssignment(USER_JACK_OID, ROLE_R1_OID, null, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		@SuppressWarnings({ "unchecked", "raw" })
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		assertTargets(evaluatedAssignment, true, "R1 R2 O3 R4 R5 R6", null, null, null, null, null);
		assertTargets(evaluatedAssignment, false, "MR1 MR2 MR3 MR4 MMR1", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "R1 R2 O3 R4 R5 R6");
		assertOrgRef(evaluatedAssignment, "O3");
		assertDelegation(evaluatedAssignment, null);

		// Constructions are named "role-level". We expect e.g. that from R1 we get a construction induced with order=1 (R1-1).
		String expectedItems = "R1-1 R2-1 O3-1 R4-1 R5-1 R6-1 MR1-2 MR2-2 MR3-2 MR4-2 MMR1-3";
		assertConstructions(evaluatedAssignment, expectedItems, null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, expectedItems);
		assertFocusPolicyRules(evaluatedAssignment, expectedItems);

		assertTargetPolicyRules(evaluatedAssignment,
				"R1-0 MR1-1 MR3-1 MR4-1 MMR1-2",
				"R4-0 R5-0 R6-0 R2-0 O3-0 MR2-1");
		assertAuthorizations(evaluatedAssignment, "R1 R2 O3 R4 R5 R6");
		assertGuiConfig(evaluatedAssignment, "R1 R2 O3 R4 R5 R6");
	}

	@Test(enabled = FIRST_PART)
	public void test020AssignMR1ToR1() throws Exception {
		final String TEST_NAME = "test020AssignMR1ToR1";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		LensContext<RoleType> context = createContextForAssignment(RoleType.class, ROLE_R1_OID, RoleType.class, ROLE_MR1_OID, null, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		// assignment of construction R1-0
		// assignment of focus mappings R1-0
		// assignment of focus policy rules R1-0
		// assignment of metarole MR1 (this will be checked)
		Collection<EvaluatedAssignmentImpl<RoleType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 4, 0, 0);
		List<EvaluatedAssignmentImpl<RoleType>> targetedAssignments = evaluatedAssignments.stream().filter(ea -> ea.getTarget() != null)
				.collect(Collectors.toList());
		assertEquals("Wrong # of targeted assignments", 1, targetedAssignments.size());
		EvaluatedAssignmentImpl<RoleType> evaluatedAssignment = targetedAssignments.get(0);

		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		// R4, R5, R6 could be optimized out
		assertTargets(evaluatedAssignment, true, "MR1 MR3 MR4", null, null, null, null, null);
		assertTargets(evaluatedAssignment, false, "MMR1 R5 R4 R6", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "MR1 MR3 MR4");
		assertOrgRef(evaluatedAssignment, "");
		assertDelegation(evaluatedAssignment, null);

		assertConstructions(evaluatedAssignment, "MR1-1 MR3-1 MMR1-2 MR4-1", null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, "MR1-1 MR3-1 MMR1-2 MR4-1");
		assertFocusPolicyRules(evaluatedAssignment, "MR1-1 MR3-1 MMR1-2 MR4-1");

		assertTargetPolicyRules(evaluatedAssignment, "MR1-0 MMR1-1", "MR3-0 MR4-0");
		assertAuthorizations(evaluatedAssignment, "MR1 MR3 MR4");
		assertGuiConfig(evaluatedAssignment, "MR1 MR3 MR4");
	}

	@Test(enabled = FIRST_PART)
	public void test030AssignR1ToJackProjectorDisabled() throws Exception {
		final String TEST_NAME = "test030AssignR1ToJackProjectorDisabled";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		LensContext<UserType> context = createContextForRoleAssignment(USER_JACK_OID, ROLE_R1_OID, null,
				a -> a.setActivation(ActivationUtil.createDisabled()), result);

		// WHEN
		projector.project(context, "", task, result);

		// THEN
		display("Output context", context);

		result.computeStatus();
		assertSuccess("Projector failed (result)", result);

		// MID-3679
		assertEquals("Wrong # of parentOrgRef entries", 0,
				context.getFocusContext().getObjectNew().asObjectable().getParentOrgRef().size());
		assertEquals("Wrong # of roleMembershipRef entries", 0,
				context.getFocusContext().getObjectNew().asObjectable().getRoleMembershipRef().size());
	}

	/**
	 * As R1 is assigned with the relation=approver, jack will "see" only this role.
	 * However, we must collect all relevant target policy rules.
	 */
	@Test(enabled = FIRST_PART)
	public void test040AssignR1ToJackAsApprover() throws Exception {
		final String TEST_NAME = "test040AssignR1ToJackAsApprover";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		LensContext<UserType> context = createContextForRoleAssignment(USER_JACK_OID, ROLE_R1_OID, SchemaConstants.ORG_APPROVER, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		assertTargets(evaluatedAssignment, false, "R1 R2 O3 R4 R5 R6 MR1 MR2 MR3 MR4 MMR1", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "R1");
		assertOrgRef(evaluatedAssignment, null);
		assertDelegation(evaluatedAssignment, null);

		assertConstructions(evaluatedAssignment, "", null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, "");
		assertFocusPolicyRules(evaluatedAssignment, "");

		assertTargetPolicyRules(evaluatedAssignment, "R1-0 MR1-1 MMR1-2 MR4-1 MR3-1", "R2-0 MR2-1 O3-0 R4-0 R5-0 R6-0");
		assertAuthorizations(evaluatedAssignment, "");
		assertGuiConfig(evaluatedAssignment, "");
	}

	/**
	 *                MMR1 -----------I------------------------------*
	 *                 ^                                             |
	 *                 |                                             I
	 *                 |                                             V
	 *                MR1 -----------I-------------*-----> MR3      MR4
	 *                 ^        MR2 --I---*        |        |        |
	 *                 |         ^        I        I        I        I
	 *                 |         |        V        V        V        V
	 *                 R1 --I--> R2       O3       R4       R5       R6
	 *                 ^
	 *                 |
	 *                 |
	 *  jack --D--> barbossa
	 *
	 *  (D = deputy assignment)
	 *
	 */
	@Test(enabled = FIRST_PART)
	public void test050JackDeputyOfBarbossa() throws Exception {
		final String TEST_NAME = "test050JackDeputyOfBarbossa";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		AssignmentType policyRuleAssignment = new AssignmentType(prismContext);
		PolicyRuleType rule = new PolicyRuleType(prismContext);
		rule.setName("barbossa-0");
		policyRuleAssignment.setPolicyRule(rule);
		@SuppressWarnings({"unchecked", "raw" })
		ObjectDelta<ObjectType> objectDelta = (ObjectDelta<ObjectType>) DeltaBuilder.deltaFor(UserType.class, prismContext)
				.item(UserType.F_ASSIGNMENT).add(
						ObjectTypeUtil.createAssignmentTo(ROLE_R1_OID, ObjectTypes.ROLE, prismContext),
						policyRuleAssignment)
				.asObjectDelta(USER_BARBOSSA_OID);
		executeChangesAssertSuccess(objectDelta, null, task, result);

		display("barbossa", getUser(USER_BARBOSSA_OID));
		objects.add(getUser(USER_BARBOSSA_OID).asObjectable());

		LensContext<UserType> context = createContextForAssignment(UserType.class, USER_JACK_OID, UserType.class, USER_BARBOSSA_OID,
				SchemaConstants.ORG_DEPUTY, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		assertTargets(evaluatedAssignment, true, "R1 R2 O3 R4 R5 R6", null, null, null, null, null);
		assertTargets(evaluatedAssignment, false, "barbossa MR1 MR2 MR3 MR4 MMR1", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "");
		assertOrgRef(evaluatedAssignment, "O3");
		assertDelegation(evaluatedAssignment, "barbossa R1 R2 O3 R4 R5 R6");
		PrismReferenceValue barbossaRef = evaluatedAssignment.getDelegationRefVals().stream()
				.filter(v -> USER_BARBOSSA_OID.equals(v.getOid())).findFirst().orElseThrow(
						() -> new AssertionError("No barbossa ref in delegation ref vals"));
		assertEquals("Wrong relation for barbossa delegation", SchemaConstants.ORG_DEPUTY, barbossaRef.getRelation());

		// Constructions are named "role-level". We expect e.g. that from R1 we get a construction induced with order=1 (R1-1).
		String expectedItems = "R1-1 R2-1 O3-1 R4-1 R5-1 R6-1 MR1-2 MR2-2 MR3-2 MR4-2 MMR1-3";
		assertConstructions(evaluatedAssignment, "Brethren_account_construction Undead_monkey_account_construction " + expectedItems, null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, expectedItems);
		assertFocusPolicyRules(evaluatedAssignment, "barbossa-0 " + expectedItems);

		// Rules for other targets are empty, which is very probably OK. All rules are bound to target "barbossa".
		// There is no alternative target, as barbossa does not induce anything.
		assertTargetPolicyRules(evaluatedAssignment, "barbossa-0 R1-1 R2-1 MR2-2 O3-1 MR1-2 MR3-2 R5-1 R4-1 MMR1-3 MR4-2 R6-1",
				"");
		assertAuthorizations(evaluatedAssignment, "R1 R2 O3 R4 R5 R6");
		assertGuiConfig(evaluatedAssignment, "R1 R2 O3 R4 R5 R6");
	}

	/**
	 *                               MMR1 -----------I------------------------------*
	 *                                ^                                             |
	 *                                |                                             I
	 *                                |                                             V
	 *                               MR1 -----------I-------------*-----> MR3      MR4
	 *                                ^        MR2 --I---*        |        |        |
	 *                                |         ^        I        I        I        I
	 *                                |         |        V        V        V        V
	 *                                R1 --I--> R2       O3       R4       R5       R6
	 *                                ^
	 *                                |
	 *                                |
	 * jack --D--> guybrush --D--> barbossa
	 *
	 * (D = deputy assignment)
	 *
	 */
	@Test(enabled = FIRST_PART)
	public void test060JackDeputyOfGuybrushDeputyOfBarbossa() throws Exception {
		final String TEST_NAME = "test060JackDeputyOfGuybrushDeputyOfBarbossa";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		AssignmentType deputyOfBarbossaAssignment = ObjectTypeUtil.createAssignmentTo(USER_BARBOSSA_OID, ObjectTypes.USER, prismContext);
		deputyOfBarbossaAssignment.getTargetRef().setRelation(SchemaConstants.ORG_DEPUTY);
		AssignmentType policyRuleAssignment = new AssignmentType(prismContext);
		PolicyRuleType rule = new PolicyRuleType(prismContext);
		rule.setName("guybrush-0");
		policyRuleAssignment.setPolicyRule(rule);
		@SuppressWarnings({"unchecked", "raw" })
		ObjectDelta<ObjectType> objectDelta = (ObjectDelta<ObjectType>) DeltaBuilder.deltaFor(UserType.class, prismContext)
				.item(UserType.F_ASSIGNMENT).add(deputyOfBarbossaAssignment, policyRuleAssignment)
				.asObjectDelta(USER_GUYBRUSH_OID);
		executeChangesAssertSuccess(objectDelta, null, task, result);

		display("guybrush", getUser(USER_GUYBRUSH_OID));
		objects.add(getUser(USER_GUYBRUSH_OID).asObjectable());

		LensContext<UserType> context = createContextForAssignment(UserType.class, USER_JACK_OID, UserType.class, USER_GUYBRUSH_OID,
				SchemaConstants.ORG_DEPUTY, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		assertTargets(evaluatedAssignment, true, "R1 R2 O3 R4 R5 R6", null, null, null, null, null);
		assertTargets(evaluatedAssignment, false, "guybrush barbossa MR1 MR2 MR3 MR4 MMR1", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "");
		assertOrgRef(evaluatedAssignment, "O3");
		assertDelegation(evaluatedAssignment, "guybrush barbossa R1 R2 O3 R4 R5 R6");
		PrismReferenceValue guybrushRef = evaluatedAssignment.getDelegationRefVals().stream()
				.filter(v -> USER_GUYBRUSH_OID.equals(v.getOid())).findFirst().orElseThrow(
						() -> new AssertionError("No guybrush ref in delegation ref vals"));
		assertEquals("Wrong relation for guybrush delegation", SchemaConstants.ORG_DEPUTY, guybrushRef.getRelation());

		String expectedItems = "R1-1 R2-1 O3-1 R4-1 R5-1 R6-1 MR1-2 MR2-2 MR3-2 MR4-2 MMR1-3";
		assertConstructions(evaluatedAssignment, "Brethren_account_construction Undead_monkey_account_construction " + expectedItems, null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, expectedItems);
		assertFocusPolicyRules(evaluatedAssignment, "guybrush-0 barbossa-0 " + expectedItems);

		// guybrush-0 is the rule assigned to the target (guybrush) - seems OK
		// barbossa-0 and Rx-y are rules attached to "indirect target" (barbossa, delegator of guybrush).
		// TODO it is not quite clear if these are to be considered direct or indirect targets
		// let's consider it OK for the moment
		assertTargetPolicyRules(evaluatedAssignment, "guybrush-0",
				"barbossa-0 R1-1 R2-1 MR2-2 O3-1 MR1-2 MR3-2 R5-1 R4-1 MMR1-3 MR4-2 R6-1");
		assertAuthorizations(evaluatedAssignment, "R1 R2 O3 R4 R5 R6");
		assertGuiConfig(evaluatedAssignment, "R1 R2 O3 R4 R5 R6");
	}

	/**
	 *                MMR1 -----------I------------------------------*
	 *                 ^                                             |
	 *                 |                                             I
	 *                 |                                             V
	 *                MR1 -----------I-------------*-----> MR3      MR4
	 *                 ^        MR2 --I---*        |        |        |
	 *                 |         ^        I        I        I        I
	 *                 |         |        V        V        V        V
	 *                 R1 --I--> R2       O3       R4       R5       R6
	 *                 ^
	 *                 A
	 *                 |
	 *  jack --D--> barbossa
	 *
	 *  (D = deputy assignment) (A = approver)
	 *
	 */
	@Test(enabled = FIRST_PART)
	public void test070JackDeputyOfBarbossaApproverOfR1() throws Exception {
		final String TEST_NAME = "test070JackDeputyOfBarbossaApproverOfR1";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		unassignAllRoles(USER_JACK_OID);
		unassignAllRoles(USER_GUYBRUSH_OID);
		unassignAllRoles(USER_BARBOSSA_OID);

		// barbossa has a policy rule barbossa-0 from test050
		assignRole(USER_BARBOSSA_OID, ROLE_R1_OID, SchemaConstants.ORG_APPROVER, task, result);

		display("barbossa", getUser(USER_BARBOSSA_OID));

		LensContext<UserType> context = createContextForAssignment(UserType.class, USER_JACK_OID, UserType.class, USER_BARBOSSA_OID,
				SchemaConstants.ORG_DEPUTY, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		assertTargets(evaluatedAssignment, true, "", null, null, null, null, null);
		assertTargets(evaluatedAssignment, false, "barbossa R1 R2 O3 R4 R5 R6 MR1 MR2 MR3 MR4 MMR1", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "");
		assertOrgRef(evaluatedAssignment, "");
		assertDelegation(evaluatedAssignment, "barbossa R1");
		PrismReferenceValue barbossaRef = evaluatedAssignment.getDelegationRefVals().stream()
				.filter(v -> USER_BARBOSSA_OID.equals(v.getOid())).findFirst().orElseThrow(
						() -> new AssertionError("No barbossa ref in delegation ref vals"));
		assertEquals("Wrong relation for barbossa delegation", SchemaConstants.ORG_DEPUTY, barbossaRef.getRelation());
		PrismReferenceValue r1Ref = evaluatedAssignment.getDelegationRefVals().stream()
				.filter(v -> ROLE_R1_OID.equals(v.getOid())).findFirst().orElseThrow(
						() -> new AssertionError("No R1 ref in delegation ref vals"));
		assertEquals("Wrong relation for R1 delegation", SchemaConstants.ORG_APPROVER, r1Ref.getRelation());

		// Constructions are named "role-level". We expect e.g. that from R1 we get a construction induced with order=1 (R1-1).
		assertConstructions(evaluatedAssignment, "Brethren_account_construction Undead_monkey_account_construction", null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, "");
		assertFocusPolicyRules(evaluatedAssignment, "barbossa-0");

		// Rules for other targets are empty, which is very probably OK. All rules are bound to target "barbossa".
		// There is no alternative target, as barbossa does not induce anything.
		assertTargetPolicyRules(evaluatedAssignment, "barbossa-0", "");
		assertAuthorizations(evaluatedAssignment, "");
		assertGuiConfig(evaluatedAssignment, "");
	}


	/**
	 * Now disable some roles. Their administrative status is simply set to DISABLED.
	 *
	 *            MMR1(D)---------I------------------------------*
	 *             ^                                             |
	 *             |                                             I
	 *             |                                             V
	 *            MR1 -----------I-------------*-----> MR3(D)   MR4
	 *             ^        MR2 --I---*        |        |        |
	 *             |         ^        I        I        I        I
	 *             |         |        V        V        V        V
	 *             R1 --I--> R2(D)    O3       R4(D)    R5       R6
	 *             ^
	 *             |
	 *             |
	 *            jack
	 */

	@Test(enabled = FIRST_PART)
	public void test100DisableSomeRoles() throws Exception {
		final String TEST_NAME = "test100DisableSomeRoles";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		// WHEN
		createObjectsInFirstPart(true, task, result, () -> disableRoles("MMR1 R2 MR3 R4"));

		// THEN
		// TODO check e.g. membershipRef for roles
	}


	@Test(enabled = FIRST_PART)
	public void test110AssignR1ToJack() throws Exception {
		final String TEST_NAME = "test010AssignR1ToJack";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		LensContext<UserType> context = createContextForRoleAssignment(USER_JACK_OID, ROLE_R1_OID, null, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		assertTargets(evaluatedAssignment, true, "R1", null, null, null, null, null);
		assertTargets(evaluatedAssignment, false, "MR1", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "R1");
		assertOrgRef(evaluatedAssignment, null);
		assertDelegation(evaluatedAssignment, null);

		// Constructions are named "role-level". We expect e.g. that from R1 we get a construction induced with order=1 (R1-1).
		String expectedItems = "R1-1 MR1-2";
		assertConstructions(evaluatedAssignment, expectedItems, null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, expectedItems);
		assertFocusPolicyRules(evaluatedAssignment, expectedItems);

		assertTargetPolicyRules(evaluatedAssignment, "R1-0 MR1-1", "");
		assertAuthorizations(evaluatedAssignment, "R1");
		assertGuiConfig(evaluatedAssignment, "R1");

	}

	/**
	 * In a similar way, let's disable some assignments. Their administrative status is simply set to DISABLED.
	 *
	 *            MMR1 -----------I------------------------------*
	 *             ^                                             |
	 *             |                                             I
	 *             |                                             V
	 *            MR1 -----------I-------------*-(D)-> MR3      MR4
	 *             ^        MR2 --I---*        |        |        |
	 *             |         ^        I        I        I        I(D)
	 *             |         |        V        V        V        V
	 *             R1-I(D)-> R2       O3       R4       R5       R6
	 *             ^
	 *             |
	 *             |
	 *            jack
	 */

	@Test(enabled = FIRST_PART)
	public void test150DisableSomeAssignments() throws Exception {
		final String TEST_NAME = "test150DisableSomeAssignments";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		// WHEN
		createObjectsInFirstPart(true, task, result, () -> disableAssignments("MR4-R6 MR1-MR3 R1-R2"));

		// THEN
	}

	@Test(enabled = FIRST_PART)
	public void test160AssignR1ToJack() throws Exception {
		final String TEST_NAME = "test160AssignR1ToJack";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		LensContext<UserType> context = createContextForRoleAssignment(USER_JACK_OID, ROLE_R1_OID, null, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		assertTargets(evaluatedAssignment, true, "R1 R4", null, null, null, null, null);
		assertTargets(evaluatedAssignment, false, "MR1 MMR1 MR4", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "R1 R4");
		assertOrgRef(evaluatedAssignment, null);
		assertDelegation(evaluatedAssignment, null);

		String expectedItems = "R1-1 MR1-2 MMR1-3 MR4-2 R4-1";
		assertConstructions(evaluatedAssignment, expectedItems, null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, expectedItems);
		assertFocusPolicyRules(evaluatedAssignment, expectedItems);

		assertTargetPolicyRules(evaluatedAssignment, "R1-0 MR1-1 MMR1-2 MR4-1", "R4-0");
		assertAuthorizations(evaluatedAssignment, "R1 R4");
		assertGuiConfig(evaluatedAssignment, "R1 R4");

	}

	/**
	 * Let's attach some conditions to assignments and roles. "+" condition means that it will be satisfied only in jack's new state.
	 * "-" condition will be satisfied only in jack's old state. "0" condition will be never satisfied.
	 *
	 *            MMR1------------I------------------------------*
	 *             ^                                             |
	 *            (+)                                            I
	 *             |                                             V
	 *         (+)MR1 -----------I-------------*-----> MR3(0)   MR4(-)
	 *             ^        MR2 --I---*        |        |        |
	 *            (+)        ^   (+)  I        I        I        I
	 *             |         |        V        V        V        V
	 *             R1 --I--> R2       O3       R4(D)    R5       R6
	 *             ^     (-)
	 *             |
	 *             |
	 *            jack
	 */

	@Test(enabled = FIRST_PART)
	public void test200AddConditions() throws Exception {
		final String TEST_NAME = "test200AddConditions";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		// WHEN
		createObjectsInFirstPart(true, task, result, () -> {
			disableRoles("R4");
			addConditionToRoles("MR1+ MR30 MR4-");
			addConditionToAssignments("R1-MR1+ MR1-MMR1+ R1-R2- MR2-O3+");
		});

		// THEN
		// TODO check e.g. membershipRef for roles
	}


	@Test(enabled = FIRST_PART)
	public void test210AssignR1ToJack() throws Exception {
		final String TEST_NAME = "test210AssignR1ToJack";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		LensContext<UserType> context = createContextForRoleAssignment(USER_JACK_OID, ROLE_R1_OID, null, null, result);
		context.getFocusContext().swallowToPrimaryDelta(
				DeltaBuilder.deltaFor(UserType.class, prismContext)
						.item(UserType.F_NAME).replace(PolyString.fromOrig("jack1"))
						.asItemDelta());

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		// R4 is not in plusInvalid, because only directly assigned targets are listed among targets (see validityOverride)
		assertTargets(evaluatedAssignment, true, "R1", null, "", null, "R2", null);
		assertTargets(evaluatedAssignment, false, "", null, "MR1 MMR1", null, "MR2", null);
		assertMembershipRef(evaluatedAssignment, "R1");
		assertOrgRef(evaluatedAssignment, null);
		assertDelegation(evaluatedAssignment, null);

		// R4-1 is not in plusInvalid (see above)
		assertConstructions(evaluatedAssignment, "R1-1", null, "MR1-2 MMR1-3", null, "R2-1 MR2-2", null);
		assertFocusMappings(evaluatedAssignment, "R1-1 MR1-2 MMR1-3");
		assertFocusPolicyRules(evaluatedAssignment, "R1-1 MR1-2 MMR1-3");

		assertTargetPolicyRules(evaluatedAssignment, "R1-0 MR1-1 MMR1-2", "");
		assertAuthorizations(evaluatedAssignment, "R1");
		assertGuiConfig(evaluatedAssignment, "R1");
	}

	/**
	 * Testing targets with multiple incoming paths.
	 *
	 *            MMR7 -------I--------*
	 *             ^^                  |
	 *             ||                  |
	 *             |+--------+         |
	 *             |         |         V
	 *            MR7       MR8       MR9
	 *             ^         ^         |
	 *             |         |         |
	 *             |         |         V
	 *             R7 --I--> R8        R9
	 *             ^
	 *             |
	 *             |
	 *            jack
	 *
	 */

	@Test(enabled = SECOND_PART)
	public void test300AssignR7ToJack() throws Exception {
		final String TEST_NAME = "test300AssignR7ToJack";
		TestUtil.displayTestTile(this, TEST_NAME);

		// GIVEN
		Task task = taskManager.createTaskInstance(TestAssignmentProcessor.class.getName() + "." + TEST_NAME);
		OperationResult result = task.getResult();

		createObjectsInSecondPart(false, task, result, null);

		LensContext<UserType> context = createContextForRoleAssignment(USER_JACK_OID, ROLE_R7_OID, null, null, result);

		// WHEN
		assignmentProcessor.processAssignmentsProjections(context, clock.currentTimeXMLGregorianCalendar(), task, result);

		// THEN
		display("Output context", context);
		display("Evaluated assignment triple", context.getEvaluatedAssignmentTriple());

		result.computeStatus();
		assertSuccess("Assignment processor failed (result)", result);

		Collection<EvaluatedAssignmentImpl<UserType>> evaluatedAssignments = assertAssignmentTripleSetSize(context, 0, 1, 0);
		EvaluatedAssignmentImpl<UserType> evaluatedAssignment = evaluatedAssignments.iterator().next();
		assertEquals("Wrong evaluatedAssignment.isValid", true, evaluatedAssignment.isValid());

		/* We expect some duplicates, namely:

        Constructions:
          DeltaSetTriple:
              zero:
                  description: R7-1
                  description: R8-1
                  description: R9-1 description: R9-1
                  description: MR7-2
                  description: MR8-2
                  description: MR9-2 description: MR9-2
                  description: MMR7-3 description: MMR7-3
              plus:
              minus:
        Roles:
          DeltaSetTriple:
              zero:
                EvaluatedAssignmentTarget:
                        name: R7
                        name: R8
                        name: R9 name: R9
                        name: MR7
                        name: MR8
                        name: MR9 name: MR9
                        name: MMR7 name: MMR7
              plus:
              minus:
        Membership:
          PRV(object=role:99999999-0000-0000-0000-0000000000R7(R7))
          PRV(object=role:99999999-0000-0000-0000-0000000000R8(R8))
          PRV(object=role:99999999-0000-0000-0000-0000000000R9(R9))     PRV(object=role:99999999-0000-0000-0000-0000000000R9(R9))
        Authorizations:
          [R7])
          [R8])
          [R9]) [R9])
        Focus Mappings:
          M(R7-1: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; ))
          M(R8-1: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; ))
          M(R9-1: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; )) M(R9-1: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; ))
          M(MR7-2: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; ))
          M(MR8-2: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; ))
          M(MR9-2: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; )) M(MR9-2: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; ))
          M(MMR7-3: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; )) M(MMR7-3: {...common/common-3}description = PVDeltaSetTriple(zero: [PPV(String:jack)]; plus: []; minus: []; ))
        Target: role:99999999-0000-0000-0000-0000000000R7(R7)
        focusPolicyRules:
          [
              name: R7-1
              name: R8-1
              name: R9-1 name: R9-1
              name: MR7-2
              name: MR8-2
              name: MR9-2 name: MR9-2
              name: MMR7-3 name: MMR7-3
          ]
        thisTargetPolicyRules:
          [
              name: R7-0
              name: MR7-1
              name: MMR7-2
              name: MR9-1
          ]
        otherTargetsPolicyRules:
          [
              name: R8-0
              name: R9-0 name: R9-0
              name: MR8-1
              name: MR9-1
              name: MMR7-2
          ]

          For all path-sensitive items (constructions, targets, focus mappings, policy rules) it is OK, because the items will
          differ in assignment path; this might be relevant (e.g. w.r.t. validity, or further processing of e.g. constructions
          or policy rules).

          Simple "scalar" results, like membership, authorizations or gui config, should not contain duplicates.
		 */

		assertTargets(evaluatedAssignment, true, "R7 R8 R9 R9", null, null, null, null, null);
		assertTargets(evaluatedAssignment, false, "MR7 MR8 MR9 MR9 MMR7 MMR7", null, null, null, null, null);
		assertMembershipRef(evaluatedAssignment, "R7 R8 R9");
		assertOrgRef(evaluatedAssignment, "");
		assertDelegation(evaluatedAssignment, "");

		String expectedItems = "R7-1 R8-1 R9-1 R9-1 MR7-2 MR8-2 MR9-2 MR9-2 MMR7-3 MMR7-3";
		assertConstructions(evaluatedAssignment, expectedItems, null, null, null, null, null);
		assertFocusMappings(evaluatedAssignment, expectedItems);
		assertFocusPolicyRules(evaluatedAssignment, expectedItems);

		assertTargetPolicyRules(evaluatedAssignment,
				"R7-0 MR7-1 MMR7-2 MR9-1",
				"R8-0 R9-0 R9-0 MR8-1 MR9-1 MMR7-2");
		assertAuthorizations(evaluatedAssignment, "R7 R8 R9");
		assertGuiConfig(evaluatedAssignment, "R7 R8 R9");
	}
	//region ============================================================= helper methods (preparing scenarios)

	private void createObjectsInFirstPart(boolean deleteFirst, Task task, OperationResult result, Runnable adjustment) throws Exception {
		role1 = createRole(1, 1);
		role1.setDelegable(true);
		role2 = createRole(1, 2);
		org3 = createOrg(3);
		role4 = createRole(1, 4);
		role5 = createRole(1, 5);
		role6 = createRole(1, 6);
		metarole1 = createRole(2, 1);
		metarole2 = createRole(2, 2);
		metarole3 = createRole(2, 3);
		metarole4 = createRole(2, 4);
		metametarole1 = createRole(3, 1);
		assign(role1, metarole1);
		assign(role2, metarole2);
		assign(metarole1, metametarole1);
		induce(role1, role2, 1);
		induce(metarole1, metarole3, 1);
		induce(metarole1, role4, 2);
		induce(metarole2, org3, 2);
		induce(metarole3, role5, 2);
		induce(metarole4, role6, 2);
		induce(metametarole1, metarole4, 2);

		objects = new ArrayList<>(
				Arrays.asList(role1, role2, org3, role4, role5, role6, metarole1, metarole2, metarole3, metarole4, metametarole1));

		createObjects(deleteFirst, task, result, adjustment);
	}

	private void createObjectsInSecondPart(boolean deleteFirst, Task task, OperationResult result, Runnable adjustment) throws Exception {
		role7 = createRole(1, 7);
		role7.setDelegable(true);
		role8 = createRole(1, 8);
		role9 = createRole(1, 9);
		metarole7 = createRole(2, 7);
		metarole8 = createRole(2, 8);
		metarole9 = createRole(2, 9);
		metametarole7 = createRole(3, 7);
		assign(role7, metarole7);
		assign(role8, metarole8);
		assign(metarole7, metametarole7);
		assign(metarole8, metametarole7);
		induce(role7, role8, 1);
		induce(metametarole7, metarole9, 2);
		induce(metarole9, role9, 2);

		objects = new ArrayList<>(
				Arrays.asList(role7, role8, role9, metarole7, metarole8, metarole9, metametarole7));

		createObjects(deleteFirst, task, result, adjustment);
	}

	private void createObjects(boolean deleteFirst, Task task, OperationResult result, Runnable adjustment) throws Exception {
		if (adjustment != null) {
			adjustment.run();
		}

		// TODO implement repoAddObjects with overwrite option
		if (deleteFirst) {
			for (ObjectType role : objects) {
				repositoryService.deleteObject(role.getClass(), role.getOid(), result);
			}
		}

		repoAddObjects(objects, result);
		recomputeAndRefreshObjects(objects, task, result);
		displayObjectTypeCollection("objects", objects);
	}

	// methods for creation-time manipulation with roles and assignments

	private void disableRoles(String text) {
		for (String name : getList(text)) {
			AbstractRoleType role = findRole(name);
			if (role.getActivation() == null) {
				role.setActivation(new ActivationType(prismContext));
			}
			role.getActivation().setAdministrativeStatus(ActivationStatusType.DISABLED);
		}
	}

	private void disableAssignments(String text) {
		for (String assignmentText : getList(text)) {
			AssignmentType assignment = findAssignmentOrInducement(assignmentText);
			if (assignment.getActivation() == null) {
				assignment.setActivation(new ActivationType(prismContext));
			}
			assignment.getActivation().setAdministrativeStatus(ActivationStatusType.DISABLED);
		}
	}

	private void addConditionToRoles(String text) {
		for (String item : getList(text)) {
			String name = StringUtils.substring(item, 0, -1);
			char conditionType = item.charAt(item.length() - 1);
			AbstractRoleType role = findRole(name);
			role.setCondition(createCondition(conditionType));
		}
	}

	private void addConditionToAssignments(String text) {
		for (String item : getList(text)) {
			String assignmentText = StringUtils.substring(item, 0,-1);
			char conditionType = item.charAt(item.length() - 1);
			AssignmentType assignment = findAssignmentOrInducement(assignmentText);
			assignment.setCondition(createCondition(conditionType));
		}
	}

	private MappingType createCondition(char conditionType) {
		ScriptExpressionEvaluatorType script = new ScriptExpressionEvaluatorType();
		switch (conditionType) {
			case '+': script.setCode("basic.stringify(name) == 'jack1'"); break;
			case '-': script.setCode("basic.stringify(name) == 'jack'"); break;
			case '0': script.setCode("basic.stringify(name) == 'never there'"); break;
			default: throw new AssertionError(conditionType);
		}
		ExpressionType expression = new ExpressionType();
		expression.getExpressionEvaluator().add(new ObjectFactory().createScript(script));
		VariableBindingDefinitionType source = new VariableBindingDefinitionType();
		source.setPath(new ItemPath(UserType.F_NAME).asItemPathType());
		MappingType rv = new MappingType();
		rv.setExpression(expression);
		rv.getSource().add(source);
		return rv;
	}

	private void induce(AbstractRoleType source, AbstractRoleType target, int inducementOrder) {
		AssignmentType inducement = ObjectTypeUtil.createAssignmentTo(target.asPrismObject());
		if (inducementOrder > 1) {
			inducement.setOrder(inducementOrder);
		}
		source.getInducement().add(inducement);
	}

	private void assign(RoleType source, RoleType target) {
		AssignmentType assignment = ObjectTypeUtil.createAssignmentTo(target.asPrismObject());
		source.getAssignment().add(assignment);
	}

	private RoleType createRole(int level, int number) {
		return prepareAbstractRole(new RoleType(prismContext), level, number, "R");
	}

	private OrgType createOrg(int number) {
		return prepareAbstractRole(new OrgType(prismContext), 1, number, "O");
	}

	private <R extends AbstractRoleType> R prepareAbstractRole(R abstractRole, int level, int number, String nameSymbol) {
		String name = StringUtils.repeat('M', level-1) + nameSymbol + number;
		String oid = getRoleOid(name);

		abstractRole.setName(PolyStringType.fromOrig(name));
		abstractRole.setOid(oid);

		// constructions
		for (int i = 0; i <= CONSTRUCTION_LEVELS; i++) {
			ConstructionType c = new ConstructionType(prismContext);
			c.setDescription(name + "-" + i);
			c.setResourceRef(ObjectTypeUtil.createObjectRef(RESOURCE_DUMMY_EMPTY_OID, ObjectTypes.RESOURCE));
			AssignmentType a = new AssignmentType(prismContext);
			a.setDescription("Assignment for " + c.getDescription());
			a.setConstruction(c);
			addAssignmentOrInducement(abstractRole, i, a);
		}

		// focus mappings
		for (int i = 0; i <= FOCUS_MAPPING_LEVELS; i++) {
			MappingType mapping = new MappingType();
			mapping.setName(name + "-" + i);
			VariableBindingDefinitionType source = new VariableBindingDefinitionType();
			source.setPath(new ItemPath(UserType.F_NAME).asItemPathType());
			mapping.getSource().add(source);
			VariableBindingDefinitionType target = new VariableBindingDefinitionType();
			target.setPath(new ItemPath(UserType.F_DESCRIPTION).asItemPathType());
			mapping.setTarget(target);
			MappingsType mappings = new MappingsType(prismContext);
			mappings.getMapping().add(mapping);
			AssignmentType a = new AssignmentType(prismContext);
			a.setFocusMappings(mappings);
			addAssignmentOrInducement(abstractRole, i, a);
		}

		// policy rules
		for (int i = 0; i <= POLICY_RULES_LEVELS; i++) {
			PolicyRuleType rule = new PolicyRuleType(prismContext);
			rule.setName(name + "-" + i);
			AssignmentType a = new AssignmentType(prismContext);
			a.setPolicyRule(rule);
			addAssignmentOrInducement(abstractRole, i, a);
		}

		// authorization
		AuthorizationType authorization = new AuthorizationType(prismContext);
		authorization.getAction().add(name);
		abstractRole.getAuthorization().add(authorization);

		// admin gui config
		AdminGuiConfigurationType guiConfig = new AdminGuiConfigurationType();
		guiConfig.setPreferredDataLanguage(name);
		abstractRole.setAdminGuiConfiguration(guiConfig);
		return abstractRole;
	}

	private <R extends AbstractRoleType> void addAssignmentOrInducement(R abstractRole, int order, AssignmentType assignment) {
		if (order == 0) {
			abstractRole.getAssignment().add(assignment);
		} else {
			assignment.setOrder(order);
			abstractRole.getInducement().add(assignment);
		}
	}

	private static String getRoleOid(String name) {
		return "99999999-0000-0000-0000-" + StringUtils.repeat('0', 12-name.length()) + name;
	}

	//endregion
	//region ============================================================= helper methods (asserts)

	private void assertMembershipRef(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, String text) {
		assertPrismRefValues("membershipRef", evaluatedAssignment.getMembershipRefVals(), findObjects(text));
	}

	private void assertDelegation(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, String text) {
		assertPrismRefValues("delegationRef", evaluatedAssignment.getDelegationRefVals(), findObjects(text));
	}

	private void assertOrgRef(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, String text) {
		assertPrismRefValues("orgRef", evaluatedAssignment.getOrgRefVals(), findObjects(text));
	}

	private void assertAuthorizations(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, String text) {
		assertUnsortedListsEquals("Wrong authorizations", getList(text), evaluatedAssignment.getAuthorizations(), a -> a.getAction().get(0));
	}

	private void assertGuiConfig(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, String text) {
		assertUnsortedListsEquals("Wrong gui configurations", getList(text),
				evaluatedAssignment.getAdminGuiConfigurations(), g -> g.getPreferredDataLanguage());
	}

	private <T> void assertUnsortedListsEquals(String message, Collection<String> expected, Collection<T> real, Function<T, String> nameExtractor) {
		Bag<String> expectedAsBag = new TreeBag<>(CollectionUtils.emptyIfNull(expected));
		Bag<String> realAsBag = new TreeBag<>(real.stream().map(nameExtractor).collect(Collectors.toList()));
		assertEquals(message, expectedAsBag, realAsBag);
	}

	private void assertFocusMappings(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, String expectedItems) {
		assertFocusMappings(evaluatedAssignment, getList(expectedItems));
	}

	private void assertFocusMappings(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, Collection<String> expectedItems) {
		assertUnsortedListsEquals("Wrong focus mappings", expectedItems, evaluatedAssignment.getFocusMappings(), m -> m.getMappingType().getName());
		// TODO look at the content of the mappings (e.g. zero, plus, minus sets)
	}

	private void assertFocusPolicyRules(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, String expectedItems) {
		assertFocusPolicyRules(evaluatedAssignment, getList(expectedItems));
	}

	private void assertFocusPolicyRules(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment, Collection<String> expectedItems) {
		assertUnsortedListsEquals("Wrong focus policy rules", expectedItems, evaluatedAssignment.getFocusPolicyRules(), r -> r.getName());
	}

	private void assertTargetPolicyRules(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment,
			String expectedThisTargetItems, String expectedOtherTargetsItems) {
		assertTargetPolicyRules(evaluatedAssignment, getList(expectedThisTargetItems), getList(expectedOtherTargetsItems));
	}

	private void assertTargetPolicyRules(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment,
			Collection<String> expectedThisTargetItems, Collection<String> expectedOtherTargetsItems) {
		expectedOtherTargetsItems = CollectionUtils.emptyIfNull(expectedOtherTargetsItems);
		expectedThisTargetItems = CollectionUtils.emptyIfNull(expectedThisTargetItems);
		assertUnsortedListsEquals("Wrong other targets policy rules", expectedOtherTargetsItems,
				evaluatedAssignment.getOtherTargetsPolicyRules(), r -> r.getName());
		assertUnsortedListsEquals("Wrong this target policy rules", expectedThisTargetItems,
				evaluatedAssignment.getThisTargetPolicyRules(), r -> r.getName());
	}

	private void assertTargets(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment,
			Boolean evaluateConstructions,
			String zeroValid, String zeroInvalid,
			String plusValid, String plusInvalid,
			String minusValid, String minusInvalid) {
		assertTargets(evaluatedAssignment, evaluateConstructions, getList(zeroValid), getList(zeroInvalid),
				getList(plusValid), getList(plusInvalid), getList(minusValid), getList(minusInvalid));
	}

	private void assertTargets(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment,
			Boolean evaluateConstructions,
			List<String> zeroValid, List<String> zeroInvalid,
			List<String> plusValid, List<String> plusInvalid,
			List<String> minusValid, List<String> minusInvalid) {
		assertTargets("zero", evaluatedAssignment.getRoles().getZeroSet(), evaluateConstructions, zeroValid, zeroInvalid);
		assertTargets("plus", evaluatedAssignment.getRoles().getPlusSet(), evaluateConstructions, plusValid, plusInvalid);
		assertTargets("minus", evaluatedAssignment.getRoles().getMinusSet(), evaluateConstructions, minusValid, minusInvalid);
	}

	private void assertTargets(String type, Collection<EvaluatedAssignmentTargetImpl> targets, Boolean evaluateConstructions,
			List<String> expectedValid, List<String> expectedInvalid) {
		targets = CollectionUtils.emptyIfNull(targets);
		Collection<EvaluatedAssignmentTargetImpl> realValid = targets.stream()
				.filter(t -> t.isValid() && matchesConstructions(t, evaluateConstructions)).collect(Collectors.toList());
		Collection<EvaluatedAssignmentTargetImpl> realInvalid = targets.stream()
				.filter(t -> !t.isValid() && matchesConstructions(t, evaluateConstructions)).collect(Collectors.toList());
		String ec = evaluateConstructions != null ? " (evaluateConstructions: " + evaluateConstructions + ")" : "";
		assertUnsortedListsEquals("Wrong valid targets in " + type + " set" + ec, expectedValid,
				realValid, t -> t.getTarget().getName().getOrig());
		assertUnsortedListsEquals("Wrong invalid targets in " + type + " set" + ec, expectedInvalid,
				realInvalid, t -> t.getTarget().getName().getOrig());
	}

	private boolean matchesConstructions(EvaluatedAssignmentTargetImpl t, Boolean evaluateConstructions) {
		return evaluateConstructions == null || t.isEvaluateConstructions() == evaluateConstructions;
	}

	private void assertConstructions(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment,
			String zeroValid, String zeroInvalid,
			String plusValid, String plusInvalid,
			String minusValid, String minusInvalid) {
		assertConstructions(evaluatedAssignment, getList(zeroValid), getList(zeroInvalid),
				getList(plusValid), getList(plusInvalid), getList(minusValid), getList(minusInvalid));
	}

	private void assertConstructions(EvaluatedAssignmentImpl<? extends FocusType> evaluatedAssignment,
			List<String> zeroValid, List<String> zeroInvalid,
			List<String> plusValid, List<String> plusInvalid,
			List<String> minusValid, List<String> minusInvalid) {
		assertConstructions("zero", evaluatedAssignment.getConstructionSet(PlusMinusZero.ZERO), zeroValid, zeroInvalid);
		assertConstructions("plus", evaluatedAssignment.getConstructionSet(PlusMinusZero.PLUS), plusValid, plusInvalid);
		assertConstructions("minus", evaluatedAssignment.getConstructionSet(PlusMinusZero.MINUS), minusValid, minusInvalid);
	}

	private void assertConstructions(String type, Collection<? extends Construction<? extends FocusType>> constructions, List<String> valid0,
			List<String> invalid0) {
		constructions = CollectionUtils.emptyIfNull(constructions);
		Collection<String> expectedValid = CollectionUtils.emptyIfNull(valid0);
		Collection<String> expectedInvalid = CollectionUtils.emptyIfNull(invalid0);
		Collection<Construction<? extends FocusType>> realValid = constructions.stream().filter(c -> c.isValid()).collect(Collectors.toList());
		Collection<Construction<? extends FocusType>> realInvalid = constructions.stream().filter(c -> !c.isValid()).collect(Collectors.toList());
		assertUnsortedListsEquals("Wrong valid constructions in " + type + " set", expectedValid,
				realValid, c -> c.getDescription());
		assertUnsortedListsEquals("Wrong invalid constructions in " + type + " set", expectedInvalid,
				realInvalid, c -> c.getDescription());
	}

	@SuppressWarnings("unchecked")
	private <F extends FocusType> Collection<EvaluatedAssignmentImpl<F>> assertAssignmentTripleSetSize(LensContext<F> context, int zero, int plus, int minus) {
		assertEquals("Wrong size of assignment triple zero set", zero, CollectionUtils.size(context.getEvaluatedAssignmentTriple().getZeroSet()));
		assertEquals("Wrong size of assignment triple plus set", plus, CollectionUtils.size(context.getEvaluatedAssignmentTriple().getPlusSet()));
		assertEquals("Wrong size of assignment triple minus set", minus, CollectionUtils.size(context.getEvaluatedAssignmentTriple().getMinusSet()));
		return (Collection) context.getEvaluatedAssignmentTriple().getAllValues();
	}

	//endregion
	//region ============================================================= helper methods (misc)

	private AssignmentType findAssignmentOrInducement(String assignmentText) {
		String[] split = StringUtils.split(assignmentText, "-");
		AbstractRoleType source = findRole(split[0]);
		AbstractRoleType target = findRole(split[1]);
		return findAssignmentOrInducement(source, target);
	}

	private AssignmentType findAssignmentOrInducement(AbstractRoleType source, AbstractRoleType target) {
		return Stream.concat(source.getAssignment().stream(), source.getInducement().stream())
				.filter(a -> a.getTargetRef() != null && target.getOid().equals(a.getTargetRef().getOid()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(source + " contains no assignment/inducement to " + target));
	}

	private AbstractRoleType findRole(String name) {
		return (AbstractRoleType) findObject(name);
	}

	private ObjectType findObject(String name) {
		return objects.stream().filter(r -> name.equals(r.getName().getOrig())).findFirst()
				.orElseThrow(() -> new IllegalStateException("No role " + name));
	}

	private List<ObjectType> findObjects(String text) {
		return getList(text).stream().map(n -> findObject(n)).collect(Collectors.toList());
	}

	private List<String> getList(String text) {
		if (text == null) {
			return Collections.emptyList();
		}
		List<String> rv = new ArrayList<>();
		for (String t : StringUtils.split(text)) {
			rv.add(t.replace('_', ' '));
		}
		return rv;
	}

	//endregion
}
