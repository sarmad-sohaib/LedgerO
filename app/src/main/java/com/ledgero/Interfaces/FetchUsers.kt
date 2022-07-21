package com.ledgero.Interfaces

import com.ledgero.DataClasses.FriendUsers

interface FetchUsers {
    fun OnAllUsersFetched(users:ArrayList<FriendUsers>?)
    fun OnSingleUserFetched(user: FriendUsers?)
}