package analyzer;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class SynonymFilter extends TokenFilter {
    private static final int SELECTION_SIZE = 3;

    private Word2Vec word2Vec;
    private AttributeSource.State current;

    private final CharTermAttribute termAtt;
    private final PositionIncrementAttribute incrAtt;

    private Stack<String> synonymStack;

    SynonymFilter(TokenStream in, Word2Vec w2v) {
        super(in);

        word2Vec = w2v;

        termAtt = addAttribute(CharTermAttribute.class);
        incrAtt = addAttribute(PositionIncrementAttribute.class);

        synonymStack = new Stack<>();
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!synonymStack.isEmpty()) {
            String synonym = synonymStack.pop();
            restoreState(current);
            termAtt.setEmpty().append(synonym);
            incrAtt.setPositionIncrement(0);
            return true;
        }

        if (!input.incrementToken()) {
            return false;
        }

        String word = termAtt.toString();
        Collection<String> synonyms = word2Vec.wordsNearest(word, SELECTION_SIZE);
        if (!synonyms.isEmpty()) {
            synonymStack.addAll(synonyms);
            current = captureState();
        }

        return true;
    }
}
