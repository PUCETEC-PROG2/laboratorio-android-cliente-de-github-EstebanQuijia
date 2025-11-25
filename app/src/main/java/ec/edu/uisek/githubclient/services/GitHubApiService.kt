package ec.edu.uisek.githubclient.services

import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {

    @GET("/user/repos")
    fun getRepos(
        @Query("sort") sort: String = "updated",
        @Query("direction") direction: String = "desc"
    ): Call<List<Repo>>

    @POST("/user/repos")
    fun addRepo(
        @Body repoRequest: RepoRequest
    ): Call<Repo>

    @PATCH("repos/{owner}/{repo}")
    fun editRepo(
        @Path("owner") owner: String,
        @Path("repo") repoName: String,
        @Body updatedRepo: RepoRequest
    ): Call<Repo>

    @DELETE("repos/{owner}/{repo}")
    fun deleteRepo(
        @Path("owner") owner: String,
        @Path("repo") repoName: String
    ): Call<ResponseBody>

}