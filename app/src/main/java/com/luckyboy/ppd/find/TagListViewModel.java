package com.luckyboy.ppd.find;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.luckyboy.libnetwork.ApiResponse;
import com.luckyboy.libnetwork.ApiService;
import com.luckyboy.ppd.core.AbsViewModel;
import com.luckyboy.ppd.core.model.Feed;
import com.luckyboy.ppd.core.model.TagList;
import com.luckyboy.ppd.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TagListViewModel extends AbsViewModel<TagList> {

    private String tagType;
    private int offset;
    private AtomicBoolean loadAfter = new AtomicBoolean();
    private MutableLiveData switchTabLiveData = new MutableLiveData();

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public MutableLiveData getSwitchTabLiveData() {
        return switchTabLiveData;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }


    private class DataSource extends ItemKeyedDataSource<Long, TagList> {
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<TagList> callback) {
            loadData(0L, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull TagList item) {
            return item.tagId;
        }


        private void loadData(Long requestKey, LoadCallback<TagList> callback) {
            if (requestKey > 0) {
                loadAfter.set(true);
            }
            ApiResponse<List<TagList>> response = ApiService.get("/tag/queryTagList")
                    .addParam("userId", UserManager.get() == null ? 0 : UserManager.get().getUserId())
                    .addParam("tagId", requestKey)
                    .addParam("tagType", tagType)
                    .addParam("pageCount", 10)
                    .addParam("offset", offset)
                    .responseType(new TypeReference<ArrayList<TagList>>() {
                    }.getType())
                    .execute();
            List<TagList> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);
            if (requestKey > 0) {
                loadAfter.set(false);
                offset += result.size();
                // 结束刷新操作？？
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            } else {
                offset = result.size();
            }
        }

    }

    @SuppressLint("RestrictedApi")
    public void loadAfter(long id, ItemKeyedDataSource.LoadCallback callback) {
        if (loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> {
            loadData(id, callback);
        });
    }

    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback callback) {
        if (tagId <= 0 || loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ((TagListViewModel.DataSource) getDataSource()).loadData(tagId, callback);
            }
        });
    }


}
