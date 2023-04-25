package com.github.juliherms.task;

import com.github.juliherms.project.Project;
import com.github.juliherms.user.User;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "tasks")
public class Task extends PanacheEntity {

    @Column(nullable = false)
    public String title;

    @Column(length = 1000)
    public String description;

    public Integer priority;

    @ManyToOne(optional = false)
    public User user;

    public ZonedDateTime complete;

    @ManyToOne
    public Project project;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    public ZonedDateTime created;

    @Version
    public int version;
}
