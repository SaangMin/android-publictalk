package com.skysmyoo.publictalk.ui.chat_room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.ui.theme.CancelColor
import com.skysmyoo.publictalk.ui.theme.DialogBackground
import com.skysmyoo.publictalk.ui.theme.SubmitColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemoveChatDialogFragment : DialogFragment() {

    private val args: RemoveChatDialogFragmentArgs by navArgs()
    private val viewModel: ChatRoomViewModel by viewModels()
    private lateinit var chatRoom: ChatRoom

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        chatRoom = args.chatRoom
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    RemoveChatDialog(
                        chatRoom = chatRoom,
                        viewModel = viewModel,
                        navigateToChatList = { navigateToChatList() }) {
                        dismiss()
                    }
                }
            }
        }
    }

    private fun navigateToChatList() {
        val action = RemoveChatDialogFragmentDirections.actionRemoveChatToChatList()
        findNavController().navigate(action)
    }

    @Composable
    fun RemoveChatDialog(
        chatRoom: ChatRoom,
        viewModel: ChatRoomViewModel,
        navigateToChatList: () -> Unit,
        dismissDialog: () -> Unit,
    ) {
        val context = LocalContext.current
        val snackbarHostState = remember { SnackbarHostState() }
        val isSuccessDeleteChat by viewModel.isSuccessDeleteChat.collectAsState()
        val isFailedDeleteChat by viewModel.isFailedDeleteChat.collectAsState()
        val isCancelClick by viewModel.isCancelClick.collectAsState()

        AlertDialog(
            containerColor = DialogBackground,
            onDismissRequest = {
                dismissDialog()
            },
            confirmButton = {},
            title = {},
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = context.getString(R.string.remove_chat_label),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(CancelColor),
                        onClick = {
                            viewModel.deleteChatRoom(chatRoom)
                        }
                    ) {
                        Text(text = stringResource(id = R.string.submit))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(SubmitColor),
                        onClick = {
                            viewModel.onCancelClick()
                        }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            }
        )

        SnackbarHost(hostState = snackbarHostState)

        LaunchedEffect(isSuccessDeleteChat) {
            if (isSuccessDeleteChat) {
                Toast.makeText(
                    context,
                    context.getString(R.string.success_remove_chat_msg),
                    Toast.LENGTH_SHORT
                ).show()
                navigateToChatList()
            }
        }

        LaunchedEffect(isFailedDeleteChat) {
            if (isFailedDeleteChat) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.failure_remove_chat_msg)
                )
            }
        }

        LaunchedEffect(isCancelClick) {
            if (isCancelClick) {
                dismissDialog()
            }
        }
    }
}