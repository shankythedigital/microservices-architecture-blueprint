package com.example.authservice.model;

import jakarta.persistence.*;
import com.example.common.jpa.BaseEntity;

@Entity
@Table(name="roles")
public class Role extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique=true,nullable=false)
    private String name;

    public Long getId(){return id;}
    public void setId(Long v){this.id=v;}
    public String getName(){return name;}
    public void setName(String v){this.name=v;}
}
