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
        if (data.getDescription() != null) {
            data.setDescriptionEmbedding(
                    embeddingService.generateEmbedding(data.getDescription()));
        }

        if (data.getReturnDescription() != null) {
            data.setReturnDescriptionEmbedding(
                    embeddingService.generateEmbedding(data.getReturnDescription()));
        }

        return repository.save(data);
    }
}