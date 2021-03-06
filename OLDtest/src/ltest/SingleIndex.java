/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ltest;

/**
 *
 * @author jin
 */
public class SingleIndex {
    
    SingleIndex(String directoryName, String fileName, String indexPath)
    {
         try{
             Directory dir = FSDirectory.open(new File(indexPath)); // write to an directory for checking
             
             Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
             IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_48, analyzer);
             
             
              IndexWriter writer = new IndexWriter(dir, iwc);
              String[] files = directoryName.list();
              
                FileInputStream fis;
                try {
                  fis = new FileInputStream(fileName);
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
                
                
              
              writer.close();

              
         } 
         catch (Exception e)
         {
             e.printStackTrace();
         }
    }
    
}
