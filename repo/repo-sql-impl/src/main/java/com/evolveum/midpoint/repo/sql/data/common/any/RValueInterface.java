package com.evolveum.midpoint.repo.sql.data.common.any;

import com.evolveum.midpoint.repo.sql.data.common.RAnyContainer;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * todo rename this interface after refactor to RValue, original RValue class should be renamed to RBaseValue
 *
 * @author lazyman
 */
public interface RValueInterface<T> extends Serializable {

    void setAnyContainer(RAnyContainer anyContainer);

    QName getName();

    QName getType();

    RValueType getValueType();

    boolean isDynamic();

    T getValue();

    void setName(QName name);

    void setType(QName type);

    void setValueType(RValueType valueType);

    void setDynamic(boolean dynamic);
}
