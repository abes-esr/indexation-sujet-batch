package fr.abes.sudoc.indexation_sujet_batch.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component 
public class FilenameUtils {

    @Value("${app.batch.action}")
    private String action;

    @Value("${app.batch.output-filename}")
    private String outputFilename;

    @Value("${app.batch.output-directory}")
    private String outputDirectory;
    
    
    
    public String getOutputFilePath(){
        return outputDirectory + "/" + outputFilename + "_" + action + ".csv";
    }


    public String getDoublonFilePath(){
        return outputDirectory + "/" + outputFilename + "_doublon.csv";
    }

}
