package org.earthChem.presentation.jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.earthChem.rest.CitationRest;
import org.earthChem.db.CitationDB;
import org.earthChem.db.CitationList;
import org.earthChem.db.CitationPurge;
import org.earthChem.db.DBUtil;
import org.earthChem.db.EquipmentDB;
import org.earthChem.db.ExpeditionDB;
import org.earthChem.db.MethodDB;
import org.earthChem.db.OrganizationDB;
import org.earthChem.db.postgresql.hbm.Affiliation;
import org.earthChem.db.postgresql.hbm.AuthorList;
import org.earthChem.db.postgresql.hbm.Citation;
import org.earthChem.db.postgresql.hbm.EcStatusInfo;
import org.earthChem.db.postgresql.hbm.Equipment;
import org.earthChem.db.postgresql.hbm.Expedition;
import org.earthChem.db.postgresql.hbm.Method;
import org.earthChem.db.postgresql.hbm.Organization;
import org.earthChem.db.postgresql.hbm.SamplingFeature;
import org.earthChem.rest.InvalidDoiException;
import org.earthChem.presentation.jsf.theme.CommentService;
import org.earthChem.presentation.jsf.theme.EquipmentService;
import org.earthChem.presentation.jsf.theme.OrganizationService;
import org.earthChem.presentation.jsf.theme.Theme;
import org.earthChem.presentation.jsf.theme.ThemeService;
import org.primefaces.PrimeFaces;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.TabCloseEvent;

 
@ManagedBean(name="checkViewBean")
@SessionScoped
public class CheckViewBean implements Serializable {

	private List<SamplingFeature> noParentAnalyzed; 
	private List<SamplingFeature> noChildStation; 
	private List<SamplingFeature> noParentSpecimen; 
	private List<SamplingFeature> noChildSpecimen; 
	private List<SamplingFeature> dupStation; 
	private List<Citation> citationList;
	
	
	private void updateMsg(String status) {
		if(status == null) 
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "INFO:", "The data were deleted!"));
		else 
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", status));
	}
	
	public void delete(Integer samplingFeatureNum) {
		List<String> qs = new ArrayList<String>();			
		qs.add("DELETE FROM sampling_feature_external_identifier WHERE sample_feature_num="+samplingFeatureNum);
		qs.add("DELETE FROM sampling_feature_taxonomic_classifier WHERE sampling_feature_num="+samplingFeatureNum);
		qs.add("DELETE FROM specimen WHERE sampling_feature_num="+samplingFeatureNum);				
		qs.add("DELETE FROM sampling_feature_annotation WHERE sampling_feature_num="+samplingFeatureNum);		
		qs.add("DELETE FROM feature_of_interest WHERE sampling_feature_num="+samplingFeatureNum);
		qs.add("DELETE FROM feature_action WHERE sampling_feature_num="+samplingFeatureNum);
		qs.add("DELETE FROM related_feature WHERE related_sampling_feature_num="+samplingFeatureNum);
		qs.add("DELETE FROM related_feature WHERE sampling_feature_num="+samplingFeatureNum);
		qs.add("DELETE FROM sampling_feature WHERE sampling_feature_num="+samplingFeatureNum);
		updateMsg(DBUtil.updateList(qs));
	}
