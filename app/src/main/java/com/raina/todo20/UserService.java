package com.raina.todo20;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserService {

    @POST("auth/login/")
    Call<LoginResponse> userLogin(@Body LoginRequest loginRequest);

    @POST("auth/register/")
    Call<UserResponse> saveUser(@Body UserRequest userRequest);

    @GET("auth/profile/")
    Call<UserResponse> getAllUsers(@Header("Authorization")String authToken);

    @GET("todo/")
    Call<List<TaskResponse>> getAllTasks(@Header("Authorization")String AuthToken);

    @POST("todo/create/")
    Call<ResponseBody> newTask(@Body TaskRequest taskRequest, @Header("Authorization")String authToken);

    @PATCH("todo/{id}/")
    Call<TaskResponse> patchTask(@Header("Authorization")String AuthToken, @Body TaskRequest taskRequest,
                                 @Path("id")int id);

    @DELETE("todo/{id}/")
    Call<Void> Delete(@Header("Authorization")String AuthToken, @Path("id")int id);



}
