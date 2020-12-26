package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.IOException;

import analyzer.WikiruWordnetAdapter;
import dataset.Accessor;
import org.apache.lucene.queryparser.classic.ParseException;

public class Main {
    private static final String DBPATH = System.getProperty("user.dir") + "/parser/news.db";
    private static final String OUTPATH = System.getProperty("user.dir") + "/resources/5.json";

    private static Index buildIndex() {
        System.out.println("Building index...");
        Index index;
        try {
            System.out.println("Extracting from " + DBPATH);
            index = new Index(DBPATH);
        } catch (SQLException exception) {
            System.out.println("ERROR: Could not access index database");
            System.out.println(exception.getMessage());
            return null;
        } catch (IOException exception) {
            System.out.println("ERROR: Could not build index, refetch articles and try again");
            System.out.println("ERROR: Maybe no morphology or word2vec model found");
            System.out.println(exception.getMessage());
            return null;
        }
        System.out.println("Index was successfuly built!");
        return index;
    }

    private static List<Map<String, String>> handleQuery(Index index, String query, boolean useSynonyms) {
        List<Map<String, String>> results;
        try {
            results = index.searchByQuery(query, useSynonyms);
        } catch (IOException exception) {
            System.out.println("ERROR: Input exception");
            System.out.println(exception.getMessage());
            return null;
        } catch (ParseException exception) {
            System.out.println("ERROR: Could not parse query");
            System.out.println(exception.getMessage());
            return null;
        }

        if (results == null || results.size() == 0) {
            System.out.println("No results were found");
            return null;
        }

        return results;
    }

    private static void fileOutput(StringBuilder contents) {
        BufferedWriter bufferedWriter = null;
        File out = new File(OUTPATH);
        BufferedWriter writer = null;
        try {
            if (!out.exists()) {
                out.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(out));
            writer.append(contents);
            System.out.println("Created at " + OUTPATH);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("ERROR: no writer created");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Index index = buildIndex();
        if (index == null) return;

        Scanner scanner = new Scanner(System.in);
        boolean useSynonyms = false;
        while (true) {
            System.out.print("search $ ");
            String query = scanner.nextLine();

            if (query.toLowerCase().equals("q")) {
                break;
            }

            if (query.toLowerCase().equals("s")) {
                useSynonyms = !useSynonyms;
                System.out.println("Synonyms mode is " + (useSynonyms ? "ON" : "OFF"));
                continue;
            }

            List<Map<String, String>> results = handleQuery(index, query, useSynonyms);
            if (results == null || results.isEmpty()) continue;

            String[] fields = Accessor.getMeaningfulFields();
            int resultsLength = results.size();
            System.out.println("RESULTS: " + resultsLength + " / " + Index.SELECTION_SIZE);

            StringBuilder contents = new StringBuilder("{\n  \"query\": \"" + query + "\",\n  \"results\": [\n");
            int id = 0;
            for (Map<String, String> article: results) {
                id++;
                contents.append("    {\n");
                int fieldId = 0;
                for (String field: fields) {
                    fieldId++;
                    contents.append("      \"")
                            .append(Accessor.getDbField(field))
                            .append("\": \"")
                            .append(article.get(field));
                    contents.append(fieldId != fields.length ? "\"," : "\"")
                            .append("\n");
                }
                contents.append("    }")
                        .append(id != resultsLength ? ",\n" : "\n");
            }
            contents.append("]\n}\n");
            fileOutput(contents);
        }
    }
}
