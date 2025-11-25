package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fabNewTask.setOnClickListener {
            displayNewRepoForm()
        }

    }

    override fun onResume(){
        super.onResume()
        setupRecyclerView()
        fetchRepositories()
    }

    private fun setupRecyclerView() {
        // 1. Definimos la acción de Editar (Lanza el formulario)
        val editAction: (Repo) -> Unit = { repo -> editRepo(repo) }

        // 2. Definimos la acción de Eliminar (Llama directamente a la API DELETE)
        // Usamos performDeleteRepo() que es la función que llama a Retrofit.
        val deleteAction: (Repo) -> Unit = { repo -> performDeleteRepo(repo.name) }

        // 3. Inicializamos el adaptador UNA SOLA VEZ, pasándole las acciones.
        reposAdapter = ReposAdapter(editAction, deleteAction)

        // 4. Configuramos el RecyclerView.
        binding.repoRecyclerView.apply {
            adapter = reposAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun fetchRepositories() {
        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.getRepos()

        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("No se encontraron repositorios")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Error de autenticación. Revisa tu token."
                        403 -> "Prohibido"
                        404 -> "No encontrado"
                        else -> "Error: ${response.code()}"
                    }
                    Log.e("MainActivity", errorMsg)
                    showMessage(errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                //no hay conexion a red
                val errorMsg = "Error de conexión: ${t.message}"
                Log.e("MainActivity", "Error de conexión", t)
                showMessage(errorMsg)
            }
        })
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun displayNewRepoForm() {
        Intent(this, RepoForm::class.java).apply {
            startActivity(this)
        }
    }
    private fun editRepo(repo: Repo) {
        Intent(this, RepoForm::class.java).apply {
            // Pasamos la bandera y los datos que el RepoForm usará
            putExtra(EXTRA_IS_EDIT_MODE, true)
            putExtra(EXTRA_REPO_NAME, repo.name)
            putExtra(EXTRA_REPO_DESCRIPTION, repo.description)
            startActivity(this)
        }
    }

    private fun performDeleteRepo(repoName: String) {
        val apiService = RetrofitClient.gitHubApiService
        val owner = "EstebanQuijia" // ¡Reemplaza con tu usuario!

        val call = apiService.deleteRepo(owner, repoName)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio '$repoName' eliminado exitosamente.")
                    fetchRepositories() // Refresca la lista inmediatamente
                } else {
                    showMessage("Error al eliminar: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showMessage("Error de red al intentar eliminar.")
            }
        })
    }
}
