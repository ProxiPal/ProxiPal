package com.mongodb.app.friends

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable

import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mongodb.app.R
import com.mongodb.app.ui.theme.MyApplicationTheme
//Vichet Chim

//report dropdown menu
@Composable
fun ReportDropDownMenu(reportedUser : String,reportViewModel: ReportViewModel){
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false)}
    var showReportOptions by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd)
    )   {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More"
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(id = R.string.report)) }, onClick = { expanded=false; showReportOptions=true})
        }
    }
    if (showReportOptions) {
        ReportOptionsDialog(
            onDismiss = { showReportOptions = false }, reportViewModel = reportViewModel, reportedUser = reportedUser
        )
    }
}


//report options alertdialog
@Composable
fun ReportOptionsDialog(
    onDismiss: () -> Unit, reportViewModel: ReportViewModel, reportedUser: String
) {
    var comments by remember { mutableStateOf("")}
    val context = LocalContext.current
    val reasons = listOf(
        context.getString(R.string.inappropriate),
        context.getString(R.string.harassment),
        context.getString(R.string.spam),
        context.getString(R.string.others)
    )
    val checkboxStates = remember {mutableStateMapOf<String, Boolean>().withDefault{false}}
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.reportuser))
        },
        text = {
            Column {
                Text(
                    text = stringResource(id = R.string.report_subheading),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ReportCheckBox(reasons, checkboxStates)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.comments),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TextField(
                    value = comments,
                    onValueChange = {comments = it},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val checkedReasons = getCheckedReasons(checkboxStates)
                    Log.d("reasons list", checkedReasons.toString())
                    reportViewModel.addReport(userReported =reportedUser ,reasons=checkedReasons, comment=comments)
                    onDismiss()

                }
            ) {
                Text(text = stringResource(id = R.string.report))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

//gets the checked reasons as a list
private fun getCheckedReasons(checkboxStates: Map<String, Boolean>): List<String> {
    return checkboxStates.filterValues { it }.keys.toList()
}

//checkbox reusable
@Composable
fun ReportCheckBox(reasons: List<String>, checkboxStates:MutableMap<String, Boolean>){
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center){
        reasons.forEach{ reason ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .toggleable(
                        value = checkboxStates.getValue(reason),
                        onValueChange = { checkboxStates[reason] = it }
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Checkbox(
                    checked = checkboxStates.getValue(reason),
                    onCheckedChange = {checkboxStates[reason]=it}
                )
                Text(text = reason)
            }
        }
    }
}

@Composable
fun ReportPreview(reportViewModel:ReportViewModel){
    MyApplicationTheme {
        ReportDropDownMenu(reportedUser = "test", reportViewModel= reportViewModel)
        //ReportOptionsDialog(onDismiss = { /*TODO*/ })

    }
}