package cs208;

import org.sqlite.SQLiteConfig;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * The Database class contains helper functions to
 * create a connection and
 * test your connection
 * with the SQLite database.
 */
public class Database
{
    private final String sqliteFileName;

    public Database(String sqliteFileName)
    {
        this.sqliteFileName = sqliteFileName;
    }

    /**
     * Creates a connection to the SQLite database file specified in the {@link #Database(String) constructor}
     *
     * @return a connection to the database, which can be used to execute SQL statements against in the database
     * @throws SQLException if we cannot connect to the database (e.g., missing driver)
     */
    public Connection getDatabaseConnection() throws SQLException
    {
        // NOTE:
        // 'jdbc' is the protocol or API for connecting from a Java application to a database (SQLite, PostgreSQL, etc.)
        // 'sqlite' is the format of the database (for PostgreSQL, we would use the 'postgresql' format)
        String databaseConnectionURL = "jdbc:sqlite:" + sqliteFileName;
        System.out.println("databaseConnectionURL = " + databaseConnectionURL);

        Connection connection;
        try
        {
            SQLiteConfig sqLiteConfig = new SQLiteConfig();
            // Enables enforcement of foreign keys constraints in the SQLite database every time we start the application
            sqLiteConfig.enforceForeignKeys(true);

            connection = DriverManager.getConnection(databaseConnectionURL, sqLiteConfig.toProperties());
            return connection;
        }
        catch (SQLException sqlException)
        {
            System.err.println("SQLException was thrown while trying to connect using the '" + databaseConnectionURL + "' connection URL");
            System.err.println(sqlException.getMessage());
            throw sqlException;
        }
    }

    /**
     * Tests the connection to the database by running a simple SQL SELECT statement
     * to return the driver version used to connect to the database
     * NOTE:
     * See below the {@link #testConnectionSimplifiedVersion() testConnectionSimplifiedVersion()} method
     * for a simplified version of this method that uses less boilerplate.
     */
    public void testConnection()
    {
        // this SELECT statement will retrieve one row with one column containing the
        // version of the driver used to connect to the SQLite database
        String sql = "SELECT sqlite_version();";

        Connection connection = null;
        Statement sqlStatement = null;
        ResultSet resultSet = null;

        try
        {
            connection = getDatabaseConnection();
            sqlStatement = connection.createStatement();

            resultSet = sqlStatement.executeQuery(sql);

            // get to the first (and only) returned record (row)
            resultSet.next();

            // get the results of the first column in the row which contains the driver version
            String driverVersionToConnectToTheDatabase = resultSet.getString(1);
            System.out.println("Connection to Database Successful!");
            System.out.println("Driver version used to connect to the database: " + driverVersionToConnectToTheDatabase);
        }
        catch (SQLException sqlException)
        {
            System.err.println("SQLException: failed to query the database");
            System.err.println(sqlException.getMessage());
        }
        finally
        {
            // Q: Do I need to write this complex boilerplate code every time?
            // No, this is just for illustration purposes.
            // See below the testConnectionSimplifiedVersion() method
            // for a simpler alternative that does not require this finally block
            try
            {
                if (resultSet != null)
                {
                    resultSet.close();
                }
            }
            catch (SQLException sqlException)
            {
                System.err.println("SQLException: failed to close the resultSet");
                System.err.println(sqlException.getMessage());
            }

            try
            {
                if (sqlStatement != null)
                {
                    sqlStatement.close();
                }
            }
            catch (SQLException sqlException)
            {
                System.err.println("SQLException: failed to close the sqlStatement");
                System.err.println(sqlException.getMessage());
            }

            try
            {
                if (connection != null)
                {
                    connection.close();
                }
            }
            catch (SQLException sqlException)
            {
                System.err.println("SQLException: failed to close the connection");
                System.err.println(sqlException.getMessage());
            }
        }
    }

    /**
     * Tests the connection to the database by running a simple SQL SELECT statement
     * to return the driver version used to connect to the database
     * <p>
     * NOTE:
     * This method is the simplified version of {@link #testConnection() testConnection()}.
     * This method uses the
     * {@link <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">try-with-resources</a>}
     * statement to automatically close the {@code connection}, {@code sqlStatement} and {@code resultSet} objects
     * in case an error occurs, thus eliminating the code from the {@code finally} block
     */
    public void testConnectionSimplifiedVersion()
    {
        // this SELECT statement will retrieve one row with one column containing the
        // version of the driver used to connect to the SQLite database
        String sql = "SELECT sqlite_version();";

        try
        (
            Connection connection = getDatabaseConnection();
            Statement sqlStatement = connection.createStatement();
            ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            // get to the first (and only) returned record (row)
            resultSet.next();

            // get the results of the first column in the row which contains the driver version
            String driverVersionToConnectToTheDatabase = resultSet.getString(1);
            System.out.println("Connection to Database Successful!");
            System.out.println("Driver version used to connect to the database: " + driverVersionToConnectToTheDatabase);
        }
        catch (SQLException sqlException)
        {
            System.err.println("SQLException: failed to query the database");
            System.err.println(sqlException.getMessage());
        }
    }

