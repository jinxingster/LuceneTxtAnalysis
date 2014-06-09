/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ltest;

/**
 *
 * @author jin
 */
class FacetLuceneSearcher {
    private static Version LUCENE_VERSION = Version.LUCENE_48;
    public static void main(String args[]) throws Exception {
        if (args.length != 3) {
            System.err.println("Parameters: [index directory] [taxonomy directory] [query]");
            System.exit(1);
        }
 
        String indexDirectory = args[0];
        String taxonomyDirectory = args[1];
        String query = args[2];
 
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDirectory)));
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
 
        TaxonomyReader taxonomyReader = new DirectoryTaxonomyReader(FSDirectory.open(new File(taxonomyDirectory)));
 
        FacetSearchParams searchParams = new FacetSearchParams(new DefaultFacetIndexingParams());
        searchParams.addFacetRequest(new CountFacetRequest(new CategoryPath("author"), 100));
        searchParams.addFacetRequest(new CountFacetRequest(new CategoryPath("book_category"), 100));
 
        ComplexPhraseQueryParser queryParser = new ComplexPhraseQueryParser(LUCENE_VERSION, "title", new StandardAnalyzer(LUCENE_VERSION));
        Query luceneQuery = queryParser.parse(query);
 
        // Collectors to get top results and facets
        TopScoreDocCollector topScoreDocCollector = TopScoreDocCollector.create(10, true);
        FacetsCollector facetsCollector = new FacetsCollector(searchParams, indexReader, taxonomyReader);
        indexSearcher.search(luceneQuery, MultiCollector.wrap(topScoreDocCollector, facetsCollector));
        System.out.println("Found:");
 
        for(ScoreDoc scoreDoc: topScoreDocCollector.topDocs().scoreDocs) {
            Document document = indexReader.document(scoreDoc.doc);
            System.out.printf("- book: id=%s, title=%s, book_category=%s, authors=%s, score=%f\n",
                    document.get("id"), document.get("title"),
                    document.get("book_category"),
                    document.get("authors"),
                    scoreDoc.score);
        }
 
        System.out.println("Facets:");
        for(FacetResult facetResult: facetsCollector.getFacetResults()) {
            System.out.println("- " + facetResult.getFacetResultNode().getLabel());
            for(FacetResultNode facetResultNode: facetResult.getFacetResultNode().getSubResults()) {
                System.out.printf("    - %s (%f)\n", facetResultNode.getLabel().toString(),
                        facetResultNode.getValue());
                for(FacetResultNode subFacetResultNode: facetResultNode.getSubResults()) {
                    System.out.printf("        - %s (%f)\n", subFacetResultNode.getLabel().toString(),
                            subFacetResultNode.getValue());
                }
            }
        }
        taxonomyReader.close();
        indexReader.close();
    }
}
