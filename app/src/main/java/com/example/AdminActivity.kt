package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.*
import com.example.data.repository.KheloBDRepository
import com.example.ui.components.BengaliConfirmDialog
import com.example.ui.components.TournamentStatusBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class AdminSection(val title: String, val icon: String) {
  OVERVIEW("◫ Overview", "◫"),
  GAMES("▧ Games", "▧"),
  ADD_TOURNAMENT("＋ Add tournament", "＋"),
  TOURNAMENT_MANAGER("⌁ Tournament manager", "⌁"),
  TOURNAMENT_FINDER("⌕ Tournament finder", "⌕"),
  RESULT_FINDER("✓ Result finder", "✓"),
  TEAMS("◈ Teams / Squads", "◈"),
  TEAM_FINDER("⌕ Team finder", "⌕"),
  TEAM_DEPOSIT("↓ Team deposit", "↓"),
  TEAM_WITHDRAW("↑ Team withdraw", "↑"),
  TEAM_CHALLENGE("⚔ Team challenge", "⚔"),
  CHALLENGE_PROOF("▣ Challenge proof", "▣"),
  CHALLENGE_MATCH("◎ Challenge match", "◎"),
  PUBLISHER_FIELDS("✦ Publisher fields", "✦"),
  APPLICATIONS("◆ Applications", "◆"),
  RESULTS("◎ Results", "◎"),
  DEPOSITS("↓ Deposits", "↓"),
  WITHDRAWALS("↑ Withdrawals", "↑"),
  PAYMENT_METHODS("৳ Payment methods", "৳"),
  SUPPORT("? Support", "?"),
  PLAYERS("◉ Players", "◉"),
  CONTENT("▤ Content", "▤"),
  BANNERS_ADS("▰ Banners / Ads", "▰"),
  SETTINGS("⚙ Settings", "⚙"),
  REPORTS("▥ Reports", "▥"),
  ACTIVITY_LOG("≡ Activity log", "≡"),
  VIEW_SITE("↗ View site", "↗")
}

class AdminActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        AdminAppRoot(
          onLaunchUserApp = {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppRoot(onLaunchUserApp: () -> Unit) {
  val repository = remember { KheloBDRepository.getInstance() }
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val snackbarHostState = remember { SnackbarHostState() }

  val tournaments by repository.tournaments.collectAsStateWithLifecycle()
  val challenges by repository.challenges.collectAsStateWithLifecycle()
  val teams by repository.teams.collectAsStateWithLifecycle()
  val teamChallenges by repository.teamChallenges.collectAsStateWithLifecycle()
  val transactions by repository.transactions.collectAsStateWithLifecycle()
  val gameCategories by repository.gameCategories.collectAsStateWithLifecycle()
  val banners by repository.banners.collectAsStateWithLifecycle()
  val topPlayers by repository.topPlayers.collectAsStateWithLifecycle()
  val currentUser by repository.currentUser.collectAsStateWithLifecycle()

  var selectedSection by remember { mutableStateOf(AdminSection.OVERVIEW) }
  var searchQuery by remember { mutableStateOf("") }
  var showLogoutConfirm by remember { mutableStateOf(false) }

  // Action dialogs
  var showCreateTourDialog by remember { mutableStateOf(false) }
  var tourToManage by remember { mutableStateOf<Tournament?>(null) }
  var proofToInspect by remember { mutableStateOf<PlayerChallenge?>(null) }

  BackHandler(enabled = selectedSection != AdminSection.OVERVIEW || drawerState.isOpen) {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else {
      selectedSection = AdminSection.OVERVIEW
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = GamingDarkSurface,
        drawerContentColor = TextPrimary,
        modifier = Modifier.width(300.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF381014), GamingDarkSurface)))
            .padding(18.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_khelo_logo),
              contentDescription = null,
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.5.dp, CyberRed, RoundedCornerShape(10.dp))
            )
            Column {
              Text(
                text = "KHELOBD ADMIN",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
              )
              Text(
                text = "SUPER ADMIN CONSOLE",
                color = CyberRed,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = GamingDarkBackground,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(CyberGreen)
              )
              Text(text = "Logged in: Mohammad Noyon", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }

        HorizontalDivider(color = GamingCardBorder)

        Text(
          text = "OPERATIONS",
          color = TextSecondary,
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp,
          letterSpacing = 1.sp,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(bottom = 12.dp)
        ) {
          items(AdminSection.entries) { section ->
            val isSelected = selectedSection == section
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) CyberRed.copy(alpha = 0.2f) else Color.Transparent,
              border = if (isSelected) BorderStroke(1.dp, CyberRed.copy(alpha = 0.6f)) else null,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .clickable {
                  if (section == AdminSection.VIEW_SITE) {
                    onLaunchUserApp()
                  } else {
                    selectedSection = section
                  }
                  scope.launch { drawerState.close() }
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Text(
                  text = section.icon,
                  color = if (isSelected) CyberRed else TextSecondary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = section.title.drop(2),
                  color = if (isSelected) Color.White else TextPrimary,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 13.sp
                )
              }
            }
          }
        }
      }
    }
  ) {
    Scaffold(
      topBar = {
        Surface(
          color = GamingDarkSurface,
          tonalElevation = 4.dp,
          border = BorderStroke(1.dp, GamingCardBorder)
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
              IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "মেনু", tint = TextPrimary)
              }
              Column {
                Text(
                  text = selectedSection.title,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
                Text(
                  text = "KheloBD Administration",
                  color = CyberRed,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
              Button(
                onClick = onLaunchUserApp,
                colors = ButtonDefaults.buttonColors(containerColor = GamingDarkSurfaceVariant, contentColor = CyberCyan),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
              ) {
                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "View site", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              IconButton(onClick = { showLogoutConfirm = true }, modifier = Modifier.size(36.dp)) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", tint = CyberRed)
              }
            }
          }
        }
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      containerColor = GamingDarkBackground,
      modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (selectedSection) {
          AdminSection.OVERVIEW -> {
            AdminOverviewPanel(
              tournaments = tournaments,
              challenges = challenges,
              teams = teams,
              transactions = transactions,
              onNavigate = { sec -> selectedSection = sec }
            )
          }

          AdminSection.GAMES -> {
            AdminGamesPanel(
              gameCategories = gameCategories,
              onAddTournamentForGame = {
                showCreateTourDialog = true
              }
            )
          }

          AdminSection.ADD_TOURNAMENT -> {
            AdminAddTournamentPanel(
              onCreate = { gameId, title, mode, map, fee, prize, slots, time, rules ->
                repository.adminCreateTournament(gameId, title, mode, map, fee, prize, slots, time, rules)
                scope.launch {
                  snackbarHostState.showSnackbar("টুর্নামেন্ট সফলভাবে পাবলিশ করা হয়েছে!")
                }
                selectedSection = AdminSection.TOURNAMENT_MANAGER
              }
            )
          }

          AdminSection.TOURNAMENT_MANAGER, AdminSection.TOURNAMENT_FINDER -> {
            AdminTournamentManagerPanel(
              tournaments = tournaments,
              searchQuery = searchQuery,
              onSearchChange = { searchQuery = it },
              onManageClick = { tourToManage = it },
              onAddClick = { showCreateTourDialog = true }
            )
          }

          AdminSection.RESULT_FINDER, AdminSection.RESULTS -> {
            AdminResultsPanel(
              tournaments = tournaments.filter { it.status == TournamentStatus.FINISHED },
              challenges = challenges.filter { it.status == ChallengeStatus.FINISHED }
            )
          }

          AdminSection.TEAMS, AdminSection.TEAM_FINDER -> {
            AdminTeamsPanel(teams = teams)
          }

          AdminSection.TEAM_DEPOSIT, AdminSection.TEAM_WITHDRAW -> {
            AdminTeamWalletReviewPanel(
              teams = teams,
              isWithdrawWindow = repository.isWithinWithdrawWindow()
            )
          }

          AdminSection.TEAM_CHALLENGE -> {
            AdminTeamChallengePanel(
              teamChallenges = teamChallenges,
              onSettleTeam = {
                scope.launch { snackbarHostState.showSnackbar("টিম চ্যালেঞ্জ আপডেট করা হয়েছে।") }
              }
            )
          }

          AdminSection.CHALLENGE_MATCH, AdminSection.CHALLENGE_PROOF -> {
            AdminChallengeMatchPanel(
              challenges = challenges,
              onInspectProof = { proofToInspect = it },
              onSettle = { chId, winId ->
                val res = repository.settleChallenge(chId, winId)
                scope.launch {
                  snackbarHostState.showSnackbar(res.getOrDefault("নিষ্পত্তি সম্পন্ন হয়েছে!"))
                }
              }
            )
          }

          AdminSection.DEPOSITS, AdminSection.WITHDRAWALS, AdminSection.PAYMENT_METHODS -> {
            AdminFinancialsPanel(
              transactions = transactions,
              onApprove = {
                scope.launch { snackbarHostState.showSnackbar("ট্রানজেকশন অ্যাপ্রুভ করা হয়েছে।") }
              }
            )
          }

          AdminSection.PLAYERS -> {
            AdminPlayersPanel(
              players = topPlayers,
              currentUser = currentUser
            )
          }

          AdminSection.BANNERS_ADS -> {
            AdminBannersPanel(banners = banners)
          }

          AdminSection.SETTINGS -> {
            AdminSettingsPanel(
              onLogout = { showLogoutConfirm = true }
            )
          }

          AdminSection.REPORTS, AdminSection.ACTIVITY_LOG -> {
            AdminReportsPanel(
              tournaments = tournaments,
              challenges = challenges,
              transactions = transactions
            )
          }

          else -> {
            AdminGenericOperationsPanel(section = selectedSection)
          }
        }
      }
    }
  }

  // Logout Confirm Dialog
  if (showLogoutConfirm) {
    BengaliConfirmDialog(
      title = "এডমিন লগআউট",
      message = "এডমিন মোহাম্মদ নয়ন (Noyon), আপনি কি এডমিন প্যানেল থেকে লগআউট করতে চান?",
      confirmButtonText = "হ্যাঁ, লগআউট",
      cancelButtonText = "বাতিল",
      isDanger = true,
      onConfirm = {
        showLogoutConfirm = false
        onLaunchUserApp()
      },
      onDismiss = { showLogoutConfirm = false }
    )
  }

  // Quick Create Tournament Dialog
  if (showCreateTourDialog) {
    var gameId by remember { mutableStateOf("freefire") }
    var title by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Solo BR") }
    var mapName by remember { mutableStateOf("Bermuda") }
    var entryFee by remember { mutableStateOf("50") }
    var prizePool by remember { mutableStateOf("2000") }
    var maxSlots by remember { mutableStateOf("48") }
    var startTime by remember { mutableStateOf("আজ রাত ৯:০০ টা") }
    var rules by remember { mutableStateOf("১. কোনো হ্যাক বা স্ক্রিপ্ট ব্যবহার করা যাবে না।\n২. রুম কোড সময়মত দেওয়া হবে।") }

    Dialog(onDismissRequest = { showCreateTourDialog = false }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, CyberRed),
        modifier = Modifier.padding(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "নতুন টুর্নামেন্ট তৈরি করুন", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("টুর্নামেন্টের নাম") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = mode, onValueChange = { mode = it }, label = { Text("মোড (যেমন: Solo BR)") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = entryFee, onValueChange = { entryFee = it }, label = { Text("এন্ট্রি ফি (৳)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = prizePool, onValueChange = { prizePool = it }, label = { Text("প্রাইজপুল (৳)") }, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(14.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showCreateTourDialog = false }, modifier = Modifier.weight(1f)) {
              Text(text = "বাতিল", color = TextSecondary)
            }
            Button(
              onClick = {
                val fee = entryFee.toDoubleOrNull() ?: 50.0
                val prize = prizePool.toDoubleOrNull() ?: 2000.0
                val slots = maxSlots.toIntOrNull() ?: 48
                repository.adminCreateTournament(gameId, title.ifBlank { "নতুন টুর্নামেন্ট" }, mode, mapName, fee, prize, slots, startTime, rules)
                showCreateTourDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyberRed, contentColor = Color.White),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "পাবলিশ", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  // Manage Tournament Dialog (Status & Room ID/Pass)
  tourToManage?.let { tour ->
    var currentStatus by remember { mutableStateOf(tour.status) }
    var roomIdInput by remember { mutableStateOf(tour.roomId ?: "") }
    var roomPassInput by remember { mutableStateOf(tour.roomPassword ?: "") }

    Dialog(onDismissRequest = { tourToManage = null }) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.padding(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "টুর্নামেন্ট নিয়ন্ত্রণ: ${tour.title}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(10.dp))

          Text(text = "স্ট্যাটাস পরিবর্তন:", color = TextSecondary, fontSize = 12.sp)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            TournamentStatus.values().forEach { st ->
              val isSel = currentStatus == st
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSel) CyberRed else GamingDarkSurfaceVariant,
                modifier = Modifier
                  .weight(1f)
                  .clickable { currentStatus = st }
              ) {
                Text(
                  text = st.name.take(4),
                  color = if (isSel) Color.White else TextSecondary,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(vertical = 6.dp),
                  textAlign = TextAlign.Center
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(value = roomIdInput, onValueChange = { roomIdInput = it }, label = { Text("রুম আইডি") }, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(value = roomPassInput, onValueChange = { roomPassInput = it }, label = { Text("রুম পাসওয়ার্ড") }, modifier = Modifier.fillMaxWidth())

          Spacer(modifier = Modifier.height(14.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { tourToManage = null }, modifier = Modifier.weight(1f)) {
              Text(text = "বাতিল", color = TextSecondary)
            }
            Button(
              onClick = {
                repository.adminUpdateTournamentStatus(tour.id, currentStatus, roomIdInput.ifBlank { null }, roomPassInput.ifBlank { null })
                tourToManage = null
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
              modifier = Modifier.weight(1f)
            ) {
              Text(text = "সংরক্ষণ", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminOverviewPanel(
  tournaments: List<Tournament>,
  challenges: List<PlayerChallenge>,
  teams: List<Team>,
  transactions: List<WalletTransaction>,
  onNavigate: (AdminSection) -> Unit
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    modifier = Modifier.fillMaxSize()
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        StatCard(title = "মোট টুর্নামেন্ট", count = "${tournaments.size}", color = CyberOrange, modifier = Modifier.weight(1f))
        StatCard(title = "১v১ ম্যাচ", count = "${challenges.size}", color = CyberCyan, modifier = Modifier.weight(1f))
        StatCard(title = "রেজিস্টার্ড স্কোয়াড", count = "${teams.size}", color = CyberGreen, modifier = Modifier.weight(1f))
      }
    }

    item {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(text = "প্ল্যাটফর্ম রাজস্ব ও ২০% ফি হিসাব", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(8.dp))
          val totalChallengePot = challenges.sumOf { it.amount * 2 }
          val estimatedFee = totalChallengePot * 0.20
          Text(text = "মোট চ্যালেঞ্জ পট ভলিউম: ৳${totalChallengePot.toInt()} টাকা", color = TextSecondary, fontSize = 13.sp)
          Text(text = "প্ল্যাটফর্ম অর্জিত কমিশন (২০%): ৳${estimatedFee.toInt()} টাকা", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      }
    }

    item {
      Text(text = "দ্রুত অ্যাকশন (Quick Actions)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = { onNavigate(AdminSection.ADD_TOURNAMENT) },
          colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text(text = "＋ Add Tournament", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Button(
          onClick = { onNavigate(AdminSection.CHALLENGE_MATCH) },
          colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Text(text = "◎ Challenge Settle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    item {
      Text(text = "সর্বশেষ লেনদেন রিকোয়েস্ট", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        transactions.take(4).forEach { tx ->
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = GamingDarkSurface,
            border = BorderStroke(1.dp, GamingCardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = tx.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "${tx.paymentMethod} • ${tx.timestamp} • TrxID: ${tx.trxId ?: "N/A"}", color = TextSecondary, fontSize = 11.sp)
              }
              Text(text = "৳${tx.amount.toInt()}", color = if (tx.isCredit) CyberGreen else CyberOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = GamingDarkSurface,
    border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = count, color = color, fontWeight = FontWeight.Black, fontSize = 20.sp)
      Text(text = title, color = TextSecondary, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
  }
}

@Composable
fun AdminGamesPanel(gameCategories: List<GameCategory>, onAddTournamentForGame: (String) -> Unit) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "সমর্থিত গেম ও ক্যাটাগরি তালিকা", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    items(gameCategories) { game ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = game.iconEmoji, fontSize = 24.sp)
            Column {
              Text(text = game.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(text = "মোডস: ${game.modes.joinToString(", ")}", color = TextSecondary, fontSize = 11.sp)
            }
          }
          Button(
            onClick = { onAddTournamentForGame(game.id) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberOrange, contentColor = Color.Black),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier.height(30.dp)
          ) {
            Text(text = "+ টুর্নামেন্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun AdminAddTournamentPanel(
  onCreate: (gameId: String, title: String, mode: String, map: String, fee: Double, prize: Double, slots: Int, time: String, rules: String) -> Unit
) {
  var gameId by remember { mutableStateOf("freefire") }
  var title by remember { mutableStateOf("") }
  var mode by remember { mutableStateOf("Solo BR") }
  var mapName by remember { mutableStateOf("Bermuda") }
  var entryFee by remember { mutableStateOf("50") }
  var prizePool by remember { mutableStateOf("2000") }
  var maxSlots by remember { mutableStateOf("48") }
  var startTime by remember { mutableStateOf("আজ রাত ৯:০০ টা") }
  var rules by remember { mutableStateOf("১. কোনো হ্যাক বা স্ক্রিপ্ট ব্যবহার করা যাবে না।\n২. রুম কোড সময়মত দেওয়া হবে।") }

  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "নতুন টুর্নামেন্ট যুক্ত করুন (Add Tournament)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))
      OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("টুর্নামেন্টের নাম") }, modifier = Modifier.fillMaxWidth())
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(value = mode, onValueChange = { mode = it }, label = { Text("মোড (যেমন: Solo BR)") }, modifier = Modifier.fillMaxWidth())
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(value = mapName, onValueChange = { mapName = it }, label = { Text("ম্যাপের নাম") }, modifier = Modifier.fillMaxWidth())
      Spacer(modifier = Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = entryFee, onValueChange = { entryFee = it }, label = { Text("এন্ট্রি ফি (৳)") }, modifier = Modifier.weight(1f))
        OutlinedTextField(value = prizePool, onValueChange = { prizePool = it }, label = { Text("প্রাইজপুল (৳)") }, modifier = Modifier.weight(1f))
      }
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("শুরুর সময়সূচি") }, modifier = Modifier.fillMaxWidth())
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(value = rules, onValueChange = { rules = it }, label = { Text("নিয়মাবলী") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
      Spacer(modifier = Modifier.height(14.dp))
      Button(
        onClick = {
          val fee = entryFee.toDoubleOrNull() ?: 50.0
          val prize = prizePool.toDoubleOrNull() ?: 2000.0
          val slots = maxSlots.toIntOrNull() ?: 48
          onCreate(gameId, title.ifBlank { "টুর্নামেন্ট ম্যাচ" }, mode, mapName, fee, prize, slots, startTime, rules)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyberRed, contentColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().height(44.dp)
      ) {
        Text(text = "পাবলিশ করুন (Publish Tournament)", fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun AdminTournamentManagerPanel(
  tournaments: List<Tournament>,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  onManageClick: (Tournament) -> Unit,
  onAddClick: () -> Unit
) {
  val filtered = remember(tournaments, searchQuery) {
    if (searchQuery.isBlank()) tournaments
    else tournaments.filter { it.title.contains(searchQuery, ignoreCase = true) || it.gameTitle.contains(searchQuery, ignoreCase = true) }
  }

  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("টুর্নামেন্ট খুঁজুন (Tournament Finder)...") },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
        modifier = Modifier.fillMaxWidth()
      )
    }

    items(filtered) { tour ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = tour.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            TournamentStatusBadge(status = tour.status)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "${tour.gameTitle} • ${tour.mode} • স্লট: ${tour.joinedSlots}/${tour.maxSlots} • ফি: ৳${tour.entryFee.toInt()}", color = TextSecondary, fontSize = 12.sp)
          if (tour.roomId != null) {
            Text(text = "রুম: ${tour.roomId} (পাস: ${tour.roomPassword})", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = { onManageClick(tour) },
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth().height(34.dp)
          ) {
            Text(text = "ম্যানেজ করুন / রুম কোড দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun AdminResultsPanel(tournaments: List<Tournament>, challenges: List<PlayerChallenge>) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "✓ সমাপ্ত ম্যাচ ও ফলাফল অনুসন্ধান (Result Finder)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    item {
      Text(text = "১v১ ফিনিশড চ্যালেঞ্জেস (${challenges.size}):", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
    items(challenges) { ch ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = "${ch.challengerName} VS ${ch.opponentName ?: "Opponent"}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text(text = "${ch.game} • প্রাইজ পট: ৳${(ch.amount * 2).toInt()} • স্ট্যাটাস: ${ch.status.labelBn}", color = TextSecondary, fontSize = 11.sp)
          Text(text = "বিজয়ী ID: ${ch.winnerId ?: "অপেক্ষমাণ"}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun AdminTeamsPanel(teams: List<Team>) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "◈ রেজিস্টার্ড টিম ও স্কোয়াড (Teams / Squads)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    items(teams) { team ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${team.name} [${team.tag}]", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = "ফান্ড: ৳${team.balance.toInt()}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "লিডার: ${team.adminName} • মেম্বার: ${team.members.size} জন • রেটিং: ${team.rating}", color = TextSecondary, fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun AdminTeamWalletReviewPanel(teams: List<Team>, isWithdrawWindow: Boolean) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "টিম ডিপোজিট ও উইথড্র অডিট (Team Wallet Manager)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(4.dp))
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isWithdrawWindow) CyberGreen.copy(alpha = 0.2f) else CyberRed.copy(alpha = 0.2f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = if (isWithdrawWindow) "✓ উইথড্র সময় উইন্ডো চালু (সন্ধ্যা ৭:০০ - রাত ১১:০০)" else "⚠ উইথড্র সময় শেষ (শুধুমাত্র সন্ধ্যা ৭:০০ - রাত ১১:০০ সময়ে অনুমোদনযোগ্য)",
          color = if (isWithdrawWindow) CyberGreen else CyberRed,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(8.dp)
        )
      }
    }
    items(teams) { team ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = team.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "টিম আইডি: ${team.publicId}", color = TextSecondary, fontSize = 11.sp)
          }
          Text(text = "ব্যালেন্স: ৳${team.balance.toInt()}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
      }
    }
  }
}

@Composable
fun AdminTeamChallengePanel(teamChallenges: List<TeamChallenge>, onSettleTeam: () -> Unit) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "⚔ লাইভ টিম চ্যালেঞ্জ (Team Challenge Manager)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    items(teamChallenges) { tc ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = "${tc.challengerTeamName} VS ${tc.opponentTeamName}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(text = "${tc.game} • স্টেক: ৳${tc.amount.toInt()} • স্ট্যাটাস: ${tc.status.labelBn}", color = TextSecondary, fontSize = 11.sp)
        }
      }
    }
  }
}

@Composable
fun AdminChallengeMatchPanel(
  challenges: List<PlayerChallenge>,
  onInspectProof: (PlayerChallenge) -> Unit,
  onSettle: (challengeId: String, winnerId: String) -> Unit
) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "◎ ১v১ চ্যালেঞ্জ ম্যাচ নিষ্পত্তি ও প্রুফ যাচাই (Challenge match & Proof)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Text(text = "ফর্মুলা: মোট পট = ২ x স্টেক | ২০% প্ল্যাটফর্ম ফি কর্তন | ৮০% বিজয়ী পেআউট", color = CyberCyan, fontSize = 12.sp)
    }

    items(challenges.filter { it.status != ChallengeStatus.FINISHED && it.status != ChallengeStatus.CANCELLED }) { ch ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "${ch.challengerName} VS ${ch.opponentName ?: "খুঁজছে..."}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = ch.status.labelBn, color = CyberOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(text = "${ch.game} • স্টেক: ৳${ch.amount.toInt()} • পট: ৳${(ch.amount * 2).toInt()} (৮০% পেআউট = ৳${(ch.amount * 2 * 0.8).toInt()})", color = TextSecondary, fontSize = 11.sp)

          if (ch.opponentId != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { onSettle(ch.id, ch.challengerId) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(34.dp)
              ) {
                Text(text = "${ch.challengerName} জয়ী", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = { onSettle(ch.id, ch.opponentId) },
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.weight(1f).height(34.dp)
              ) {
                Text(text = "${ch.opponentName} জয়ী", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminFinancialsPanel(transactions: List<WalletTransaction>, onApprove: () -> Unit) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "↓ Deposits & ↑ Withdrawals (পেমেন্ট ম্যানেজমেন্ট)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(4.dp))
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = GamingDarkSurfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp)) {
          Text(text = "৳ পেমেন্ট মেথড নম্বরসমূহ:", color = CyberGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          Text(text = "• বিকাশ পার্সোনাল: 01798123456\n• নগদ পার্সোনাল: 01798123456\n• রকেট পার্সোনাল: 01798123456", color = TextSecondary, fontSize = 11.sp)
        }
      }
    }

    items(transactions) { tx ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = tx.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "${tx.paymentMethod} • TrxID: ${tx.trxId ?: "N/A"}", color = TextSecondary, fontSize = 11.sp)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(text = "৳${tx.amount.toInt()}", color = if (tx.isCredit) CyberGreen else CyberOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = tx.status, color = CyberCyan, fontSize = 11.sp)
          }
        }
      }
    }
  }
}

@Composable
fun AdminPlayersPanel(players: List<LeaderboardItem>, currentUser: UserProfile) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "◉ প্লেয়ার ডাটাবেজ (Players Management)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    items(players) { p ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = p.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = "ম্যাচ: ${p.matches} • জয়: ${p.wins} • আয়: ৳${p.earnings.toInt()}", color = TextSecondary, fontSize = 11.sp)
          }
          Surface(shape = RoundedCornerShape(4.dp), color = CyberGreen.copy(alpha = 0.2f)) {
            Text(text = "অ্যাক্টিভ", color = CyberGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }
      }
    }
  }
}

@Composable
fun AdminBannersPanel(banners: List<BannerSlide>) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item {
      Text(text = "▰ Banners & Promotional Ads", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
    items(banners) { b ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(text = b.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text(text = b.subtitle, color = TextSecondary, fontSize = 11.sp)
          Text(text = "টার্গেট ডেস্টিনেশন: ${b.targetDestination}", color = CyberOrange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
fun AdminSettingsPanel(onLogout: () -> Unit) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item {
      Text(text = "⚙ সিস্টেম সেটিংস (Admin Settings)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(text = "এডমিন প্রোফাইল: Mohammad Noyon (Admin)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(text = "ইমেইল: mohammadnoyon965@gmail.com", color = TextSecondary, fontSize = 12.sp)
          Text(text = "প্ল্যাটফর্ম: KheloBD Android Native (Esports Tournament Platform)", color = TextSecondary, fontSize = 12.sp)
          Text(text = "লোগো স্ট্যাটাস: KheloBD Official Shield Logo (Active)", color = CyberCyan, fontSize = 12.sp)

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("admin_logout_btn")
          ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "লগআউট করুন (Noyon Logout)", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
fun AdminReportsPanel(
  tournaments: List<Tournament>,
  challenges: List<PlayerChallenge>,
  transactions: List<WalletTransaction>
) {
  LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item {
      Text(text = "▥ আর্থিক রিপোর্ট ও অডিট (Reports & Ledger)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = GamingDarkSurface,
        border = BorderStroke(1.dp, GamingCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(text = "মোট টুর্নামেন্ট এন্ট্রি ফি সংগৃহীত: ৳${tournaments.sumOf { it.entryFee * it.joinedSlots }.toInt()}", color = TextPrimary, fontSize = 13.sp)
          Text(text = "মোট চ্যালেঞ্জ পট ভলিউম: ৳${challenges.sumOf { it.amount * 2 }.toInt()}", color = TextPrimary, fontSize = 13.sp)
          Text(text = "প্ল্যাটফর্ম ফি আয় (২০%): ৳${(challenges.sumOf { it.amount * 2 } * 0.20).toInt()}", color = CyberGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text(text = "মোট সক্রিয় ট্রানজেকশন রেকর্ড: ${transactions.size} টি", color = TextSecondary, fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun AdminGenericOperationsPanel(section: AdminSection) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = section.icon, fontSize = 42.sp, color = CyberRed)
      Spacer(modifier = Modifier.height(12.dp))
      Text(text = section.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
      Spacer(modifier = Modifier.height(6.dp))
      Text(text = "এই সেকশনের সকল কনফিগারেশন ও ডাটা অটোমেটিক সিঙ্ক হচ্ছে।", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
  }
}
