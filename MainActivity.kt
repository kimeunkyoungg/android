package com.example.workmanager

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

// The repository pattern is a strategy for abstracting data access.
// ViewModel delegates the data-fetching process to the repository.

class MyAdapter(val items:List<Repo>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>(){

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvOwner = v.findViewById<TextView>(R.id.tvOwner)
        val tvRepo = v.findViewById<TextView>(R.id.tvRepo)

        fun bind(repo: Repo) {
            tvRepo.text = repo.name
            tvOwner.text = repo.owner.login
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_layout, parent, false)
        val viewHolder = MyViewHolder(view)

        return viewHolder

    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //holder.textView.text=items[position].name
        val repo = items[position]
        holder.bind(repo)
    }

}

class MainActivity : AppCompatActivity() {
    private lateinit var myViewModel : MyViewModel
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: MyAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        //리싸이클러뷰 초기화
        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = MyAdapter(emptyList())

        myViewModel = ViewModelProvider(this, MyViewModel.Factory(this)).get(MyViewModel::class.java)
        findViewById<Button>(R.id.startWorker).setOnClickListener {
            val username = findViewById<EditText>(R.id.editUsername).text.toString()
            if(username.isNotEmpty()) {
                startWorker(username)
            }

            }
        findViewById<Button>(R.id.stopWorker).setOnClickListener { stopWorker() }


        /* 실습코드
        myViewModel.repos.observe(this) { repos ->
            val response = StringBuilder().apply {
                repos.forEach {
                    append(it.name)
                    append(" - ")
                    append(it.owner)
                    append("\n")
                }
            }.toString()
            findViewById<TextView>(R.id.editUsername).text = response
        }


         */

        myViewModel.repos.observe(this) { reposD ->

            val repos = reposD.map {
                Repo(it.name, Owner(it.owner), "")
            }

            recyclerview.adapter = MyAdapter(repos)
        }

        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData(MyWorker.name)
            .observe(this) { workInfo ->
                if (workInfo.isNotEmpty()) {
                    when (workInfo[0].state) {
                        WorkInfo.State.ENQUEUED -> println("Worker enqueued!")
                        WorkInfo.State.RUNNING -> println("Worker running!")
                        WorkInfo.State.SUCCEEDED -> println("Worker succeeded!")  // only for one time worker
                        WorkInfo.State.CANCELLED -> println("Worker cancelled!")
                        else -> println(workInfo[0].state)
                    }
                }
            }
    }

    private fun startWorker(username : String) {
        //val oneTimeRequest = OneTimeWorkRequest.Builder<MyWorker>()
        //        .build()

        //val username = findViewById<EditText>(R.id.editUsername)

        val constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.UNMETERED) // un-metered network such as WiFi
            setRequiresBatteryNotLow(true)
            //setRequiresCharging(true)
            // setRequiresDeviceIdle(true) // android 6.0(M) or higher
        }.build()

        //val repeatingRequest = PeriodicWorkRequestBuilder<MyWorker>(1, TimeUnit.DAYS)
        val repeatingRequest = PeriodicWorkRequestBuilder<MyWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(workDataOf("username" to username))
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            MyWorker.name,
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest)



    }

    private fun stopWorker() {
        // to stop the MyWorker
        WorkManager.getInstance(this).cancelUniqueWork(MyWorker.name)
    }
}
//버튼 누르면 종료되버림 ->수정하기	Start Worker(ID: startWorker) 버튼을 누르면 입력 받은 username을 Worker로 전달하고, Worker에서 MyRepository의 refreshData의 인자로 전달한다.