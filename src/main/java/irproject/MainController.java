package irproject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import entity.CustomEntity;
import entity.Search;
import entity.UserFields;
import entity.UserModel;
import extractor.UserClassification;
import twitter.TweetExtractor;
import twitter4j.Status;
import twitter4j.TwitterException;
import util.TextRazorUtil;

@Controller//@RestController
public class MainController {
	
	@RequestMapping(value="/")
    public String index(Model model) {
        return "index";
    }

    @RequestMapping("/build")
    public String build(Model model) {
    	try {
	    	TweetExtractor stream = new TweetExtractor();
	    	System.out.println("stream");
			ArrayList<UserFields> users = stream.execute();
			System.out.println("stream execute");
			UserClassification cinni = new UserClassification();
			ArrayList<UserFields> uf = cinni.buildUserFieldList(users);
			System.out.println("class");
			IndexCreator.create(uf);
			System.out.println("index created");

			ArrayList<UserFields> u = new ArrayList<UserFields>();
			u.addAll(uf);
			
			if (u.size() > 5)
				model.addAttribute("u", u.subList(0, 5));
			else
				model.addAttribute("u", u);
			return "index";
    	} catch(Exception e) {
    		System.out.println(e.getMessage());
    		System.out.println(e.getStackTrace());
	    	return "error";
	    }
    }
    
    @RequestMapping(value="/best-friend", method=RequestMethod.GET)
    public String bestFriendForm(Model model) {
        model.addAttribute("search", new UserFields());
        return "best_friend";
    }
    
    @RequestMapping(value="/best-friend", method=RequestMethod.POST)
    public String bestFriendsearch(@ModelAttribute UserFields search,
    		Model model) throws TwitterException {
    	TweetExtractor twe = new TweetExtractor();
    	TextRazorUtil tru = new TextRazorUtil();
    	List<Status> statuses = twe.getStatuses(search.getScreenName());
    	    	
    	String concSt = "";
    	for (Status s : statuses) {
    		concSt += " " + s.getText();
    	}
    	
    	ArrayList<CustomEntity> ces = tru.getEntities(concSt);

    	if (ces == null)
    		return "noresults";
    	    	
    	HashMap<String, String> ht = new HashMap<String, String>();
		ht.put("tweet", "OR");
		ht.put("gender", "OR");
		ht.put("age", "OR");
		ht.put("geolocation", "OR");
				
		SearchHelper se;
		try {
			se = new SearchHelper(ht);
			ArrayList<UserModel> listTmp = null;
			ArrayList<UserModel> returnList = new ArrayList<UserModel>();
			for (CustomEntity ce : ces) {
				listTmp = se.search("\"" + ce.getMatchedText() + "\"", "", 0,0,0,0,5,(false));
				for (UserModel u1 : listTmp) {
					if (returnList.contains(u1)){
						for (UserModel u2 : returnList) {
							if (u1.getScreenName().equals(u2.getScreenName())) {
								u2.fragments.addAll(u1.getFragments());
								break;
							}
						}
					} else {
						returnList.addAll(listTmp);
					}
				}
			}
			//ArrayList<UserModel> list = se.search(concatEntities, "", 0,0,0,0,5,(false));
	    	
			if (returnList.size() > 0) {
				double maxScore = returnList.get(0).getScore();

			    model.addAttribute("u", returnList);
			    model.addAttribute("max_score", maxScore);
				return "showresults";
			} else {
				return "noresults";
			}
		} catch (IOException | NumberFormatException | ParseException | TwitterException e) {
			e.printStackTrace();
			return "error";
		}
    }
    
    @RequestMapping(value="/search", method=RequestMethod.GET)
    public String searchForm(Model model) {
        model.addAttribute("search", new Search());
        return "search";
    }
    
    @RequestMapping(value="/search", method=RequestMethod.POST)
    public String search(@ModelAttribute Search search,
    		Model model) {
    	
		HashMap<String, String> ht = new HashMap<String, String>();
		ht.put("tweet", (search.isInterestDic() ? "AND" : "OR"));
		ht.put("gender", (search.isGenderDic() ? "AND" : "OR"));
		ht.put("age", (search.isAgeDic() ? "AND" : "OR"));
		ht.put("geolocation", (search.isGeoDic() ? "AND" : "OR"));
				
		SearchHelper se;
		try {
			se = new SearchHelper(ht);
			ArrayList<UserModel> list = se.search(
	    			search.getInterest().toLowerCase().trim(), 
	    			search.getGender().toLowerCase().trim(), 
	    			(search.getAge() != null && !search.getAge().isEmpty() ? Integer.parseInt(search.getAge()) : 0),
	    			(search.getLongitude() != null && !search.getLongitude().isEmpty() ? Double.parseDouble(search.getLongitude()) : 0),
	    			(search.getLatitude() != null && !search.getLatitude().isEmpty() ? Double.parseDouble(search.getLatitude()) : 0),
	    			(search.getRadius() != null && !search.getRadius().isEmpty() ? Integer.parseInt(search.getRadius()) : 0),
	    			(search.getNumber() != null && !search.getNumber().isEmpty() ? Integer.parseInt(search.getNumber()) : 0),
	    			(search.isBoost()));
	    	
			if (list.size() > 0) {
				double maxScore = list.get(0).getScore();

			    model.addAttribute("u", list);
			    model.addAttribute("max_score", maxScore);
				return "showresults";
			} else {
				return "noresults";
			}
		} catch (IOException | NumberFormatException | ParseException | TwitterException e) {
			e.printStackTrace();
			return "error";
		}
    }
}
