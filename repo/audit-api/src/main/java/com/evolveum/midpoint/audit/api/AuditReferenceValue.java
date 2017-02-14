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

package com.evolveum.midpoint.audit.api;

import com.evolveum.midpoint.util.QNameUtil;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Objects;

/**
 * Restricted version of ObjectReferenceType/PrismReferenceValue to be used for audit records.
 *
 * @author mederly
 */
public class AuditReferenceValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private String oid;
	private QName type;
	private String targetName;

	public AuditReferenceValue() {
	}

	public AuditReferenceValue(String oid, QName type, String targetName) {
		this.oid = oid;
		this.type = type;
		this.targetName = targetName;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public QName getType() {
		return type;
	}

	public void setType(QName type) {
		this.type = type;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	@Override
	public String toString() {
		return "AuditObjectReference{" +
				"oid='" + oid + '\'' +
				", type=" + type +
				", targetName='" + targetName + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AuditReferenceValue))
			return false;
		AuditReferenceValue that = (AuditReferenceValue) o;
		return Objects.equals(oid, that.oid) &&
				QNameUtil.match(type, that.type) &&
				Objects.equals(targetName, that.targetName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oid);
	}
}
