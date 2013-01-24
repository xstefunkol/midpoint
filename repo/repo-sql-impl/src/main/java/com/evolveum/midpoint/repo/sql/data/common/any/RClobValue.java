/*
 * Copyright (c) 2012 Evolveum
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
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.data.common.any;

import com.evolveum.midpoint.repo.sql.util.RUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.xml.namespace.QName;

/**
 * @author lazyman
 */
@Embeddable
public class RClobValue extends RValue<String> {

    private String value;
    private String checksum;

    public RClobValue() {
    }

    public RClobValue(String value) {
        this(null, null, value);
    }

    public RClobValue(QName name, QName type, String value) {
        setName(name);
        setType(type);
        setValue(value);
    }

    @Lob
    @Type(type = RUtil.LOB_STRING_TYPE)
    @Column(name = "clobValue")
    @Override
    public String getValue() {
        return value;
    }

    /**
     * This method is used for content comparing when querying database (we don't want to compare clob values).
     *
     * @return md5 hash of {@link com.evolveum.midpoint.repo.sql.data.common.any.RClobValue#getValue()}
     */
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        //checksum is always computed from value, this setter is only for hibernate satisfaction
    }

    public void setValue(String value) {
        this.value = value;

        checksum = StringUtils.isNotEmpty(this.value) ? DigestUtils.md5Hex(this.value) : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RClobValue that = (RClobValue) o;

        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (checksum != null ? !checksum.equals(that.checksum) : that.checksum != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        return result;
    }
}
