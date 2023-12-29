package com.gallery.galleryapplication.util;

import com.gallery.galleryapplication.models.Person;
import com.gallery.galleryapplication.services.PersonDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PersonValidator implements Validator {
    private final PersonDetailService personDetailService;

    @Autowired
    public PersonValidator(PersonDetailService personDetailService) {
        this.personDetailService = personDetailService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target;
        if (personDetailService.getUser(person.getLogin()).isPresent()){
            errors.rejectValue("username","","Пользователь с таким именем уже существует");
        }
    }
}
