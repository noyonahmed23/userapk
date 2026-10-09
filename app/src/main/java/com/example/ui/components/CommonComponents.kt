package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TournamentStatus
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun AppHeader(
  title: String = "খেলো বিডি",
  unreadNotificationCount: Int = 0,
  userBalance: Double,
  onMenuClick: () -> Unit,
  onNotificationClick: () -> Unit,
  onProfileClick: () -> Unit,
  onWalletClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = GamingDarkSurface,
    tonalElevation = 4.dp,
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(
          onClick = onMenuClick,
          modifier = Modifier
            .size(44.dp)
            .testTag("menu_button")
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "মেনু খুলুন",
            tint = TextPrimary
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { onProfileClick() }
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(CyberOrange, CyberRed))),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "KB",
              color = Color.White,
              fontWeight = FontWeight.Black,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Text(
              text = title,
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
            Text(
              text = "ESPORTS ARENA",
              color = CyberOrangeGlow,
              fontWeight = FontWeight.SemiBold,
              fontSize = 9.sp,
              letterSpacing = 1.sp
            )
          }
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Wallet Balance pill
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = GamingDarkSurfaceVariant,
          border = androidx.compose.foundation.BorderStroke(1.dp, CyberOrange.copy(alpha = 0.5f)),
          modifier = Modifier
            .clickable { onWalletClick() }
            .testTag("wallet_pill")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AccountBalanceWallet,
              contentDescription = "ওয়ালেট",
              tint = CyberOrange,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "৳${userBalance.toInt()}",
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        }

        // Notification Icon with unread badge (Account icon has red badge)
        IconButton(
          onClick = onNotificationClick,
          modifier = Modifier
            .size(40.dp)
            .testTag("notification_button")
        ) {
          Box(contentAlignment = Alignment.TopEnd) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "নোটিফিকেশন",
              tint = TextPrimary
            )
            if (unreadNotificationCount > 0) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(CyberRed)
                  .border(1.dp, GamingDarkSurface, CircleShape)
              )
            }
          }
        }

        // Account Profile Icon
        IconButton(
          onClick = onProfileClick,
          modifier = Modifier
            .size(40.dp)
            .testTag("profile_button")
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(GamingDarkSurfaceVariant)
              .border(1.5.dp, CyberCyan, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "প্রোফাইল",
              tint = CyberCyan,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun TournamentStatusBadge(status: TournamentStatus, modifier: Modifier = Modifier) {
  val (bgColor, textColor, textBn) = when (status) {
    TournamentStatus.JOIN_OPEN -> Triple(CyberGreen.copy(alpha = 0.15f), CyberGreen, "JOIN OPEN")
    TournamentStatus.JOIN_CLOSED -> Triple(CyberOrange.copy(alpha = 0.15f), CyberOrange, "JOIN CLOSED")
    TournamentStatus.LIVE -> Triple(CyberRed.copy(alpha = 0.2f), CyberRed, "LIVE MATCH")
    TournamentStatus.FINISHED -> Triple(Color(0xFFEAB308).copy(alpha = 0.15f), Color(0xFFEAB308), "FINISHED")
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = bgColor,
    border = androidx.compose.foundation.BorderStroke(1.dp, textColor.copy(alpha = 0.4f)),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(textColor)
      )
      Text(
        text = textBn,
        color = textColor,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun OnlineStatusBadge(isOnline: Boolean, modifier: Modifier = Modifier) {
  val color = if (isOnline) CyberGreen else TextMuted
  val label = if (isOnline) "অনলাইন" else "অফলাইন"

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    modifier = modifier
  ) {
    Box(
      modifier = Modifier
        .size(7.dp)
        .clip(CircleShape)
        .background(color)
    )
    Text(
      text = label,
      color = color,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun PlayerClickableChip(
  name: String,
  isOnline: Boolean = true,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = GamingDarkSurfaceVariant,
    modifier = modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(22.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(CyberCyan, CyberOrange))),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = name.take(1).uppercase(),
          color = Color.Black,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        )
      }
      Text(
        text = name,
        color = TextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(if (isOnline) CyberGreen else TextMuted)
      )
    }
  }
}

@Composable
fun BengaliConfirmDialog(
  title: String,
  message: String,
  confirmButtonText: String = "নিশ্চিত করুন",
  cancelButtonText: String = "বাতিল",
  isDanger: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(16.dp),
    containerColor = GamingDarkSurface,
    titleContentColor = TextPrimary,
    textContentColor = TextSecondary,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(
          imageVector = if (isDanger) Icons.Default.Warning else Icons.Default.Info,
          contentDescription = null,
          tint = if (isDanger) CyberRed else CyberOrange
        )
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
      }
    },
    text = {
      Text(text = message, fontSize = 14.sp, lineHeight = 20.sp)
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isDanger) CyberRed else CyberOrange,
          contentColor = if (isDanger) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("dialog_confirm_button")
      ) {
        Text(text = confirmButtonText, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
        modifier = Modifier.testTag("dialog_cancel_button")
      ) {
        Text(text = cancelButtonText)
      }
    }
  )
}

@Composable
fun PublicProfileDialog(
  user: UserProfile,
  onDismiss: () -> Unit,
  onChallengeClick: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = GamingDarkSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, GamingCardBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Banner avatar header
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "KHELOBD VERIFIED GAMER",
            color = CyberCyan.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Avatar
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(CyberOrange, CyberRed)))
            .border(2.dp, CyberCyan, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = user.fullName.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 26.sp
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = user.fullName,
          color = TextPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "@${user.username} (${user.userCode})",
            color = TextSecondary,
            fontSize = 13.sp
          )
          OnlineStatusBadge(isOnline = user.isOnline)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics Cards Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkSurfaceVariant,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "ম্যাচ", color = TextSecondary, fontSize = 11.sp)
              Text(text = "${user.matchesPlayed}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkSurfaceVariant,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(text = "বিজয়", color = CyberGreen, fontSize = 11.sp)
              Text(text = "${user.wins}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = GamingDarkSurfaceVariant,
            modifier = Modifier.weight(1f)
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              val winRate = if (user.matchesPlayed > 0) (user.wins * 100 / user.matchesPlayed) else 0
              Text(text = "উইনরেট", color = CyberOrange, fontSize = 11.sp)
              Text(text = "$winRate%", color = CyberOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Game IDs
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GamingDarkSurfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "Free Fire UID:", color = TextSecondary, fontSize = 12.sp)
              Text(text = user.freeFireUid.ifBlank { "নট সেট" }, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(text = "PUBG UID:", color = TextSecondary, fontSize = 12.sp)
              Text(text = user.pubgUid.ifBlank { "নট সেট" }, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Text(text = "বন্ধ করুন", color = TextSecondary)
          }

          Button(
            onClick = {
              onDismiss()
              onChallengeClick()
            },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "চ্যালেঞ্জ দিন", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
