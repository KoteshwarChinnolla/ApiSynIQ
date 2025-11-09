package com.APISynIq.ApiResolver.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "syniq_description_embeddings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DescriptionEmbeddingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "embedding", columnDefinition = "vector(768)")
    @JsonIgnore
    private float[] embedding;
    private String type;
}

