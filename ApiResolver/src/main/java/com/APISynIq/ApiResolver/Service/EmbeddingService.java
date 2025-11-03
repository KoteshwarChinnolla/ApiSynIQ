package com.APISynIq.ApiResolver.Service;

import java.util.List;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String input) {
        EmbeddingRequest request = new EmbeddingRequest(List.of(input), null);
        return embeddingModel.call(request).getResults().get(0).getOutput();
    }


    
}
