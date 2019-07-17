package com.arc.lucene;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

    private IndexWriter writer;

    public Indexer(String indexDirectoryPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(indexDirectory, iwc);
    }

    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }

    private Document getDocument(Path file, long lastModified) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            Document document = new Document();

            Field pathField = new StringField(Constants.FILE_PATH, file.toString(), Field.Store.YES);
            document.add(pathField);
            document.add(new LongPoint(Constants.MODIFIED, lastModified));
            document.add(new TextField(Constants.CONTENTS, new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));

            return document;
        }
    }

    private void indexFile(Path file, long lastModified) throws IOException {
        Document document = getDocument(file, lastModified);
        writer.addDocument(document);
    }

    public int createIndex(String dataDirPath) throws IOException {

        final Path docDir = Paths.get(dataDirPath);
        if (!Files.isReadable(docDir)) {
            System.exit(1);
        }

        if (Files.isDirectory(docDir)) {
            Files.walkFileTree(docDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        indexFile(file, attrs.lastModifiedTime().toMillis());
                    } catch (IOException ignore) {
                        // don't index
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            indexFile(docDir, Files.getLastModifiedTime(docDir).toMillis());
        }

        return writer.numRamDocs();
    }
}
