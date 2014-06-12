import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;


public class CreateIndex {
	
	public static void createIndex(String textPath, String indd) throws CorruptIndexException, LockObtainFailedException, IOException {
		
		Directory dire = FSDirectory.open(new File(indd));
		
		//Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
        IndexWriter writer = new IndexWriter(dire, iwc);
		
		File dir = new File(textPath);
		File[] files = dir.listFiles();
		
		// iterate all the files
		for (File file : files) {
			Document doc = new Document();

			String path = file.getCanonicalPath();
			
			//file path
			Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
			doc.add(pathField);
			
			FileInputStream fis= new FileInputStream(file);
			
			//content
			
		    doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))));

		    if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		         // New index, so we just add the document (no old document can be there):
                 System.out.println("adding " + file);
		    	     writer.addDocument(doc);
		        } else {
		        	
		        	System.out.println("updating " + file);
	            writer.updateDocument(new Term("path", file.getPath()), doc);
		        	
		        }
		    
		    fis.close();
		        }
	
		writer.close();
		//return ramDirectory;
	}

}
