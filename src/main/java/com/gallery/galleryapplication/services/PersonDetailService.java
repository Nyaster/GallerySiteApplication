package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Person;
import com.gallery.galleryapplication.repositories.PersonRepository;
import com.gallery.galleryapplication.security.PersonDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonDetailService implements UserDetailsService {
    private final PersonRepository personRepository;
@Autowired
    public PersonDetailService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Person> person = personRepository.findByLogin(username);
        if (person.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }
        return new PersonDetails(person.get());
    }
    public Optional<Person> getUser(String username){
        return personRepository.findByLogin(username);
    }
}
