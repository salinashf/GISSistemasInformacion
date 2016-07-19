package utilidades.conexion;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BaseConexion {
    private static Connection cns = null;
    private static String host = "192.168.150.100";
    private static String dataBase = "ProyectoSistInf";
    private static String user = "usergis";
    private static String pass = "usergis";

    public static Connection getConectar() {
        conectar();
        return cns;
    }

    public static void conectar() {
        try {
            if (cns == null) {
                 conexion();
            } else {
                if (cns.isClosed()) {
                     conexion();
                }

            }
        } catch (ClassNotFoundException ex) {
            System.out.print("Error Interno! Registro de Coneccion falló");
            //JOptionPane.showMessageDialog(null, "Error Interno! ", "Registro de Coneccion falló "+ ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            System.out.print("Acceso denegado!! Usuario NO Autorizado");
            //JOptionPane.showMessageDialog(null, "Acceso denegado!!", "Usuario NO Autorizado"+ ex.getMessage(), JOptionPane.ERROR_MESSAGE);
        }

    }

    private static void conexion() throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://" + host + ":5432/" + dataBase; //String url = "jdbc:postgresql://localhost:5432/gis_data";
        cns = DriverManager.getConnection(url, user, pass);
    }
    public static void cerrarConexion() throws SQLException {
        if (cns != null) {
            if (!cns.isClosed()) {
                cns.close();
            }
        }
    }
}