    public void listAllStudentsInClass(int classID) {

        String sql =
                "SELECT students.first_name, students.last_name\n" +
                "FROM students\n" +
                "JOIN registered_students ON students.id = registered_students.student_id\n" +
                        "WHERE registered_students.class_id = ?;";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql);
                )
        {

            sqlStatement.setInt(1, classID);
            ResultSet resultSet = sqlStatement.executeQuery();


            //print table header
            printTableHeader(new String[]{"first name", "last"});

            // resultSet.next() either
            // advances to the next returned record (row)
            // or
            // returns false if there are no more records
            while (resultSet.next())
            {
                // extract the values from the current row
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                // print the results of the current row
                System.out.printf("| %s | %s |%n", firstName, lastName);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the classes table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }

    }

    public void listStudentsClasses(int studentID) {
        String sql =
                "SELECT classes.title\n" +
                        "FROM classes\n" +
                        "JOIN registered_students ON classes.id = registered_students.class_id\n" +
                        "WHERE registered_students.student_id = ?;";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql);
                )
        {

            sqlStatement.setInt(1, studentID);
            ResultSet resultSet = sqlStatement.executeQuery();


            //print table header
            printTableHeader(new String[]{"Class"});

            // resultSet.next() either
            // advances to the next returned record (row)
            // or
            // returns false if there are no more records
            while (resultSet.next())
            {
                // extract the values from the current row
                String className = resultSet.getString("title");

                // print the results of the current row
                System.out.printf("| %s |%n", className);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the classes table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }

    }

    public void listAllClasses()
    {
        String sql =
                "SELECT id, code, title, description, max_students\n" +
                "FROM classes;";

        try
        (
                Connection connection = getDatabaseConnection();
                Statement sqlStatement = connection.createStatement();
                ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            //print table header
            printTableHeader(new String[]{"id", "code", "title", "description", "max_students"});

            // resultSet.next() either
            // advances to the next returned record (row)
            // or
            // returns false if there are no more records
            while (resultSet.next())
            {
                // extract the values from the current row
                int id = resultSet.getInt("id");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");
                String description = resultSet.getString("description");
                int maxStudents = resultSet.getInt("max_students");

                // print the results of the current row
                System.out.printf("| %d | %s | %s | %s | %d |%n", id, code, title, description, maxStudents);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the classes table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }

    public void addNewClass(Class newClass)
    {
        String sql =
                "INSERT INTO classes (code, title, description, max_students)\n" +
                "VALUES (?, ?, ?, ?);";

        try
        (
            Connection connection = getDatabaseConnection();
            PreparedStatement sqlStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        )
        {
            sqlStatement.setString(1, newClass.getCode());
            sqlStatement.setString(2, newClass.getTitle());
            sqlStatement.setString(3, newClass.getDescription());
            sqlStatement.setInt(4, newClass.getMaxStudents());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                ResultSet resultSet = sqlStatement.getGeneratedKeys();

                while (resultSet.next())
                {
                    // "last_insert_rowid()" is the column name that contains the id of the last inserted row
                    // alternatively, we could have used resultSet.getInt(1); to get the id of the first column returned
                    int generatedIdForTheNewlyInsertedClass = resultSet.getInt("last_insert_rowid()");
                    System.out.println("SUCCESSFULLY inserted a new class with id = " + generatedIdForTheNewlyInsertedClass);

                    // this can be useful if we need to make additional processing on the newClass object
                    newClass.setId(generatedIdForTheNewlyInsertedClass);
                }

                resultSet.close();
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to insert into the classes table");
            System.out.println(sqlException.getMessage());
        }
    }

    public Class getExistingClassInformation(int classIDCode) {
        Class newClass = null;

        String sql =
                "SELECT *\n" +
                "FROM classes\n" +
                "WHERE classes.id = ?;";
        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql);

                )
        {

            sqlStatement.setInt(1, classIDCode);
            ResultSet resultSet = sqlStatement.executeQuery();

            int classID = -1;
            int maxStudents = -1;
            String classCode = null;
            String classTitle = null;
            String classDescription = null;

            while (resultSet.next())
            {
                // extract the values from the current row
                classID = resultSet.getInt("id");
                classCode = resultSet.getString("code");
                classTitle = resultSet.getString("title");
                classDescription = resultSet.getString("description");
                maxStudents = resultSet.getInt("max_students");
            }

            newClass = new Class(classID, classCode, classTitle, classDescription, maxStudents);

            // get the results of the first column in the row which contains the driver version
            String driverVersionToConnectToTheDatabase = resultSet.getString(1);
            System.out.println("Connection to Database Successful!");
            System.out.println("Driver version used to connect to the database: " + driverVersionToConnectToTheDatabase);
        }
        catch (SQLException sqlException)
        {
            System.err.println("SQLException: failed to query the database");
            System.err.println(sqlException.getMessage());
        }

        return newClass;

    }


    public void updateExistingClassInformation(Class classToUpdate)
    {
        String sql =
                "UPDATE classes\n" +
                "SET code = ?, title = ?, description = ?, max_students = ?\n" +
                "WHERE id = ?;";

        try
        (
            Connection connection = getDatabaseConnection();
            PreparedStatement sqlStatement = connection.prepareStatement(sql);
        )
        {
            sqlStatement.setString(1, classToUpdate.getCode());
            sqlStatement.setString(2, classToUpdate.getTitle());
            sqlStatement.setString(3, classToUpdate.getDescription());
            sqlStatement.setInt(4, classToUpdate.getMaxStudents());
            sqlStatement.setInt(5, classToUpdate.getId());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                System.out.println("SUCCESSFULLY updated the class with id = " + classToUpdate.getId());
            }
            else
            {
                System.out.println("!!! WARNING: failed to update the class with id = " + classToUpdate.getId());
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to update the class with id = " + classToUpdate.getId());
            System.out.println(sqlException.getMessage());
        }
    }

    public void deleteExistingClass(int idOfClassToDelete)
    {
        String sql =
                "DELETE FROM classes\n" +
                "WHERE id = ?;";

        try
        (
            Connection connection = getDatabaseConnection();
            PreparedStatement sqlStatement = connection.prepareStatement(sql);
        )
        {
            sqlStatement.setInt(1, idOfClassToDelete);

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                System.out.println("SUCCESSFULLY deleted the class with id = " + idOfClassToDelete);
            }
            else
            {
                System.out.println("!!! WARNING: failed to delete the class with id = " + idOfClassToDelete);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to delete the class with id = " + idOfClassToDelete);
            System.out.println(sqlException.getMessage());
        }
    }

    public void listAllStudents()
    {
        String sql =
                "SELECT id, first_name, last_name, birth_date\n" +
                "FROM students;";

        try
        (
            Connection connection = getDatabaseConnection();
            Statement sqlStatement = connection.createStatement();
            ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            printTableHeader(new String[]{"id", "first_name", "last_name", "birth_date"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");

                // the resultSet.getDate() does not work in this case, so we're using the getString() method instead
                String birthDate = resultSet.getString("birth_date");

                System.out.printf("| %d | %s | %s | %s |%n", id, firstName, lastName, birthDate);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }

    public void addNewStudent(Student newStudent)
    {
        // 💡 HINT: in a prepared statement
        // to set the date parameter in the format "YYYY-MM-DD", use the code:
        // sqlStatement.setString(columnIndexTBD, newStudent.getBirthDate().toString());
        //
        // to set the date parameter in the unix format (i.e., milliseconds since 1970), use this code:
        // sqlStatement.setDate(columnIndexTBD, newStudent.getBirthDate());

        String sql =
                "INSERT INTO students (first_name, last_name, birth_date)\n" +
                        "VALUES (?, ?, ?);";

        try (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        )
        {
            sqlStatement.setString(1, newStudent.getFirstName());
            sqlStatement.setString(2, newStudent.getLastName());
            sqlStatement.setString(3, newStudent.getBirthDate().toString());

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                ResultSet resultSet = sqlStatement.getGeneratedKeys();

                while (resultSet.next())
                {
                    // "last_insert_rowid()" is the column name that contains the id of the last inserted row
                    // alternatively, we could have used resultSet.getInt(1); to get the id of the first column returned
                    int generatedIdForTheNewlyInsertedClass = resultSet.getInt("last_insert_rowid()");
                    System.out.println("SUCCESSFULLY inserted a new class with id = " + generatedIdForTheNewlyInsertedClass);

                    // this can be useful if we need to make additional processing on the newClass object
                    newStudent.setId(generatedIdForTheNewlyInsertedClass);
                }

                resultSet.close();
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to insert into the classes table");
            System.out.println(sqlException.getMessage());
        }
    }

    public void deleteExistingStudent(int studentID)
    {
        String sql =
                "DELETE FROM students\n" +
                        "WHERE id = ?;";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql);
                )
        {
            sqlStatement.setInt(1, studentID);

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                System.out.println("SUCCESSFULLY deleted the class with id = " + studentID);
            }
            else
            {
                System.out.println("!!! WARNING: failed to delete the class with id = " + studentID);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to delete the class with id = " + studentID);
            System.out.println(sqlException.getMessage());
        }
    }

    public void updateExistingStudentInformation(int studentID, String firstName, String lastName, Date birthDate)
    {
        String sql =
                "UPDATE students\n" +
                        "SET first_name = ?, last_name = ?, birth_date = ?\n" +
                        "WHERE id = ?;";

        try
                (
                        Connection connection = getDatabaseConnection();
                        PreparedStatement sqlStatement = connection.prepareStatement(sql);
                )
        {
            sqlStatement.setString(1, firstName);
            sqlStatement.setString(2, lastName);
            sqlStatement.setString(3, birthDate.toString());
            sqlStatement.setInt(4, studentID);

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

            if (numberOfRowsAffected > 0)
            {
                System.out.println("SUCCESSFULLY updated the class with id = " + studentID);
            }
            else
            {
                System.out.println("!!! WARNING: failed to update the student with id = " + studentID);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to update the student with id = " + studentID);
            System.out.println(sqlException.getMessage());
        }
    }

    public void addNewStudentToClass(int classCode, int studentID) {
        String sql =
                "INSERT INTO registered_students (class_id, student_id)\n" +
                        "VALUES (?, ?);";

        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement sqlStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        )
        {

            sqlStatement.setInt(1, classCode);
            sqlStatement.setInt(2, studentID);

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);

        } catch (SQLException sqlException) {
            System.out.println("!!! SQLException: failed to insert into the classes table");
            System.out.println(sqlException.getMessage());
        }
    }

    public void dropStudentFromClass(int classCode, int studentID) {
        String sql =
                "DELETE FROM registered_students\n" +
                        "WHERE student_id = ? AND class_id = ?;";
        try (
                Connection connection = getDatabaseConnection();
                PreparedStatement sqlStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ) {
            sqlStatement.setInt(1, studentID);
            sqlStatement.setInt(2, classCode);

            int numberOfRowsAffected = sqlStatement.executeUpdate();
            System.out.println("numberOfRowsAffected = " + numberOfRowsAffected);
        } catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to insert into the classes table");
            System.out.println(sqlException.getMessage());
        }
    }

    public void listAllRegisteredStudents()
    {
        String sql =
                "SELECT students.id, students.first_name || ' ' || students.last_name AS student_full_name, classes.code, classes.title\n" +
                "FROM students\n" +
                "INNER JOIN registered_students ON students.id = registered_students.student_id\n" +
                "INNER JOIN classes ON classes.id = registered_students.class_id\n" +
                "ORDER BY students.last_name, students.first_name, classes.code;";

        try
        (
            Connection connection = getDatabaseConnection();
            Statement sqlStatement = connection.createStatement();
            ResultSet resultSet = sqlStatement.executeQuery(sql);
        )
        {
            printTableHeader(new String[]{"students.id", "student_full_name", "classes.code", "classes.title"});

            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                String studentFullName = resultSet.getString("student_full_name");
                String code = resultSet.getString("code");
                String title = resultSet.getString("title");

                System.out.printf("| %d | %s | %s | %s |%n", id, studentFullName, code, title);
            }
        }
        catch (SQLException sqlException)
        {
            System.out.println("!!! SQLException: failed to query the registered_students table. Make sure you executed the schema.sql and seeds.sql scripts");
            System.out.println(sqlException.getMessage());
        }
    }

    private void printTableHeader(String[] listOfColumnNames)
    {
        System.out.print("| ");
        for (String columnName : listOfColumnNames)
        {
            System.out.print(columnName + " | ");
        }
        System.out.println();
        System.out.println(Utils.characterRepeat('-', 80));
    }
}
