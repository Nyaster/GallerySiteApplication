package com.gallery.galleryapplication.models;

import jakarta.persistence.*;

@Entity
@Table(name = "person")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;
    @Column(name = "login",unique = true)
    private String login;
    @Column(name = "password")
    private String password;
    @OneToOne(mappedBy = "author")
    private Author author;
}
