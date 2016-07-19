
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;

import utilidades.conexion.BaseConexion;


import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.map.Marker;

@ManagedBean(name = "addMarkersView")
@SessionScoped
public class Controlador implements Serializable {

    private static final Logger logger = Logger.getLogger(Controlador.class.getName());


    private UploadedFile file;
    private MapModel emptyModel;
    private String title;
    private String  descripcion ;
    private double lat;
    private double lng;
    private Marker marker;
    private int currentInsert= 0 ;


    @PostConstruct
    public void init() {
        logger.log(java.util.logging.Level.SEVERE, null, "ccccc");
        emptyModel = new DefaultMapModel();
        IniciarPuntos();
    }

 
    public UploadedFile getFile() {
        return file;
    }
 
    public void setFile(UploadedFile file) {
        this.file = file;
    }
    public void IniciarPuntos() {
        leerDB();
    }

    public MapModel getEmptyModel() {
        return emptyModel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        marker = (Marker) event.getOverlay();
    }

    public Marker getMarker() {
        return marker;
    }

    private int insertRegistros(Connection pcnx, String sqlStatement, Marker pLatLng) {
        PreparedStatement psta = null;
        ResultSet keyset = null ;
        int   valueInsert =  0;       
        try {         
            psta = pcnx.prepareStatement(sqlStatement,Statement.RETURN_GENERATED_KEYS);
            psta.setString(1, pLatLng.getTitle());
            
            psta.setString(2,  ((MarkerData) (pLatLng.getData())).getDescription() );
            psta.setObject(3, pLatLng.getLatlng().getLng());
            psta.setObject(4, pLatLng.getLatlng().getLat());
            psta.executeUpdate();
            keyset = psta.getGeneratedKeys();
           
             while (keyset.next()) {
             currentInsert =    keyset.getInt("puntos_id");
            
            } 
            psta.close();
        } catch (SQLException ex) {
            GenereraError("Error", ex.getMessage());
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        } finally {
            
            try {
                keyset.close();
                
                if (psta != null) {
                    try {
                        if (!psta.isClosed()) {
                            psta.close();
                        }
                    } catch (SQLException ex) {
                        GenereraError("Error", ex.getMessage());
                        logger.log(java.util.logging.Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return  valueInsert ;
    }

    public void guardarDB(Marker mk) {
        final Connection cnx;
        try {
            cnx = BaseConexion.getConectar();
            String insertPointSQL = "INSERT INTO puntos  (nombre , descripcion , punto_map) values(?, ? , ST_SetSRID(ST_MakePoint(?, ?), 4326))";
            insertRegistros(cnx, insertPointSQL, mk);
        } finally {
            try {
                BaseConexion.cerrarConexion();
            } catch (SQLException ex) {
                GenereraError("Error", ex.getMessage());
                logger.log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }
    
       public void guardarImagenDB(int position, String name ,byte [] img ) {
        final Connection cnx;
        try {
            cnx = BaseConexion.getConectar();
            String insertPointSQL = "update  puntos  set  imagen_bit = ? , imagen_name = ?   where puntos_id  =1080";
            PreparedStatement ps = cnx.prepareStatement(insertPointSQL) ;
            ps.setString(2, name);
            ps.setBytes(1, img);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                BaseConexion.cerrarConexion();
            } catch (SQLException ex) {
                GenereraError("Error", ex.getMessage());
                logger.log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }

    
      public void elimiarDB(Marker mk) {
        final Connection cnx;
        Statement  stm = null;
        try {
            cnx = BaseConexion.getConectar();
            String deletePointSQL = "delete from puntos  where puntos_id  =  "+((MarkerData) (mk.getData())).getPoint_ID() ;
           
            
                stm = cnx.createStatement();
                stm.executeUpdate(deletePointSQL) ;
                 
           
            
        }  catch (SQLException ex) {
                Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stm.close();
                BaseConexion.cerrarConexion();
            } catch (SQLException ex) {
                GenereraError("Error", ex.getMessage());
                logger.log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }
    
    public void leerDB() {
        final Connection cnx;
        Statement sentencia = null;
        ResultSet rs = null;
        try {
            cnx = BaseConexion.getConectar();
            String selectPointSQL = "select  puntos_id,  nombre , descripcion , ST_y(punto_map)  as Lat , ST_X(punto_map) as Lng   from   puntos   ";
            sentencia = cnx.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = sentencia.executeQuery(selectPointSQL);

            while (rs.next()) {
                MarkerData x =  new  MarkerData( rs.getInt("puntos_id"), rs.getString("descripcion"));
                
                Marker puntom = new Marker(new LatLng(rs.getDouble("lat"), rs.getDouble("lng")), rs.getString("nombre"), x);
                emptyModel.addOverlay(puntom);
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (sentencia != null) {
                    sentencia.close();
                }
                BaseConexion.cerrarConexion();
            } catch (SQLException ex) {
                GenereraError("Error", ex.getMessage());
                logger.log(java.util.logging.Level.SEVERE, null, ex);
            }
        }
    }
    public void GenereraError(String vmsmTitle, String vmsgContent) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, vmsmTitle, vmsgContent));
    }
    public   void removeMarket(){
      elimiarDB(marker);
      emptyModel = new DefaultMapModel();
      leerDB();
      FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Punto Eliminado ", "Lat"));
    }
    public void addMarker() {
        MarkerData mData =  new MarkerData(-1, descripcion) ;
        Marker mInsert= new Marker(new LatLng(lat, lng), title, mData ) ;
       
        guardarDB(mInsert);
        mData.setPoint_ID(currentInsert);
        

        emptyModel.addOverlay(mInsert);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Punto Agregado", "Lat:" + lat + ", Lng:" + lng));
        
        
        
        
        
        setTitle("");
        setDescripcion("");
        setLat(0);
        setLng(0);
        
    }
        public static byte [] ImageToByte(InputStream file) throws FileNotFoundException{
        //FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = file.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);      
            }
        } catch (IOException ex) {
        }
        byte[] bytes = bos.toByteArray();
      
     return bytes; 
    }
    public void fileUploadDB(FileUploadEvent event)  {
           UploadedFile uFile = event.getFile();
           
        try {
            MarkerData  x =  (MarkerData) marker.getData();
           
            
            guardarImagenDB( x.getPoint_ID()  ,uFile.getFileName() , ImageToByte(event.getFile().getInputstream()));
           
            
        } catch (IOException ex) {
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        }
           
        }  
        
        
    public void fileUpload(FileUploadEvent event)  {
        
        

        UploadedFile uFile = event.getFile();
        FileOutputStream fos =  null  ;
        InputStream is  =  null ;
        String    sFile  =   "H:\\ImgDatas\\"+event.getFile().getFileName();
        File    fTmp =  new  File(sFile) ;
        if  (fTmp.exists())  {
            fTmp.delete();           
        }
        try {
            fos = new FileOutputStream(fTmp);
            is = uFile.getInputstream();
            byte[] bytes = new byte[1024];
            int size;
            while((size = is.read(bytes)) != -1){
                    fos.write(bytes, 0, size);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if  (fos!= null){
                try { 
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
                }
            }  
            if  (is!= null){
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    

     public void armarArchivo(byte[] bytes, String arquivo) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(arquivo);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
