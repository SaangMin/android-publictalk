package com.skysmyoo.publictalk.data.source.remote.response

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiCallAdapter(private val type: Type) : CallAdapter<Type, Call<ApiResponse<Type>>> {
    override fun responseType(): Type = type

    override fun adapt(call: Call<Type>): Call<ApiResponse<Type>> {
        return ApiCall(call)
    }
}

class ApiCallAdapterFactory private constructor() : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != ApiResponse::class.java) {
            return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        return ApiCallAdapter(resultType)
    }

    companion object {
        fun create() = ApiCallAdapterFactory()
    }
}