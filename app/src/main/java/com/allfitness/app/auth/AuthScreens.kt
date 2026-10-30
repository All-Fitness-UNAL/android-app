package com.allfitness.app.auth

import android.util.Patterns
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.allfitness.app.BuildConfig
import com.allfitness.app.R
import com.allfitness.app.core.network.AppResult
import com.allfitness.app.core.network.ConnectivityObserver
import com.allfitness.app.core.ui.AppTopBar
import com.allfitness.app.core.ui.OfflineHint
import com.allfitness.app.core.ui.messageRes
import com.allfitness.app.core.ui.theme.Spacing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Validación de formato antes de enviar (FR-002); las credenciales las verifica el servidor. */
object AuthValidation {
    @StringRes
    fun name(value: String): Int? = if (value.isBlank()) R.string.error_required_field else null

    @StringRes
    fun email(value: String): Int? = when {
        value.isBlank() -> R.string.error_required_field
        !Patterns.EMAIL_ADDRESS.matcher(value.trim()).matches() -> R.string.error_email_format
        else -> null
    }

    @StringRes
    fun password(value: String): Int? = when {
        value.isEmpty() -> R.string.error_required_field
        value.length < 8 -> R.string.error_password_length
        else -> null
    }
}

data class AuthFormState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val acceptedLegal: Boolean = false,
    @StringRes val nameError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val legalError: Int? = null,
    @StringRes val error: Int? = null,
    val loading: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    connectivity: ConnectivityObserver,
) : ViewModel() {
    private val _state = MutableStateFlow(AuthFormState())
    val state: StateFlow<AuthFormState> = _state.asStateFlow()
    val isOnline = connectivity.isOnline

    fun onName(value: String) = _state.update { it.copy(name = value, nameError = null, error = null) }
    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, error = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, passwordError = null, error = null) }
    fun onAcceptLegal(value: Boolean) = _state.update { it.copy(acceptedLegal = value, legalError = null) }

    fun login() {
        val s = _state.value
        val checked = s.copy(emailError = AuthValidation.email(s.email), passwordError = AuthValidation.password(s.password))
        submit(checked, checked.emailError == null && checked.passwordError == null) {
            repository.login(s.email, s.password)
        }
    }

    fun register() {
        val s = _state.value
        val checked = s.copy(
            nameError = AuthValidation.name(s.name),
            emailError = AuthValidation.email(s.email),
            passwordError = AuthValidation.password(s.password),
            legalError = if (s.acceptedLegal) null else R.string.error_accept_legal,
        )
        val valid = listOf(checked.nameError, checked.emailError, checked.passwordError, checked.legalError).all { it == null }
        submit(checked, valid) { repository.register(s.name, s.email, s.password) }
    }

    private fun submit(checked: AuthFormState, valid: Boolean, call: suspend () -> AppResult<Unit>) {
        if (checked.loading) return
        _state.value = checked
        if (!valid) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            // Con éxito, SessionManager cambia de estado y la app navega sola.
            val result = call()
            _state.update { it.copy(loading = false, error = (result as? AppResult.Failure)?.error?.messageRes()) }
        }
    }
}

@Composable
fun LoginScreen(sessionExpired: Boolean, onRegister: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    AuthScaffold(title = stringResource(R.string.login_title)) {
        Text(stringResource(R.string.login_subtitle), style = MaterialTheme.typography.bodyLarge)
        if (sessionExpired) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(
                    stringResource(R.string.error_session_expired),
                    modifier = Modifier.padding(Spacing.m),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
        EmailField(state.email, state.emailError, viewModel::onEmail)
        PasswordField(state.password, state.passwordError, viewModel::onPassword)
        SubmitArea(state, online, stringResource(R.string.action_login), viewModel::login)
        TextButton(onClick = onRegister, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.login_go_register))
        }
    }
}

@Composable
fun RegisterScreen(onBack: () -> Unit, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val online by viewModel.isOnline.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    AuthScaffold(title = stringResource(R.string.register_title), onBack = onBack) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onName,
            label = { Text(stringResource(R.string.field_name)) },
            isError = state.nameError != null,
            supportingText = state.nameError?.let { { Text(stringResource(it)) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        EmailField(state.email, state.emailError, viewModel::onEmail)
        PasswordField(state.password, state.passwordError, viewModel::onPassword)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = state.acceptedLegal, role = Role.Checkbox, onValueChange = viewModel::onAcceptLegal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // FR-002a: desmarcada por defecto y obligatoria.
            Checkbox(checked = state.acceptedLegal, onCheckedChange = null)
            Text(stringResource(R.string.register_accept_legal), style = MaterialTheme.typography.bodyMedium)
        }
        state.legalError?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            TextButton(onClick = { uriHandler.openUri(BuildConfig.TERMS_URL) }) { Text(stringResource(R.string.register_read_terms)) }
            TextButton(onClick = { uriHandler.openUri(BuildConfig.PRIVACY_URL) }) { Text(stringResource(R.string.register_read_privacy)) }
        }
        SubmitArea(state, online, stringResource(R.string.action_register), viewModel::register)
    }
}

@Composable
private fun AuthScaffold(title: String, onBack: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = { AppTopBar(title, onBack) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) { content() }
    }
}

@Composable
private fun EmailField(value: String, @StringRes error: Int?, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.field_email)) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordField(value: String, @StringRes error: Int?, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.field_password)) },
        isError = error != null,
        supportingText = { Text(stringResource(error ?: R.string.password_hint)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SubmitArea(state: AuthFormState, online: Boolean, label: String, onSubmit: () -> Unit) {
    state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
    if (!online) OfflineHint()
    Button(onClick = onSubmit, enabled = online && !state.loading, modifier = Modifier.fillMaxWidth()) {
        Text(if (state.loading) stringResource(R.string.loading) else label)
    }
}
