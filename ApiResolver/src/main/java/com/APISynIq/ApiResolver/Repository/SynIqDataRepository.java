package com.APISynIq.ApiResolver.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.APISynIq.ApiResolver.Entity.SynIqData;

import java.util.List;


@Repository
public interface SynIqDataRepository extends JpaRepository<SynIqData, Long>  {
    @Query(value = "SELECT * FROM syniq_data WHERE description_embedding_id IN (:ids)", nativeQuery = true)
    List<SynIqData> findAllByEmbeddingIds(@Param("ids") List<Long> ids);
}