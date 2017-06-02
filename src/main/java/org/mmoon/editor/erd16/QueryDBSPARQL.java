package org.mmoon.editor.erd16;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.GraphStore;
import org.apache.jena.update.GraphStoreFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

/**
 * Class which implements the methods for searching a string in the database,
 * searching for properties and objects related to a special subject and defines a method
 * to insert new triples
 */
@SuppressWarnings("deprecation")
public class QueryDBSPARQL {
        /**
         * Final string as the query prefix with all prefixes defined in the database
         */
        public static final String QUERY_PREFIX =
                        "prefix : <http://mmoon.org/lang/deu/inventory/og/>\n" +
                        "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
                        "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                        "prefix xml: <http://www.w3.org/XML/1998/namespace>\n" +
                        "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "prefix gold: <http://purl.org/linguistics/gold/>\n" +
                        "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                        "prefix mmoon: <http://mmoon.org/mmoon/>\n" +
                        "prefix deu_schema: <http://mmoon.org/lang/deu/schema/og/>\n" +
                        "prefix deu_inventory: <http://mmoon.org/lang/deu/inventory/og/>\n" +
                        "base <http://mmoon.org/lang/deu/inventory/og/>\n" +
                        "\n";

        /**
         * Static string to define the deu_inventory namespace
         */
        public static final String DEU_INVENTORY_NAME_SPACE = "deu_inventory:";

        /**
         * Static string to define the deu_schema namespace
         */
        public static final String DEU_SCHEMA_NAME_SPACE = "deu_schema:";

        /**
         * Static string to define the core namespace
         */
        public static final String CORE_NAME_SPACE = "mmoon:";

        /**
         * List with all found objects in terms of query
         */
        private ArrayList<String> answersObject;

        /**
         * List with all found properties in terms of query
         */
        private ArrayList<String> answersProperty;

        /**
         * List with all found objects in terms of query
         */
        private ArrayList<String> answersSubject;

        /**
         * List with all properties which are not set
         */
        private ArrayList<String> emptyProperty;

        /**
         * String to the TDB directory for synchronized access
         */
        private String tdbDirectory;

        /**
         * TDB dataset where the database is loaded in. TDB is a component
         * of JENA for storage and query a database
         */
        private Dataset dataset;

        public static final String TDB_PATH = Configuration.tdb_path;

        //public static final String TTL_PATH = Configuration.ttl_path;

        public static final Map<String, String> ONT_Sources = Configuration.ont_sources;


        /**
         * Constructor which sets the model with a default path
         */
        public QueryDBSPARQL(){
                tdbDirectory = TDB_PATH;

                //Create TDB dataset
                dataset = TDBFactory.createDataset(tdbDirectory);

                //Initialize all lists
                answersSubject = new ArrayList<String>();
                answersProperty = new ArrayList<String>();
                answersObject = new ArrayList<String>();
                emptyProperty = new ArrayList<String>();
        }

        /**
         * Run this method before first use for initializing the TDB directory with the
         * necessary folders and binaries. Important: Use only once
         */
        public static void initializeTDB(){
        		boolean tdbEmpty = TDBFactory.inUseLocation(TDB_PATH);
        		if (!tdbEmpty) {
	        		System.out.println("config: start initalizing TDB at "+TDB_PATH);
	                //Set directory path to TDB folder
	                String tdbDirectory = TDB_PATH;

	                //Create TDB dataset
	                Dataset dataset = TDBFactory.createDataset(tdbDirectory);
	                TDB.sync(dataset);

	                //Create OntModel where all turtle files are loaded in
	                OntModel morphemeDB = ModelFactory.createOntologyModel(
	                                OntModelSpec.getDefaultSpec("http://www.w3.org/2002/07/owl#"), dataset.getDefaultModel());

	                //Lock writing session
	                dataset.begin(ReadWrite.READ);
	                //TODO: do not rely on specific file names here, use ontology IRI to file mappings read in the Configuration utility class and specify ontology IRIs instead
	                //morphemeDB.read(TTL_PATH+"/deu_inventory.ttl");
	                //morphemeDB.read(TTL_PATH+"/deu_schema.ttl");
	                //morphemeDB.read(TTL_PATH+"/mmoon.ttl");

                  for (String file : ONT_Sources.values()) {
                    morphemeDB.read(file);
                  }

	                //Bugfix
	                QueryDBSPARQL db = new QueryDBSPARQL();
	                db.getAllTypes();
	                dataset.end();
	                System.out.println("config: finished initalizing TDB");
        		} else {
        			System.out.println("config: TDB already initalized or directory not empty");
        		}
        }

