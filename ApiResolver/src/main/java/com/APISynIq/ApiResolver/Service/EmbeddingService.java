package com.APISynIq.ApiResolver.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.APISynIq.ApiResolver.Entity.SynIqData;
import com.APISynIq.ApiResolver.Repository.SynIqDataRepository;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {
    private final EmbeddingModel embeddingModel;
private final SynIqDataRepository synIqDataRepository;
    public EmbeddingService(EmbeddingModel embeddingModel, SynIqDataRepository synIqDataRepository) {
        this.embeddingModel = embeddingModel;
        this.synIqDataRepository = synIqDataRepository;
    }
    @Autowired
    private JdbcClient jdbcClient;


    public float[] generateEmbedding(String input) {
        EmbeddingRequest request = new EmbeddingRequest(List.of(input), null);
        return embeddingModel.embed(input);
    }

    public List<SynIqData> queryModel(String input){
        EmbeddingRequest request = new EmbeddingRequest(List.of(input), null);
        float[] embeddingsForInput = embeddingModel.embed(input);
        String pgVectorLiteral = EmbedToString(embeddingsForInput);

        JdbcClient.StatementSpec query = jdbcClient.sql(
                        "SELECT name, endpoint, http_method,description, return_description, response_body, auto_execute, inputs, output_body,filtering_tags, dto_schemas, describe_dtos_for_parms FROM syniq_data " +
                                "WHERE 1 - (description_embedding <=> CAST(:user_promt AS vector)) > 0.3 " +
                                "ORDER BY description_embedding <=> CAST(:user_promt AS vector) LIMIT 1")
                .param("user_promt", pgVectorLiteral);

        return query.query(SynIqData.class).list();

    }

    public String EmbedToString(float[] embeddingsForInput){
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < embeddingsForInput.length; i++) {
            builder.append(embeddingsForInput[i]);
            if (i < embeddingsForInput.length - 1) builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }


    
}
