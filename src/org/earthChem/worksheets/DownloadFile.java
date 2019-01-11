 package org.earthChem.worksheets;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.poi.ss.usermodel.Workbook;
import org.earthChem.db.DBUtil;
import org.earthChem.db.postgresql.hbm.StringTable;
//import org.earthChem.dal.ds.Query;
import org.primefaces.event.FileUploadEvent;
 
@ManagedBean(name="downloadFile")
@SessionScoped
public class DownloadFile implements Serializable {
	
	private String materialType;
	private String [] selectedVariableTypeCodes;
	
	public void download() {
			String fileName=materialType+".xlsx";			
			String condition = "";
			if(selectedVariableTypeCodes!=null && selectedVariableTypeCodes.length >0) {
				String codes = "";
				for(int i= 0; i < selectedVariableTypeCodes.length; i++) {
					if(i != 0) codes +=",";
					codes +="'"+selectedVariableTypeCodes[i]+"'";
				}
				condition = " and t.variable_type_code in ("+codes+") ";
			}
		
			createFile(DBUtil.sampleDownload(materialType, condition), fileName);
	}

	

	 private void createFile(StringTable table, String fileName)  {
		 	Workbook workbook = WorkbookUtil.databaseSheet(table, "database");
			try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
		    ExternalContext externalContext = facesContext.getExternalContext();
		    externalContext.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");	
		    externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\""+fileName+"\""); 	 
		    workbook.write(externalContext.getResponseOutputStream());	
		    facesContext.responseComplete();
		    workbook.close();		    
			}
		    catch(Exception e) {
		        e.printStackTrace();
		    }
	}


	public String []  getVariableTypeCodes() {		
		return DBUtil.stringArray("select  variable_type_code from variable_type order by variable_type_code");
	}

	public String getMaterialType() {
		return materialType;
	}



	public void setMaterialType(String materialType) {
		this.materialType = materialType;
	}



	public String[] getSelectedVariableTypeCodes() {
		return selectedVariableTypeCodes;
	}



	public void setSelectedVariableTypeCodes(String[] selectedVariableTypeCodes) {
		this.selectedVariableTypeCodes = selectedVariableTypeCodes;
	}


	

}