package com.APISynIq.ApiResolver.Service;

import com.APISynIq.ApiResolver.Entity.DescriptionEmbeddingEntity;
import com.APISynIq.ApiResolver.Repository.EmbdRepo;
import com.apisyniq.grpc.InputData;
import com.apisyniq.grpc.InputsAndReturnsMatch;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.APISynIq.ApiResolver.Entity.SynIqData;
import com.APISynIq.ApiResolver.Repository.SynIqDataRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    @Async
    @Transactional
    public CompletableFuture<SynIqData> save(InputData data) {
        String generatedId = String.format("%s-%s", data.getHttpMethod(), data.getEndpoint());

        SynIqData synIqData = repository.findById(generatedId).orElse(new SynIqData());

        CompletableFuture<Void> inputEmbeddingFuture = CompletableFuture.runAsync(() -> {
            try {
                String newDesc = data.getDescription();
                String oldDesc = synIqData.getDescription();

                if (newDesc != null && !newDesc.equals(oldDesc)) {
                    float[] embd = generateEmbedding(newDesc);
                    DescriptionEmbeddingEntity descriptionEmbeddingEntity = new DescriptionEmbeddingEntity();
                    descriptionEmbeddingEntity.setEmbedding(embd);
                    descriptionEmbeddingEntity.setType("InputDescription");
                    DescriptionEmbeddingEntity saved = embdRepo.save(descriptionEmbeddingEntity);
                    synIqData.setInputDescriptionEmbedding(saved);
                }
            } catch (Exception e) {
                e.printStackTrace(); // optionally use logger
            }
        });

        CompletableFuture<Void> returnEmbeddingFuture = CompletableFuture.runAsync(() -> {
            try {
                String newDesc = data.getReturnDescription();
                String oldDesc = synIqData.getReturnDescription();

                if (newDesc != null && !newDesc.equals(oldDesc)) {
                    float[] embd = generateEmbedding(newDesc);
                    DescriptionEmbeddingEntity descriptionEmbeddingEntity = new DescriptionEmbeddingEntity();
                    descriptionEmbeddingEntity.setEmbedding(embd);
                    descriptionEmbeddingEntity.setType("ReturnDescription");
                    DescriptionEmbeddingEntity saved = embdRepo.save(descriptionEmbeddingEntity);
                    synIqData.setReturnDescriptionEmbedding(saved);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        CompletableFuture.allOf(inputEmbeddingFuture, returnEmbeddingFuture).join();
        synIqData.grpcToEntity(data);
        if (synIqData.getId() == null) {
            synIqData.setId(generatedId);
        }

        SynIqData savedEntity = repository.save(synIqData);

        return CompletableFuture.completedFuture(savedEntity);
    }


    @Transactional(readOnly = true)
    public InputsAndReturnsMatch queryForBoth(String input){
        float[] embeddingsForInput = embeddingModel.embed(input);
        CompletableFuture<List<SynIqData>> inputsFuture = findAllByInputDescription(embeddingsForInput);
        CompletableFuture<List<SynIqData>> returnsFuture = findAllByReturnDescription(embeddingsForInput);
        CompletableFuture.allOf(inputsFuture, returnsFuture).join();
        InputsAndReturnsMatch.Builder builder = InputsAndReturnsMatch.newBuilder();
        builder.addAllInputsMatchData(inputsFuture.join().stream().map(SynIqData::toGrpcInputData).collect(Collectors.toList()));
        builder.addAllReturnMatchData(returnsFuture.join().stream().map(SynIqData::toGrpcInputData).collect(Collectors.toList()));
        return builder.build();
    }

    @Transactional(readOnly = true)
    public List<SynIqData> inputsDesMatch(String input){
        float[] embeddingsForInput = embeddingModel.embed(input);
        CompletableFuture<List<SynIqData>> inputsFuture = findAllByInputDescription(embeddingsForInput);
        return inputsFuture.join();
    }

    @Transactional
    public List<SynIqData> returnDesMatch(String input){
        float[] embeddingsForInput = embeddingModel.embed(input);
        CompletableFuture<List<SynIqData>> returnsFuture = findAllByReturnDescription(embeddingsForInput);
        return returnsFuture.join();
    }

    @Transactional(readOnly = true)
    @Async
    public CompletableFuture<List<SynIqData>> findAllByInputDescription(float[] embeddingsForInput){
        List<Long> listDesInputs = jdbcClient.sql("""
            SELECT id
            FROM syniq_description_embeddings
            WHERE type = 'InputDescription'
            ORDER BY embedding <=> CAST(:user_promt AS vector)
            LIMIT 1
        """)
                .param("user_promt", embeddingsForInput)
                .query((rs, rowNum) -> rs.getLong("id"))
                .list();
        System.out.println(listDesInputs);
        return  CompletableFuture.completedFuture(repository.findAllByEmbeddingIds(listDesInputs));
    }

    @Transactional(readOnly = true)
    @Async
    public CompletableFuture<List<SynIqData>> findAllByReturnDescription(float[] embeddingsForInput){
        List<Long> listDesReturn = jdbcClient.sql("""
            SELECT id
            FROM syniq_description_embeddings
            WHERE type = 'ReturnDescription'
            ORDER BY embedding <=> CAST(:user_promt AS vector)
            LIMIT 1
        """)
                .param("user_promt", embeddingsForInput)
                .query((rs, rowNum) -> rs.getLong("id"))
                .list();
        System.out.println(listDesReturn);
        return  CompletableFuture.completedFuture(repository.findAllByReturnDescription(listDesReturn));
    }
}