package com.skysmyoo.publictalk.ui.searching

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.UserRepository
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultError
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchingUiState(
    val isNotExistUser: Boolean = false,
    val isFoundUser: Boolean = false,
    val isAlreadyFriend: Boolean = false,
    val isAddedFriend: Boolean = false,
    val isNetworkError: Boolean = false,
    val isFailedAddFriend: Boolean = false,
    val isEmptyTarget: Boolean = false,
)

@HiltViewModel
class SearchingViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _searchingUiState = MutableStateFlow(SearchingUiState())
    val searchingUiState: StateFlow<SearchingUiState> = _searchingUiState

    val searchingTargetEmail = MutableLiveData<String>()
    var gettingUser: User? = null

    fun searchingFriend() {
        viewModelScope.launch {
            if (!searchingTargetEmail.value.isNullOrEmpty()) {
                when (val foundUser =
                    repository.searchFriendFromRemote(searchingTargetEmail.value ?: "")) {
                    is ApiResultSuccess -> {
                        gettingUser = foundUser.data
                        _searchingUiState.value = _searchingUiState.value.copy(isFoundUser = true)
                        delay(1000)
                        _searchingUiState.value = _searchingUiState.value.copy(isFoundUser = false)
                    }

                    is ApiResultError -> {
                        _searchingUiState.value =
                            _searchingUiState.value.copy(isNetworkError = true)
                        delay(1000)
                        _searchingUiState.value =
                            _searchingUiState.value.copy(isNetworkError = false)
                    }

                    else -> {
                        _searchingUiState.value =
                            _searchingUiState.value.copy(isNotExistUser = true)
                        delay(1000)
                        _searchingUiState.value =
                            _searchingUiState.value.copy(isNotExistUser = false)
                    }
                }
            } else {
                _searchingUiState.value = _searchingUiState.value.copy(isEmptyTarget = true)
                delay(1000)
                _searchingUiState.value = _searchingUiState.value.copy(isEmptyTarget = false)
            }
        }
    }

    fun addFriendButtonClick() {
        if (gettingUser == null) return
        viewModelScope.launch {
            val friendEmailList =
                repository.getFriends().map { it.userEmail }
            if (friendEmailList.contains(searchingTargetEmail.value) || gettingUser?.userEmail == repository.getMyEmail()) {
                _searchingUiState.value = _searchingUiState.value.copy(isAlreadyFriend = true)
                _searchingUiState.value = _searchingUiState.value.copy(isAlreadyFriend = false)
            } else {
                val myInfo = repository.getMyInfo() ?: return@launch
                val response = repository.addFriend(myInfo, gettingUser ?: return@launch)
                if (response is ApiResultSuccess) {
                    _searchingUiState.value = _searchingUiState.value.copy(isAddedFriend = true)
                    _searchingUiState.value = _searchingUiState.value.copy(isAddedFriend = false)
                } else {
                    _searchingUiState.value = _searchingUiState.value.copy(isFailedAddFriend = true)
                    _searchingUiState.value = _searchingUiState.value.copy(isNetworkError = true)
                    _searchingUiState.value =
                        _searchingUiState.value.copy(isFailedAddFriend = false)
                    _searchingUiState.value = _searchingUiState.value.copy(isNetworkError = false)
                }
            }
        }
    }
}