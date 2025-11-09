package com.APISynIq.ApiResolver.Service;

import com.APISynIq.ApiResolver.Entity.DescriptionEmbeddingEntity;
import com.APISynIq.ApiResolver.Repository.EmbdRepo;
import com.apisyniq.grpc.InputData;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import com.APISynIq.ApiResolver.Entity.SynIqData;
import com.APISynIq.ApiResolver.Repository.SynIqDataRepository;

import java.util.List;

@Service
public class SynIqDataService {
    private final SynIqDataRepository repository;
    private final EmbdRepo embdRepo;
    private final EmbeddingModel embeddingModel;
    private final JdbcClient jdbcClient;

    // Spring automatically injects dependencies via constructor
    public SynIqDataService(SynIqDataRepository repository,
                            EmbdRepo embdRepo,
                            EmbeddingModel embeddingModel,
                            JdbcClient jdbcClient) {
        this.repository = repository;
        this.embdRepo = embdRepo;
        this.embeddingModel = embeddingModel;
        this.jdbcClient = jdbcClient;
    }

    public float[] generateEmbedding(String input) {
        return embeddingModel.embed(input);
    }

    public SynIqData save(InputData data) {
        SynIqData synIqData = new SynIqData();
        synIqData.grpcToEntity(data);
        if (data.getDescription() != null) {
            float[] embd = generateEmbedding(data.getDescription());
            DescriptionEmbeddingEntity descriptionEmbeddingEntity = new DescriptionEmbeddingEntity();
            descriptionEmbeddingEntity.setEmbedding(embd);
            descriptionEmbeddingEntity.setType("InputDescription");
            DescriptionEmbeddingEntity saved = embdRepo.save(descriptionEmbeddingEntity);
            synIqData.setInputDescriptionEmbedding(saved);
        }
        if (data.getReturnDescription() != null) {
            float[] embd = generateEmbedding(data.getReturnDescription());
            DescriptionEmbeddingEntity descriptionEmbeddingEntity = new DescriptionEmbeddingEntity();
            descriptionEmbeddingEntity.setEmbedding(embd);
            descriptionEmbeddingEntity.setType("ReturnDescription");
            DescriptionEmbeddingEntity saved = embdRepo.save(descriptionEmbeddingEntity);
            synIqData.grpcToEntity(data);
            synIqData.setReturnDescriptionEmbedding(saved);
        }
        return repository.save(synIqData);
    }

    public List<SynIqData> queryModel(String input){
        float[] embeddingsForInput = embeddingModel.embed(input);
        List<Long> listDes = jdbcClient.sql("""
                    SELECT id
                    FROM syniq_description_embeddings
                    ORDER BY embedding <=> CAST(:user_promt AS vector)
                    LIMIT 1
                """)
                .param("user_promt", embeddingsForInput)
                .query((rs, rowNum) -> rs.getLong("id"))
                .list();
        return repository.findAllByEmbeddingIds(listDes);
    }
}