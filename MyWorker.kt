package com.example.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MyWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val repository = MyRepository(applicationContext) //직접 repository 객체 만들기
        val username = inputData.getString("username")?:"" //
        try {
            repository.refreshData(username)
        } catch (e: Exception) {
            return Result.retry() //네트워크 오류등으로 다시 시도
        }
        return Result.success() //성공일 경우 리턴
    }

    companion object { //worker식별자로 사용할 이름
        const val name = "com.example.workmanager.MyWorker"
    }
}