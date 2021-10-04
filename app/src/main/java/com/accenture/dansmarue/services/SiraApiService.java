package com.accenture.dansmarue.services;

import com.accenture.dansmarue.mvp.models.Position;
import com.accenture.dansmarue.services.models.CategoryRequest;
import com.accenture.dansmarue.services.models.CategoryResponse;
import com.accenture.dansmarue.services.models.ChangeStatusRequest;
import com.accenture.dansmarue.services.models.ChangeStatusResponse;
import com.accenture.dansmarue.services.models.CheckVersionRequest;
import com.accenture.dansmarue.services.models.CheckVersionResponse;
import com.accenture.dansmarue.services.models.CongratulateAnomalieRequest;
import com.accenture.dansmarue.services.models.FollowRequest;
import com.accenture.dansmarue.services.models.GetIncidentByIdRequest;
import com.accenture.dansmarue.services.models.GetIncidentByIdResponse;
import com.accenture.dansmarue.services.models.GetIncidentsByPositionRequest;
import com.accenture.dansmarue.services.models.GetIncidentsByPositionResponse;
import com.accenture.dansmarue.services.models.GetIncidentsByUserRequest;
import com.accenture.dansmarue.services.models.GetIncidentsByUserResponse;
import com.accenture.dansmarue.services.models.IdentityRequest;
import com.accenture.dansmarue.services.models.IdentityResponse;
import com.accenture.dansmarue.services.models.IncidentResolvedRequest;
import com.accenture.dansmarue.services.models.ProcessWorkflowRequest;
import com.accenture.dansmarue.services.models.SaveIncidentRequest;
import com.accenture.dansmarue.services.models.SaveIncidentResponse;
import com.accenture.dansmarue.services.models.SiraRequest;
import com.accenture.dansmarue.services.models.SiraSimpleResponse;
import com.accenture.dansmarue.services.models.UnfollowRequest;

import java.util.Map;

import io.reactivex.Single;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by PK on 27/03/2017.
 * Retrofit interface for the SiraServices
 */
public interface SiraApiService {

    /**
     * Retrieve all the categories
     *
     * @param request request
     * @return All the categories
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<CategoryResponse> getCategories(@Field("jsonStream") CategoryRequest request);

    /**
     * Retrieve application version on play store
     *
     * @param request request
     * @return version info
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<CheckVersionResponse> checkVersion(@Field("jsonStream") CheckVersionRequest request);

    /**
     * Create a incident
     *
     * @param request request
     * @return Response containing the incident ID
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<SaveIncidentResponse> saveIncident(@Field("jsonStream") SaveIncidentRequest request);

    /**
     * Search for all the incident near a position (lont, lat)
     *
     * @param request request
     * @return Response the list of incidents near the given position
     */
    @FormUrlEncoded
    @POST("signalement/api")
    Single<GetIncidentsByPositionResponse> getIncidentsByPosition(@Field("jsonStream") GetIncidentsByPositionRequest request);

//    @POST("sira/signalement/api")


    /**
     * Follow an incident
     *
     * @param request request containing the incident id to follow
     * @return Response containing the status code
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<SiraSimpleResponse> follow(@Field("jsonStream") FollowRequest request);

    /**
     * Unfollow an incident
     *
     * @param request request containing the incident id to unfollow
     * @return Response containing the status code
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<SiraSimpleResponse> unfollow(@Field("jsonStream") UnfollowRequest request);

    /**
     * Retrive the details of an incident
     *
     * @param request request containing the incident id to get detail from
     * @return response containing the details
     */
    @FormUrlEncoded
    @POST("signalement/api")
    Single<GetIncidentByIdResponse> getIncidentById(@Field("jsonStream") GetIncidentByIdRequest request);

//    @POST("sira/signalement/api")


    /**
     * Attach a picture to an incident
     *
     * @param headers header map
     * @param picture picture file
     * @return status 0 is upload ok
     */
    @POST("signalement/api/photo")
    Single<ResponseBody> uploadPicture(@HeaderMap Map<String, String> headers, @Body RequestBody picture);

    /**
     * Launch the workflow post creation.<br>
     * This has to be call after all the pictures are uploaded so the incident is valid when the process is launch in the BO (email sending etc)
     *
     * @param request request containing the id of the newly created incident
     * @return Response containing the status code
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<SiraSimpleResponse> processWorkflow(@Field("jsonStream") ProcessWorkflowRequest request);


    /**
     * Retreive the list of incidents created and/or followed by a user
     *
     * @param request request containig the user id
     * @return list of incidents
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<GetIncidentsByUserResponse> getIncidentsByUser(@Field("jsonStream") GetIncidentsByUserRequest request);


    /**
     * Congratulate an anomaly
     *
     * @param request request
     * @return Response containing the status code
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<SiraSimpleResponse> congratulateAnomalie(@Field("jsonStream") CongratulateAnomalieRequest request);

    /**
     * Resolve an incident
     *
     * @param request request containig hte incident id to resolve
     * @return Response containing the status code
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<SiraSimpleResponse> incidentResolved(@Field("jsonStream") IncidentResolvedRequest request);

    /**
     * Resolve an incident
     *
     * @param request request containig the incident id to resolve
     * @return Response containing the status code
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<ChangeStatusResponse> changeStatus(@Field("jsonStream") ChangeStatusRequest request);

    /**
     * Retrieve user informations base on his guid (id compte parisien)
     *
     * @param request request containing a valid user guid
     * @return response containing firstname lastname and user email
     */
    @FormUrlEncoded
    @POST("signalement/identitystore")
    Single<IdentityResponse> getUserInformations(@Field("jsonStream") IdentityRequest request);

    @GET("signalement/isDmrOnline")
    Single<SiraSimpleResponse> isDmrOnline();

    /**
     * Find incident by number.
     * @param incidentNumber
     *     number to serach
     * @return response containing incidnet find
     */
    @GET("signalement/getAnomalieByNumber/{number}")
    Single<GetIncidentsByPositionResponse> getAnomalieByNumber(@Path("number") String incidentNumber);

    /**
     * Resolve an incident
     *
     * @param request request containig the incident id to resolve
     * @return Response containing the status code
     */
    @FormUrlEncoded
    @POST("signalement/api/")
    Single<SiraSimpleResponse> isEmailAgent(@Field("jsonStream") SiraRequest request);

}
