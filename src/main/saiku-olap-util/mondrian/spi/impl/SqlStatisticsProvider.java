package mondrian.spi.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.sql.DataSource;
import mondrian.rolap.RolapUtil;
import mondrian.rolap.SqlStatement;
import mondrian.server.Execution;
import mondrian.server.Locus;
import mondrian.spi.Dialect;
import mondrian.spi.StatisticsProvider;


/**
 * Created by huangqiang on 2017/6/20.
 */

public class SqlStatisticsProvider implements StatisticsProvider {

    public int getTableCardinality(Dialect dialect, DataSource dataSource, String catalog, String schema, String table, Execution execution) {
        StringBuilder buf = new StringBuilder("select count(*) from ");
        dialect.quoteIdentifier(buf, new String[] { catalog, schema, table });
        String sql = buf.toString();

        SqlStatement stmt = RolapUtil.executeQuery(dataSource, sql, new Locus(execution, "SqlStatisticsProvider.getTableCardinality", "Reading row count from table " + Arrays.asList(new String[] { catalog, schema, table })));
        try
        {
            ResultSet resultSet = stmt.getResultSet();
            int i;
            if (resultSet.next())
            {
                stmt.rowCount += 1;
                return resultSet.getInt(1);
            }
            return -1;
        }
        catch (SQLException e)
        {
            throw stmt.handle(e);
        }
        finally
        {
            stmt.close();
        }
    }

    public int getQueryCardinality(Dialect dialect, DataSource dataSource, String sql, Execution execution) {
        StringBuilder buf = new StringBuilder();
        buf.append("select count(*) from (").append(sql).append(")");
        if (dialect.requiresAliasForFromQuery()) {
            if (dialect.allowsAs()) {
                buf.append(" as ");
            } else {
                buf.append(" ");
            }
            dialect.quoteIdentifier(buf, new String[] { "init" });
        }
        String countSql = buf.toString();

        SqlStatement stmt = RolapUtil.executeQuery(dataSource, countSql, new Locus(execution, "SqlStatisticsProvider.getQueryCardinality", "Reading row count from query [" + sql + "]"));
        try {
            ResultSet resultSet = stmt.getResultSet();
            int i;
            if (resultSet.next()) {
                stmt.rowCount += 1;
                return resultSet.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw stmt.handle(e);
        } finally {
            stmt.close();
        }
    }

    public int getColumnCardinality(Dialect dialect, DataSource dataSource, String catalog, String schema, String table, String column, Execution execution)
    {
        String sql = generateColumnCardinalitySql(dialect, schema, table, column);
        if (sql == null) {
            return -1;
        }
        SqlStatement stmt = RolapUtil.executeQuery(dataSource, sql, new Locus(execution, "SqlStatisticsProvider.getColumnCardinality", "Reading cardinality for column " + Arrays.asList(new String[] { catalog, schema, table, column })));
        try {
            ResultSet resultSet = stmt.getResultSet();
            int i;
            if (resultSet.next()) {
                stmt.rowCount += 1;
                return resultSet.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw stmt.handle(e);
        } finally {
            stmt.close();
        }
    }

    private static String generateColumnCardinalitySql(Dialect dialect, String schema, String table, String column)
    {
        StringBuilder buf = new StringBuilder();
        String exprString = dialect.quoteIdentifier(column);
        if (dialect.allowsCountDistinct() && !dialect.getDatabaseProduct().name().equalsIgnoreCase("KYLIN")) {
            buf.append("select count(distinct ").append(exprString).append(") from ");
            dialect.quoteIdentifier(buf, new String[] { schema, table });
            return buf.toString();
        }
        if (dialect.allowsFromQuery()) {
            buf.append("select count(*) from (select distinct ").append(exprString).append(" from ");
            dialect.quoteIdentifier(buf, new String[] { schema, table });
            buf.append(")");
            if (dialect.requiresAliasForFromQuery()) {
                if (dialect.allowsAs()) {
                    buf.append(" as ");
                } else {
                    buf.append(' ');
                }
                dialect.quoteIdentifier(buf, new String[] { "init" });
            }
            return buf.toString();
        }
        return null;
    }
}
