package fr.abes.sudoc.indexation-sujetbatchdump.writer;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;
import fr.abes.sudoc.iarbatchdump.utils.FilenameUtils;

@Configuration
public class NoticeCsvWriterConfig {

    @Value("${app.batch.action}")
    private String action;

    @Autowired 
    FilenameUtils filenameUtils;
    

    @Bean
    @StepScope
    @Primary
    public FlatFileItemWriter<CsvRecord> noticeCsvWriter() {
        
        FlatFileItemWriter<CsvRecord> writer = new FlatFileItemWriter<>();
        writer.setName("noticeCsvWriter");
        writer.setResource(new FileSystemResource(filenameUtils.getDoublonFilePath()));
        writer.setAppendAllowed(true);
        writer.setLineAggregator(item -> item.toCsvLine());
        writer.setHeaderCallback(w -> w.write("ppn\tthese\ttitre\tresume\trameau\tlangue"));
        return writer;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CsvRecord> deduplicationCsvWriter() {

        FlatFileItemWriter<CsvRecord> writer = new FlatFileItemWriter<>();

        writer.setName("deduplicationCsvWriter");
        writer.setResource( new FileSystemResource(filenameUtils.getOutputFilePath()) );
        writer.setAppendAllowed(true);
        writer.setLineAggregator(CsvRecord::toCsvLine);
        writer.setHeaderCallback(w -> w.write("ppn\tthese\ttitre\tresume\trameau\tlangue"));

        return writer;
    }

}
