package com.cla.adapter.library

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import com.cla.adapter.library.ClaBaseAdapter.Companion.REFRESH_ADAPTER_EMPTY
import com.cla.adapter.library.ClaBaseAdapter.Companion.REFRESH_ADAPTER_PRE_LOAD
import java.lang.ref.WeakReference

internal class ClaBaseAdapterHandler<T>(adapter: ClaBaseAdapter<T>) : Handler(Looper.getMainLooper()) {

    companion object {
        /** 刷新数据 */
        const val REFRESH_DATA = 0x001

        /** 添加数据 */
        const val ADD_DATA = 0x002

        /** 删除数据 */
        const val REMOVE_DATA = 0x003

        /** 刷新item */
        const val REFRESH_ITEM = 0X004

        /** 刷新预加载view */
        const val REFRESH_PRE_FAILED = 0x005

        /** 刷新预加载view */
        const val REFRESH_PRE_NO_MORE = 0x006

        /** 刷新预加载view */
        const val REFRESH_PRE_LOADING = 0x007

        /** 刷新showHeaderView */
        const val REFRESH_SHOW_HEADER_VIEW = 0x008

        /** 刷新headerView */
        const val REFRESH_HEADER_VIEW = 0x009

        /** 刷新showFooterView */
        const val REFRESH_SHOW_FOOTER_VIEW = 0X010

        /** 刷新footerView */
        const val REFRESH_FOOTER_VIEW = 0x011

        /*** 刷新items */
        const val REFRESH_ITEMS = 0x012

        /** 滚动到某个位置 */
        const val SCROLL_TO_POSITION = 0X013

        /** 关闭预加载 */
        const val CLOSE_PRE_LOAD = 0x014

        /** 替换items */
        const val REPLACE_ITEMS = 0x015
    }

    private val ref = WeakReference(adapter)

    @SuppressLint("NotifyDataSetChanged")
    override fun handleMessage(msg: Message) {
        val adapter = ref.get() ?: return

        println(
            "ClaBaseAdapterHandler.handleMessage lwl msg.what=${
                when (msg.what) {
                    REFRESH_DATA -> "refresh_data"
                    ADD_DATA -> "add_data"
                    REMOVE_DATA -> "remove_data"
                    REFRESH_ITEM -> "refresh_item"
                    REFRESH_PRE_FAILED -> "refresh_pre_failed"
                    REFRESH_PRE_NO_MORE -> "refresh_pre_no_more"
                    REFRESH_PRE_LOADING -> "refresh_pre_loading"
                    REFRESH_SHOW_HEADER_VIEW -> "refresh_show_header_view"
                    REFRESH_HEADER_VIEW -> "refresh_header_view"
                    REFRESH_SHOW_FOOTER_VIEW -> "refresh_show_footer_view"
                    REFRESH_FOOTER_VIEW -> "refresh_footer_view"
                    REFRESH_ITEMS -> "refresh_items"
                    SCROLL_TO_POSITION -> "scroll_to_position"
                    CLOSE_PRE_LOAD -> "close_pre_load"
                    REPLACE_ITEMS -> "replace_items"
                    else -> "else"
                }
            }"
        )

