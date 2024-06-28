package org.example.champicker.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "champion")
public class Champion {

    @Id
    private String name;

    public Champion() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