        /**
         * Method to get a current OntModel from TDB database so it is possible on
         * an up-to-date model
         * @return updated model
         */
        private OntModel getOntModel(){
                TDB.sync(dataset);
                //Creates OntModel
            dataset.begin(ReadWrite.WRITE);
                OntModel morphemeDB = ModelFactory.createOntologyModel(
                                OntModelSpec.getDefaultSpec("http://www.w3.org/2002/07/owl#"), dataset.getDefaultModel());
                dataset.end();
                return morphemeDB;
        }

        /**
         * Method to save updates in db and closes tdb session so changes are accessable
         * for future sessions
         * @param model model with which was worked with
         */
        private void saveModel(OntModel model){
                if(model != null && dataset != null){
                        TDB.sync(dataset);
                        model.commit();
                //      model.close();
                //      dataset.close();
                }
        }

        /**
         * Private method to clear all lists
         */
        private void clearAllLists(){
                this.answersObject = new ArrayList<String>();
                this.answersProperty = new ArrayList<String>();
                this.answersSubject = new ArrayList<String>();
                this.emptyProperty = new ArrayList<String>();
        }

        /**
         * Get dataset
         * @return dataset
         */
        public Dataset getDataset(){
                return dataset;
        }

        /**
         * Get the list with the results from querying the subject which has the passed
         * representation
         * @return subject values list
         */
        public ArrayList<String> getSubjectList(){
                return this.answersSubject;
        }

        /**
         * Get the list with the properties related to given subject
         * @return property values list
         */
        public ArrayList<String> getPropertyList(){
                return this.answersProperty;
        }

        /**
         * Get the list with the objects related to given subject
         * @return object values list
         */
        public ArrayList<String> getObjectList(){
                return this.answersObject;
        }

        /**
         * Get the list with the properties which were not set
         * @return property values list
         */
        public ArrayList<String> getEmptyPropertyList(){
                return this.emptyProperty;
        }

        /**
         * method to search for all assignable values which can be combined with the a given property
         * @param property property value
         * @return list with all assignable objects
         */
        public ArrayList<String> searchForAssignableObjects(String property){
          //Creates OntModel
            OntModel morphemeDB = this.getOntModel();

            //Set Dataset in ReadOnly mode
            dataset.begin(ReadWrite.READ);

            //Creates a SPARQL query string
            String searchPropertyIRI = QueryDBSPARQL.CORE_NAME_SPACE + property;
            String querySearch =
                            QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                            + "SELECT DISTINCT ?object\n" // Selects final result values
                            + "WHERE{\n"
                            +       searchPropertyIRI + " rdfs:range ?range .\n"
                            +       "?subclass rdfs:subClassOf* ?range . \n"
                            +       "?object a ?subclass . \n"
                            +       "FILTER regex(str(?object), 'inventory')\n"
                            + "}"
                            + "";

            ResultSet results;
            //Runs SPARQL query against database
            try(QueryExecution qe = QueryExecutionFactory.create(querySearch, morphemeDB)){
                    //Stores result in variable
                    results =  qe.execSelect();
                    //Copies result to use it outer scope
                    results = ResultSetFactory.copyResults(results);
                    //Frees resources
                qe.close();
            }
            //Close dataset transaction
            finally{
                    dataset.end();
                    this.saveModel(morphemeDB);
            }

            ArrayList<String> assignableObjects = new ArrayList<String>();
            //Stores all subjects which have the representation of the passed string
            while(results.hasNext()){
                    QuerySolution qs = results.next();
                    String name = qs.get("object").toString();
                    for(int i = name.length() - 1; i >= 0; i-- ){
                            if(name.charAt(i) == '/'){
                                    name = name.substring(i+1, name.length());
                                    break;
                            }
                    }
                    assignableObjects.add(name);
            }
            return assignableObjects;

        }

