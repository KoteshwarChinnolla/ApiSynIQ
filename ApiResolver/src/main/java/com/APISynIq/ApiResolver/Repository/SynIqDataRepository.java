package com.APISynIq.ApiResolver.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.APISynIq.ApiResolver.Entity.EndpointDataEntity;

import java.util.List;


@Repository
public interface SynIqDataRepository extends JpaRepository<EndpointDataEntity, String>  {
    @Query(value = "SELECT * FROM syniq_data WHERE input_description_embedding_id IN (:ids)", nativeQuery = true)
    List<EndpointDataEntity> findAllByEmbeddingIds(@Param("ids") List<Long> ids);

    @Query(value = "SELECT * FROM syniq_data WHERE return_description_embedding_id IN (:ids)", nativeQuery = true)
    List<EndpointDataEntity> findAllByReturnDescription(@Param("ids") List<Long> ids);


}