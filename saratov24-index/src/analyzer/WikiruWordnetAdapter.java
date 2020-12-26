package analyzer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class WikiruWordnetAdapter {
    private static final String PATH = "synonyms/";
    private String script;
    private String interpreter;

    public WikiruWordnetAdapter() throws IOException {
        script = Paths.get(PATH + "main.py").toString();
        interpreter = Paths.get(PATH + "venv/bin/python").toString();
        if (!(new File(interpreter)).exists()) {
            throw new IOException();
        }
    }

    public List<String> getSynonyms(String word, int number) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(interpreter, script, word, "" + number);
        Process p = pb.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        List<String> synonyms = new ArrayList<>();
        while ((line = in.readLine()) != null) {
            synonyms.add(line);
        }

        return synonyms;
    }
}
