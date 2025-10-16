package com.nucleonforge.axile.sbs.spring.integrations.rdbms;

import java.util.Set;

import javax.sql.DataSource;

/**
 * Represents different types of the relational databases Axile can work with.
 *
 * @since 07.07.2025
 * @author Mikhail Polivakha
 */
public enum RDBMSType {
    POSTGRESQL(Set.of("Postgres", "PostgreSQL"), "PostgreSQL Database"),
    ORACLE(Set.of("Oracle", "Ora"), "Oracle Database"),
    MY_SQL(Set.of("MySQL", "MySql"), "MySQL Database"),
    SQL_SERVER(Set.of("Sql Server", "MSSQL", "MS SQL", "Yukon", "Katmai", "Denali"), "Sql Server Database"),
    ;

    /**
     * Aliases names for this database.
     */
    private final Set<String> aliases;

    /**
     * Checks whether the given {@link DataSource} provides connections for given database.
     */
    private final String displayName;

    RDBMSType(Set<String> aliases, String displayName) {
        this.aliases = aliases;
        this.displayName = displayName;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public String getDisplayName() {
        return displayName;
    }
}
