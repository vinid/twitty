package irproject;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.GeoPointField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import analyzer.CustomAnalyzer;
import entity.Locator;
import entity.UserFields;

public class IndexCreator {

	private static final String[] boostVars = {"follower"};
	
	private static IndexWriter createIndexWriter() {
		
		Path path = FileSystems.getDefault().getPath("logs", "index");

		/*
		//delete old index
		if (path.toFile().isDirectory())
			for (File file: Arrays.asList(path.toFile().listFiles())) {
				file.delete();
			}
		*/
		Directory index = null;
		try {
			index = new SimpleFSDirectory(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	        
		// map field-name to analyzer
		Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
		analyzerPerField.put("tweet", new CustomAnalyzer());
		 
		// create a per-field analyzer wrapper using the StandardAnalyzer as .. standard analyzer ;)
		PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), analyzerPerField);
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter w = null;
		try {
			w = new IndexWriter(index, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return w;
	}
	
	private static void populateIndex(IndexWriter w, ArrayList<UserFields> users ) {
		
		java.lang.reflect.Field[] fields = UserFields.class.getDeclaredFields();
		
		if (w != null) {
			for (UserFields user : users) {
				Document doc = new Document();
				for (java.lang.reflect.Field field : fields ){
					if (field.getModifiers() == 1) {
						String type = field.getGenericType().toString();
						
						switch (type){
						case "class java.lang.String":
							try {
								if (field.get(user) != null) {
									System.out.println("String attribute: " + field.getName() + " Value: " + (String)field.get(user) );
									doc.add(new TextField(field.getName(), (String)field.get(user), Field.Store.YES));
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
							break;
						case "class entity.Locator":
							try {
								if (field.get(user) != null) {
									System.out.println("Locator attribute: " + field.getName() + " Value (latitude): " + ((Locator)field.get(user)).getLatitude() );
									Locator lc = (Locator) field.get(user);
									doc.add(new GeoPointField("geolocation", lc.getLongitude(), lc.getLatitude(), Field.Store.YES));
									doc.add(new TextField("city", lc.getLocality(), Field.Store.YES));
									doc.add(new TextField("country", lc.getCountry(), Field.Store.YES));
									doc.add(new TextField("address", lc.getAddress(), Field.Store.YES));
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
							break;	
						case "int":
							try {
								System.out.println("Int attribute: " + field.getName() + " Value: " + (int)field.get(user) );
								if (Arrays.asList(boostVars).contains(field.getName())) {
									System.out.println("Faccio boost");
									doc.add(new NumericDocValuesField(field.getName(), (int)field.get(user)));
									doc.add(new StoredField(field.getName(), (int)field.get(user))); //also store info
								} else {
									doc.add(new IntField(field.getName(), (int)field.get(user), Field.Store.YES));
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
							break;
						case "java.util.ArrayList<java.lang.String>":
							try {
								@SuppressWarnings("unchecked")
								ArrayList<String> strings = (ArrayList<String>) field.get(user);
								if (strings != null) {
									System.out.println("List attribute: " + field.getName() + " Value: " + strings );
									for (String string: strings){
										doc.add(new TextField(field.getName(), string, Field.Store.YES));
									}
								}
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							}
							break;
						default:
							throw new IllegalArgumentException("Invalid type " + type);
						}
					}
				}
				try {
					w.addDocument(doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void create(ArrayList<UserFields> users) {
		// Create index
		IndexWriter w = createIndexWriter();
		
		// Populate index
		populateIndex(w, users);
	}
	
}