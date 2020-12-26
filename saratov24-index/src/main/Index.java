package main;

import java.util.*;
import java.io.IOException;
import java.sql.SQLException;

import analyzer.ComplexAnalyzer;
import analyzer.SynonymFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.morphology.russian.RussianAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import dataset.Accessor;

class Index {
    static final int SELECTION_SIZE = 40;

    private Set<Map<String, String>> dataset;
    private Directory index;
    private RussianAnalyzer inputAnalyzer;
    private ComplexAnalyzer complexAnalyzer;
    private StandardAnalyzer queryAnalyzer;

    private static final String NUMERIC_FIELD = "Время";

    Index(String dbPath) throws SQLException, IOException {
        dataset = (new Accessor(dbPath)).selectAll();
        index = new RAMDirectory();
        inputAnalyzer = new RussianAnalyzer();
        queryAnalyzer = new StandardAnalyzer();
        try {
            System.out.println("Initializing word2vec and morphology...");
            complexAnalyzer = new ComplexAnalyzer();
            System.out.println("DONE");
        } catch (IOException e) {
            System.out.println("ERROR: no morphology or word2vec model found");
        }

        makeIndex();
    }

    private Query makeQuery(String search, Analyzer analyzer) throws ParseException {
        try {
            int searchNum = Integer.parseInt(search);
            return IntPoint.newRangeQuery(NUMERIC_FIELD, searchNum, searchNum);
        } catch (NumberFormatException nfe) {
            return new MultiFieldQueryParser(Accessor.getMeaningfulFields(), analyzer).parse(search);
        }
    }

    List<Map<String, String>> searchByQuery(String search, boolean useSynonyms) throws IOException, ParseException {
        IndexReader indexReader = DirectoryReader.open(index);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        List<Map<String, String>> results = new ArrayList<>();
        TopDocs topDocs = indexSearcher.search(makeQuery(search, useSynonyms ? complexAnalyzer : queryAnalyzer), SELECTION_SIZE);
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = indexSearcher.doc(scoreDoc.doc);
            Map<String, String> article = new HashMap<>();
            article.put("id", document.getField("id").stringValue());
            for (String field: Accessor.getMeaningfulFields()) {
                article.put(field, document.getField(field).stringValue());
            }
            results.add(article);
        }
        return results;
    }

    private void addDocTextField(Document doc, Map<String, String> article, String field) {
        doc.add(new TextField(field, article.get(field), Field.Store.YES));
    }

    private void addDocTimeIntField(Document doc, int value) {
        doc.add(new StoredField(NUMERIC_FIELD, value));
        doc.add(new IntPoint(NUMERIC_FIELD, value));
    }

    private void addDocTimeField(Document doc, String value) {
        if (value == null || value.isEmpty()) {
            addDocTimeIntField(doc, 0);
            return;
        }

        String time = value.split(" ", 2)[0];
        try {
            int timeNumber = Integer.parseInt(time);
            addDocTimeIntField(doc, timeNumber);
        } catch (NumberFormatException e) {
            addDocTimeIntField(doc, 0);
        }
    }

    private void makeIndex() throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(inputAnalyzer);
        IndexWriter indexWriter = new IndexWriter(index, indexWriterConfig);

        for (Map<String, String> article: dataset) {
            Document document = new Document();
            int id = Integer.parseInt(article.get("id"));
            document.add(new StoredField("id", id));

            for (String field: Accessor.getFields()) {
                if (field.equals("Время")) {
                    addDocTimeField(document, article.get(field));
                } else {
                    addDocTextField(document, article, field);
                }
            }

            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }
}
