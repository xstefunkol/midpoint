CREATE TABLE m_account_shadow (
  accountType              VARCHAR(255),
  allowedIdmAdminGuiAccess BIT,
  passwordXml              VARCHAR(MAX),
  id                       BIGINT      NOT NULL,
  oid                      VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_any (
  owner_id  BIGINT      NOT NULL,
  owner_oid VARCHAR(36) NOT NULL,
  ownerType INT         NOT NULL,
  PRIMARY KEY (owner_id, owner_oid, ownerType)
);

CREATE TABLE m_any_clob (
  checksum               VARCHAR(32) NOT NULL,
  anyContainer_owner_id  BIGINT      NOT NULL,
  anyContainer_owner_oid VARCHAR(36) NOT NULL,
  anyContainer_ownertype INT         NOT NULL,
  dynamicDef             BIT,
  name_namespace         VARCHAR(255),
  name_localPart         VARCHAR(255),
  type_namespace         VARCHAR(255),
  type_localPart         VARCHAR(255),
  clobValue              VARCHAR(MAX),
  valueType              INT,
  PRIMARY KEY (checksum, anyContainer_owner_id, anyContainer_owner_oid, anyContainer_ownertype)
);

CREATE TABLE m_any_date (
  owner_id       BIGINT      NOT NULL,
  owner_oid      VARCHAR(36) NOT NULL,
  ownerType      INT         NOT NULL,
  dateValue      DATETIME2,
  dynamicDef     BIT,
  name_namespace VARCHAR(255),
  name_localPart VARCHAR(255),
  type_namespace VARCHAR(255),
  type_localPart VARCHAR(255),
  valueType      INT
);

CREATE TABLE m_any_long (
  owner_id       BIGINT      NOT NULL,
  owner_oid      VARCHAR(36) NOT NULL,
  ownerType      INT         NOT NULL,
  longValue      BIGINT,
  dynamicDef     BIT,
  name_namespace VARCHAR(255),
  name_localPart VARCHAR(255),
  type_namespace VARCHAR(255),
  type_localPart VARCHAR(255),
  valueType      INT
);

CREATE TABLE m_any_reference (
  owner_id       BIGINT      NOT NULL,
  owner_oid      VARCHAR(36) NOT NULL,
  ownerType      INT         NOT NULL,
  oidValue       VARCHAR(255),
  dynamicDef     BIT,
  name_namespace VARCHAR(255),
  name_localPart VARCHAR(255),
  type_namespace VARCHAR(255),
  type_localPart VARCHAR(255),
  valueType      INT
);

CREATE TABLE m_any_string (
  owner_id       BIGINT      NOT NULL,
  owner_oid      VARCHAR(36) NOT NULL,
  ownerType      INT         NOT NULL,
  stringValue    VARCHAR(255),
  dynamicDef     BIT,
  name_namespace VARCHAR(255),
  name_localPart VARCHAR(255),
  type_namespace VARCHAR(255),
  type_localPart VARCHAR(255),
  valueType      INT
);

CREATE TABLE m_assignment (
  accountConstruction         VARCHAR(MAX),
  enabled                     BIT,
  validFrom                   DATETIME2,
  validTo                     DATETIME2,
  description                 VARCHAR(MAX),
  owner_id                    BIGINT      NOT NULL,
  owner_oid                   VARCHAR(36) NOT NULL,
  targetRef_description       VARCHAR(MAX),
  targetRef_filter            VARCHAR(MAX),
  targetRef_relationLocalPart VARCHAR(255),
  targetRef_relationNamespace VARCHAR(255),
  targetRef_targetOid         VARCHAR(36),
  targetRef_type              INT,
  id                          BIGINT      NOT NULL,
  oid                         VARCHAR(36) NOT NULL,
  extId                       BIGINT,
  extOid                      VARCHAR(36),
  extType                     INT,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_audit_delta (
  RAuditEventRecord_id BIGINT NOT NULL,
  deltas               VARCHAR(MAX)
);

CREATE TABLE m_audit_event (
  id                BIGINT NOT NULL,
  channel           VARCHAR(255),
  eventIdentifier   VARCHAR(255),
  eventStage        INT,
  eventType         INT,
  hostIdentifier    VARCHAR(255),
  initiator         VARCHAR(MAX),
  outcome           INT,
  sessionIdentifier VARCHAR(255),
  target            VARCHAR(MAX),
  targetOwner       VARCHAR(MAX),
  taskIdentifier    VARCHAR(255),
  taskOID           VARCHAR(255),
  timestampValue    BIGINT,
  PRIMARY KEY (id)
);

CREATE TABLE m_connector (
  connectorBundle  VARCHAR(255),
  connectorType    VARCHAR(255),
  connectorVersion VARCHAR(255),
  framework        VARCHAR(255),
  name_norm        VARCHAR(255),
  name_orig        VARCHAR(255),
  namespace        VARCHAR(255),
  xmlSchema        VARCHAR(MAX),
  id               BIGINT      NOT NULL,
  oid              VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_connector_host (
  hostname          VARCHAR(255),
  name_norm         VARCHAR(255),
  name_orig         VARCHAR(255),
  port              VARCHAR(255),
  protectConnection BIT,
  sharedSecret      VARCHAR(MAX),
  timeout           INT,
  id                BIGINT      NOT NULL,
  oid               VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

CREATE TABLE m_connector_target_system (
  connector_id     BIGINT      NOT NULL,
  connector_oid    VARCHAR(36) NOT NULL,
  targetSystemType VARCHAR(255)
);

CREATE TABLE m_container (
  id  BIGINT      NOT NULL,
  oid VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_exclusion (
  description VARCHAR(MAX),
  owner_id    BIGINT      NOT NULL,
  owner_oid   VARCHAR(36) NOT NULL,
  policy      INT,
  id          BIGINT      NOT NULL,
  oid         VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_generic_object (
  name_norm  VARCHAR(255),
  name_orig  VARCHAR(255),
  objectType VARCHAR(255),
  id         BIGINT      NOT NULL,
  oid        VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

CREATE TABLE m_node (
  clusteredNode          BIT,
  hostname               VARCHAR(255),
  internalNodeIdentifier VARCHAR(255),
  jmxPort                INT,
  lastCheckInTime        DATETIME2,
  name_norm              VARCHAR(255),
  name_orig              VARCHAR(255),
  nodeIdentifier         VARCHAR(255),
  running                BIT,
  id                     BIGINT      NOT NULL,
  oid                    VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

CREATE TABLE m_object (
  description VARCHAR(MAX),
  version     BIGINT      NOT NULL,
  id          BIGINT      NOT NULL,
  oid         VARCHAR(36) NOT NULL,
  extId       BIGINT,
  extOid      VARCHAR(36),
  extType     INT,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_operation_result (
  owner_oid        VARCHAR(36) NOT NULL,
  owner_id         BIGINT      NOT NULL,
  details          VARCHAR(MAX),
  localizedMessage VARCHAR(MAX),
  message          VARCHAR(MAX),
  messageCode      VARCHAR(255),
  operation        VARCHAR(MAX),
  params           VARCHAR(MAX),
  partialResults   VARCHAR(MAX),
  status           INT,
  token            BIGINT,
  PRIMARY KEY (owner_oid, owner_id)
);

CREATE TABLE m_org (
  costCenter       VARCHAR(255),
  displayName_norm VARCHAR(255),
  displayName_orig VARCHAR(255),
  identifier       VARCHAR(255),
  locality_norm    VARCHAR(255),
  locality_orig    VARCHAR(255),
  id               BIGINT      NOT NULL,
  oid              VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_org_closure (
  id             BIGINT NOT NULL,
  depthValue     INT,
  ancestor_id    BIGINT,
  ancestor_oid   VARCHAR(36),
  descendant_id  BIGINT,
  descendant_oid VARCHAR(36),
  PRIMARY KEY (id)
);

CREATE TABLE m_org_org_type (
  org_id  BIGINT      NOT NULL,
  org_oid VARCHAR(36) NOT NULL,
  orgType VARCHAR(255)
);

CREATE TABLE m_password_policy (
  lifetime     VARCHAR(MAX),
  name_norm    VARCHAR(255),
  name_orig    VARCHAR(255),
  stringPolicy VARCHAR(MAX),
  id           BIGINT      NOT NULL,
  oid          VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

CREATE TABLE m_reference (
  reference_type INT          NOT NULL,
  owner_id       BIGINT       NOT NULL,
  owner_oid      VARCHAR(36)  NOT NULL,
  relLocalPart   VARCHAR(255) NOT NULL,
  relNamespace   VARCHAR(255) NOT NULL,
  targetOid      VARCHAR(36)  NOT NULL,
  description    VARCHAR(MAX),
  filter         VARCHAR(MAX),
  containerType  INT,
  PRIMARY KEY (owner_id, owner_oid, relLocalPart, relNamespace, targetOid)
);

CREATE TABLE m_resource (
  administrativeState            INT,
  capabilities_cachingMetadata   VARCHAR(MAX),
  capabilities_configured        VARCHAR(MAX),
  capabilities_native            VARCHAR(MAX),
  configuration                  VARCHAR(MAX),
  connectorRef_description       VARCHAR(MAX),
  connectorRef_filter            VARCHAR(MAX),
  connectorRef_relationLocalPart VARCHAR(255),
  connectorRef_relationNamespace VARCHAR(255),
  connectorRef_targetOid         VARCHAR(36),
  connectorRef_type              INT,
  consistency                    VARCHAR(MAX),
  name_norm                      VARCHAR(255),
  name_orig                      VARCHAR(255),
  namespace                      VARCHAR(255),
  o16_lastAvailabilityStatus     INT,
  schemaHandling                 VARCHAR(MAX),
  scripts                        VARCHAR(MAX),
  synchronization                VARCHAR(MAX),
  xmlSchema                      VARCHAR(MAX),
  id                             BIGINT      NOT NULL,
  oid                            VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

CREATE TABLE m_resource_shadow (
  enabled                  BIT,
  validFrom                DATETIME2,
  validTo                  DATETIME2,
  attemptNumber            INT,
  dead                     BIT,
  failedOperationType      INT,
  intent                   VARCHAR(255),
  name_norm                VARCHAR(255),
  name_orig                VARCHAR(255),
  objectChange             VARCHAR(MAX),
  class_namespace          VARCHAR(255),
  class_localPart          VARCHAR(255),
  synchronizationSituation INT,
  synchronizationTimestamp DATETIME2,
  id                       BIGINT      NOT NULL,
  oid                      VARCHAR(36) NOT NULL,
  attrId                   BIGINT,
  attrOid                  VARCHAR(36),
  attrType                 INT,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_role (
  approvalExpression    VARCHAR(MAX),
  approvalProcess       VARCHAR(255),
  approvalSchema        VARCHAR(MAX),
  automaticallyApproved VARCHAR(MAX),
  name_norm             VARCHAR(255),
  name_orig             VARCHAR(255),
  requestable           BIT,
  roleType              VARCHAR(255),
  id                    BIGINT      NOT NULL,
  oid                   VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

CREATE TABLE m_sync_situation_description (
  shadow_id      BIGINT      NOT NULL,
  shadow_oid     VARCHAR(36) NOT NULL,
  chanel         VARCHAR(255),
  situation      INT,
  timestampValue DATETIME2
);

CREATE TABLE m_system_configuration (
  connectorFramework             VARCHAR(MAX),
  d22_description                VARCHAR(MAX),
  defaultUserTemplateRef_filter  VARCHAR(MAX),
  d22_relationLocalPart          VARCHAR(255),
  d22_relationNamespace          VARCHAR(255),
  d22_targetOid                  VARCHAR(36),
  defaultUserTemplateRef_type    INT,
  g36                            VARCHAR(MAX),
  g23_description                VARCHAR(MAX),
  globalPasswordPolicyRef_filter VARCHAR(MAX),
  g23_relationLocalPart          VARCHAR(255),
  g23_relationNamespace          VARCHAR(255),
  g23_targetOid                  VARCHAR(36),
  globalPasswordPolicyRef_type   INT,
  logging                        VARCHAR(MAX),
  modelHooks                     VARCHAR(MAX),
  name_norm                      VARCHAR(255),
  name_orig                      VARCHAR(255),
  notificationConfiguration      VARCHAR(MAX),
  id                             BIGINT      NOT NULL,
  oid                            VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

CREATE TABLE m_task (
  binding                     INT,
  canRunOnNode                VARCHAR(255),
  category                    VARCHAR(255),
  claimExpirationTimestamp    DATETIME2,
  exclusivityStatus           INT,
  executionStatus             INT,
  handlerUri                  VARCHAR(255),
  lastRunFinishTimestamp      DATETIME2,
  lastRunStartTimestamp       DATETIME2,
  modelOperationState         VARCHAR(MAX),
  name_norm                   VARCHAR(255),
  name_orig                   VARCHAR(255),
  nextRunStartTime            DATETIME2,
  node                        VARCHAR(255),
  objectRef_description       VARCHAR(MAX),
  objectRef_filter            VARCHAR(MAX),
  objectRef_relationLocalPart VARCHAR(255),
  objectRef_relationNamespace VARCHAR(255),
  objectRef_targetOid         VARCHAR(36),
  objectRef_type              INT,
  otherHandlersUriStack       VARCHAR(MAX),
  ownerRef_description        VARCHAR(MAX),
  ownerRef_filter             VARCHAR(MAX),
  ownerRef_relationLocalPart  VARCHAR(255),
  ownerRef_relationNamespace  VARCHAR(255),
  ownerRef_targetOid          VARCHAR(36),
  ownerRef_type               INT,
  parent                      VARCHAR(255),
  progress                    BIGINT,
  recurrence                  INT,
  resultStatus                INT,
  schedule                    VARCHAR(MAX),
  taskIdentifier              VARCHAR(255),
  threadStopAction            INT,
  id                          BIGINT      NOT NULL,
  oid                         VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid)
);

CREATE TABLE m_user (
  enabled                  BIT,
  validFrom                DATETIME2,
  validTo                  DATETIME2,
  additionalName_norm      VARCHAR(255),
  additionalName_orig      VARCHAR(255),
  costCenter               VARCHAR(255),
  allowedIdmAdminGuiAccess BIT,
  passwordXml              VARCHAR(MAX),
  emailAddress             VARCHAR(255),
  employeeNumber           VARCHAR(255),
  familyName_norm          VARCHAR(255),
  familyName_orig          VARCHAR(255),
  fullName_norm            VARCHAR(255),
  fullName_orig            VARCHAR(255),
  givenName_norm           VARCHAR(255),
  givenName_orig           VARCHAR(255),
  honorificPrefix_norm     VARCHAR(255),
  honorificPrefix_orig     VARCHAR(255),
  honorificSuffix_norm     VARCHAR(255),
  honorificSuffix_orig     VARCHAR(255),
  locale                   VARCHAR(255),
  locality_norm            VARCHAR(255),
  locality_orig            VARCHAR(255),
  name_norm                VARCHAR(255),
  name_orig                VARCHAR(255),
  nickName_norm            VARCHAR(255),
  nickName_orig            VARCHAR(255),
  preferredLanguage        VARCHAR(255),
  telephoneNumber          VARCHAR(255),
  timezone                 VARCHAR(255),
  title_norm               VARCHAR(255),
  title_orig               VARCHAR(255),
  id                       BIGINT      NOT NULL,
  oid                      VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

CREATE TABLE m_user_employee_type (
  user_id      BIGINT      NOT NULL,
  user_oid     VARCHAR(36) NOT NULL,
  employeeType VARCHAR(255)
);

CREATE TABLE m_user_organization (
  user_id  BIGINT      NOT NULL,
  user_oid VARCHAR(36) NOT NULL,
  norm     VARCHAR(255),
  orig     VARCHAR(255)
);

CREATE TABLE m_user_organizational_unit (
  user_id  BIGINT      NOT NULL,
  user_oid VARCHAR(36) NOT NULL,
  norm     VARCHAR(255),
  orig     VARCHAR(255)
);

CREATE TABLE m_user_template (
  accountConstruction  VARCHAR(MAX),
  name_norm            VARCHAR(255),
  name_orig            VARCHAR(255),
  propertyConstruction VARCHAR(MAX),
  id                   BIGINT      NOT NULL,
  oid                  VARCHAR(36) NOT NULL,
  PRIMARY KEY (id, oid),
  UNIQUE (name_norm)
);

ALTER TABLE m_account_shadow
ADD CONSTRAINT fk_account_shadow
FOREIGN KEY (id, oid)
REFERENCES m_resource_shadow;

ALTER TABLE m_any_clob
ADD CONSTRAINT fk_any_clob
FOREIGN KEY (anyContainer_owner_id, anyContainer_owner_oid, anyContainer_ownerType)
REFERENCES m_any;

CREATE INDEX iDate ON m_any_date (dateValue);

ALTER TABLE m_any_date
ADD CONSTRAINT fk_any_date
FOREIGN KEY (owner_id, owner_oid, ownerType)
REFERENCES m_any;

CREATE INDEX iLong ON m_any_long (longValue);

ALTER TABLE m_any_long
ADD CONSTRAINT fk_any_long
FOREIGN KEY (owner_id, owner_oid, ownerType)
REFERENCES m_any;

CREATE INDEX iOid ON m_any_reference (oidValue);

ALTER TABLE m_any_reference
ADD CONSTRAINT fk_any_reference
FOREIGN KEY (owner_id, owner_oid, ownerType)
REFERENCES m_any;

CREATE INDEX iString ON m_any_string (stringValue);

ALTER TABLE m_any_string
ADD CONSTRAINT fk_any_string
FOREIGN KEY (owner_id, owner_oid, ownerType)
REFERENCES m_any;

CREATE INDEX iAssignmentEnabled ON m_assignment (enabled);

ALTER TABLE m_assignment
ADD CONSTRAINT fk_assignment
FOREIGN KEY (id, oid)
REFERENCES m_container;

ALTER TABLE m_assignment
ADD CONSTRAINT fk_assignment_owner
FOREIGN KEY (owner_id, owner_oid)
REFERENCES m_object;

ALTER TABLE m_audit_delta
ADD CONSTRAINT fk_audit_delta
FOREIGN KEY (RAuditEventRecord_id)
REFERENCES m_audit_event;

CREATE INDEX iConnectorName ON m_connector (name_norm);

ALTER TABLE m_connector
ADD CONSTRAINT fk_connector
FOREIGN KEY (id, oid)
REFERENCES m_object;

ALTER TABLE m_connector_host
ADD CONSTRAINT fk_connector_host
FOREIGN KEY (id, oid)
REFERENCES m_object;

ALTER TABLE m_connector_target_system
ADD CONSTRAINT fk_connector_target_system
FOREIGN KEY (connector_id, connector_oid)
REFERENCES m_connector;

ALTER TABLE m_exclusion
ADD CONSTRAINT fk_exclusion
FOREIGN KEY (id, oid)
REFERENCES m_container;

ALTER TABLE m_exclusion
ADD CONSTRAINT fk_exclusion_owner
FOREIGN KEY (owner_id, owner_oid)
REFERENCES m_object;

ALTER TABLE m_generic_object
ADD CONSTRAINT fk_generic_object
FOREIGN KEY (id, oid)
REFERENCES m_object;

ALTER TABLE m_node
ADD CONSTRAINT fk_node
FOREIGN KEY (id, oid)
REFERENCES m_object;

ALTER TABLE m_object
ADD CONSTRAINT fk_object
FOREIGN KEY (id, oid)
REFERENCES m_container;

ALTER TABLE m_operation_result
ADD CONSTRAINT fk_result_owner
FOREIGN KEY (owner_id, owner_oid)
REFERENCES m_object;

ALTER TABLE m_org
ADD CONSTRAINT fk_org
FOREIGN KEY (id, oid)
REFERENCES m_role;

CREATE INDEX iDescendant ON m_org_closure (descendant_oid, descendant_id);

CREATE INDEX iAncestor ON m_org_closure (ancestor_oid, ancestor_id);

ALTER TABLE m_org_closure
ADD CONSTRAINT fk_descendant
FOREIGN KEY (descendant_id, descendant_oid)
REFERENCES m_object;

ALTER TABLE m_org_closure
ADD CONSTRAINT fk_ancestor
FOREIGN KEY (ancestor_id, ancestor_oid)
REFERENCES m_object;

ALTER TABLE m_org_org_type
ADD CONSTRAINT fk_org_org_type
FOREIGN KEY (org_id, org_oid)
REFERENCES m_org;

ALTER TABLE m_password_policy
ADD CONSTRAINT fk_password_policy
FOREIGN KEY (id, oid)
REFERENCES m_object;

ALTER TABLE m_reference
ADD CONSTRAINT fk_reference_owner
FOREIGN KEY (owner_id, owner_oid)
REFERENCES m_container;

ALTER TABLE m_resource
ADD CONSTRAINT fk_resource
FOREIGN KEY (id, oid)
REFERENCES m_object;

CREATE INDEX iResourceObjectShadowEnabled ON m_resource_shadow (enabled);

CREATE INDEX iResourceShadowName ON m_resource_shadow (name_norm);

ALTER TABLE m_resource_shadow
ADD CONSTRAINT fk_resource_object_shadow
FOREIGN KEY (id, oid)
REFERENCES m_object;

CREATE INDEX iRequestable ON m_role (requestable);

ALTER TABLE m_role
ADD CONSTRAINT fk_role
FOREIGN KEY (id, oid)
REFERENCES m_object;

ALTER TABLE m_sync_situation_description
ADD CONSTRAINT fk_shadow_sync_situation
FOREIGN KEY (shadow_id, shadow_oid)
REFERENCES m_resource_shadow;

ALTER TABLE m_system_configuration
ADD CONSTRAINT fk_system_configuration
FOREIGN KEY (id, oid)
REFERENCES m_object;

CREATE INDEX iTaskName ON m_task (name_norm);

ALTER TABLE m_task
ADD CONSTRAINT fk_task
FOREIGN KEY (id, oid)
REFERENCES m_object;

CREATE INDEX iFullName ON m_user (fullName_norm);

CREATE INDEX iLocality ON m_user (locality_norm);

CREATE INDEX iHonorificSuffix ON m_user (honorificSuffix_norm);

CREATE INDEX iEmployeeNumber ON m_user (employeeNumber);

CREATE INDEX iGivenName ON m_user (givenName_norm);

CREATE INDEX iFamilyName ON m_user (familyName_norm);

CREATE INDEX iAdditionalName ON m_user (additionalName_norm);

CREATE INDEX iHonorificPrefix ON m_user (honorificPrefix_norm);

CREATE INDEX iUserEnabled ON m_user (enabled);

ALTER TABLE m_user
ADD CONSTRAINT fk_user
FOREIGN KEY (id, oid)
REFERENCES m_object;

ALTER TABLE m_user_employee_type
ADD CONSTRAINT fk_user_employee_type
FOREIGN KEY (user_id, user_oid)
REFERENCES m_user;

ALTER TABLE m_user_organization
ADD CONSTRAINT fk_user_organization
FOREIGN KEY (user_id, user_oid)
REFERENCES m_user;

ALTER TABLE m_user_organizational_unit
ADD CONSTRAINT fk_user_org_unit
FOREIGN KEY (user_id, user_oid)
REFERENCES m_user;

ALTER TABLE m_user_template
ADD CONSTRAINT fk_user_template
FOREIGN KEY (id, oid)
REFERENCES m_object;

CREATE TABLE hibernate_sequence (
  next_val BIGINT
);

INSERT INTO hibernate_sequence VALUES (1);