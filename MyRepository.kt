package com.example.workmanager

import android.content.Context
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MyRepository(context: Context) {
    private val baseURL = "https://api.github.com/"
    private val api = retrofitInit(baseURL)

    private val myDao = MyDatabase.getDatabase(context).myDao
    val repos = myDao.getAll() // LiveData<List<ReposD>

    suspend fun refreshData(username : String) {
        withContext(Dispatchers.IO) {

            val repos = api.listRepos(username)
            // convert Repo to RepoD
            val repoDs = repos.map {
                RepoD(it.name, it.owner.login)
            }
            myDao.insertAll(repoDs)
        }
    }
}

