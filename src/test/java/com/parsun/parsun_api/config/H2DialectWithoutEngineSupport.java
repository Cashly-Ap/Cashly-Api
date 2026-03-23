package com.parsun.parsun_api.config;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.identity.H2IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupport;

public class H2DialectWithoutEngineSupport extends H2Dialect {

    @Override
    public String getTableTypeString() {
        return "";
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new H2IdentityColumnSupport() {
            @Override
            public String getIdentityColumnString(int type) {
                return "AUTO_INCREMENT";
            }
        };
    }
}
