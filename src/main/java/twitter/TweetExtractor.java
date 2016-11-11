package twitter;

import twitter4j.Paging;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import util.GoogleMapsLocator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import entity.Locator;
import entity.UserFields;

public class TweetExtractor {

	private final int TOTALUSER = 100;
	
	private final Object lock = new Object();
	private static GoogleMapsLocator gml;
	private static ConfigurationBuilder cb;
	private static Twitter twitter;
	private static Configuration c;
	
	public TweetExtractor(){
		gml = new GoogleMapsLocator();
		cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey("INSERT KEY")
				.setOAuthConsumerSecret("INSERT KEY")
				.setOAuthAccessToken("INSERT KEY-INSERT KEY")
				.setOAuthAccessTokenSecret("INSERT KEY");
		c = cb.build();
		twitter = new TwitterFactory(c).getInstance();
	}
	
	public  List<Status>  getStatuses(String screenName){
		Paging paging = new Paging(1, 200);
		List<Status> statuses = null;
		try {
			statuses = twitter.getUserTimeline(screenName, paging);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return statuses;
	}
	
	public String getDescription(String screenName) throws TwitterException {
		User user = twitter.showUser(screenName);
		return user.getDescription();
	}
		
	public ArrayList<UserFields> execute() throws TwitterException {

		final HashSet<UserFields> users = new HashSet<UserFields>();

		TwitterStream twitterStream = new TwitterStreamFactory(c).getInstance();

		StatusListener listener = new StatusListener() {

			public void onStatus(Status status) {
				// Prendo l'utente
				User user = status.getUser();
				// Creo lo UserFields
				UserFields uf = new UserFields();
				
				// Se abbiamo trovato un utente proviamo a salvarlo nell'hashset
				System.out.println("Questo utente: " + user.getLocation() + " " + user.getLang() + " " + user.getStatusesCount());
				
				// Verifica della location
				String location = user.getLocation(); //location di Twitter
				Locator l = null;
				if (location != null && !location.isEmpty())
					l = gml.getLocationData(location); // location di Google
				
				// Riempio UserFields
				if (l != null && (user.getStatusesCount() > 200) && (user.getLang().equals("en"))) {
					
					// Username
					String screenName = user.getScreenName();
					
					// Recupero i 200 stati e riempio Tweets
					List<Status> statuses = getStatuses(screenName);
					ArrayList <String> tweets = new ArrayList<String>();
					for (Status s : statuses) {
						tweets.add(s.getText());
					}
					
					//Setto tutti i parametri
					uf.screenName = screenName;
					uf.profileImageURL = user.getOriginalProfileImageURL();
					uf.coverImageURL = (user.getProfileBannerURL() != null ? user.getProfileBannerURL() : user.getProfileBackgroundImageURL());
					uf.follower = user.getFollowersCount();
					uf.description = user.getDescription();
					uf.numberOfTweets = user.getStatusesCount();
					uf.name = user.getName();
					uf.tweet = tweets;
					uf.locator = l;
					
					users.add(uf);
					System.out.println("Adesso abbiamo " + users.size() + " utenti");
				}

				if (users.size() >= TOTALUSER) {
					synchronized (lock) {
						lock.notifyAll();
					}
					System.out.println("unlocked");
				}
			}

			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
			}

			public void onScrubGeo(long userId, long upToStatusId) {
				System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}

			public void onStallWarning(StallWarning sw) {
				System.out.println(sw.getMessage());

			}
		};

		twitterStream.addListener(listener);
		twitterStream.sample();

		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("returning users");
		twitterStream.shutdown();
		
		ArrayList<UserFields> auf = new ArrayList<UserFields>();
		auf.addAll(users);
		return auf;
	}
}
