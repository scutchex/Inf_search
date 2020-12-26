package analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.analyzer.MorphologyFilter;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;

import java.io.File;
import java.io.IOException;

public class ComplexAnalyzer extends Analyzer {
    // https://wikipedia2vec.github.io/wikipedia2vec/pretrained/#russian
    private static final String PATH = "resources/W2V.bin";

    private LuceneMorphology luceneMorph;
    private Word2Vec word2Vec;

    public ComplexAnalyzer() throws IOException {
        luceneMorph = new RussianLuceneMorphology();
        word2Vec = WordVectorSerializer.readWord2VecModel(PATH);
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        StandardTokenizer st = new StandardTokenizer();
        TokenStream ts = new LowerCaseFilter(st);
        ts = new MorphologyFilter(ts, luceneMorph);
        ts = new SynonymFilter(ts, word2Vec);
        return new TokenStreamComponents(st, ts);
    }
}
