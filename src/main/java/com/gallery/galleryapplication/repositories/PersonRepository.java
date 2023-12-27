package com.gallery.galleryapplication.repositories;

import com.gallery.galleryapplication.models.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person,Integer> {
    Optional<Person> findByLogin(String username);
}
