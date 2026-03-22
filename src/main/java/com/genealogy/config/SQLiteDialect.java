package com.genealogy.config;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.SQLiteDialect;

@Deprecated
public class SQLiteDialect extends org.hibernate.community.dialect.SQLiteDialect {
    public SQLiteDialect() {
        super(DatabaseVersion.make(3));
    }
}