        /**
         * Method sets a SPARQL query with the passed string as subject instance
         * @param Object IRI which represents the subject the algorithm is searching for
         */
        public void searchForEmptyProperty(String searchSubject){
                this.clearAllLists();

                //Creates OntModel
                OntModel morphemeDB = this.getOntModel();

                //Set Dataset in ReadOnly mode
                dataset.begin(ReadWrite.READ);

                Set<String> resultSet = new HashSet<String>();
                //Set SPARQL string
                String searchSubjectIRI = QueryDBSPARQL.DEU_INVENTORY_NAME_SPACE + searchSubject;
                String querySearch =
                QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                + "SELECT DISTINCT ?property\n" // Selects final result values
                + "WHERE {\n"
                +       searchSubjectIRI + " a ?subclass . \n"
                +       "?subclass rdfs:subClassOf* ?class . \n"
                +       "?property rdfs:domain ?class . \n"
                +       "FILTER regex(str(?property), 'mmoon')\n"
                +       "FILTER NOT EXISTS{ " + searchSubjectIRI + "?property ?object }\n"
                + "}";

                ResultSet results;
                //Runs SPARQL query against database
                try(QueryExecution qe = QueryExecutionFactory.create(querySearch, morphemeDB)){
                        //Stores result in variable
                        results =  qe.execSelect();
                        //Copies result to use it outer scope
                        results = ResultSetFactory.copyResults(results);
                        //Frees resources
                        qe.close();
                }
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);
                }

                //Stores all subjects which have the representation of the passed string
                while(results.hasNext()){
                        QuerySolution qs = results.next();
                        String name = qs.get("property").toString();
                        for(int i = name.length() - 1; i >= 0; i-- ){
                                if(name.charAt(i) == '/'){
                                        name = name.substring(i+1, name.length());
                                        break;
                                }
                        }
                        resultSet.add(name);
                }

                //Second SPARQL query to get all non functional properties
              //Creates OntModel
                morphemeDB = this.getOntModel();

                //Set Dataset in ReadOnly mode
                dataset.begin(ReadWrite.READ);

                //Set SPARQL string
                querySearch =
                QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                + "SELECT DISTINCT ?property\n" // Selects final result values
                + "WHERE {\n"
                +       searchSubjectIRI + " a ?subclass . \n"
                +       "?subclass rdfs:subClassOf* ?class . \n"
                +       "?property rdfs:domain ?class . \n"
                +       "FILTER regex(str(?property), 'mmoon')\n"
                +       "FILTER NOT EXISTS{?property rdf:type  owl:FunctionalProperty .}\n"
                + "}";

                //Runs SPARQL query against database
                try(QueryExecution qe = QueryExecutionFactory.create(querySearch, morphemeDB)){
                        //Stores result in variable
                        results =  qe.execSelect();
                        //Copies result to use it outer scope
                        results = ResultSetFactory.copyResults(results);
                        //Frees resources
                        qe.close();
                }
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);
                }

              //Stores all subjects which have the representation of the passed string
                while(results.hasNext()){
                        QuerySolution qs = results.next();
                        String name = qs.get("property").toString();
                        for(int i = name.length() - 1; i >= 0; i-- ){
                                if(name.charAt(i) == '/'){
                                        name = name.substring(i+1, name.length());
                                        break;
                                }
                        }
                        resultSet.add(name);
                }

                emptyProperty.addAll(resultSet);
        }

        /**
        * Method to search for all properties and object related to a given subject
        * @param searchSubject string the representation of the subject
        */
        public void searchForObject(String searchSubject){
                //Clear lists
                this.clearAllLists();

                //Create SPARQL query string
                String searchObjectIRI = QueryDBSPARQL.DEU_INVENTORY_NAME_SPACE + searchSubject;
                String querySearch =
                QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                + "SELECT DISTINCT ?property ?object\n" // Selects final result values
                + "WHERE {\n"
                +       searchObjectIRI + "?property ?object .\n"
                +       "FILTER NOT EXISTS {\n"
                +               "?subproperty rdfs:subPropertyOf* ?property .\n"
                +               "FILTER (?subproperty != ?property)\n"
                +       "}\n"
                + "}";

                this.searchForEmptyProperty(searchSubject);

                //Creates OntModel
                OntModel morphemeDB = this.getOntModel();

                //Set Dataset in ReadOnly mode
                dataset.begin(ReadWrite.READ);

                ResultSet results;
                //Runs SPARQL query against database
                try(QueryExecution qe = QueryExecutionFactory.create(querySearch, morphemeDB)){
                        //Stores result in variable
                        results =  qe.execSelect();
                        //Copies result to use it outer scope
                        results = ResultSetFactory.copyResults(results);
                        //Frees resources
                        qe.close();
                }
                //Close dataset transaction
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);
                }

                //Stores all properties and objects
                while(results.hasNext()){
                        QuerySolution triple = results.next();
                        String nameProperty = triple.get("property").toString();
                        String nameObject = triple.get("object").toString();

                        for(int i = nameObject.length() - 1; i >= 0; i-- ){
                                if(nameObject.charAt(i) == '/'){
                                        nameObject = nameObject.substring(i+1, nameObject.length());
                                        break;
                                }
                        }

                        if(nameProperty.contains("#")){
                                continue; //rdf intern definitions
                        }

                        for(int i = nameProperty.length() - 1; i >= 0; i-- ){
                                if(nameProperty.charAt(i) == '/'){
                                        nameProperty = nameProperty.substring(i+1, nameProperty.length());
                                        break;
                                }
                        }
                        answersProperty.add(nameProperty);
                        answersObject.add(nameObject);
                }
        }

        /**
         * Method sets a SPARQL query with the passed string as orthographic representation
         * @param searchString the string which is searched for as orthographic representation
         * @return returns a set with the detected triples
         */
        public ArrayList<String> searchForSubject(String searchString){
                //Clear all list
                this.clearAllLists();
                //Creates OntModel
                OntModel morphemeDB = this.getOntModel();

                //Set Dataset in ReadOnly mode
                dataset.begin(ReadWrite.READ);

                //Creates a SPARQL query string
                String querySearch =
                                QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                                + "SELECT DISTINCT ?subject\n" // Selects final result values
                                + "WHERE{\n"
                                +       "?object mmoon:orthographicRepresentation \"" + searchString +"\" .\n"
                                +       "?subject ?property ?object . \n"
                                + "}"
                                + "";

                ResultSet results;
                //Runs SPARQL query against database
                try(QueryExecution qe = QueryExecutionFactory.create(querySearch, morphemeDB)){
                        //Stores result in variable
                        results =  qe.execSelect();
                        //Copies result to use it outer scope
                        results = ResultSetFactory.copyResults(results);
                        //Frees resources
                    qe.close();
                }
                //Close dataset transaction
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);

                }

                //Stores all subjects which have the representation of the passed string
                while(results.hasNext()){
                        String name = results.next().get("subject").toString();
                        for(int i = name.length() - 1; i >= 0; i-- ){
                                if(name.charAt(i) == '/'){
                                        name = name.substring(i+1, name.length());
                                        break;
                                }
                        }
                        answersSubject.add(name);
                }
                return answersSubject;
        }

        /**
         * method to search for all possible subjects which hold an representation
         * @return list with all stored subjects
         */
        public ArrayList<String> getAllSubjects(){
            //Clear all list
            this.clearAllLists();
            //Creates OntModel
            OntModel morphemeDB = this.getOntModel();

            //Set Dataset in ReadOnly mode
            dataset.begin(ReadWrite.READ);

                //Creates a SPARQL query string
                String querySearch =
                        QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                        + "SELECT DISTINCT ?subject\n" // Selects final result values
                        + "WHERE{\n"
                        +       "?object mmoon:orthographicRepresentation ?representaion .\n"
                        +       "?subject ?property ?object . \n"
                        + "}"
                        + "";

            ResultSet results;
            //Runs SPARQL query against database
            try(QueryExecution qe = QueryExecutionFactory.create(querySearch, morphemeDB)){
                    //Stores result in variable
                    results =  qe.execSelect();
                    //Copies result to use it outer scope
                    results = ResultSetFactory.copyResults(results);
                    //Frees resources
                qe.close();
            }
            //Close dataset transaction
            finally{
                    dataset.end();
                    this.saveModel(morphemeDB);

            }

            //Stores all subjects which have the representation of the passed string
            while(results.hasNext()){
                    String name = results.next().get("subject").toString();
                    for(int i = name.length() - 1; i >= 0; i-- ){
                            if(name.charAt(i) == '/'){
                                    name = name.substring(i+1, name.length());
                                    break;
                            }
                    }
                    answersSubject.add(name);
            }
            return answersSubject;
        }

        /**
         * Delete triple from database
         * @param subjectString String of subject
         * @param propertyString String of property
         * @param objectString String of object
         */
        public void deleteValue(String subjectString, String propertyString, String objectString){
            if(subjectString.equals("") || propertyString.equals("")||objectString.equals("")){
                return;
            }
                String subjectIRI = QueryDBSPARQL.DEU_INVENTORY_NAME_SPACE + subjectString;
                String propertyIRI = QueryDBSPARQL.CORE_NAME_SPACE + propertyString;

                //Creates OntModel
                OntModel morphemeDB = this.getOntModel();
                //Write transaction mode
                dataset.begin(ReadWrite.WRITE);
                try{

                        //Creates graph store for SPARQL update
                        GraphStore graphStore = GraphStoreFactory.create(dataset);

                        //Creates a SPARQL update string
                        String queryUpdate =
                                        QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                                        + "DELETE{\n"
                                        +       subjectIRI + " " + propertyIRI + " ?object .\n"
                                        + "}\n"
                                        + "WHERE{\n" //Needs where clause to check property restriction and
                                                                 //object iri matching
                                        +       "?s ?p ?object .\n"
                                        +       "?subject ?p2 ?o .\n"
                                        +       "FILTER regex(str(?object), '" + objectString +"')\n"
                                        +       "FILTER regex(str(?subject), '" + subjectString +"')\n"
                                        + "}";

                        //run update factory
                        UpdateRequest request = UpdateFactory.create(queryUpdate);
                        UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
                        proc.execute();

                        // Finally, commit the transaction.
                        dataset.commit();
                }
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);
                }

        }

        /**
         * Add new triple to database
         * @param subjectString String of subject
         * @param propertyString String of property
         * @param objectString String of object
         */
        public void insertValue(String subjectString, String propertyString, String objectString){
                if(subjectString.equals("") || propertyString.equals("")||objectString.equals("")){
                    return;
                }
                String subjectIRI = QueryDBSPARQL.DEU_INVENTORY_NAME_SPACE + subjectString;
                String propertyIRI = QueryDBSPARQL.CORE_NAME_SPACE + propertyString;

                //Creates an OntModel
                OntModel morphemeDB = this.getOntModel();
                //Write transaction mode
                dataset.begin(ReadWrite.WRITE);
                try{
                        //Creates graph store for SPARQL update
                        GraphStore graphStore = GraphStoreFactory.create(dataset);

                        //Creates a SPARQL update string
                        String queryUpdate =
                                        QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                                        + "INSERT{\n"
                                        +       subjectIRI + " " + propertyIRI + " ?object .\n"
                                        + "}\n"
                                        + "WHERE{\n" //Needs where clause to check property restriction and
                                                                 //object iri matching
                                        +       "?object ?p ?o .\n"
                                        +       "?subject ?p2 ?o2 .\n"
                                        +       "FILTER regex(str(?object), '" + objectString +"')\n"
                                        +       "FILTER regex(str(?subject), '" + subjectString +"')\n"
                                        + "}";

                        UpdateRequest request = UpdateFactory.create(queryUpdate);
                        UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
                    proc.execute();

                        // Finally, commit the transaction.
                        dataset.commit();
                }
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);
                }
        }

        /**
         * Method to get all possible morpheme types
         * @return list with all morpheme types
         */
        public ArrayList<String> getAllTypes(){
                 OntModel morphemeDB = this.getOntModel();

                //Set Dataset in ReadOnly mode
                dataset.begin(ReadWrite.READ);

                //Creates a SPARQL query string
                String querySearch =
                                QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                                + "SELECT DISTINCT ?type\n" // Selects final result values
                                + "WHERE{\n"
                                +       "?subject a ?type . \n"
                                +       "?type rdfs:subClassOf* ?class . \n"
                                +       "FILTER regex(str(?type), 'schema/')\n"
                                + "}";

                ResultSet results;
                //Runs SPARQL query against database
                try(QueryExecution qe = QueryExecutionFactory.create(querySearch, morphemeDB)){
                        //Stores result in variable
                        results =  qe.execSelect();
                        //Copies result to use it outer scope
                        results = ResultSetFactory.copyResults(results);
                        //Frees resources
                    qe.close();
                }
                //Close dataset transaction
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);

                }

                ArrayList<String> typeList = new ArrayList<String>();
                while(results.hasNext()){
                        String type = results.next().get("type").toString();
                        for(int i = type.length() - 1; i >= 0; i-- ){
                                if(type.charAt(i) == '/'){
                                        type = type.substring(i+1, type.length());
                                        break;
                                }
                        }
                        typeList.add(type);
                }

                return typeList;

        }

        /**
         * Method to insert a completely new morpheme
         * @param subject new morpheme
         * @param type morpheme type
         * @param representation representation of the morpheme
         */
        public void insertNewSubject(String subject, String type, String representation){
                String typeIRI = QueryDBSPARQL.DEU_SCHEMA_NAME_SPACE + type;
                String subjectIRI = QueryDBSPARQL.DEU_INVENTORY_NAME_SPACE + type + "_" + subject;

                //Creates an OntModel
                OntModel morphemeDB = this.getOntModel();
                //Write transaction mode
                dataset.begin(ReadWrite.WRITE);

                //First transaction: create a subject with a special db confrom type
                try{
                        //Creates graph store for SPARQL update
                        GraphStore graphStore = GraphStoreFactory.create(dataset);

                        //Creates a SPARQL update string
                        String queryUpdate =
                                        QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                                        + "INSERT{\n"
                                        +       subjectIRI + " rdf:type " + typeIRI + " .\n"
                                        + "}\n"
                                        + "WHERE{\n" //Needs where clause to check property restriction and
                                                                 //object iri matching
                                        //+     typeIRI + " rdfs:subClassOf* ?superClass .\n"
                                        + "}";


                        UpdateRequest request = UpdateFactory.create(queryUpdate);
                        UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
                        proc.execute();

                        // Finally, commit the transaction.
                        morphemeDB.commit();
                        dataset.commit();
                }
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);
                }

                // Value for recursive calls
                if(representation == null){
                        return;
                }

                //check whether the representation already exists
                boolean existRepr = this.searchForSubject(representation).size() > 0;

                String propertyIRI;
                if(!existRepr){
                        //Second transaction: recursive call of the same method to create a representation subject
                        this.insertNewSubject("Representation_" + subject, "Representation", null);

                        //Overrides names
                        subjectIRI = QueryDBSPARQL.DEU_INVENTORY_NAME_SPACE + "Representation_" + subject;
                        propertyIRI = QueryDBSPARQL.CORE_NAME_SPACE + "orthographicRepresentation";
                        //Creates an OntModel
                        morphemeDB = this.getOntModel();
                        //Write transaction mode
                        dataset.begin(ReadWrite.WRITE);

                        //Third transaction: insert orthographic representation
                        try{
                                //Creates graph store for SPARQL update
                                GraphStore graphStore = GraphStoreFactory.create(dataset);

                                //Creates a SPARQL update string
                                String queryUpdate =
                                                QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                                                + "INSERT{\n"
                                                +       subjectIRI + " " + propertyIRI + " \"" + representation + "\" .\n"                                      + "}\n"
                                                + "WHERE{\n" //Needs where clause to check property restriction and
                                                                        //object iri matching
                                                +       propertyIRI + " a owl:DatatypeProperty .\n"
                                                + "}";

                                UpdateRequest request = UpdateFactory.create(queryUpdate);
                                UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
                                proc.execute();

                                // Finally, commit the transaction.
                                morphemeDB.commit();
                                dataset.commit();
                        }
                        finally{
                                dataset.end();
                                this.saveModel(morphemeDB);
                        }
                }
                //Change names
                subjectIRI = QueryDBSPARQL.DEU_INVENTORY_NAME_SPACE + type + "_" + subject;
                propertyIRI = QueryDBSPARQL.CORE_NAME_SPACE + "hasRepresentation";

                //Creates an OntModel
                morphemeDB = this.getOntModel();
                //Write transaction mode
                dataset.begin(ReadWrite.WRITE);

                //Fourth transaction: set triple relationship between representation and new subject
                try{
                        //Creates graph store for SPARQL update
                        GraphStore graphStore = GraphStoreFactory.create(dataset);

                        //Creates a SPARQL update string
                        String queryUpdate =
                                        QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                                        + "INSERT{\n"
                                        +       subjectIRI + " " + propertyIRI + " ?object .\n"
                                        + "}\n"
                                        + "WHERE{\n" //Needs where clause to check property restriction and
                                                                 //object iri matching
                                        +       "?object mmoon:orthographicRepresentation  \"" + representation +"\" .\n"
                                        + "}";

                        UpdateRequest request = UpdateFactory.create(queryUpdate);
                        UpdateProcessor proc = UpdateExecutionFactory.create(request, graphStore);
                    proc.execute();

                        // Finally, commit the transaction.
                    morphemeDB.commit();
                        dataset.commit();
                }
                finally{
                        dataset.end();
                        this.saveModel(morphemeDB);
                }
        }


        /**
         * Method to check whether the update methods (insertValue or deleteValue) was successful. Needs to be run in a new thread
         * after the old thread was closed
         * @param subjectString String of subject
         * @param propertyString String of property
         * @param objectString String of object
         * @return true if the value was found, false if not - so if to check whether a deleteVlaue method was successful the values
         * need to be changed
         */
        public boolean verifyUpdate(String subjectString, String propertyString, String objectString){
                //Set Dataset in ReadOnly mode
                String subjectIRI = QueryDBSPARQL.DEU_INVENTORY_NAME_SPACE + subjectString;
                String propertyIRI = QueryDBSPARQL.CORE_NAME_SPACE + propertyString;

                //Creates OntModel
                OntModel morphemeDB = this.getOntModel();
                dataset.begin(ReadWrite.READ);

                //Creates a SPARQL query string
                String querySearch =
                        QueryDBSPARQL.QUERY_PREFIX //Sets prefix which was declared above
                                + "SELECT DISTINCT ?object\n" // Selects final result values
                                + "WHERE{\n"
                                +       subjectIRI + " " + propertyIRI + " ?object .\n"
                                +       "FILTER regex(str(?object), '" + objectString +"')\n"
                                + "}"
                                + "";

                ResultSet results;
                //Runs SPARQL query against database
                try(QueryExecution qe = QueryExecutionFactory.create(querySearch, morphemeDB)){
                        //Stores result in variable
                        results =  qe.execSelect();
                        //Copies result to use it outer scope
                        results = ResultSetFactory.copyResults(results);
                        if(results.hasNext()){
                                //Frees resources
                                qe.close();
                                dataset.end();
                                this.saveModel(morphemeDB);
                                return true;
                        }
                        else{
                                //Frees resources
                                qe.close();
                                dataset.end();
                                this.saveModel(morphemeDB);
                                return false;
                        }
                }
        }
        /**
         * Add values from file to the existing database
         * @param filePath path to file
         */
        public void addValuesFromFile(String filePath){
                RDFDataMgr.read(dataset, filePath);
        }

        /**
         * Writes current database state into a turtle file in block format
         * @param filePath path to turtle file
         * @throws IOException exception is thrown if there is an error writing into file
         */
        public void convertDatabaseToTurtleFile(String filePath){
            if(filePath.contains(".") & !filePath.contains(".ttl")){
                System.err.println("File " + filePath + " has to be in .ttl format");
            }

            if(!filePath.contains(".")){
                filePath += File.separator;
                filePath += "MorphemeDatabase.ttl";
                File f = new File(filePath);
                f.getParentFile().mkdirs();
                try{
                    f.createNewFile();
                }
                catch(IOException ioe){
                    System.err.println("Cannot create new TURTLE file");
                }
            }
                //Create new BufferedWriter
                BufferedWriter writeToFile = null;
                //Set dataset into read mode
                dataset.begin(ReadWrite.READ);
                try{
                        //Create buffered writer and print output into file in turtle block format
                        writeToFile = new BufferedWriter(new FileWriter(new File(filePath)));
                        RDFDataMgr.write(writeToFile, dataset.getDefaultModel(), RDFFormat.TURTLE_BLOCKS) ;
                }
                //Catch block if the file does not exist
                catch(FileNotFoundException fnfe){
                        System.err.println("File " + filePath + " does not exist");
                }
                //Catch block if there was an error writing into file
                catch(IOException ioe){
                        System.err.println("Error while writing into file");
                }
                finally{
                        //End transaction
                        dataset.end();
                        //Close buffered reader
                        if(writeToFile != null){
                            try{
                                writeToFile.close();
                            }
                            catch(IOException ioe){
                                System.err.println("Error while closing the file");
                            }
                        }
                }
        }

        /**
         * Test Main
         * @param args command line parameters
         */
        public static void main(String[] args){
//        	new Configuration();
//        	QueryDBSPARQL db = new QueryDBSPARQL();
//        	db.initializeTDB();
//        	System.out.println(db.getAllTypes());
//                QueryDBSPARQL db = new QueryDBSPARQL();
//                System.out.println(db.getAllTypes());
//                ArrayList<String> list = db.getAllSubjects();
//                for(String temp: list){
//                    System.out.println(temp);
//                }
//
//                System.out.println();
//                db.searchForEmptyProperty("Lexeme_schlafen");
//                for(String temp: db.getEmptyPropertyList()){
//                    System.out.println(temp);
//                }
//
//                System.out.println();
//                ArrayList<String> list = db.searchForAssignableObjects("hasWordform");
//                for(String temp: list){
//                    System.out.println(temp);
//                }
//
//
//
//
//               QueryDBSPARQL db = new QueryDBSPARQL();
//               db.searchForObject("Lexeme_kaufen");
//               for(int i = 0; i < db.getObjectList().size(); i++){
//                 System.out.println(db.getPropertyList().get(i) + "\t" + db.getObjectList().get(i));
//             }
//
//             db.insertNewSubject("schnarchen", "Lexeme", "schnarchen");
//             ArrayList<String> list = db.searchForSubject("schnarchen");
//             for(String temp: list){
//                 System.out.println(temp);
//             }
//             db.searchForObject("Lexeme_schnarchen");
//             System.out.println(db.getObjectList().size());
//             System.out.println(db.getPropertyList().size());
//             for(int i = 0; i < db.getObjectList().size(); i++){
//                 System.out.println(db.getPropertyList().get(i) + "\t" + db.getObjectList().get(i));
//             }
//             ArrayList<String> list;
//             db.insertValue("Lexeme_kaufen", "hasWordform" , "SyntheticWordform_kaufe2");
//             System.out.println(db.getAllTypes().size());
               //db.insertNewSubject(subject, type, representation);
//              db.searchForObject("");
//              list = db.getObjectList();
//              System.out.println(list.size());
//              System.out.println(db.getEmptyPropertyList().size());
//
//
//              System.out.println();
//
//             /* db.searchForObject("djfaaipäasdvmlxcvüpaef#yxcC943C<ASDA");
//              list = db.getObjectList();
//                System.out.println(list.size());*/
//
//                System.out.println();
//
//                db.searchForObject("Lexeme_huepfenX");
//                list = db.getObjectList();
//                System.out.println(list.size());
//                System.out.println(db.getEmptyPropertyList().size());
//
//                System.out.println();
//
//                db.searchForObject("Lexeme_rufenX");
//                list = db.getObjectList();
//                System.out.println(list.size());
//                System.out.println(db.getEmptyPropertyList().size());
//
//                System.out.println();
//
//                db.searchForObject("Lexeme_schlafen");
//                list = db.getObjectList();
//                System.out.println(list.size());
//                System.out.println(db.getEmptyPropertyList().size());
//
//                System.out.println();
//
//                db.searchForObject("SyntheticWordform_schlafen1");
//                list = db.getObjectList();
//                System.out.println(list.size());
//                System.out.println(db.getEmptyPropertyList().size());
//
//                System.out.println();
//
//                db.searchForObject("Lexeme_kaufen");
//
//                list = db.getObjectList();
//                System.out.println(db.getObjectList().size());
//                System.out.println(db.getPropertyList().size());
//                System.out.println(db.getEmptyPropertyList().size());
//
//                ArrayList<String> listP = db.getPropertyList();
//                for(int i = 0; i < list.size(); i++){
//                    System.out.println(listP.get(i) + "\t" + list.get(i));
//                }
//


        }
}
