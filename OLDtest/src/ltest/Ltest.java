/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ltest;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

/**
 *
 * @author jin
 */
public class Ltest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
       
        generateIndex("LargeText","tt");
        listIndex("tt");
     
        
         
        
    }
    
    static void listIndex(String indx){
           try
        {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indx)));
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

   
        // TODO code application logic here
        
        
        
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    static void generateIndex (String source, String dest)
    {
         String indexPath = dest;
         String docsPath = null;
         //boolean create=true;
         docsPath=source;
         final File docDir = new File(docsPath);
         if (!docDir.exists() || !docDir.canRead()) {
         System.out.println("Document directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
         System.exit(1);
         }
         Date start=new Date();
         System.out.println("Indexing to directory '" + indexPath + "'...");

         try{
             Directory dir = FSDirectory.open(new File(indexPath));
             
             Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
             IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
             
             
              IndexWriter writer = new IndexWriter(dir, iwc);
              indexDocs(writer, docDir);
              
              writer.close();

              Date end = new Date();
             
              System.out.println(end.getTime() - start.getTime() + " total milliseconds");
         } 
         catch (Exception e)
         {
             e.printStackTrace();
         }
    }
    
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
  }


}
