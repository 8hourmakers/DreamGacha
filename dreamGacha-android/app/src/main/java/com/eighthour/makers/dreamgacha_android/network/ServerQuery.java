package com.eighthour.makers.dreamgacha_android.network;


import retrofit2.Callback;


/**
 * Created by Omjoon on 2017. 7. 30..
 */

public class ServerQuery {

	public static void example( Callback callback ) {

		ServiceGenerator.createService( ApiService.class, false ).getStrings().enqueue( callback );
	}
}
