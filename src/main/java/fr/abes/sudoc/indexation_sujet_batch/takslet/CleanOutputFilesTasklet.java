package fr.abes.sudoc.indexation_sujet_batch.takslet;

import fr.abes.sudoc.indexation_sujet_batch.utils.FilenameUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CleanOutputFilesTasklet implements Tasklet {

    @Autowired 
    FilenameUtils filenameUtils;


    @Override
    public RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) throws IOException {

        Files.deleteIfExists(Path.of(filenameUtils.getDoublonFilePath()));
        Files.deleteIfExists(Path.of(filenameUtils.getOutputFilePath()));

        return RepeatStatus.FINISHED;
    }
}