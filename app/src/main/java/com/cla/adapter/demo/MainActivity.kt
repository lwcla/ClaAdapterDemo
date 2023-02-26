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
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.cla.round.view.widget.ClaRoundTextView
import com.cla.adapter.library.ClaBaseAdapter
import com.cla.adapter.library.MultiAdapterAbs
import com.cla.adapter.library.MultiAdapterDelegateAbs
import com.cla.adapter.library.SingleAdapterAbs
import com.cla.adapter.library.holder.ClaBaseViewHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal val Int.dp: Int
    get() {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

class MainActivity : AppCompatActivity() {

    private val rvData by lazy { findViewById<RecyclerView>(R.id.rvData) }
    private val tvRefreshData by lazy { findViewById<TextView>(R.id.tvRefreshData) }
    private val tvShowHeaderView by lazy { findViewById<TextView>(R.id.tvShowHeaderView) }
    private val tvHideHeaderView by lazy { findViewById<TextView>(R.id.tvHideHeaderView) }
    private val tvUpdateHeaderView by lazy { findViewById<TextView>(R.id.tvUpdateHeaderView) }
    private val tvShowEmptyView by lazy { findViewById<TextView>(R.id.tvShowEmptyView) }
    private val tvShowFooterView by lazy { findViewById<TextView>(R.id.tvShowFooterView) }
    private val tvHideFooterView by lazy { findViewById<TextView>(R.id.tvHideFooterView) }
    private val tvReplaceItem by lazy { findViewById<TextView>(R.id.tvReplaceItem) }

    private lateinit var adapter: ClaBaseAdapter<String>

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
            it.layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300.dp)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        adapter = getTextAdapter()
        adapter = getMultiAdapter().also {
            it.setOnLoadMoreListener { loadData() }
            it.setItemChildClickListener { view, i, s ->
                when (view.id) {
                    R.id.tv_item_1 -> Toast.makeText(this, "点击了偶数$s", Toast.LENGTH_SHORT).show()
                    R.id.tv_item_2 -> Toast.makeText(this, "点击了奇数$s", Toast.LENGTH_SHORT).show()
                }
            }
        }

        adapter.headerView = headerView
        adapter.showHeaderView = false

        adapter.emptyView = emptyView

        adapter.footerView = footerView
        adapter.showFooterView = false

        tvRefreshData.setOnClickListener {
            //刷新数据
            refreshData()
        }

        tvShowHeaderView.setOnClickListener {
            //显示headerView
            adapter.showHeaderView = true
        }

        tvHideHeaderView.setOnClickListener {
            //隐藏headerView
            adapter.showHeaderView = false
        }

        tvUpdateHeaderView.setOnClickListener {
            //修改headerView
            headerView.text = "headerView被修改了"
            headerView.updateLayoutParams<MarginLayoutParams> {
                height = 150.dp
            }
        }

        tvShowEmptyView.setOnClickListener {
            //显示emptyView
            //当把adapter中的集合设置为空时，就会显示emptyView；当adapter中有数据之后，emptyView会被移除
            adapter.refreshData(emptyList())
        }

        tvShowFooterView.setOnClickListener {
            //显示footerView
            adapter.showFooterView = true
        }

        tvHideFooterView.setOnClickListener {
            //隐藏footerView
            adapter.showFooterView = false
        }

        tvReplaceItem.setOnClickListener {
            //替换数据
            val list = mutableListOf<String>()
            repeat(5) { list.add("这是被替换的数据-$it") }
            adapter.replaceItems(10, list)
        }

        rvData.layoutManager = LinearLayoutManager(this)
        rvData.adapter = adapter

        refreshData()
    }

    private fun refreshData() {
        val list = mutableListOf<String>()
        repeat(20) { list.add("$it") }
        adapter.refreshData(list)
    }

    private fun loadData() {
        lifecycleScope.launch {
            delay(1500)
            val list = mutableListOf<String>()
            val lastIndex = adapter.dataList.size
            repeat(20) {
                list.add((lastIndex + it).toString())
            }
            adapter.addData(list)
        }
    }

    private fun getTextAdapter() = TextAdapter(this)

    private fun getMultiAdapter() = MyAdapter(this)
}

