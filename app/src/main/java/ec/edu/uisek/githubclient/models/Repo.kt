package ec.edu.uisek.githubclient.models

import org.intellij.lang.annotations.Language

data class Repo(
    val id: Long,
    val name: String,
    val description: String?,
    val language: String?,
    val owner: RepoOwner
)
