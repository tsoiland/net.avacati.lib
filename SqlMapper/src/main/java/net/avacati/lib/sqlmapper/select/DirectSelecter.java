package net.avacati.lib.sqlmapper.select;

import net.avacati.lib.sqlmapper.typeconfig.TypeConfig;
import net.avacati.lib.sqlmapper.typeconfig.TypeMap;
import net.avacati.lib.sqlmapper.util.*;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DirectSelecter {
    private TypeMap typeMap;
    private SqlDoer sqlDoer;

    public DirectSelecter(TypeMap typeMap, SqlDoer sqlDoer) {
        this.typeMap = typeMap;
        this.sqlDoer = sqlDoer;
    }

    public <Dbo> Optional<Dbo> getDbo(Class<Dbo> type, String id) {
        // Get the config
        TypeConfig typeConfig = this.typeMap.get(type);

        // Define sql
        String sql = "SELECT * FROM " + typeConfig.getTableNameForDbo() + " WHERE " + typeConfig.getPrimaryKeyFieldName() + "='" + id + "'";

        // Execute query
        ResultSet resultSet = this.sqlDoer.query(sql);

        // Map result set to dbos
        List<Dbo> dbos = this.mapResultSetToDbo(type, resultSet);

        // There is just supposed to be one or zero.
        if (dbos.size() == 0) {
            return Optional.empty();
        } else if (dbos.size() == 1) {
            return Optional.of(dbos.get(0));
        } else {
            throw new AssertionError("Wasn't supposed to be more than one row.");
        }
    }

    private <Dbo> List<Dbo> mapResultSetToDbo(Class<? extends Dbo> type, ResultSet resultSet) {
        try {
            List<Dbo> dbos = new ArrayList<>();
            while(resultSet.next()) {
                try {
                    // Instantiate dbo
                    Dbo dbo = type.newInstance();

                    // Populate fields from result set
                    Arrays
                        .stream(dbo.getClass().getFields())
                        .forEach(field -> this.populateDboFieldFromResultSet(dbo, field, resultSet));

                    dbos.add(dbo);

                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            return dbos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateDboFieldFromResultSet(Object dbo, Field field, ResultSet resultSet) {
        try {
            // Get the type config
            TypeConfig typeConfig = this.typeMap.get(field.getType());

            // The value of our field can be processed in three ways:
            // - directly from a column in the resultset.
            // - by looking up a sub dbo from another table.
            // - by looking up several sub dbos from another table.
            String columnName = field.getName();

            if (typeConfig.isDboThatMapsToItsOwnTable()) {
                // Direct fields, and subDbos (which have fks) have raw values, but not list fields.
                String rawValue = resultSet.getString(columnName);

                // If value is null, just leave it as null
                if (rawValue != null) {
                    // ... otherwise, RECURSIVELY query for sub-dbo
                    Object subDbo = this.getDbo(field.getType(), rawValue).orElse(null);

                    // Assign sub-dbo to dbo
                    field.set(dbo, subDbo);
                }

                return;

            } else if (typeConfig.shouldMapDirectlyToColumn()) {
                // Direct fields, and subDbos (which have fks) have raw values, but not list fields.
                String rawValue = resultSet.getString(columnName);

                // Map it to correct type
                Object correctTypeValue = typeConfig.reverseMap(rawValue);

                // Assign it to the dbo
                field.set(dbo, correctTypeValue);
                return;

            } else if (typeConfig.shouldTreatAsList()) {
                // Get the type map config for the parent dbo we're working on. That is, the one that has the list field on it.
                TypeConfig typeConfigForDbo = this.typeMap.get(dbo.getClass());

                // Determine value of foreign key to query for
                final String fkColumnName = dbo.getClass().getSimpleName() + "_" + field.getName();
                final String fkColumnValue = typeConfigForDbo.getPrimaryKey(dbo);
                DbField foreignKeyToQuery = new DbField(fkColumnName, fkColumnValue);

                Class<?> erasedType = typeConfigForDbo.getErasedType(field.getName());

                TypeConfig tm2 = this.typeMap.get(erasedType);
                // Query for sub dbos
                List<Object> subDbos = this.querySubDbos(erasedType, tm2.getTableNameForDbo(), foreignKeyToQuery);

                // Assign list of sub dbos
                field.set(dbo, subDbos);
                return;
            }
        } catch (IllegalAccessException | SQLException e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Field was not mapped at all.");
    }

    private <SubDbo> List<SubDbo> querySubDbos(Class<? extends SubDbo> subDboType, String tableName, DbField foreignKeyDbField) {
        // Select where
        String queryForSubDbos = "SELECT * FROM " + tableName + " WHERE " + foreignKeyDbField.columnName + "='" + foreignKeyDbField.value + "'";

        // Execute query
        ResultSet resultSet = this.sqlDoer.query(queryForSubDbos);

        // RECURSE on resultset
        List<SubDbo> subDbo = this.mapResultSetToDbo(subDboType, resultSet);

        return subDbo;
    }
}
