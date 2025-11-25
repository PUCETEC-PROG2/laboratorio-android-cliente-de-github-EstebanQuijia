package ec.edu.uisek.githubclient

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ec.edu.uisek.githubclient.databinding.FragmentRepoItemBinding
import ec.edu.uisek.githubclient.models.Repo

// 1. Clase ViewHolder (SIN CAMBIOS, ya está correcto para recibir las acciones)
class RepoViewHolder(private val binding: FragmentRepoItemBinding) : RecyclerView.ViewHolder(binding.root) {

    // Función para vincular datos y configurar los Listeners
    fun bind(repo: Repo,
             onEditClick: (Repo) -> Unit, // Recibe la función de Editar
             onDeleteClick: (Repo) -> Unit // Recibe la función de Eliminar
    ) {
        binding.repoName.text = repo.name
        binding.repoDescription.text = repo.description ?: "El repositorio no tiene descripción"
        binding.repoLanguage.text = repo.language ?: "Lenguaje no especificado"

        Glide.with(binding.root.context)
            .load(repo.owner.avatarUrl)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .circleCrop()
            .into(binding.repoOwnerImage)

        // Llama a la acción de edición definida en MainActivity
        binding.btnEditRepo.setOnClickListener {
            onEditClick(repo)
        }

        // Llama a la acción de eliminación definida en MainActivity
        binding.btnDeleteRepo.setOnClickListener {
            onDeleteClick(repo)
        }
    }
}

// 2. Clase Adapter (MODIFICADA: Se añade el constructor)
class ReposAdapter(
    // NUEVO: El constructor ahora requiere las dos funciones de la Activity
    private val onEditClick: (Repo) -> Unit,
    private val onDeleteClick: (Repo) -> Unit
) : RecyclerView.Adapter<RepoViewHolder>() {

    private var repositories : List<Repo> = emptyList()
    override fun getItemCount(): Int = repositories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoViewHolder {
        val binding = FragmentRepoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RepoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = repositories[position]

        // MODIFICADO: Ahora pasamos las funciones REALES recibidas en el constructor
        // **Se eliminó el código de simulación con Toast.**
        holder.bind(repo, onEditClick, onDeleteClick)
    }

    fun updateRepositories(newRepos: List<Repo>) {
        repositories = newRepos
        notifyDataSetChanged()
    }
}