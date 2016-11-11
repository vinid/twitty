package extractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import com.google.code.uclassify.client.UClassifyClient;
import com.google.code.uclassify.client.UClassifyClientFactory;
import com.uclassify.api._1.responseschema.Classification;

import entity.UserFields;

import com.uclassify.api._1.responseschema.Class;

public class UserClassification {

	final UClassifyClientFactory factory;
	final UClassifyClient client;
	
	public UserClassification() {
		this.factory = UClassifyClientFactory.newInstance("INSERT KEY", null);
		this.client = factory.createUClassifyClient();
	}

	public String getGender(String screenName, String tweetData){

		Map<String, Classification> classifications = this.client.classify("uClassify", "GenderAnalyzer_v5",
				Arrays.asList(tweetData));
		String returnClass = "None";	
		
		for (String text : classifications.keySet()) {

			Classification classification = classifications.get(text);
			double pointClass = -1;
			for (Class clazz : classification.getClazz()) {
				String currentClass = clazz.getClassName();
				double currentPoint = clazz.getP();
				if (clazz.getP() > pointClass) {
					returnClass = currentClass;
					pointClass = currentPoint;
				}
			}
		}
		return returnClass;
	}

	public String getAge(String screenName, String tweetData) {

		Map<String, Classification> classifications = this.client.classify("uClassify", "Ageanalyzer",
				Arrays.asList(tweetData));
		String returnClass = "None";

		for (String text : classifications.keySet()) {

			Classification classification = classifications.get(text);
			double pointClass = -1;
			for (Class clazz : classification.getClazz()) {
				String currentClass = clazz.getClassName();
				double currentPoint = clazz.getP();
				if (clazz.getP() > pointClass) {
					returnClass = currentClass;
					pointClass = currentPoint;
				}
			}
		}
		return returnClass;
	}
	
	public ArrayList<UserFields> buildUserFieldList (ArrayList<UserFields> users) throws Exception {
		for (UserFields user : users) {
			String tweetData = "";
			for (String tweet : user.tweet) {
				tweetData +=  " " + tweet;
			}
			user.gender = this.getGender(user.screenName, tweetData);
			System.out.println("gender");
			user.age = this.getAge(user.screenName, tweetData);
			System.out.println("age");
		}
		return users;
	}
}
