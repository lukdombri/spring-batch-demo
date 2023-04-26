package com.example.springbatchdemo.processors;

import com.example.springbatchdemo.dto.PersonDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class PersonItemProcessor implements ItemProcessor<PersonDTO, PersonDTO> {


    @Override
    public PersonDTO process(final PersonDTO person) throws Exception{
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();

        //if("JASON".equals(firstName)) throw new IllegalArgumentException("Niedozowlone Imie!");

        final PersonDTO transformedPerson = new PersonDTO(firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }

}
