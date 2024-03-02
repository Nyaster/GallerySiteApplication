package com.gallery.galleryapplication.services;

import com.gallery.galleryapplication.models.Author;
import com.gallery.galleryapplication.repositories.AuthorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class AuthorService {
    final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public void addNewAuthors(List<Author> authors) {
        List<Author> all = authorRepository.findAll();
        List<Author> temp = new ArrayList<>(authors);
        if (!all.isEmpty()){
            temp.removeAll(all);
        }
        if (temp.isEmpty()){
            return;
        }
        authorRepository.saveAllAndFlush(temp);
    };
    public List<Author> findAll(){
         return authorRepository.findAll();
    };
}
