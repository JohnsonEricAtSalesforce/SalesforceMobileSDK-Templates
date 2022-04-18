package com.salesforce.mobilesyncexplorerkotlintemplate.contacts.detailscomponent.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.salesforce.mobilesyncexplorerkotlintemplate.R.drawable.ic_undo
import com.salesforce.mobilesyncexplorerkotlintemplate.R.string.*
import com.salesforce.mobilesyncexplorerkotlintemplate.contacts.detailscomponent.ContactDetailsClickHandler
import com.salesforce.mobilesyncexplorerkotlintemplate.contacts.detailscomponent.ContactDetailsUiState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.SObjectUiSyncState
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.state.WINDOW_SIZE_MEDIUM_CUTOFF_DP
import com.salesforce.mobilesyncexplorerkotlintemplate.core.ui.theme.SalesforceMobileSDKAndroidTheme
import com.salesforce.mobilesyncexplorerkotlintemplate.model.contacts.ContactObject

@Composable
fun RowScope.ContactDetailsTopBarContentExpanded(
    detailsUiState: ContactDetailsUiState,
    eventHandler: ContactDetailsClickHandler
) {
    when (detailsUiState) {
        is ContactDetailsUiState.NoContactSelected -> {}
        is ContactDetailsUiState.ViewingContactDetails -> {
            if (detailsUiState.uiSyncState != SObjectUiSyncState.Deleted) {
                IconButton(onClick = eventHandler::deleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(id = cta_delete)
                    )
                }
            }
            when {
                detailsUiState.uiSyncState == SObjectUiSyncState.Deleted -> {
                    IconButton(onClick = eventHandler::undeleteClick) {
                        Icon(
                            painter = painterResource(id = ic_undo),
                            contentDescription = stringResource(id = cta_undelete)
                        )
                    }
                }
                detailsUiState.isEditingEnabled -> {
                    IconButton(onClick = eventHandler::saveClick) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = stringResource(id = cta_save)
                        )
                    }
                }
                else -> {
                    IconButton(onClick = eventHandler::editClick) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(id = cta_edit)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = WINDOW_SIZE_MEDIUM_CUTOFF_DP)
@Composable
private fun ContactDetailsTopBarExpandedPreview() {
    val mockContact = ContactObject(
        firstName = "First",
        lastName = "Last",
        title = null,
        department = null
    )
    SalesforceMobileSDKAndroidTheme {
        Surface {
            Column {
                TopAppBar(modifier = Modifier.padding(8.dp)) {
                    Text(mockContact.fullName)
                    Spacer(modifier = Modifier.weight(1f))
                    ContactDetailsTopBarContentExpanded(
                        detailsUiState = mockContact.toPreviewViewingContactDetails(),
                        eventHandler = PREVIEW_CONTACT_DETAILS_UI_HANDLER
                    )
                }

                TopAppBar(modifier = Modifier.padding(8.dp)) {
                    Text(mockContact.fullName)
                    Spacer(modifier = Modifier.weight(1f))
                    ContactDetailsTopBarContentExpanded(
                        detailsUiState = mockContact.toPreviewViewingContactDetails(
                            isEditingEnabled = true
                        ),
                        eventHandler = PREVIEW_CONTACT_DETAILS_UI_HANDLER
                    )
                }

                TopAppBar(modifier = Modifier.padding(8.dp)) {
                    Text(mockContact.fullName)
                    Spacer(modifier = Modifier.weight(1f))
                    ContactDetailsTopBarContentExpanded(
                        detailsUiState = mockContact.toPreviewViewingContactDetails(
                            uiSyncState = SObjectUiSyncState.Deleted
                        ),
                        eventHandler = PREVIEW_CONTACT_DETAILS_UI_HANDLER
                    )
                }

                TopAppBar(modifier = Modifier.padding(8.dp)) {
                    Text(stringResource(id = label_contacts))
                    Spacer(modifier = Modifier.weight(1f))
                    ContactDetailsTopBarContentExpanded(
                        detailsUiState = ContactDetailsUiState.NoContactSelected(),
                        eventHandler = PREVIEW_CONTACT_DETAILS_UI_HANDLER
                    )
                }
            }
        }
    }
}
