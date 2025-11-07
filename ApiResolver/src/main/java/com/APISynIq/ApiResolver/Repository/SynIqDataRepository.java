package com.APISynIq.ApiResolver.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.APISynIq.ApiResolver.Entity.SynIqData;

import java.util.List;


@Repository
public interface SynIqDataRepository extends JpaRepository<SynIqData, Long>  {
    @Query(value = "SELECT * FROM syniq_data ORDER BY description_embedding <-> CAST(:embedding AS vector) LIMIT 1", nativeQuery = true)
    List<SynIqData> findTop5ByEmbeddingSimilarity(@Param("embedding") float[] embedding);

}
