package fr.abes.sudoc.indexation_sujet_batch.takslet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import fr.abes.sudoc.indexation_sujet_batch.utils.FilenameUtils;

@Component 
public class UploadCsvFileTasklet implements Tasklet {

    @Autowired 
    FilenameUtils filenameUtils;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.batch.upload-url}")
    protected String UPLOAD_URL;

    @Value("${app.batch.action}")
    protected String action;
    

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String URL_API = UPLOAD_URL + action + "/upload" ;
        
        FileSystemResource file = new FileSystemResource(filenameUtils.getOutputFilePath());

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);
        

        restTemplate.postForEntity(
                URL_API,
                request,
                Void.class
        );

        return RepeatStatus.FINISHED;
    }
}
