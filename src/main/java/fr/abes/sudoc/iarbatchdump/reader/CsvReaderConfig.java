package fr.abes.sudoc.indexation-sujetbatchdump.reader;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import fr.abes.sudoc.iarbatchdump.model.CsvRecord;
import fr.abes.sudoc.iarbatchdump.utils.FilenameUtils;

@Configuration
public class CsvReaderConfig {

    @Autowired 
    FilenameUtils filenameUtils;


    @Bean
    @StepScope
    public FlatFileItemReader<CsvRecord> csvReader() {

        return new FlatFileItemReaderBuilder<CsvRecord>()
            .name("csvReader")
            .resource( new FileSystemResource( filenameUtils.getDoublonFilePath() ) )
            .linesToSkip(1)
            .lineMapper((line, lineNumber) -> {

                String[] fields = line.split("\t", -1);

                if (fields.length != 6) {
                    throw new FlatFileParseException(
                            "Nombre de colonnes incorrect : " + fields.length,
                            line,
                            lineNumber
                    );
                }

                CsvRecord record = new CsvRecord();

                record.setPpn(fields[0]);
                record.setThese(fields[1]);
                record.setTitre(fields[2]);
                record.setResume(fields[3]);
                record.setLibelleRameau(fields[4]);
                record.setLangue(fields[5]);

                return record;
            })
            .build();
    }
}
