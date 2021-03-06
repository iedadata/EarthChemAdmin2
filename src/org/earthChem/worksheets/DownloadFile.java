 package org.earthChem.worksheets;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.earthChem.db.DBUtil;
import org.earthChem.db.GMADB;
import org.earthChem.db.SampleDownload;
import org.earthChem.db.postgresql.hbm.StringTable;
//import org.earthChem.dal.ds.Query;
import org.primefaces.event.FileUploadEvent;
 
@ManagedBean(name="downloadFile")
@SessionScoped
public class DownloadFile implements Serializable {
	//convert to text file: org.apache.poi.xssf.extractor.XSSFExcelExtractor
	private String materialType;
	private String [] selectedVariableTypeCodes;
	private String query;
	private Workbook wb;
	private CellStyle headerCellStyle;
	
	public void download_item_codeA_new() {		
		createFile(GMADB.item_codeA_new(), "item_codeA_new.xlsx"); 
	} 
	
	public void download_stations_new() {
		createFile(GMADB.stations_new(), "stations_new.xlsx"); 
	}
	
	public void download_expeditions_new() {
		createFile(GMADB.expeditions_new(), "expeditions_new.xlsx"); 
	}
	
	public void download_pdb_dataC_new() {
		createFile(GMADB.pdb_dataC_new(), "pdb_dataC_new.xlsx"); 
	}
	
	public void download_locations_new() {
		createFile(GMADB.locations_new(), "locations_new.xlsx"); 
	}
	
	
	public void download() {
			String fileName=query+"_"+materialType+".xlsx";			
			String variableType = "";
			
			String condition = null;
			if(query.equals("Basalt")) {
				condition =	" and array_to_string(m.taxon,',') like '%igneous:volcanic:mafic|BASALT%' ";  //BASALT
			} else if (query.equals("Aleutians")) {
				condition =	" and array_to_string(m.geographic_location,',') SIMILAR TO '%ALEUTIAN%|%KAMCHATKA%|%ALASKAN%' ";
			} else if (query.equals("Ophiolite")) {
				condition =	" and array_to_string(m.tectonic_settings,',') like '%OPHIOLITE%' ";
			} else if(query.equals("MeltInclusions")) {
				condition =	" and d.inclusion_type = 'GLASS' ";
			} else if(query.equals("EastPacificRise")) {
				condition =	" and array_to_string(m.geographic_location,',') like '%EAST PACIFIC RISE%' ";
			} else if(query.equals("MantleXenoliths")) {
				condition =	" and array_to_string(m.taxon,',') SIMILAR TO '%PERIDOTITE%|%ECLOGITE%|%DUNITE%|%WEBSTERITE%|%WEHRLITE%|%LHERZOLITE%|%HARZBURGITE%|%PYROXENITE%' ";
			} 
			
			

			if(selectedVariableTypeCodes.length > 0) {
				String codes = "";
				for(int i= 0; i < selectedVariableTypeCodes.length; i++) {
					if(i != 0) codes +=",";
					codes +="'"+selectedVariableTypeCodes[i]+"'";
				}
				variableType = " and t.variable_type_code in ("+codes+") ";
			}
			
			 if("ROCK".equals(materialType)) {
				 materialType = " and d.material_code in ('LE','GL','ROCK','WR')";
			 } else if("ALL".equals(materialType)){
				 materialType = "";
			 } else {
				 materialType = " and d.material_code in ('"+materialType+"')";
			 }		 
			 StringTable table = SampleDownload.getData(materialType, variableType, condition, query);
			 createWorkbook ();
			 WorkbookUtil.getData(table, wb.createSheet("Data"), headerCellStyle);
			 table = SampleDownload.getMethods(materialType,condition,  query);
			 WorkbookUtil.getData(table, wb.createSheet("Methods"), headerCellStyle);
			 table = SampleDownload.getReferences(materialType,condition,  query);
			 WorkbookUtil.getData(table, wb.createSheet("References"), headerCellStyle);
			 WorkbookUtil.getDataMetadata(wb.createSheet("Data Metadata"), headerCellStyle);
			 createFile(fileName);			 
	}

	private void createWorkbook () {
		wb = new XSSFWorkbook();
		Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK.getIndex());     
        headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFont(headerFont);
	}
	
	private void createFile(String fileName)  {
		try {
		FacesContext facesContext = FacesContext.getCurrentInstance();
	    ExternalContext externalContext = facesContext.getExternalContext();
	    externalContext.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");	
	    externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\""+fileName+"\""); 	 
	    wb.write(externalContext.getResponseOutputStream());	
	    facesContext.responseComplete();
	    wb.close();		    
		}
	    catch(Exception e) {
	        e.printStackTrace();
	    }
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



	public String getQuery() {
		return query;
	}



	public void setQuery(String query) {
		this.query = query;
	}

	
	

}