package com.cla.adapter.demo

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.cla.round.view.widget.ClaRoundTextView
import com.cla.adapter.library.*
import com.cla.adapter.library.holder.ClaBaseViewHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val Int.dp: Int
    get() {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

class MainActivity : AppCompatActivity() {

    private val mainVm by lazy { ViewModelProviders.of(this).get(MainVm::class.java) }

    private val clRoot by lazy { findViewById<CoordinatorLayout>(R.id.clRoot) }
    private val rvData by lazy { findViewById<RecyclerView>(R.id.rvData) }
    private val tvRefreshData by lazy { findViewById<TextView>(R.id.tvRefreshData) }
    private val tvShowHeaderView by lazy { findViewById<TextView>(R.id.tvShowHeaderView) }
    private val tvHideHeaderView by lazy { findViewById<TextView>(R.id.tvHideHeaderView) }
    private val tvUpdateHeaderView by lazy { findViewById<TextView>(R.id.tvUpdateHeaderView) }
    private val tvShowEmptyView by lazy { findViewById<TextView>(R.id.tvShowEmptyView) }
    private val tvShowFooterView by lazy { findViewById<TextView>(R.id.tvShowFooterView) }
    private val tvHideFooterView by lazy { findViewById<TextView>(R.id.tvHideFooterView) }
    private val tvReplaceItem by lazy { findViewById<TextView>(R.id.tvReplaceItem) }
    private val tvAddToCenter by lazy { findViewById<TextView>(R.id.tvAddToCenter) }
    private val tvRemove by lazy { findViewById<TextView>(R.id.tvRemove) }
    private val tvRefreshAllData by lazy { findViewById<TextView>(R.id.tvRefreshAllData) }
    private val tvAddToFirst by lazy { findViewById<TextView>(R.id.tvAddToFirst) }

    private lateinit var adapter: ClaBaseAdapter<ShowDataEntity>

    private val headerView by lazy {
        ClaRoundTextView(this).also {
            it.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100.dp)
            it.updateLayoutParams<MarginLayoutParams> {
                setMargins(10.dp)
            }
            it.text = "这是headerView"
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            it.gravity = Gravity.CENTER
            it.setClaBackground {
                borderColor = ContextCompat.getColor(this@MainActivity, com.cla.adapter.library.R.color.color_eeeeee)
                borderWidth = 1.dp.toFloat()
                radius = 4.dp.toFloat()
            }
            it.changeAlphaWhenPress = true
            it.setOnClickListener {
                Toast.makeText(this, "点击headerView", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val emptyView by lazy {
        ClaRoundTextView(this).also {
            it.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700.dp)
            it.updateLayoutParams<MarginLayoutParams> {
                setMargins(10.dp)
            }
            it.text = "这是emptyView"
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            it.gravity = Gravity.CENTER
            it.setClaBackground {
                borderColor = ContextCompat.getColor(this@MainActivity, com.cla.adapter.library.R.color.color_eeeeee)
                borderWidth = 1.dp.toFloat()
                radius = 4.dp.toFloat()
            }
            it.changeAlphaWhenPress = true
            it.setOnClickListener {
                Toast.makeText(this, "点击emptyView", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val footerView by lazy {
        ClaRoundTextView(this).also {
            it.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100.dp)
            it.updateLayoutParams<MarginLayoutParams> {
                setMargins(10.dp)
            }
            it.text = "这是footerView"
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            it.gravity = Gravity.CENTER
            it.setClaBackground {
                borderColor = ContextCompat.getColor(this@MainActivity, com.cla.adapter.library.R.color.color_eeeeee)
                borderWidth = 1.dp.toFloat()
                radius = 4.dp.toFloat()
            }
            it.changeAlphaWhenPress = true
            it.setOnClickListener {
                Toast.makeText(this, "点击footerView", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val showList get() = mainVm.showList

    private fun toast(text: String) {
        println("MainActivity.toast lwl text=$text")
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        adapter = getTextAdapter().also {
//            it.setOnLoadMoreListener { loadData() }
//        }
        adapter = getMultiAdapter().also {
            it.setOnLoadMoreListener { loadData() }
            it.setItemChildClickListener { view, i, s ->
                when (view.id) {
                    R.id.tv_item_1 -> toast("点击了偶数${s.text}-${s.index} 位置=$i")
                    R.id.tv_item_2 -> toast("点击了奇数$${s.text}-${s.index} 位置=$i")
                    R.id.tv_item_3 -> toast("点击了3的倍数$${s.text}-${s.index} 位置=$i")
                }
            }
        }

        adapter.headerView = headerView
        adapter.showHeaderView = true
        adapter.emptyView = emptyView
        adapter.footerView = footerView
        adapter.showFooterView = true

        tvRefreshData.setOnClickListener {
            // 刷新数据
            refreshData()
        }

        tvShowHeaderView.setOnClickListener {
            // 显示headerView
//            adapter.headerView = headerView
            adapter.showHeaderView = true
        }

        tvHideHeaderView.setOnClickListener {
            // 隐藏headerView
            adapter.showHeaderView = false
        }

        tvUpdateHeaderView.setOnClickListener {
            // 修改headerView
            headerView.text = "headerView被修改了"
            headerView.updateLayoutParams<MarginLayoutParams> {
                height = 150.dp
            }
        }

        tvShowEmptyView.setOnClickListener {
            // 显示emptyView
            // 当把adapter中的集合设置为空时，就会显示emptyView；当adapter中有数据之后，emptyView会被移除
            adapter.refreshData(emptyList())
        }

        tvShowFooterView.setOnClickListener {
            // 显示footerView
            adapter.showFooterView = true
        }

        tvHideFooterView.setOnClickListener {
            // 隐藏footerView
            adapter.showFooterView = false
        }

        tvReplaceItem.setOnClickListener {
            // 替换数据
            val list = mutableListOf<ShowDataEntity>()
            repeat(100) { list.add(ShowDataEntity(it, "这是被替换的数据-$it")) }
            adapter.replaceItems({ 10 }, list)
        }

        tvAddToCenter.setOnClickListener {
            val list = mutableListOf<ShowDataEntity>()
            repeat(5) { list.add(ShowDataEntity(it, "这是被添加的数据-$it")) }
            adapter.addData(list) { dataList -> dataList.size / 2 }
        }

        tvRemove.setOnClickListener {
            adapter.dataList { list ->
                list.getOrNull(10)?.let { adapter.removeData(it) }
            }
        }

        tvRefreshAllData.setOnClickListener {
            adapter.refreshAllItems("refresh all items")
        }

        tvAddToFirst.setOnClickListener {
            adapter.addData(ShowDataEntity(0, "这是添加到第一个位置的数据-0")) { 0 }
        }

        val manager = LinearLayoutManager(this)
//        val manager = GridLayoutManager(this, 4)
//        val manager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)

//        rvData.itemAnimator = null
        rvData.layoutManager = manager
        rvData.adapter = adapter
        rvData.setHasFixedSize(true)

//        rvData.scheduleLayoutAnimation()

        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when {
                        adapter.isHeaderHolder(position) || adapter.isFooterHolder(position) || adapter.isEmptyHolder(position) || adapter.isLoadHolder(position) -> 4
                        else -> 1
                    }
                }
            }
        }

        println("MainActivity.onCreate lwl showList=${System.identityHashCode(showList)}")
        val isEmpty = showList.isEmpty()
        if (isEmpty) {
            repeat(20) { showList.add(ShowDataEntity(it, "$it")) }
        }
        adapter.refreshData(showList)
//        refreshData()
    }

    private fun refreshData() {
        showList.clear()
        repeat(20) { showList.add(ShowDataEntity(it, "$it")) }
        adapter.refreshData(showList, scrollToTop = true)
    }

    private fun loadData() {
        lifecycleScope.launch {
            delay(1500)
            val list = mutableListOf<ShowDataEntity>()
            adapter.dataSize { lastIndex ->
                repeat(20) {
                    list.add(ShowDataEntity(lastIndex + it, "${lastIndex + it}"))
                }
                showList.addAll(list)
                adapter.addData(list)
            }
        }
    }

    private fun getTextAdapter() = TextAdapter(this)

    private fun getMultiAdapter() = MyAdapter(this)
}

// ************************************************item只有一种类型*******************************************************************
class TextAdapter(context: Context) : SingleAdapterAbs<ShowDataEntity>(context, R.layout.adapter_text) {

    override fun ClaBaseViewHolder<ShowDataEntity>.initHolder() {
        clickBean<TextView>(R.id.tvText) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun ClaBaseViewHolder<ShowDataEntity>.bindHolder(t: ShowDataEntity, pos: Int, payload: String?) {
        if (payload == REFRESH_ADAPTER_POS) {
            return
        }

        setText(R.id.tvText, "$t-$pos ")
    }

    override fun ClaBaseViewHolder<ShowDataEntity>.detachedFromWindow() {}

    override fun ClaBaseViewHolder<ShowDataEntity>.attachedToWindow() {}
}
// ************************************************item只有一种类型*******************************************************************

// *************************************************item有多种类型********************************************************************
class MyAdapter(context: Context) : MultiAdapterAbs<ShowDataEntity>(context) {
    override fun addDelegate() = listOf(
        MyAdapterDelegate1(),
        MyAdapterDelegate2(),
        MyAdapterDelegate3(),
    )
}

class MyAdapterDelegate1 : MultiAdapterDelegateAbs<ShowDataEntity>() {
    override fun isForViewType(t: ShowDataEntity, position: Int) = t.index % 2 == 0 && t.index % 3 != 0

    override fun ClaBaseViewHolder<ShowDataEntity>.initHolder() {
        addChildClickListener(R.id.tv_item_1)
    }

    @SuppressLint("SetTextI18n")
    override fun ClaBaseViewHolder<ShowDataEntity>.bindHolder(t: ShowDataEntity, pos: Int, payload: String?) {
//        if (payload == REFRESH_ADAPTER_POS) {
//            return
//        }

        val textView = getView<TextView>(R.id.tv_item_1)
        textView.text = "这是偶数--${t.text}-$pos"
    }

    override fun createItemView() = ClaRoundTextView(context).also {
        it.id = R.id.tv_item_1
        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        it.gravity = Gravity.CENTER
        it.setPadding(55.dp)
        it.resetClaLine {
            lineColor = ContextCompat.getColor(context, com.cla.adapter.library.R.color.color_eeeeee)
            lineWidth = 1.dp.toFloat()
            lineSpace = 10.dp.toFloat()
            showBottom = true
        }
        it.changeAlphaWhenPress = true
    }
}

class MyAdapterDelegate2 : MultiAdapterDelegateAbs<ShowDataEntity>() {
    override fun isForViewType(t: ShowDataEntity, position: Int) = t.index % 2 != 0 && t.index % 3 != 0

    override fun ClaBaseViewHolder<ShowDataEntity>.initHolder() {
        addChildClickListener(R.id.tv_item_2)
    }

    @SuppressLint("SetTextI18n")
    override fun ClaBaseViewHolder<ShowDataEntity>.bindHolder(t: ShowDataEntity, pos: Int, payload: String?) {
//        if (payload == REFRESH_ADAPTER_POS) {
//            return
//        }

        val textView = getView<TextView>(R.id.tv_item_2)
        textView.text = "这是奇数-${t.text}-$pos"
    }

    override fun createItemView() = ClaRoundTextView(context).also {
        it.id = R.id.tv_item_2
        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        it.setTextColor(ContextCompat.getColor(context, R.color.purple_700))
        it.gravity = Gravity.CENTER
        it.setPadding(20.dp)
        it.resetClaLine {
            lineColor = ContextCompat.getColor(context, R.color.purple_700)
            lineWidth = 1.dp.toFloat()
            lineSpace = 10.dp.toFloat()
            showBottom = true
        }
        it.changeAlphaWhenPress = true
    }
}

class MyAdapterDelegate3 : MultiAdapterDelegateAbs<ShowDataEntity>() {
    override fun isForViewType(t: ShowDataEntity, position: Int) = t.index % 3 == 0

    override fun ClaBaseViewHolder<ShowDataEntity>.initHolder() {
        addChildClickListener(R.id.tv_item_3)
    }

    @SuppressLint("SetTextI18n")
    override fun ClaBaseViewHolder<ShowDataEntity>.bindHolder(t: ShowDataEntity, pos: Int, payload: String?) {
//        if (payload == REFRESH_ADAPTER_POS) {
//            return
//        }

        val textView = getView<TextView>(R.id.tv_item_3)
        textView.text = "这是3的倍数-${t.text}-$pos"
    }

    override fun createItemView() = ClaRoundTextView(context).also {
        it.id = R.id.tv_item_3
        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50.dp)
        it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        it.setTextColor(ContextCompat.getColor(context, R.color.purple_200))
        it.gravity = Gravity.CENTER
        it.setPadding(15.dp)
        it.resetClaLine {
            lineColor = ContextCompat.getColor(context, R.color.purple_200)
            lineWidth = 5.dp.toFloat()
            lineSpace = 10.dp.toFloat()
            showBottom = true
        }
        it.changeAlphaWhenPress = true
    }
}

// *************************************************item有多种类型********************************************************************

data class ShowDataEntity(
    val index: Int,
    val text: String,
)
