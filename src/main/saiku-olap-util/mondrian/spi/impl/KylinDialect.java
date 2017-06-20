package mondrian.spi.impl;

import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import mondrian.rolap.SqlStatement;

public class KylinDialect extends JdbcDialectImpl {

    public static final JdbcDialectFactory FACTORY =
        new JdbcDialectFactory(KylinDialect.class, DatabaseProduct.KYLIN) {
            protected boolean acceptsConnection(Connection connection) {
                return super.acceptsConnection(connection);
             }
        };

    /**
     * Creates a KylinDialect.
     *
     * @param connection Connection
     * @throws SQLException on error
     */
    public KylinDialect(Connection connection) throws SQLException {
        super(connection);
    }

    @Override
    public boolean allowsCountDistinct() {
        return true;
    }

    @Override
    public boolean allowsJoinOn() {
        return true;
    }

//    public SqlStatement.Type getType(ResultSetMetaData metaData, int columnIndex) throws SQLException {
//        return metaData.getColumnType(columnIndex + 1) == -5 ? SqlStatement.Type.LONG : super.getType(metaData, columnIndex);
//    }
//
//    public String generateOrderItem(String expr, boolean nullable, boolean ascending, boolean collateNullsLast)
//    {
//        if (ascending) {
//            return expr + " ASC";
//        }
//        return expr + " DESC";
//    }
//
//    public String toUpper(String expr)
//    {
//        return expr;
//    }
}