//************************************************item只有一种类型*******************************************************************
class TextAdapter(context: Context) : SingleAdapterAbs<String>(context, R.layout.adapter_text) {

    override fun ClaBaseViewHolder<String>.initHolder() {
        clickBean<TextView>(R.id.tvText) {
            Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
        }
    }

    override fun ClaBaseViewHolder<String>.bindHolder(t: String, pos: Int, payload: String?) {
        setText(R.id.tvText, "${t}-$pos ")
    }

    override fun ClaBaseViewHolder<String>.detachedFromWindow() {

    }

    override fun ClaBaseViewHolder<String>.attachedToWindow() {

    }
}
//************************************************item只有一种类型*******************************************************************

//*************************************************item有多种类型********************************************************************
class MyAdapter(context: Context) : MultiAdapterAbs<String>(context) {
    override fun addDelegate() = listOf(
        MyAdapterDelegate1(),
        MyAdapterDelegate2(),
        MyAdapterDelegate3(),
    )
}

class MyAdapterDelegate1 : MultiAdapterDelegateAbs<String>() {
    override fun isForViewType(t: String, position: Int) = position % 2 == 0 && position % 3 != 0

    override fun ClaBaseViewHolder<String>.initHolder() {
        addChildClickListener(R.id.tv_item_1)
    }

    @SuppressLint("SetTextI18n")
    override fun ClaBaseViewHolder<String>.bindHolder(t: String, pos: Int, payload: String?) {
        val textView = itemView.covert<TextView>()
        textView.text = "这是偶数--$t"
    }

    override fun createItemView() = ClaRoundTextView(context).also {
        it.id = R.id.tv_item_1
        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        it.gravity = Gravity.CENTER
        it.setPadding(15.dp)
        it.resetClaLine {
            lineColor = ContextCompat.getColor(context, com.cla.adapter.library.R.color.color_eeeeee)
            lineWidth = 1.dp.toFloat()
            lineSpace = 10.dp.toFloat()
            showBottom = true
        }
        it.changeAlphaWhenPress = true
    }
}

class MyAdapterDelegate2 : MultiAdapterDelegateAbs<String>() {
    override fun isForViewType(t: String, position: Int) = position % 2 == 1 && position % 3 != 0

    override fun ClaBaseViewHolder<String>.initHolder() {
        addChildClickListener(R.id.tv_item_2)
    }

    @SuppressLint("SetTextI18n")
    override fun ClaBaseViewHolder<String>.bindHolder(t: String, pos: Int, payload: String?) {
        val textView = itemView.covert<TextView>()
        textView.text = "这是奇数-$t"
    }

    override fun createItemView() = ClaRoundTextView(context).also {
        it.id = R.id.tv_item_2
        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        it.setTextColor(ContextCompat.getColor(context, R.color.purple_700))
        it.gravity = Gravity.CENTER
        it.setPadding(7.dp)
        it.resetClaLine {
            lineColor = ContextCompat.getColor(context, R.color.purple_700)
            lineWidth = 1.dp.toFloat()
            lineSpace = 10.dp.toFloat()
            showBottom = true
        }
        it.changeAlphaWhenPress = true
    }
}

class MyAdapterDelegate3 : MultiAdapterDelegateAbs<String>() {
    override fun isForViewType(t: String, position: Int) = position % 3 == 0

    override fun ClaBaseViewHolder<String>.initHolder() {
        addChildClickListener(R.id.tv_item_2)
    }

    @SuppressLint("SetTextI18n")
    override fun ClaBaseViewHolder<String>.bindHolder(t: String, pos: Int, payload: String?) {
        val textView = itemView.covert<TextView>()
        textView.text = "这是3的倍数-$t"
    }

    override fun createItemView() = ClaRoundTextView(context).also {
        it.id = R.id.tv_item_2
        it.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        it.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        it.setTextColor(ContextCompat.getColor(context, R.color.purple_200))
        it.gravity = Gravity.CENTER
        it.setPadding(7.dp)
        it.resetClaLine {
            lineColor = ContextCompat.getColor(context, R.color.purple_200)
            lineWidth = 3.dp.toFloat()
            lineSpace = 25.dp.toFloat()
            showBottom = true
            showLeft = true
            showRight = true
        }
        it.changeAlphaWhenPress = true
    }
}

//*************************************************item有多种类型********************************************************************



