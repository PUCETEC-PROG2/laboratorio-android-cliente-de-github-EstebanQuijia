package ec.edu.uisek.githubclient

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ec.edu.uisek.githubclient.databinding.ActivityRepoFormBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.models.RepoRequest
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Constantes para pasar datos al Intent
const val EXTRA_REPO_NAME = "extra_repo_name"
const val EXTRA_REPO_DESCRIPTION = "extra_repo_description"
const val EXTRA_IS_EDIT_MODE = "extra_is_edit_mode"

class RepoForm : AppCompatActivity() {

    private lateinit var repoFormBinding: ActivityRepoFormBinding

    // Variables NUEVAS para el modo Edición
    private var isEditMode = false
    private var originalRepoName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repoFormBinding = ActivityRepoFormBinding.inflate(layoutInflater)
        setContentView(repoFormBinding.root)

        handleIntentData() // Revisa el Intent para ver si es Edición
        setupUiMode()      // Configura la UI (título, campo no editable)

        repoFormBinding.btnCancelNewProject.setOnClickListener {
            finish()
        }

        // MODIFICADO: Llama a la función que decide (POST o PATCH)
        repoFormBinding.btnSaveNewProject.setOnClickListener {
            performRepoAction()
        }
    }

    // ** Funciones para el Modo Dual **

    private fun handleIntentData() {
        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT_MODE, false)

        if (isEditMode) {
            originalRepoName = intent.getStringExtra(EXTRA_REPO_NAME)
            val description = intent.getStringExtra(EXTRA_REPO_DESCRIPTION)

            repoFormBinding.etNewProjectName.setText(originalRepoName)
            repoFormBinding.etNewProjectDetails.setText(description)
        }
    }

    private fun setupUiMode() {

        if (isEditMode) {
            supportActionBar?.title = "Editar Repositorio"
            // REQUISITO: Deshabilita el campo Nombre
            repoFormBinding.etNewProjectName.isEnabled = false
            repoFormBinding.etNewProjectName.isFocusable = false
        } else {
            supportActionBar?.title = "Crear Nuevo Repositorio"
            repoFormBinding.etNewProjectName.isEnabled = true
            repoFormBinding.etNewProjectName.isFocusableInTouchMode = true
        }
    }

    private fun performRepoAction() {
        if (!validateForm()) {
            return
        }

        if (isEditMode && originalRepoName != null) {
            editRepo(originalRepoName!!) // Llama a PATCH
        } else {
            createdRepo() // Llama a POST
        }
    }

    // ** Función de Edición (@PATCH) **

    private fun editRepo(repoName: String) {
        val repoDescription = repoFormBinding.etNewProjectDetails.text.toString()
        val repoRequest: RepoRequest = RepoRequest(
            name = repoName,
            description = repoDescription
        )

        val owner = "EstebanQuijia" // ¡Reemplaza con tu usuario!

        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.editRepo(owner, repoName, repoRequest) // Llama a @PATCH

        call.enqueue(object : Callback<Repo>{
            override fun onResponse(call: Call<Repo?>, response: Response<Repo?>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio editado exitosamente")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    showMessage("Error al editar: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Repo?>, t: Throwable) {
                showMessage("Error de red al editar: ${t.message}")
            }
        })
    }

    // ** Función de Creación (@POST) **

    private fun createdRepo(){
        if (!validateForm()) {
            return
        }
        val repoName = repoFormBinding.etNewProjectName.text.toString()
        val repoDescription = repoFormBinding.etNewProjectDetails.text.toString()

        val repoRequest: RepoRequest = RepoRequest(
            name = repoName,
            description = repoDescription
        )

        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.addRepo(repoRequest)

        call.enqueue(object : Callback<Repo>{
            override fun onResponse(call: Call<Repo?>, response: Response<Repo?>) {
                if (response.isSuccessful) {
                    showMessage("El repositorio fue creado exitosamente")
                    setResult(RESULT_OK)
                    finish()
                } else{
                    showMessage("Error al crear: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Repo?>, t: Throwable) {
                showMessage("Error de red: ${t.message}")
            }
        })
    }

    // ** Funciones Auxiliares **

    private fun validateForm(): Boolean {
        // ... (Tu código de validación existente, solo modificado para ignorar el nombre si isEditMode es true)
        val repoName = repoFormBinding.etNewProjectName.text.toString()

        if (repoName.isBlank()) {
            repoFormBinding.etNewProjectName.error = "El nombre del repositorio es requerido"
            return false
        }

        if (!isEditMode && repoName.contains(" ")) {
            repoFormBinding.etNewProjectName.error = "El nombre del repositorio no puede contener espacios"
            return false
        }
        return true
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}