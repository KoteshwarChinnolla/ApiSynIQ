package com.APISynIq.ApiResolver.Repository;

import com.APISynIq.ApiResolver.Entity.DescriptionEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmbdRepo extends JpaRepository<DescriptionEmbeddingEntity, Long> {
}
