package com.example.launcherapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appRecyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter
    private lateinit var searchEditText: EditText
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var allApps: List<AppInfo> = listOf()
    private lateinit var searchContainer: View
    private lateinit var favoriteAppsRecycler: RecyclerView
    private lateinit var favoriteAppsAdapter: AppListAdapter
    private lateinit var clockText: TextView
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val clockHandler = Handler(Looper.getMainLooper())
    private val updateClockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            clockHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clockText = findViewById(R.id.clockText)
        startClock()

        appRecyclerView = findViewById(R.id.appRecyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        searchContainer = findViewById(R.id.searchContainer)
        val bottomSheet = findViewById<ConstraintLayout>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)

        appRecyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve installed applications
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        allApps = packages.filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .map { AppInfo(
                it.loadLabel(packageManager).toString(),
                it.packageName,
                packageManager.getLaunchIntentForPackage(it.packageName)
            ) }.sortedBy { it.name }

        // Initialize empty adapter first
        appListAdapter = AppListAdapter(mutableListOf()) { appInfo ->
            appInfo.launchIntent?.let { startActivity(it) }
        }.apply {
            setOnLongClickListener { appInfo ->
                val packageName = appInfo.packageName
                if (packageName in FavoriteApps.getFavoriteApps(this@MainActivity)) {
                    FavoriteApps.removeFavoriteApp(this@MainActivity, packageName)
                } else {
                    FavoriteApps.addFavoriteApp(this@MainActivity, packageName)
                }
                updateFavoriteApps()
                true
            }
        }

        appRecyclerView.adapter = appListAdapter
        setupSearch()
        setupBottomSheet()

        val fastScroller = findViewById<FastScroller>(R.id.fastScroller)
        fastScroller.attachToRecyclerView(appRecyclerView)

        favoriteAppsRecycler = findViewById(R.id.favoriteAppsRecycler)
        favoriteAppsRecycler.layoutManager = LinearLayoutManager(this)

        // Initialize favorite apps
        val favoritePackages = FavoriteApps.getFavoriteApps(this)
        val favoriteApps = allApps
            .filter { it.packageName in favoritePackages }
            .sortedByDescending { it.name.length }
        favoriteAppsAdapter = AppListAdapter(favoriteApps.toMutableList()) { appInfo ->
            appInfo.launchIntent?.let { startActivity(it) }
        }
        favoriteAppsRecycler.adapter = favoriteAppsAdapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s.toString())
            }
        })
    }

    private fun filterApps(query: String) {
        val filteredApps = if (query.isEmpty()) {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                allApps
            } else {
                allApps.take(4)
            }
        } else {
            allApps.filter { it.name.contains(query, ignoreCase = true) }
        }
        appListAdapter.updateApps(filteredApps)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior.apply {
            peekHeight = 120
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheet.setBackgroundColor(0xFF000000.toInt())
                        searchContainer.visibility = View.VISIBLE
                        findViewById<FastScroller>(R.id.fastScroller).visibility = View.VISIBLE
                        filterApps(searchEditText.text.toString())
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        bottomSheet.setBackgroundColor(0xFF000000.toInt())
                        searchContainer.visibility = View.GONE
                        findViewById<FastScroller>(R.id.fastScroller).visibility = View.GONE
                        filterApps("")
                    }
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // Gradually show/hide search bar during slide
                    searchContainer.alpha = slideOffset
                    val fastScroller = findViewById<FastScroller>(R.id.fastScroller)
                    fastScroller.alpha = slideOffset
                    if (slideOffset > 0) {
                        if (searchContainer.visibility == View.GONE) {
                            searchContainer.visibility = View.VISIBLE
                        }
                        if (fastScroller.visibility == View.GONE) {
                            fastScroller.visibility = View.VISIBLE
                        }
                        bottomSheet.setBackgroundColor(0xFF000000.toInt())
                    } else if (slideOffset == 0f) {
                        searchContainer.visibility = View.GONE
                        fastScroller.visibility = View.GONE
                        bottomSheet.setBackgroundColor(0xFF000000.toInt())
                    }
                }
            })
        }
    }

    private fun updateFavoriteApps() {
        val favoritePackages = FavoriteApps.getFavoriteApps(this)
        val favoriteApps = allApps
            .filter { it.packageName in favoritePackages }
            .sortedByDescending { it.name.length }
        favoriteAppsAdapter.updateApps(favoriteApps)
    }

    private fun updateClock() {
        clockText.text = timeFormat.format(Date())
    }

    private fun startClock() {
        updateClock()
        clockHandler.post(updateClockRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        clockHandler.removeCallbacks(updateClockRunnable)
    }
}