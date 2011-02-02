/*
 * Table.java
 *
 * Created on Jan 24, 2011, 12:02:44 PM
 *
 *
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package savant.sql;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Small class which provides information about a table within a database.
 *
 * @author tarkvara
 */
public class Table {
    private static final Log LOG = LogFactory.getLog(Table.class);

    private String name;
    private Column[] columns;
    private Database database;

    Table(String name, Database database) {
        this.name = name;
        this.database = database;
    }

    /**
     * Override toString so that we can display a Table object directly in a combo-box.
     *
     * @return
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * As part of the plugin's cleanup, it should close the JDBC connection.
     */
    public void closeConnection() {
        database.closeConnection();
    }

    synchronized Column[] getColumns() throws SQLException {
        if (columns == null) {
            ResultSet rs = executeQuery("SELECT * FROM %s LIMIT 1", name);
            ResultSetMetaData md = rs.getMetaData();
            int numCols = md.getColumnCount();
            columns = new Column[numCols];
            for (int i = 0; i < numCols; i++) {
                columns[i] = new Column(md.getColumnName(i + 1), md.getColumnType(i + 1));
            }
        }
        return columns;
    }

    ResultSet executeQuery(String format, Object... args) throws SQLException {
        Statement st = database.getConnection().createStatement();
        return st.executeQuery(String.format(format, args));
    }

    /**
     * Does this table contain a column with the given name?
     */
    Column findColumnByName(String colName) throws SQLException {
        for (Column col: getColumns()) {
            if (col.name.equals(colName)) {
                return col;
            }
        }
        return null;
    }

    /**
     * Return the table-specific URI.  This will include the full database URI with
     * the table name appended as the last component.
     */
    URI getURI() {
        return URI.create(database.serverURI + "/" + database.name + "/" + name);
    }

    /**
     * Small class which provides information about a column within a table.
     */
    class Column {
        String name;
        int type;

        Column(String name, int type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}