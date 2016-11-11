package entity;

import java.util.ArrayList;

/**
 * Class used for the creation of the index in Lucene. Used with reflection.
 */
public class UserFields {
	
	public static final String MALE = "male";
	public static final String FEMALE = "female";
	
	public int follower;
	public String screenName;
	public Locator locator;
	public String profileImageURL;
	public String coverImageURL;
	public String gender;
	public String age;
	public ArrayList<String> tweet = new ArrayList<String>();
	public int numberOfTweets;
	public String name;
	public String description;
	
	
	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	
	
	
}