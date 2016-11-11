package irproject;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import analyzer.CustomAnalyzer;
import entity.UserModel;
import twitter4j.TwitterException;

public class SearchHelper {

	private static IndexSearcher searcher = null;
	Map<String, String> dictionary;
	private Highlighter tweetHighlighter = null;
	private String[] uClassifyRanges = {"13-17", "18-25", "26-35", "36-50", "51-65", "65-100"};
	private IndexReader indexReader;
	/**
	 * Crea un search helper per poter effetture le ricerce
	 * @param ht HashMap con all'interno i campi 
	 * con le rispettive direttive (es: devono essere in and o or i field?)
	 * @throws IOException
	 */
	public SearchHelper(HashMap<String, String> ht) throws IOException {
		Path path = FileSystems.getDefault().getPath("logs", "index");
		dictionary  = ht;
		this.indexReader = DirectoryReader.open(FSDirectory.open(path));
		searcher = new IndexSearcher(indexReader);
	}

	public ArrayList<UserModel> search(String tweet,  String gender, int age, double longitude, double latitude, int d, int n, boolean boost) throws IOException, ParseException, TwitterException {
		
		TopDocs topDocs = this.performSearch(tweet, gender, age, longitude, latitude, d, n, boost);
		System.out.println("sto cercando");
		ScoreDoc[] hits = topDocs.scoreDocs;
				
		// retrieve each matching document from the ScoreDoc array
		ArrayList<UserModel> uml = new ArrayList<UserModel>();
		for (int i = 0; i < hits.length && (n != 0 ? i < n : true); i++) {
			Document doc = searcher.doc(hits[i].doc);
			String name = doc.get("screenName");
			System.out.println(name + " punteggio:  " + hits[i].score + " ");
		    
			UserModel um = new UserModel();
			um.screenName = name;
			um.profileImageURL = doc.get("profileImageURL");
			um.coverImageURL = doc.get("coverImageURL");
			um.follower = Integer.valueOf(doc.get("follower"));
			um.description = doc.get("description");
			um.numberOfTweets = Integer.valueOf(doc.get("numberOfTweets"));
			um.name = doc.get("name");
			um.setScore(hits[i].score);
			uml.add(um);
			System.out.println(um.name);
			
			if (tweetHighlighter != null) {
				IndexableField[] tweets = doc.getFields("tweet");
				for (IndexableField field : tweets) {
	                @SuppressWarnings("resource")
					TokenStream tokenStream = new CustomAnalyzer().tokenStream("", field.stringValue());
	                TextFragment[] fragments = null;
					try {
						fragments = this.tweetHighlighter.getBestTextFragments(tokenStream, field.stringValue(), false, 5);
					} catch (InvalidTokenOffsetsException e) {
						e.printStackTrace();
					}
	
	                for (TextFragment t : fragments) {
	                	if (t.getScore() > 0.0)
	                		um.getFragments().add(t.toString());
	                }
	            }
			}

		}
		return uml;
	}

	public TopDocs performSearch(String tweet,  String gender, int age, double longitude, double latitude, double radius, int n, boolean boost)
			throws IOException, ParseException {
		
		BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
		
		//Query sui tweet
		if (tweet != null && !tweet.isEmpty()){
			QueryParser parser = new QueryParser("tweet", new CustomAnalyzer());
			if (tweet.indexOf("\"") == 0 && tweet.lastIndexOf("\"") == tweet.length()-1){
				parser.setDefaultOperator(Operator.AND);
			}
			Query queryTweet = parser.parse(QueryParser.escape(tweet));
			
			QueryScorer queryScorerTweet = new QueryScorer(queryTweet, "tweet");
	        Fragmenter fragment = new SimpleSpanFragmenter(queryScorerTweet, 200);

	        tweetHighlighter = new Highlighter(queryScorerTweet);
	        tweetHighlighter.setTextFragmenter(fragment);

	        booleanQuery.add(queryTweet, this.getBoolClause("tweet"));
		}
		
		//Query sul genere
		if (gender != null && (!gender.equals("male") || !gender.equals("female"))){
			Query queryGender = new TermQuery(new Term("gender", gender));
			booleanQuery.add(queryGender, this.getBoolClause("gender"));
		}
		
		//Query sull'età
		if (age > 0) {
			String ageToSearch = "";
			for (int i = 0; i < uClassifyRanges.length; ++i) {
				String ageRange = uClassifyRanges[i];
				String[] part = ageRange.split("-");
				if (age >= Integer.valueOf(part[0]) && (age <= Integer.valueOf(part[1]))){
					ageToSearch = uClassifyRanges[i];
					break;
				}
			}
			Query queryAge = new TermQuery(new Term("age", ageToSearch));
			
			booleanQuery.add(queryAge, this.getBoolClause("age"));
		}
		
		//query sulla geolocalizzazione
		if (longitude != 0 && latitude != 0) {
			if (radius <= 0)
				radius = 1;
			GeoPointDistanceQuery queryGeolocation = new GeoPointDistanceQuery("geolocation", longitude, latitude, radius);
			booleanQuery.add(queryGeolocation, this.getBoolClause("geolocation"));
		}
		
		//Controllo numero risultati
		if (n <= 0)
			n = Integer.MAX_VALUE;

		//Query sui follower (boost)
		if (boost) {
			Query q = new CustomScoreQuery(booleanQuery.build(), new FunctionQuery(new LongFieldSource("follower")));
			return searcher.search(q, n);
		} else {
			return searcher.search(booleanQuery.build(), n);
		}
		
	}

	public Document getDocument(int docId) throws IOException {
		return searcher.doc(docId);
	}
	
	private Occur getBoolClause(String field) {
		return (this.dictionary.get(field).equals("AND") ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD);
	}

}
