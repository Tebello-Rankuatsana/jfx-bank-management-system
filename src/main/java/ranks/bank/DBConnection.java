package ranks.bank;


import java.sql.*;

public class DBConnection {
    private Connection databaseLink;
    private final String databaseName = "bank_db";
    private String databaseUser;
    private String databasePassword;
    private final String url = "jdbc:mysql://localhost/"+databaseName;
    private ResultSet rs;
    private Statement stmt;
    private PreparedStatement pstmt;

    public DBConnection(String user, String pass){
        this.databaseUser = user;
        this.databasePassword = pass;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.databaseLink = DriverManager.getConnection(this.url,this.databaseUser,this.databasePassword);
        } catch (SQLException s) {
            s.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getDatabaseLink() {
        return this.databaseLink;
    }

    public ResultSet getRs() {
        return rs;
    }

    public void setRs(String query) {
        try{
            this.rs = this.getDatabaseLink().createStatement().executeQuery(query);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public Statement getStmt() {
        return stmt;
    }

    public void setStmt(String query) {
        try {
            this.stmt = this.getDatabaseLink().createStatement();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public PreparedStatement getPstmt() {
        return pstmt;
    }

    public void setPstmt(String query) {
        try{
            this.pstmt = this.getDatabaseLink().prepareStatement(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void executePsmt(){
        try{
            this.getPstmt().executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeUpdatePstmt(){
        try{
            this.getPstmt().executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeDataLink(){
        try{
            this.getDatabaseLink().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void openConnection() {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.databaseLink = DriverManager.getConnection(this.url,this.databaseUser,this.databasePassword);
        } catch (SQLException s) {
            s.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}