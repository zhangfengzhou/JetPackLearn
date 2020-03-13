package com.luckyboy.ppd.mine;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.luckyboy.libnetwork.ApiResponse;
import com.luckyboy.libnetwork.ApiService;
import com.luckyboy.ppd.core.AbsViewModel;
import com.luckyboy.ppd.core.model.Feed;
import com.luckyboy.ppd.login.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserBehaviorViewModel extends AbsViewModel<Feed> {

    private int mBehavior;

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setBehavior(int behavior){
        this.mBehavior = behavior;
    }

    class DataSource extends ItemKeyedDataSource<Integer, Feed>{

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(params.requestedInitialKey, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }

        private void loadData(int feedId, LoadCallback<Feed> callback){
            ApiResponse<List<Feed>> response =  ApiService.get("/feeds/queryUserBehaviorList/")
                    .addParam("behavior", mBehavior)
                    .addParam("feedId", feedId)
                    .addParam("pageCount", 10)
                    .addParam("userId", UserManager.get().getUserId())
                    .responseType(
                            new TypeReference<ArrayList<Feed>>(){}.getType())
                    .execute();

            List<Feed> result = response.body ==null? Collections.emptyList()
                    : response.body;

            callback.onResult(result);
            if (feedId>0){
                ((MutableLiveData)getBoundaryPageData()).postValue(result.size()>0);
            }

        }
    }

}
