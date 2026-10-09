package com.example.ui.screens.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.NotificationItem
import com.example.ui.theme.*

@Composable
fun NotificationsDialog(
  notifications: List<NotificationItem>,
  onDismiss: () -> Unit,
  onNotificationClick: (NotificationItem) -> Unit,
  onMarkAllRead: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GamingDarkSurface,
      border = BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(max = 520.dp)
        .padding(14.dp)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = CyberOrange)
            Text(text = "নোটিফিকেশন", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
          }

          if (notifications.any { !it.isRead }) {
            Text(
              text = "সব পঠিত করুন",
              color = CyberCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.clickable { onMarkAllRead() }
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(text = "কোনো নতুন নোটিফিকেশন নেই", color = TextSecondary, fontSize = 13.sp)
          }
        } else {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
          ) {
            items(notifications) { notif ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (notif.isRead) GamingDarkSurfaceVariant.copy(alpha = 0.5f) else GamingDarkSurfaceVariant,
                border = BorderStroke(1.dp, if (notif.isRead) Color.Transparent else CyberOrange.copy(alpha = 0.4f)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onNotificationClick(notif) }
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.Top,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(CircleShape)
                      .background(
                        when (notif.targetType) {
                          "CHALLENGE" -> CyberCyan.copy(alpha = 0.2f)
                          "TOURNAMENT" -> CyberOrange.copy(alpha = 0.2f)
                          "WALLET" -> CyberGreen.copy(alpha = 0.2f)
                          else -> GamingCardBorder
                        }
                      ),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = when (notif.targetType) {
                        "CHALLENGE" -> Icons.Default.SportsKabaddi
                        "TOURNAMENT" -> Icons.Default.EmojiEvents
                        "WALLET" -> Icons.Default.AccountBalanceWallet
                        else -> Icons.Default.Notifications
                      },
                      contentDescription = null,
                      tint = when (notif.targetType) {
                        "CHALLENGE" -> CyberCyan
                        "TOURNAMENT" -> CyberOrange
                        "WALLET" -> CyberGreen
                        else -> TextPrimary
                      },
                      modifier = Modifier.size(16.dp)
                    )
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = notif.title,
                      color = TextPrimary,
                      fontWeight = if (notif.isRead) FontWeight.SemiBold else FontWeight.Bold,
                      fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = notif.message,
                      color = TextSecondary,
                      fontSize = 11.sp,
                      lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = notif.timestamp,
                      color = TextMuted,
                      fontSize = 10.sp
                    )
                  }

                  if (!notif.isRead) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberRed)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = onDismiss,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(text = "বন্ধ করুন", color = TextSecondary)
        }
      }
    }
  }
}
