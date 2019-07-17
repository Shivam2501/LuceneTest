package com.arc.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

public class Tester {

    Indexer indexer;
    Searcher searcher;

    public static void main(String[] args) {
        String usage =
                "Usage:\tjava com.arc.lucene.Tester [-index INDEX_PATH] [-docs DOCS_PATH] [-query String]";
        if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
            System.out.println(usage);
            System.exit(0);
        }

        String index = "index";
        String doc = "doc";
        String query = null;
        for (int i = 0; i < args.length; i++) {
            if ("-index".equals(args[i])) {
                index = args[i+1];
                i++;
            } else if ("-docs".equals(args[i])) {
                doc = args[i+1];
                i++;
            } else if ("-query".equals(args[i])) {
                query = args[i+1];
                i++;
            }
        }

        Tester tester;
        try {
            tester = new Tester();
            tester.createIndex(index, doc);
            tester.search(index, query);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void createIndex(String indexDir, String dataDir) throws IOException {
        indexer = new Indexer(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(dataDir);
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed + " File Indexed, time taken: " + (endTime - startTime) + " ms");
    }

    private void search(String indexDir, String searchQuery) throws IOException, ParseException {
        searcher = new Searcher(indexDir);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();

        System.out.println(hits.totalHits + " documents found. Time: " + (endTime - startTime));

        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("File: " + doc.get(Constants.FILE_PATH));
        }
        searcher.close();
    }
}
