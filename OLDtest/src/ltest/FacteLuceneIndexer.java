/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ltest;

/**
 *
 * @author jin
 */
class FacetLuceneIndexer {
    private static Version LUCENE_VERSION = Version.LUCENE_48;
    public static void main(String args[]) throws Exception {
        if (args.length != 3) {
            System.err.println("Parameters: [index directory] [taxonomy directory] [txt file]");
            System.exit(1);
        }
 
        String indexDirectory = args[0];
        String taxonomyDirectory = args[1];
        String textFileName = args[2];
 
        IndexWriterConfig writerConfig = new IndexWriterConfig(LUCENE_VERSION, new WhitespaceAnalyzer(LUCENE_VERSION));
        writerConfig.setOpenMode(OpenMode.CREATE);
        IndexWriter indexWriter = new IndexWriter(FSDirectory.open(new File(indexDirectory)), writerConfig);
 
        TaxonomyWriter taxonomyWriter = new DirectoryTaxonomyWriter(new MMapDirectory(new File(taxonomyDirectory)), OpenMode.CREATE);
        CategoryDocumentBuilder categoryDocumentBuilder = new CategoryDocumentBuilder(taxonomyWriter, new DefaultFacetIndexingParams());
 
        String content = IOUtils.toString(new FileInputStream(jsonFileName));
        JSONArray bookArray = new JSONArray(content);
 
        Field idField = new IntField("id", 0, Store.YES);
        Field titleField = new TextField("title", "", Store.YES);
        Field authorsField = new TextField("authors", "", Store.YES);
        Field bookCategoryField = new TextField("book_category", "", Store.YES);
 
        for(int i = 0 ; i < bookArray.length() ; i++) {
            Document document = new Document();
 
            JSONObject book = bookArray.getJSONObject(i);
            int id = book.getInt("id");
            String title = book.getString("title");
            String bookCategory = book.getString("book_category");
 
            List categoryPaths = new ArrayList();
            String authorsString = "";
            JSONArray authors = book.getJSONArray("authors");
            for(int j = 0 ; j < authors.length() ; j++) {
                String author = authors.getString(j);
                if (j > 0) {
                    authorsString += ", ";
                }
                categoryPaths.add(new CategoryPath("author", author));
                authorsString += author;
            }
            categoryPaths.add(new CategoryPath("book_category" + bookCategory, '/'));
            categoryDocumentBuilder.setCategoryPaths(categoryPaths);
            categoryDocumentBuilder.build(document);
 
            idField.setIntValue(id);
            titleField.setStringValue(title);
            authorsField.setStringValue(authorsString);
            bookCategoryField.setStringValue(bookCategory);
 
            document.add(idField);
            document.add(titleField);
            document.add(authorsField);
            document.add(bookCategoryField);
 
            indexWriter.addDocument(document);
 
            System.out.printf("Book: id=%d, title=%s, book_category=%s, authors=%s\n",
                id, title, bookCategory, authors);
        }
        taxonomyWriter.commit();
        taxonomyWriter.close();
 
        indexWriter.commit();
        indexWriter.close();
    }
}