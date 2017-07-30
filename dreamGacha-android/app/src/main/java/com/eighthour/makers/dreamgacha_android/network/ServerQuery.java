package com.eighthour.makers.dreamgacha_android.network;


import java.io.File;

import okhttp3.MultipartBody;
import retrofit2.Callback;


/**
 * Created by Omjoon on 2017. 7. 30..
 */

public class ServerQuery {

	public static void example( Callback callback ) {

		ServiceGenerator.createService( ApiService.class, false ).getStrings().enqueue( callback );
	}

//	public static void goRegister( int kind, String email, String nickName, String password, String gender, String birthDay, File profileImage, String device_id, retrofit2.Callback callback ) {
//
//		okhttp3.RequestBody requestBody = null;
//		if ( profileImage != null )
//			requestBody = okhttp3.RequestBody.create( okhttp3.MediaType.parse( "multipart/form-data" ), profileImage );
//		MultipartBody.Part body =
//				MultipartBody.Part.createFormData( "profileImage", profileImage.getName(), requestBody );
//		String descriptionString = "hello, this is description speaking";
//		okhttp3.RequestBody description =
//				okhttp3.RequestBody.create(
//						okhttp3.MediaType.parse( "multipart/form-data" ), descriptionString );
//
//		retrofit2.Call<ResponseRegisterJson> call = ServiceGenerator.createService( ServerAPI.class, true ).goRegister( kind, "", email, nickName, password, gender, birthDay, description, body, "A", device_id );
//
//		call.enqueue( callback );
//	}
}
