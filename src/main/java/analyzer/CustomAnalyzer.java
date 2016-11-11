package analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceCharFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CustomAnalyzer extends Analyzer {
	
    public static final String ENGLISH = "a about above after again against all am an and any are aren't as at be because been before being below between both but by can't cannot could couldn't did didn't do does doesn't doing don't down during each few for from further had hadn't has hasn't have haven't having he he'd he'll he's her here here's hers herself him himself his how how's i i'd i'll i'm i've if in into is isn't it it's its itself let's me more most mustn't my myself no nor not of off on once only or other ought our ours ourselves out over own same shan't she she'd she'll she's should shouldn't so some such than that that's the their theirs them themselves then there there's these they they'd they'll they're they've this those through to too under until up very was wasn't we we'd we'll we're we've were weren't what what's when when's where where's which while who who's whom why why's with won't would wouldn't you you'd you'll you're you've your yours yourself yourselves ";

	
    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        CharFilter cf;

        cf = new PatternReplaceCharFilter(Pattern.compile("#"), "__________", reader); // replace #;
        cf = new PatternReplaceCharFilter(Pattern.compile("@"), "___________", cf); // replace @ 
        cf = new PatternReplaceCharFilter(Pattern.compile("(http(s?)://t.co/[0-9A-Za-z]+)"), "", cf); // replace links (only twitter link)
        cf = new PatternReplaceCharFilter(Pattern.compile("[0-9]{18}"), "", cf); //replace tweet id

        return cf;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {

        Tokenizer tokenizer = new StandardTokenizer();
        TokenStream ts;
        
        ts = new PatternReplaceFilter(tokenizer, Pattern.compile("___________"), "@", true); // restore previous @
        ts = new PatternReplaceFilter(ts, Pattern.compile("__________"), "#", true); // restore previous #
        
        ts = new StandardFilter(ts); //normalizes tokens extracted
        ts = new EnglishPossessiveFilter(ts); //removes possessives (trailing 's) from words
        ts = new LowerCaseFilter(ts); //required also by PorterStemFilter
        ts = new StopFilter(ts, CharArraySet.copy(StandardAnalyzer.STOP_WORDS_SET));
        
        List<String> stopWords = new ArrayList <String> ();
        stopWords.addAll(Arrays.asList(ENGLISH.split(" ")));
        ts = new StopFilter(ts, StopFilter.makeStopSet(stopWords)); //custom stop words list
        
        ts = new PorterStemFilter(ts); //stemming
        ts = new LengthFilter(ts, 3, 25); //n-char filter

        return new TokenStreamComponents(tokenizer, ts);
    }
}