/**
 * 
 */

/**
 * @author jin
 *
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class ttest {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		System.out.println("The memory test of Lucene memory index!!");
		
		Directory directory = new RAMDirectory();  
		geneIndex(directory);
		listIndex(directory);
		
	}
	
	public static void geneIndex(Directory dire)
	{
		try{
			final File docDir = new File("../LargeText");
			
			//is the text directory right?
			
			 if (!docDir.exists() || !docDir.canRead()) {
				System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
				System.exit(1);
			   }
			
			// Directory dire = FSDirectory.open(new File("inddd"));
			 Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
			 IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
			 IndexWriter writer = new IndexWriter(dire, iwc);
			 System.out.println("Lucene OK!!");
			 // generate the indexes
			 
			 indexDocs(writer, docDir);
			 writer.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		

	}
	
	
	// the index writer method
	
	 static void indexDocs(IndexWriter writer, File file) throws IOException {
		    // do not try to index files that cannot be read
		    if (file.canRead()) {
		      if (file.isDirectory()) {
		        String[] files = file.list();
		        // an IO error could occur
		        if (files != null) {
		          for (int i = 0; i < files.length; i++) {
		            indexDocs(writer, new File(file, files[i]));
		          }
		        }
		      } else {

		        FileInputStream fis;
		        try {
		          fis = new FileInputStream(file);
		        } catch (FileNotFoundException fnfe) {
		          // at least on windows, some temporary files raise this exception with an "access denied" message
		          // checking if the file can be read doesn't help
		          return;
		        }

		        try {

		          // make a new, empty document
		          Document doc = new Document();

		          // Add the path of the file as a field named "path".  Use a
		          // field that is indexed (i.e. searchable), but don't tokenize 
		          // the field into separate words and don't index term frequency
		          // or positional information:
		          Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
		          doc.add(pathField);

		          // Add the last modified date of the file a field named "modified".
		          // Use a LongField that is indexed (i.e. efficiently filterable with
		          // NumericRangeFilter).  This indexes to milli-second resolution, which
		          // is often too fine.  You could instead create a number based on
		          // year/month/day/hour/minutes/seconds, down the resolution you require.
		          // For example the long value 2011021714 would mean
		          // February 17, 2011, 2-3 PM.
		          doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));

		          // Add the contents of the file to a field named "contents".  Specify a Reader,
		          // so that the text of the file is tokenized and indexed, but not stored.
		          // Note that FileReader expects the file to be in UTF-8 encoding.
		          // If that's not the case searching for special characters will fail.
		          doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))));

		          if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
		            // New index, so we just add the document (no old document can be there):
		            System.out.println("adding " + file);
		            writer.addDocument(doc);
		          } else {
		            // Existing index (an old copy of this document may have been indexed) so 
		            // we use updateDocument instead to replace the old one matching the exact 
		            // path, if present:
		            System.out.println("updating " + file);
		            writer.updateDocument(new Term("path", file.getPath()), doc);
		          }
		          
		        } finally {
		          fis.close();
		        }
		      }
		    }
		    
		    // list index
		    
		    
		    
	 }

	 //list index
	 
	 public  static void listIndex (Directory dir) {
         try
      {
          IndexReader reader = IndexReader.open(dir);
        		  //DirectoryReader.open(FSDirectory.open(new File(indx)));
          //System.out.println(reader.getSumTotalTermFreq("women"));
          Fields fields = MultiFields.getFields(reader);
         for(String field : fields) {
              Terms terms = fields.terms(field);
              TermsEnum termsEnum = terms.iterator(null);
              BytesRef text;
              while((text = termsEnum.next()) != null) {
                  Term termInstance = new Term("contents", text.utf8ToString());
                  long termFreq = reader.totalTermFreq(termInstance);
                  long docCount = reader.docFreq(termInstance);
                  System.out.println("field=" + field + "; text=" + text.utf8ToString()+"; termFreq="+termFreq +"; docCount=" + docCount);
                 
            }
          }
       
         reader.close();  
      }            

 
      catch (Exception ex)
      {
          ex.printStackTrace();
      }
  }

}
	 