        when (msg.what) {

            REFRESH_DATA -> adapter.apply {
                val data = msg.obj as? AdapterRefreshData<T>? ?: return
                val list = data.list

                val lastItemCount = itemCount
                //刚装载adapter的时候，如果设置了loadHolder，那itemCount就不等于0，但是这个时候还是可以当成空数据来处理
                //如果只是用showDataSize来判断的话，很有可能不准，这个时候可能正在显示emptyView
                val adapterIsEmpty = lastItemCount == 0 || (lastItemCount == 1 && isLoadHolder(0))
                val lastDataSize = showDataSize

                if (System.identityHashCode(showDataList) != System.identityHashCode(list)) {
                    showDataList.clear()
                    showDataList.addAll(list)
                }

                preLoadBuilder.let {
                    //设置预加载偏移量，默认是到列表的二分之一的时候开始加载下一页的数据
                    it.preloadItemCount = it.preCount.invoke(showDataSize)
                    it.preloadClose = false
                    it.reset()
                }

                //如果adapter在刷新数据之前就已经是空的，那就刷新所有
                if (adapterIsEmpty) {
                    notifyDataSetChanged()
                    return
                }

                //先滚动到第一个的位置，这样即使列表已经滚动到很后面的位置了，在删除数据的时候也看不到删除的过程
                if (data.scrollToTop) {
                    val pos = if (!data.scrollToTopIncludeHeader) adapterPos(0) else 0
                    scrollToPosWithOffset(pos, data.scrollToTopOffset)
                }

                //不要刷新headerView和footerView
                //如果headerView是webView，那么notifyDataSetChanged会导致WebView闪屏
                val startPos = adapterPos(0)

                val removeCount = lastDataSize - showDataSize
                val deleteStartPos = maxOf(startPos + showDataSize - 1, 0)

                if (removeCount > 0) {
                    //从后面删除多余的数据
                    //如果从前面删的话，刷新数据的时候能明显看到数据被删除的过程
                    notifyItemRangeRemoved(deleteStartPos, removeCount)
                }

                //刷新数据item
                notifyVisibleItems(startPos, showDataSize, "")
                //刷新预加载view
                notifyPreLoad()
                //恢复状态
                restoreState.value
            }

            ADD_DATA -> adapter.apply {
                val index = msg.arg1
                val addToEnd = msg.arg2 == 1
                val list = msg.obj as? List<T> ?: return
                if (list.isEmpty()) {
                    return
                }

                if (isShowEmpty && list.isNotEmpty()) {
                    //如果当前显示的是emptyView，新添加的数据不为空的情况下，需要先调用notifyItemRemoved方法
                    //否则在notifyItemRangeInserted会去检查数据下标，然后报数组越界的错
                    val emptyPos = findPositionByType(ClaBaseAdapter.EMPTY_VIEW)
                    if (emptyPos >= 0) {
                        notifyItemRemoved(emptyPos)
                        notifyVisibleItems(emptyPos, itemCount, REFRESH_ADAPTER_EMPTY)
                    }
                }

                val addIndex = if (index > showDataSize) showDataSize else index
                showDataList.addAll(addIndex, list)
                if (addToEnd) {
                    preLoadBuilder.reset()
                }

                val aPos = adapterPos(addIndex)
                val count = list.size

                //刷新数据item
                notifyVisibleItems(aPos, showDataSize, "")
                //刷新预加载view
                notifyPreLoad()
            }

            REMOVE_DATA -> adapter.apply {
                val removeData = msg.obj as? T? ?: return

                val showPos = showDataList.indexOf(removeData)
                if (showPos !in showDataList.indices) {
                    return
                }

                showDataList.removeAt(showPos)

                val aPos = adapterPos(showPos)
                notifyVisibleItems(aPos, showDataSize, "")
            }

            REFRESH_ITEM -> adapter.apply {
                val item = msg.obj as? AdapterRefreshItem<T>? ?: return
                val pos = showDataList.indexOf(item.data)
                if (pos < 0) {
                    return
                }

                notifyItemChanged(adapterPos(pos), item.payload)
            }

            REFRESH_ITEMS -> adapter.apply {
                val item = msg.obj as? AdapterRefreshItems<T>? ?: return
                val startPos = showDataList.indexOf(item.data)
                if (startPos < 0) {
                    return
                }

                val count = minOf(showDataSize - startPos, item.count)
                val payload = item.payload
                //刷新数据
                notifyItemRangeChanged(adapterPos(startPos), count, payload)
            }

            //刷新预加载view
            REFRESH_PRE_FAILED -> adapter.apply {
                if (!needShowPreView) {
                    return
                }

                if (preLoadBuilder.isLoadFailed) {
                    return
                }

                //handler中再设置一次，是因为addData之中会重置preLoadBuilder的状态
                //那先addData之后再loadFailed，preLoadBuilder在addData被重置成初始状态了。failed状态就一直都显示不出来
                //handler是按顺序执行的，倒是不用担心这个状态会冲掉后面设置的状态
                preLoadBuilder.loadFailed()
                val aPos = findPositionByType(ClaBaseAdapter.LOADING_VIEW)
                if (aPos < 0) {
                    notifyItemInserted(loadHolderPos)
                }
                notifyPreLoad()
            }

            //刷新预加载view
            REFRESH_PRE_NO_MORE -> adapter.apply {
                if (!needShowPreView) {
                    return
                }

                if (preLoadBuilder.isNoMoreData) {
                    return
                }

                preLoadBuilder.noMoreData()
                val aPos = findPositionByType(ClaBaseAdapter.LOADING_VIEW)
                if (aPos < 0) {
                    notifyItemInserted(loadHolderPos)
                }
                notifyPreLoad()
            }

            //刷新预加载view
            REFRESH_PRE_LOADING -> adapter.apply {
                if (!needShowPreView) {
                    return
                }

                if (preLoadBuilder.isLoading) {
                    return
                }

                preLoadBuilder.loading()

                val aPos = findPositionByType(ClaBaseAdapter.LOADING_VIEW)
                if (aPos < 0) {
                    notifyItemInserted(loadHolderPos)
                }
                notifyPreLoad()
            }

            //关闭预加载
            CLOSE_PRE_LOAD -> adapter.apply {
                if (!needShowPreView) {
                    return
                }

                preLoadBuilder.close()

                val aPos = findPositionByType(ClaBaseAdapter.LOADING_VIEW)
                if (aPos >= 0) {
                    notifyItemRemoved(aPos)
                }
                notifyPreLoad()
            }

            REFRESH_SHOW_HEADER_VIEW -> adapter.apply {
                val show = msg.obj as? Boolean? ?: return
                if (_showHeaderView == show) {
                    return
                }

                val originalPos = findPositionByType(ClaBaseAdapter.HEADER_VIEW)
                _showHeaderView = show
                refreshHeaderView(originalPos)
            }

            REFRESH_HEADER_VIEW -> adapter.apply {
                val view = msg.obj as? View?
                if (_headerView == view) {
                    return
                }

                val originalPos = findPositionByType(ClaBaseAdapter.HEADER_VIEW)
                _headerView = view
                refreshHeaderView(originalPos)
            }

            REFRESH_SHOW_FOOTER_VIEW -> adapter.apply {
                val show = msg.obj as? Boolean? ?: return
                if (_showFooterView == show) {
                    return
                }

                val originalPos = findPositionByType(ClaBaseAdapter.FOOTER_VIEW)
                _showFooterView = show
                refreshFooterView(originalPos)
            }

            REFRESH_FOOTER_VIEW -> adapter.apply {
                val view = msg.obj as? View?
                if (_footerView == view) {
                    return
                }

                val originalPos = findPositionByType(ClaBaseAdapter.FOOTER_VIEW)
                _footerView = view
                refreshFooterView(originalPos)
            }

            SCROLL_TO_POSITION -> adapter.apply {
                val rv = recyclerView ?: return
                val pos = msg.arg1
                rv.scrollToPosition(adapterPos(pos))
            }

            REPLACE_ITEMS -> adapter.apply {
                val replace = msg.obj as AdapterReplaceItems<T>? ?: return
                val pos = replace.pos
                val newList = replace.list
                val payload = replace.payload

                if (System.identityHashCode(showDataList) != System.identityHashCode(newList)) {
                    val removeList = showDataList.filterIndexed { index, t ->
                        index >= pos && index < pos + newList.size
                    }
                    showDataList.removeAll(removeList)
                    if (showDataList.lastIndex < pos) {
                        showDataList.addAll(newList)
                    } else {
                        showDataList.addAll(pos, newList)
                    }
                }

                //刷新数据
                val count = minOf(showDataSize - pos, newList.size)
                notifyVisibleItems(adapterPos(pos), count, payload)
            }
        }
    }

    /** 刷新预加载view */
    fun notifyPreLoad() = ref.get()?.apply {
        if (needShowPreView) {
            notifyItemChanged(loadHolderPos, REFRESH_ADAPTER_PRE_LOAD)
        }
    }

    /** 删除刷新预加载的msg */
    fun removePreLoadMsg() {
        removeMessages(ClaBaseAdapterHandler.CLOSE_PRE_LOAD)
        removeMessages(ClaBaseAdapterHandler.REFRESH_PRE_FAILED)
        removeMessages(ClaBaseAdapterHandler.REFRESH_PRE_NO_MORE)
        removeMessages(ClaBaseAdapterHandler.REFRESH_PRE_LOADING)
    }
}

internal data class AdapterRefreshItem<T>(val data: T, val payload: String?)
internal data class AdapterRefreshItems<T>(val data: T, val count: Int, val payload: String?)
internal data class AdapterReplaceItems<T>(val pos: Int, val list: List<T>, val payload: String?)

/**
 * 刷新列表
 * @param T
 * @property list 集合
 * @property scrollToTop 是否滚动到顶部
 * @property scrollToTopIncludeHeader 滚动到顶部的时候，是否包含headerView的位置
 * @property scrollToTopOffset 滚动到顶部的偏移量
 * @constructor
 */
internal data class AdapterRefreshData<T>(
    val list: List<T>,
    val scrollToTop: Boolean,
    val scrollToTopIncludeHeader: Boolean,
    val scrollToTopOffset: Int,
)