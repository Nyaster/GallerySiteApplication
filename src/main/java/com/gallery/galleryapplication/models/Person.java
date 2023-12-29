package com.gallery.galleryapplication.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true)
    private String login;
    private String password;
    @OneToOne(mappedBy = "person")
    private Author author;
    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id && Objects.equals(login, person.login) && Objects.equals(password, person.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login, password);
    }
}
