package nerWork;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ie.util.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;

import org.json.*;

import java.io.*;
import java.util.*;

public class PodcastNER{

    public static String text = "Marie was born in Paris.";
    static StanfordCoreNLP pipeline;

    public static void main(String[] args){
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        // build pipeline
        pipeline = new StanfordCoreNLP(props);
        // create a document object
        // CoreDocument document = pipeline.processToCoreDocument(text);

        ArrayList<String> fileList =  getFilesFromPodcastDirectory("temp/podcast-data-generator-0.10.0/podcasts");

        for(String file : fileList){
            // System.out.println(file);
            parsePodcast(file);
        }
    }


    private static ArrayList<String> getFilesFromPodcastDirectory(String folderpath) {
        final File folder = new File(folderpath);

        ArrayList<String> fileList = new ArrayList<String>();
        for (final File fileEntry : folder.listFiles()) {
            // System.out.println(fileEntry.getName());
            fileList.add(fileEntry.getName());
        }

        return fileList;
    }

    private static void parsePodcast (String podcastFile) {
        try {
            /* read podcast JSON file */
            String filepath = ("temp/podcast-data-generator-0.10.0/podcasts/" + podcastFile);
            String file = new Scanner(new File(filepath)).useDelimiter("\\Z").next();
            JSONObject podcast = new JSONObject(file);

            /* Process Episode named entities */
            CoreDocument podcastDoc = pipeline.processToCoreDocument(podcast.get("description").toString());
            JSONArray podcastEntities = new JSONArray();
            for(CoreEntityMention entity: podcastDoc.entityMentions()){
                podcastEntities.put(entity);
            }
            podcast.put("entities", podcastEntities);

            /* Process Episode named entities */
           JSONArray episodes = (JSONArray) podcast.get("items");
           for(Object o: episodes){
               JSONObject episode =  (JSONObject) o;

               Set<String> episodeEntities = new HashSet<>();
               String episodeDescription = (String) episode.get("content");
               CoreDocument descriptionDoc = pipeline.processToCoreDocument(episodeDescription);
               for(CoreEntityMention entity: descriptionDoc.entityMentions()){
                   episodeEntities.add(entity.toString());
               }
               JSONArray entities = new JSONArray();
               for(String e: episodeEntities){
                   entities.put(e);
               }
               episode.put("entities",entities);
           }

            /* Write to file */
            FileWriter outputFile = new FileWriter("temp/podcast-data-ner-output/"+podcastFile);
            outputFile.write(podcast.toString());
            outputFile.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}