package com.APISynIq.ApiResolver.Service;

import com.APISynIq.ApiResolver.Entity.DescriptionEmbeddingEntity;
import com.APISynIq.ApiResolver.Repository.EmbdRepo;
import com.apisyniq.grpc.EndpointData;
import com.apisyniq.grpc.InputsAndReturnsMatch;
import org.hibernate.Hibernate;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.APISynIq.ApiResolver.Entity.EndpointDataEntity;
import com.APISynIq.ApiResolver.Repository.SynIqDataRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
    public CompletableFuture<EndpointDataEntity> save(EndpointData data) {
        String generatedId = String.format("%s-%s", data.getHttpMethod(), data.getEndpoint());
        System.out.println(generatedId);
        EndpointDataEntity endpointDataEntity = repository.findById(generatedId).orElse(new EndpointDataEntity());
        CompletableFuture<Void> inputEmbeddingFuture = CompletableFuture.runAsync(() -> {
            try {
                String newDesc = data.getDescription();
                String oldDesc = endpointDataEntity.getDescription();

                if (newDesc != null && !newDesc.equals(oldDesc)) {
                    float[] embd = generateEmbedding(newDesc);
                    DescriptionEmbeddingEntity descriptionEmbeddingEntity = new DescriptionEmbeddingEntity();
                    descriptionEmbeddingEntity.setEmbedding(embd);
                    descriptionEmbeddingEntity.setType("InputDescription");
                    DescriptionEmbeddingEntity saved = embdRepo.save(descriptionEmbeddingEntity);
                    endpointDataEntity.setInputDescriptionEmbedding(saved);
                }
            } catch (Exception e) {
                e.printStackTrace(); // optionally use logger
            }
        });

        CompletableFuture<Void> returnEmbeddingFuture = CompletableFuture.runAsync(() -> {
            try {
                String newDesc = data.getReturnDescription();
                String oldDesc = endpointDataEntity.getReturnDescription();

                if (newDesc != null && !newDesc.equals(oldDesc)) {
                    float[] embd = generateEmbedding(newDesc);
                    DescriptionEmbeddingEntity descriptionEmbeddingEntity = new DescriptionEmbeddingEntity();
                    descriptionEmbeddingEntity.setEmbedding(embd);
                    descriptionEmbeddingEntity.setType("ReturnDescription");
                    DescriptionEmbeddingEntity saved = embdRepo.save(descriptionEmbeddingEntity);
                    endpointDataEntity.setReturnDescriptionEmbedding(saved);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        CompletableFuture.allOf(inputEmbeddingFuture, returnEmbeddingFuture).join();
        EndpointDataEntity dataToSave = new  EndpointDataEntity();
        dataToSave.grpcToEntity(data);
        dataToSave.setInputDescriptionEmbedding(endpointDataEntity.getInputDescriptionEmbedding());
        dataToSave.setReturnDescriptionEmbedding(endpointDataEntity.getReturnDescriptionEmbedding());
        dataToSave.setId(generatedId);

        EndpointDataEntity savedEntity = repository.save(dataToSave);

        return CompletableFuture.completedFuture(savedEntity);
    }


    @Transactional(readOnly = true)
    public InputsAndReturnsMatch queryForBoth(String input, int limit){
        float[] embeddingsForInput = embeddingModel.embed(input);
        CompletableFuture<List<EndpointDataEntity>> inputsFuture = findAllByInputDescription(embeddingsForInput, limit);
        CompletableFuture<List<EndpointDataEntity>> returnsFuture = findAllByReturnDescription(embeddingsForInput, limit);
        CompletableFuture.allOf(inputsFuture, returnsFuture).join();
        InputsAndReturnsMatch.Builder builder = InputsAndReturnsMatch.newBuilder();
        builder.addAllInputsMatchData(inputsFuture.join().stream().map(EndpointDataEntity::toGrpcEndpointData).collect(Collectors.toList()));
        builder.addAllReturnMatchData(returnsFuture.join().stream().map(EndpointDataEntity::toGrpcEndpointData).collect(Collectors.toList()));
        return builder.build();
    }

    @Transactional(readOnly = true)
    public List<EndpointData> inputsDesMatch(String input, int limit){
        float[] embeddingsForInput = embeddingModel.embed(input);
        CompletableFuture<List<EndpointDataEntity>> inputsFuture = findAllByInputDescription(embeddingsForInput, limit);
        return inputsFuture.join().stream().map(EndpointDataEntity::toGrpcEndpointData).collect(Collectors.toList());
    }

    @Transactional
    public List<EndpointData> returnDesMatch(String input, int limit){
        float[] embeddingsForInput = embeddingModel.embed(input);
        CompletableFuture<List<EndpointDataEntity>> returnsFuture = findAllByReturnDescription(embeddingsForInput, limit);
        return returnsFuture.join().stream().map(EndpointDataEntity::toGrpcEndpointData).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Async
    public CompletableFuture<List<EndpointDataEntity>> findAllByInputDescription(float[] embeddingsForInput, int limit) {
        List<Long> listDesInputs = jdbcClient.sql("""
            SELECT id
            FROM syniq_description_embeddings
            WHERE type = 'InputDescription'
            ORDER BY embedding <=> CAST(:user_promt AS vector)
            LIMIT :limit
        """)
                .param("user_promt", embeddingsForInput)
                .param("limit", limit)
                .query((rs, rowNum) -> rs.getLong("id"))
                .list();

        System.out.println(listDesInputs);

        // fetch entities and initialize lazy fields
        List<EndpointDataEntity> entities = repository.findAllByEmbeddingIds(listDesInputs);

        return CompletableFuture.completedFuture(entities);
    }

    @Transactional(readOnly = true)
    @Async
    public CompletableFuture<List<EndpointDataEntity>> findAllByReturnDescription(float[] embeddingsForInput, Integer limit){
        List<Long> listDesReturn = jdbcClient.sql("""
            SELECT id
            FROM syniq_description_embeddings
            WHERE type = 'ReturnDescription'
            ORDER BY embedding <=> CAST(:user_promt AS vector)
            LIMIT :limit
        """)
                .param("user_promt", embeddingsForInput)
                .param("limit", limit)
                .query((rs, rowNum) -> rs.getLong("id"))
                .list();
        System.out.println(listDesReturn);
        return  CompletableFuture.completedFuture(repository.findAllByReturnDescription(listDesReturn));
    }
}