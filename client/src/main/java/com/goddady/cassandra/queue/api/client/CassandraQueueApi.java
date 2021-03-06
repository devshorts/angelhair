package com.goddady.cassandra.queue.api.client;

import com.squareup.okhttp.ResponseBody;
import retrofit.Call;
import retrofit.JacksonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface CassandraQueueApi {

    static CassandraQueueApi createClient(String baseUri) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUri)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        CassandraQueueApi service = retrofit.create(CassandraQueueApi.class);

        return service;
    }

    @POST("v1/queues")
    Call<ResponseBody> createQueue(@Body QueueCreateOptions queueName);

    @GET("v1/queues/{queueName}/messages/next")
    Call<GetMessageResponse> getMessage(@Path("queueName") QueueName queueName);

    @GET("v1/queues/{queueName}/messages/next")
    Call<GetMessageResponse> getMessage(@Path("queueName") QueueName queueName, @Query("invisibilityTime") Long invisibilityTimeSeconds);

    @POST("v1/queues/{queueName}/messages")
    Call<ResponseBody> addMessage(@Path("queueName") QueueName queueName, @Body String message);

    @POST("v1/queues/{queueName}/messages")
    Call<ResponseBody> addMessage(
            @Path("queueName") QueueName queueName,
            @Body String message,
            @Query("initialInvisibilitySeconds") Long initialInvisibilitySeconds);

    @DELETE("v1/queues/{queueName}/messages")
    Call<ResponseBody> ackMessage(
            @Path("queueName") QueueName queueName,
            @Query("popReceipt") String popReceipt);


}
