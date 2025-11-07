package com.APISynIq.ApiResolver.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.APISynIq.ApiResolver.Entity.SynIqData;
import com.APISynIq.ApiResolver.Repository.SynIqDataRepository;

@Service
public class SynIqDataService {

    @Autowired
    private SynIqDataRepository repository;

    @Autowired
    private EmbeddingService embeddingService;

    public SynIqData save(SynIqData data) {
        System.out.println(data);
        if (data.getDescription() != null) {
            data.setDescriptionEmbedding(
                    embeddingService.generateEmbedding(data.getDescription()));
        }
        System.out.println("First step passes");

        if (data.getReturnDescription() != null) {
            data.setReturnDescriptionEmbedding(
                    embeddingService.generateEmbedding(data.getReturnDescription()));
        }
        System.out.println("second step passes");
        return repository.save(data);
    }
}