/*	
	public void deleteNoParentAnalyzed(Integer samplingFeatureNum) {
		DBUtil.update("DELETE FROM sampling_feature WHERE sampling_feature_num="+samplingFeatureNum);
	}
	
	public void deleteNoChildStation(Integer samplingFeatureNum) {
		DBUtil.update("DELETE FROM sampling_feature WHERE sampling_feature_num="+samplingFeatureNum);
	}
	
	public void deleteNoParentSpecimen(Integer samplingFeatureNum) {
		DBUtil.update("DELETE FROM sampling_feature WHERE sampling_feature_num="+samplingFeatureNum);
	}
	
	public void deleteNoChildSpecimen(Integer samplingFeatureNum) {
		DBUtil.update("DELETE FROM sampling_feature WHERE sampling_feature_num="+samplingFeatureNum);
	}
*/	
	public void deleteAll(List<SamplingFeature> list) {
		for(SamplingFeature sf: list) {
			delete(sf.getSamplingFeatureNum());
		}
		
	}
	
	public void deleteDupStation(Integer relatedFeatureNum) {
		updateMsg(DBUtil.update("DELETE FROM related_feature WHERE related_feature_num="+relatedFeatureNum));
	}
	
	public List<SamplingFeature> getNoParentAnalyzed() {
		String q = "select vas.sampling_feature_num, vas.sampling_feature_type_num, vas.sampling_feature_code from v_analyzed_samples vas "+
				" where vas.sampling_feature_num not in (select distinct sampling_feature_num from related_feature rf where rf.relationship_type_num=9) ";
		noParentAnalyzed = getList(q);
		return noParentAnalyzed;
	}
	
	
	public List<SamplingFeature> getNoChildStation() {
		String q = "select  vas.sampling_feature_num, vas.sampling_feature_type_num, vas.sampling_feature_code from v_stations vas "+
				" where vas.sampling_feature_num not in (select distinct related_sampling_feature_num from related_feature rf where rf.relationship_type_num=22 )";
		noChildStation = getList(q);
		return noChildStation;
	}

	public List<SamplingFeature> getNoParentSpecimen() {
		String q = "select  vas.sampling_feature_num, vas.sampling_feature_type_num, vas.sampling_feature_code from v_specimens vas "+
				" where vas.sampling_feature_num not in (select distinct sampling_feature_num from related_feature rf where rf.relationship_type_num=22)";
		noParentSpecimen = getList(q);
		return noParentSpecimen;
	}
	
	public List<SamplingFeature> getNoChildSpecimen() {
		String q = "select vas.sampling_feature_num, vas.sampling_feature_type_num, vas.sampling_feature_code from v_specimens vas "+
				" where vas.sampling_feature_num not in (select distinct related_sampling_feature_num from related_feature rf where rf.relationship_type_num=9)";
		noChildSpecimen = getList(q);
		return noChildSpecimen;
	}
	
	private List<SamplingFeature> getList(String q) {
		List<SamplingFeature> list =  new ArrayList<SamplingFeature>();
		List<Object[]> olist = DBUtil.getECList(q);
		for(Object[] i: olist) {
			SamplingFeature s = new SamplingFeature();
			s.setSamplingFeatureNum((Integer)i[0]);
			s.setSamplingFeatureTypeNum((Integer)i[1]);
			s.setSamplingFeatureCode(""+i[2]);
			list.add(s);
		}
		return list;
	}
	
	public List<SamplingFeature> getDupStation() {
		String q = "select r.related_feature_num, c.sampling_feature_num, c.sampling_feature_code, p.sampling_feature_num, p.sampling_feature_code, r.relationship_type_num " + 
				" from related_feature r, sampling_feature c, sampling_feature p " + 
				" where r.sampling_feature_num = c.sampling_feature_num and p.sampling_feature_num = r.related_sampling_feature_num and c.sampling_feature_num in" + 
				" (select sampling_feature_num from related_feature where relationship_type_num = 22 group by sampling_feature_num having count(*) > 1) order by c.sampling_feature_num";
		List<SamplingFeature> list =  new ArrayList<SamplingFeature>();
		List<Object[]> olist = DBUtil.getECList(q);
		for(Object[] i: olist) {
			SamplingFeature s = new SamplingFeature();
			s.setRelatedFeatureNum((Integer)i[0]);
			s.setSamplingFeatureNum((Integer)i[1]);
			s.setSamplingFeatureCode(""+i[2]);
			s.setParentNum((Integer)i[3]);
			s.setParentCode(""+i[4]);
		    s.setRelationShipType((Integer)i[5]);
			list.add(s);
		}
		return list;
	}
	
	public List<Citation> getCitationList() {
		String q = "select c.citation_num, c.title "+
		" from ec_status_info e, citation c "+
		" where e.data_status in ('COMPLETED','DATA_LOADED') and e.citation_num = c.citation_num and c.citation_num not in "+
		" (select c.citation_num "+
		" from ec_status_info e, citation c, author_list a "+
		" where e.data_status in ('COMPLETED','DATA_LOADED') and e.citation_num = c.citation_num and e.citation_num = a.citation_num)";
		List<Citation> list = new ArrayList<Citation>();
		List<Object[]> olist = DBUtil.getECList(q);
		for(Object[] i: olist) {
			Citation c = new Citation((Integer)i[0]);
			c.setTitle(""+i[1]);
			list.add(c);
		}
		return list;
		
	}
}