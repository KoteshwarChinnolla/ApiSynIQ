package com.APISynIq.ApiResolver.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.APISynIq.ApiResolver.Entity.SynIqData;


@Repository
public interface SynIqDataRepository extends JpaRepository<SynIqData, Long>  {   
}
