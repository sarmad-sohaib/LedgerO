package com.ledgero.Interfaces



interface FetchUsers {
    fun OnAllUsersFetched(usersUID:ArrayList<String>?)
    fun OnSingleUserFetched(userUID: String?,userEmail:String?,userName:String?)
}