package com.apiguave.tinderclonecompose.home

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiguave.tinderclonecompose.model.ProfilePictureState
import com.apiguave.tinderclonedomain.match.Match
import com.apiguave.tinderclonedomain.profile.Profile
import com.apiguave.tinderclonedomain.usecase.GetPictureUseCase
import com.apiguave.tinderclonedomain.usecase.GetProfilesUseCase
import com.apiguave.tinderclonedomain.usecase.LikeProfileUseCase
import com.apiguave.tinderclonedomain.usecase.PassProfileUseCase
import com.apiguave.tinderclonedomain.usecase.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val likeProfileUseCase: LikeProfileUseCase,
    private val passProfileUseCase: PassProfileUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getPictureUseCase: GetPictureUseCase
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(HomeViewState(HomeViewDialogState.NoDialog, HomeViewContentState.Loading))
    val uiState = _uiState.asStateFlow()

    init {
        fetchProfiles()
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(dialogState = HomeViewDialogState.NoDialog)
        }
    }

    fun sendMessage(matchId: String, text: String) = viewModelScope.launch {
        sendMessageUseCase(matchId, text)
    }

    fun swipeUser(profileState: ProfileState, isLike: Boolean) = viewModelScope.launch {
        if (isLike) {
            likeProfileUseCase(profileState.profile).onSuccess { result ->
                result?.let { newMatch ->
                    _uiState.update {
                        it.copy(
                            dialogState = HomeViewDialogState.NewMatchDialog(newMatch, profileState.pictureUrls)
                        )
                    }
                }
            }
        } else {
            passProfileUseCase(profileState.profile)
        }
    }

    fun removeLastProfile() {
        _uiState.update {
            if (it.contentState is HomeViewContentState.Success) {
                it.copy(
                    contentState = it.contentState.copy(
                        profileStates = it.contentState.profileStates.dropLast(1)
                    )
                )
            } else it
        }
    }

    fun fetchProfiles() = viewModelScope.launch {
        _uiState.update { it.copy(contentState = HomeViewContentState.Loading) }
        val profiles = fakeProfiles

        val profileStates = profiles.map { profile ->
            ProfileState(
                profile,
                fixedImageUrls.take(profile.pictureNames.size)
            )
        }
        _uiState.update { it.copy(contentState = HomeViewContentState.Success(profileStates)) }

// Carregar as imagens dos perfis
        profiles.forEach { profile ->
            loadProfilePictures(profile.id, profile.pictureNames)
        }
    }

    private suspend fun loadProfilePictures(userId: String, pictureNames: List<String>) {
        pictureNames.forEachIndexed { index, pictureName ->
            viewModelScope.launch {
                getPictureUseCase(userId, pictureName).onSuccess { pictureUrl ->
                    updatePicturesState(userId, index, pictureUrl)
                }
            }
        }
    }

    private fun updatePicturesState(userId: String, pictureIndex: Int, pictureUrl: String) {
        _uiState.update {
            if (it.contentState is HomeViewContentState.Success) {
                it.copy(contentState = it.contentState.copy(
                    profileStates = it.contentState.profileStates.map { profileState ->
                        if (profileState.profile.id == userId) {
                            profileState.copy(pictureUrls = profileState.pictureUrls.toMutableList().apply {
                                set(pictureIndex, pictureUrl)
                            })
                        } else profileState
                    }
                ))
            } else it
        }
    }

}

@Immutable
data class HomeViewState(
    val dialogState: HomeViewDialogState,
    val contentState: HomeViewContentState
)

@Immutable
data class ProfileState(val profile: Profile, val pictureUrls: List<String>)

@Immutable
sealed class HomeViewDialogState {
    object NoDialog : HomeViewDialogState()
    data class NewMatchDialog(val match: Match, val pictureUrls: List<String>) : HomeViewDialogState()
}
// URLs fixas das imagens
val fixedImageUrls = listOf(
    "https://projetandopessoas.com.br/wp-content/uploads/2018/05/marilia-sore-289x300.jpg",
    "https://igd-wp-uploads-pluginaws.s3.amazonaws.com/wp-content/uploads/2016/05/30105213/Qual-é-o-Perfil-do-Empreendedor.jpg",
    "https://projetandopessoas.com.br/wp-content/uploads/2015/01/unnamed5.jpg",
    "https://projetandopessoas.com.br/wp-content/uploads/2018/02/gilmar-carneiro-300x283.jpg",
    "https://projetandopessoas.com.br/wp-content/uploads/2018/05/marilia-sore-289x300.jpg",
    "https://www.projetandopessoas.com.br/wp-content/uploads/2013/11/andre-lima.jpg",
    "https://static1.purepeople.com.br/articles/2/38/55/92/@/4428806-camilla-camargo-foi-uma-das-pessoas-crit-580x0-2.jpg",
    "https://i.pinimg.com/originals/4f/73/3b/4f733b83724e86f43c759de191f7e9fc.jpg"
)

// Vamos criar um perfil fictício para demonstrar
val fakeProfiles = listOf(
    Profile(id = "1", name = "Daniel Oliveira", age = 27, pictureNames = listOf("profile1")),
    Profile(id = "2", name = "Felipe", age = 27, pictureNames = listOf("profile2")),
    Profile(id = "3", name = "Unnamed5", age = 27, pictureNames = listOf("profile3")),
    Profile(id = "4", name = "Gilmar Carneiro", age = 27, pictureNames = listOf("profile4")),
    Profile(id = "5", name = "Marilia Sore", age = 27, pictureNames = listOf("profile5")),
    Profile(id = "6", name = "Andre Lima", age = 27, pictureNames = listOf("profile6")),
    Profile(id = "7", name = "Camilla Camargo", age = 27, pictureNames = listOf("profile7")),
    Profile(id = "8", name = "Maria", age = 27, pictureNames = listOf("profile8"))
)


val listaImutavel = listOf(
    ProfilePictureState.Loading("nome1"),
    ProfilePictureState.Remote(Uri.parse("https://assets.propmark.com.br/uploads/2023/02/13DanielOliveira-1.png")),
    ProfilePictureState.Loading("nome2"),
    ProfilePictureState.Remote(Uri.parse("https://igd-wp-uploads-pluginaws.s3.amazonaws.com/wp-content/uploads/2016/05/30105213/Qual-é-o-Perfil-do-Empreendedor.jpg"))
)
@Immutable
sealed class HomeViewContentState {
    object Loading : HomeViewContentState()
    data class Success(val profileStates: List<ProfileState>) : HomeViewContentState()
    data class Error(val message: String) : HomeViewContentState()
}
