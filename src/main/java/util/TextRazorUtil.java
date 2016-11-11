package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.textrazor.AnalysisException;
import com.textrazor.NetworkException;
import com.textrazor.TextRazor;
import com.textrazor.annotations.AnalyzedText;
import com.textrazor.annotations.Entity;
import com.textrazor.annotations.Response;

import entity.CustomEntity;

public class TextRazorUtil {
	
	TextRazor client = new TextRazor("INSERT KEY");
	
	public TextRazorUtil(){
		client.addExtractor("entities");
	}
	
	private Response analyze(String input) {
		AnalyzedText response = null;
		try {
			response = client.analyze(input);
			return response.getResponse();
		} catch (NetworkException | AnalysisException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	public ArrayList<CustomEntity> getEntities(String input) {
		Response response = this.analyze(input);
		if (response == null)
			return null;
		
		List<Entity> entities = response.getEntities();
		if (entities == null)
			return null;
		
		ArrayList<CustomEntity> returnEntities = new ArrayList<CustomEntity>();

		for (Entity e : entities) {
			CustomEntity me = new CustomEntity(e.getConfidenceScore(), e.getMatchedText());
			me.setMatchedText(me.getMatchedText().replaceAll("((http(s?))+:\\/\\/)?[\\w|-]+(\\.([\\w|-]+))+(([|\\/|\\?|&|=|\\.|\\!|\\#|\\+]*[\\w|-]+)*(\\/|\\;)*)*", ""));
			if (!me.getMatchedText().trim().isEmpty() && me.getConfidenceScore() > 2 && !me.getMatchedText().contains("http"))
				returnEntities.add(me);
		}
		
		Set<CustomEntity> noDup = new HashSet<CustomEntity>();
		noDup.addAll(returnEntities);
		returnEntities.clear();
		returnEntities.addAll(noDup);
		Collections.sort(returnEntities);
		return returnEntities;
	}
	
	public List<CustomEntity> getEntities(String input, int n) {
		ArrayList<CustomEntity> ce = this.getEntities(input);
		if (ce == null)
			return null;
		List<CustomEntity> returnEntities = new ArrayList<CustomEntity>();
		returnEntities.addAll(ce);
		if (returnEntities.size() > n)
			return returnEntities.subList(0, n);
		return returnEntities;
	}
}
