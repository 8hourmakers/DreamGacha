package com.eighthour.makers.dreamgacha_android.network;


import retrofit2.Call;
import retrofit2.http.GET;


/**
 * Created by Omjoon on 2017. 7. 30..
 */

public interface ApiService {

	@GET
	Call<String> getStrings();
}
