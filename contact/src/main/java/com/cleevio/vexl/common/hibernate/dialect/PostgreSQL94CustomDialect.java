package com.cleevio.vexl.common.hibernate.dialect;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import org.hibernate.dialect.PostgreSQL94Dialect;

public class PostgreSQL94CustomDialect extends PostgreSQL94Dialect {

    private static final int JDBC_TYPE = 2003;

    public PostgreSQL94CustomDialect() {
        this.registerHibernateType(JDBC_TYPE, StringArrayType.class.getName());
    }
}
