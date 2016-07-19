
import java.io.Serializable;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Henry
 */
public class MarkerData implements Serializable{

    private String description;
    private int point_ID =0;

    public int getPoint_ID() {
        return point_ID;
    }

    public void setPoint_ID(int point_ID) {
        this.point_ID = point_ID;
        
    }
    

     MarkerData(int _pointID , String _descripcion) {
        this.description = _descripcion ;
        this.point_ID=  _pointID;
    }
    public String getDescription() {
        return  description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
