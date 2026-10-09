package com.example.data.repository

import com.example.data.model.*
import com.example.data.remote.ApiStateClient
import com.example.data.remote.RemoteAppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class KheloBDRepository private constructor() {

  private val remoteClient = ApiStateClient()
  private val remoteScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  @Volatile private var remoteReady = false
  @Volatile private var lastSyncedState: RemoteAppState? = null

  companion object {
    @Volatile
    private var instance: KheloBDRepository? = null

    fun getInstance(): KheloBDRepository {
      return instance ?: synchronized(this) {
        instance ?: KheloBDRepository().also { instance = it }
      }
    }
  }

  // Production starts as a guest; account data must come from the backend.
  private val _currentUser = MutableStateFlow(
    UserProfile(
      id = "guest",
      username = "Guest",
      fullName = "নতুন খেলোয়াড়",
      phone = "",
      email = "",
      walletBalance = 0.0,
      isOnline = false,
      matchesPlayed = 0,
      wins = 0,
      totalEarnings = 0.0,
      freeFireUid = "",
      pubgUid = "",
      isAdmin = false,
      userCode = "GUEST"
    )
  )
  val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

  private val _tournaments = MutableStateFlow<List<Tournament>>(emptyList())
  val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()

  private val _challenges = MutableStateFlow<List<PlayerChallenge>>(emptyList())
  val challenges: StateFlow<List<PlayerChallenge>> = _challenges.asStateFlow()

  private val _teams = MutableStateFlow<List<Team>>(emptyList())
  val teams: StateFlow<List<Team>> = _teams.asStateFlow()

  private val _teamChallenges = MutableStateFlow<List<TeamChallenge>>(emptyList())
  val teamChallenges: StateFlow<List<TeamChallenge>> = _teamChallenges.asStateFlow()

  private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
  val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

  private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  private val _banners = MutableStateFlow<List<BannerSlide>>(emptyList())
  val banners: StateFlow<List<BannerSlide>> = _banners.asStateFlow()

  private val _gameCategories = MutableStateFlow<List<GameCategory>>(emptyList())
  val gameCategories: StateFlow<List<GameCategory>> = _gameCategories.asStateFlow()

  private val _topPlayers = MutableStateFlow<List<LeaderboardItem>>(emptyList())
  val topPlayers: StateFlow<List<LeaderboardItem>> = _topPlayers.asStateFlow()

  private val _paymentSettings = MutableStateFlow(
    PaymentMethodSettings(
      bkashNumber = "",
      nagadNumber = "",
      bkashInstruction = "এডমিন কর্তৃক বিকাশ পেমেন্ট নম্বর সেট করা হয়নি।",
      nagadInstruction = "এডমিন কর্তৃক নগদ পেমেন্ট নম্বর সেট করা হয়নি।"
    )
  )
  val paymentSettings: StateFlow<PaymentMethodSettings> = _paymentSettings.asStateFlow()

  private val _depositRequests = MutableStateFlow<List<DepositRequest>>(emptyList())
  val depositRequests: StateFlow<List<DepositRequest>> = _depositRequests.asStateFlow()

  init {
    loadProductionDefaults()
    startRemoteSync()
  }

  private fun loadProductionDefaults() {
    _gameCategories.value = listOf(
      GameCategory(
        id = "freefire",
        name = "Free Fire",
        activeTournamentsCount = 0,
        modes = listOf("Solo BR", "Duo BR", "Squad BR", "Clash Squad", "Lone Wolf"),
        iconEmoji = "🔥"
      ),
      GameCategory(
        id = "pubg",
        name = "PUBG Mobile",
        activeTournamentsCount = 0,
        modes = listOf("BR Solo", "BR Duo", "BR Squad"),
        iconEmoji = "🎯"
      ),
      GameCategory(
        id = "dls",
        name = "DLS",
        activeTournamentsCount = 0,
        modes = listOf("Solo", "Cup"),
        iconEmoji = "⚽"
      ),
      GameCategory(
        id = "efootball",
        name = "eFootball",
        activeTournamentsCount = 0,
        modes = listOf("1v1", "League"),
        iconEmoji = "🏆"
      )
    )
    _tournaments.value = emptyList()
    _challenges.value = emptyList()
    _teams.value = emptyList()
    _teamChallenges.value = emptyList()
    _transactions.value = emptyList()
    _notifications.value = emptyList()
    _banners.value = emptyList()
    _topPlayers.value = emptyList()
    _depositRequests.value = emptyList()
  }

  private fun isLegacyDemoState(state: RemoteAppState): Boolean {
    return state.currentUser.id == "user_001" &&
      state.currentUser.username == "NoyonGamer_BD" &&
      state.tournaments.any { it.id == "tour_101" && it.title == "Free Fire Live Match" }
  }

  private fun snapshotState(): RemoteAppState = RemoteAppState(
    currentUser = _currentUser.value,
    tournaments = _tournaments.value,
    challenges = _challenges.value,
    teams = _teams.value,
    teamChallenges = _teamChallenges.value,
    transactions = _transactions.value,
    notifications = _notifications.value,
    banners = _banners.value,
    gameCategories = _gameCategories.value,
    topPlayers = _topPlayers.value,
    paymentSettings = _paymentSettings.value,
    depositRequests = _depositRequests.value
  )

  private fun applyRemoteState(state: RemoteAppState) {
    _currentUser.value = state.currentUser
    _tournaments.value = state.tournaments
    _challenges.value = state.challenges
    _teams.value = state.teams
    _teamChallenges.value = state.teamChallenges
    _transactions.value = state.transactions
    _notifications.value = state.notifications
    _banners.value = state.banners
    _gameCategories.value = state.gameCategories
    _topPlayers.value = state.topPlayers
    _paymentSettings.value = state.paymentSettings
    _depositRequests.value = state.depositRequests
  }

  private fun startRemoteSync() {
    remoteScope.launch {
      // Never write local data until a successful server read proves the API is reachable.
      // This prevents an offline launch from overwriting real database data with defaults.
      while (isActive && !remoteReady) {
        val result = remoteClient.loadResult()
        if (!result.successful) {
          delay(15000)
          continue
        }

        val serverState = result.state
        if (serverState != null && !isLegacyDemoState(serverState)) {
          applyRemoteState(serverState)
          lastSyncedState = snapshotState()
          remoteReady = true
        } else {
          val cleanState = snapshotState()
          if (serverState != null && !remoteClient.save(cleanState)) {
            delay(10000)
            continue
          }
          // state=null means a reachable, empty database; wait for real admin/user changes
          // before creating the first row in app_state.
          lastSyncedState = cleanState
          remoteReady = true
        }
      }

      // Pull server changes when the local model is unchanged; push only locally changed data.
      // Unlike the old unconditional POST loop, an idle app cannot overwrite another app's changes.
      while (isActive && remoteReady) {
        delay(5000)
        val result = remoteClient.loadResult()
        if (!result.successful) continue

        val serverState = result.state
        val localState = snapshotState()
        val baseline = lastSyncedState ?: localState

        if (serverState != null && isLegacyDemoState(serverState)) {
          // A still-installed old demo build may rewrite its sample state. Clear it again.
          if (localState != baseline) {
            if (remoteClient.save(localState)) lastSyncedState = localState
          } else {
            if (remoteClient.save(localState)) lastSyncedState = localState
          }
          continue
        }

        if (serverState != baseline) {
          if (localState == baseline) {
            if (serverState != null) applyRemoteState(serverState)
            lastSyncedState = snapshotState()
          } else {
            if (remoteClient.save(localState)) lastSyncedState = localState
          }
        } else if (localState != baseline) {
          if (remoteClient.save(localState)) lastSyncedState = localState
        }
      }
    }
  }

  // --- Profile Operations ---
  fun updateProfile(fullName: String, phone: String, freeFireUid: String, pubgUid: String) {
    _currentUser.value = _currentUser.value.copy(
      fullName = fullName,
      phone = phone,
      freeFireUid = freeFireUid,
      pubgUid = pubgUid
    )
  }

  fun setCurrentUser(user: UserProfile) {
    _currentUser.value = user
  }

  fun toggleAdminRole() {
    _currentUser.value = _currentUser.value.copy(
      isAdmin = !_currentUser.value.isAdmin
    )
  }

  // --- Tournament Operations ---
  fun joinTournament(tournamentId: String, requestedSlotNumber: Int? = null): Result<String> {
    val user = _currentUser.value
    val tournament = _tournaments.value.find { it.id == tournamentId }
      ?: return Result.failure(Exception("টুর্নামেন্ট পাওয়া যায়নি!"))

    if (tournament.status == TournamentStatus.FINISHED) {
      return Result.failure(Exception("এই টুর্নামেন্টটি সমাপ্ত হয়ে গেছে!"))
    }

    if (tournament.joinedSlots >= tournament.maxSlots) {
      return Result.failure(Exception("টুর্নামেন্টের সব স্লট পূর্ণ হয়ে গেছে!"))
    }

    if (tournament.joinedPlayerIds.contains(user.id)) {
      return Result.failure(Exception("আপনি ইতোমধ্যে এই টুর্নামেন্টে জয়েন করেছেন!"))
    }

    if (user.walletBalance < tournament.entryFee) {
      return Result.failure(Exception("অপর্যাপ্ত ব্যালেন্স! আপনার ওয়ালেটে কমপক্ষে ৳${tournament.entryFee.toInt()} টাকা থাকতে হবে। দয়া করে আগে ডিপোজিট করুন।"))
    }

    // Determine target slot
    val targetSlotNum = requestedSlotNumber
      ?: tournament.slots.firstOrNull { !it.isFilled }?.slotNumber
      ?: (tournament.joinedSlots + 1)

    // Deduct entry fee
    val newBalance = user.walletBalance - tournament.entryFee
    _currentUser.value = user.copy(
      walletBalance = newBalance,
      matchesPlayed = user.matchesPlayed + 1
    )

    // Update Tournament slots & participants
    val updatedSlots = if (tournament.slots.isNotEmpty()) {
      tournament.slots.map { slot ->
        if (slot.slotNumber == targetSlotNum) {
          slot.copy(
            isFilled = true,
            playerId = user.id,
            playerName = user.username,
            playerUid = if (user.freeFireUid.isNotBlank()) user.freeFireUid else "548291048"
          )
        } else slot
      }
    } else {
      (1..tournament.maxSlots).map { num ->
        if (num == targetSlotNum) {
          TournamentSlot(num, true, user.id, user.username, user.freeFireUid.ifBlank { "548291048" })
        } else TournamentSlot(num, num <= tournament.joinedSlots)
      }
    }

    _tournaments.value = _tournaments.value.map {
      if (it.id == tournamentId) {
        it.copy(
          joinedSlots = it.joinedSlots + 1,
          joinedPlayerIds = it.joinedPlayerIds + user.id,
          slots = updatedSlots
        )
      } else it
    }

    // Add transaction
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val currentTime = sdf.format(Date())
    _transactions.value = listOf(
      WalletTransaction(
        id = "tx_${System.currentTimeMillis()}",
        title = "টুর্নামেন্ট ফি (${tournament.title})",
        amount = tournament.entryFee,
        type = "TOURNAMENT_FEE",
        paymentMethod = "Wallet",
        timestamp = "আজ $currentTime",
        isCredit = false,
        status = "সফল"
      )
    ) + _transactions.value

    return Result.success("সফলভাবে টুর্নামেন্টে জয়েন সম্পন্ন হয়েছে! রুম শুরুর পূর্বে নোটিফিকেশন চেক করুন।")
  }

  fun adminCreateTournament(
    gameId: String,
    title: String,
    mode: String,
    mapName: String,
    entryFee: Double,
    prizePool: Double,
    maxSlots: Int,
    startTime: String,
    rules: String
  ) {
    val gameTitle = if (gameId == "freefire") "Free Fire" else if (gameId == "pubg") "PUBG Mobile" else if (gameId == "dls") "DLS" else "eFootball"
    val newTour = Tournament(
      id = "tour_${System.currentTimeMillis()}",
      gameId = gameId,
      gameTitle = gameTitle,
      title = title,
      mode = mode,
      mapName = mapName,
      entryFee = entryFee,
      prizePool = prizePool,
      maxSlots = maxSlots,
      joinedSlots = 0,
      startTime = startTime,
      status = TournamentStatus.JOIN_OPEN,
      rules = rules
    )
    _tournaments.value = listOf(newTour) + _tournaments.value
  }

  fun adminUpdateTournamentStatus(tournamentId: String, newStatus: TournamentStatus, roomId: String? = null, roomPass: String? = null) {
    _tournaments.value = _tournaments.value.map {
      if (it.id == tournamentId) {
        it.copy(
          status = newStatus,
          roomId = roomId ?: it.roomId,
          roomPassword = roomPass ?: it.roomPassword
        )
      } else it
    }
  }

  // --- Player 1v1 Challenge Operations ---
  fun createChallenge(
    game: String,
    mode: String,
    mapName: String,
    rule: ChallengeRule,
    amount: Double,
    note: String,
    targetOpponentId: String? = null,
    targetOpponentName: String? = null
  ): Result<String> {
    val user = _currentUser.value

    // RULE: এক সাথে একজনের একটির বেশি একটিভ চ্যালেঞ্জ চলতে পারবে না (শেষ বা ক্যান্সেল না হওয়া পর্যন্ত ২য় চ্যালেঞ্জ দেওয়া যাবে না)
    val hasActiveChallenge = _challenges.value.any { ch ->
      (ch.challengerId == user.id || ch.opponentId == user.id) &&
        (ch.status == ChallengeStatus.OPEN || ch.status == ChallengeStatus.ACCEPTED ||
         ch.status == ChallengeStatus.ROOM_SUBMITTED || ch.status == ChallengeStatus.PROOF_SUBMITTED)
    }
    if (hasActiveChallenge) {
      return Result.failure(Exception("আপনার একটি চ্যালেঞ্জ ইতিমধ্যে চলমান আছে! সেই চ্যালেঞ্জ শেষ বা বাতিল না হওয়া পর্যন্ত নতুন চ্যালেঞ্জ দেওয়া যাবে না।"))
    }

    if (amount < 50.0 || amount > 200.0) {
      return Result.failure(Exception("চ্যালেঞ্জের পরিমাণ ৫০ থেকে ২০০ টাকার মধ্যে হতে হবে!"))
    }

    if (user.walletBalance < amount) {
      return Result.failure(Exception("অপর্যাপ্ত ব্যালেন্স! চ্যালেঞ্জ পোস্ট করতে ৳${amount.toInt()} টাকা প্রয়োজন। দয়া করে ওয়ালেটে টাকা যোগ করুন।"))
    }

    // Hold amount from challenger's wallet
    _currentUser.value = user.copy(walletBalance = user.walletBalance - amount)

    val now = System.currentTimeMillis()
    val newChallenge = PlayerChallenge(
      id = "ch_${System.currentTimeMillis()}",
      challengerId = user.id,
      challengerName = user.username,
      challengerIsOnline = true,
      game = game,
      mode = mode,
      mapName = mapName,
      rule = rule,
      amount = amount,
      challengerNote = note,
      createdAtMillis = now,
      expiresAtMillis = now + (10 * 60 * 1000), // 10 minutes expiry
      status = ChallengeStatus.OPEN,
      opponentId = targetOpponentId,
      opponentName = targetOpponentName
    )

    _challenges.value = listOf(newChallenge) + _challenges.value

    _transactions.value = listOf(
      WalletTransaction(
        id = "tx_${System.currentTimeMillis()}",
        title = "১v১ চ্যালেঞ্জ হোল্ড ($game)",
        amount = amount,
        type = "CHALLENGE_HOLD",
        paymentMethod = "Wallet",
        timestamp = "এখন",
        isCredit = false,
        status = "হোল্ডকৃত"
      )
    ) + _transactions.value

    // If targeted to a specific player, notify them
    if (targetOpponentId != null) {
      _notifications.value = listOf(
        NotificationItem(
          id = "notif_${System.currentTimeMillis()}",
          title = "নতুন ১v১ চ্যালেঞ্জ পেয়েছেন!",
          message = "${user.username} আপনাকে ৳${amount.toInt()} টাকার $game চ্যালেঞ্জ পাঠিয়েছে!",
          timestamp = "এইমাত্র",
          isRead = false,
          targetType = "CHALLENGE",
          targetId = newChallenge.id
        )
      ) + _notifications.value
    }

    return Result.success("চ্যালেঞ্জ সফলভাবে পাঠানো হয়েছে! ১০ মিনিটের মধ্যে একসেপ্ট করা যাবে।")
  }

  fun acceptChallenge(challengeId: String): Result<String> {
    val challenge = _challenges.value.find { it.id == challengeId }
      ?: return Result.failure(Exception("চ্যালেঞ্জটি পাওয়া যায়নি!"))

    val user = _currentUser.value
    if (challenge.challengerId == user.id) {
      return Result.failure(Exception("আপনি নিজের চ্যালেঞ্জ নিজেই একসেপ্ট করতে পারবেন না!"))
    }

    if (challenge.status != ChallengeStatus.OPEN) {
      return Result.failure(Exception("এই চ্যালেঞ্জটি উন্মুক্ত নেই বা ইতিমধ্যে একসেপ্ট করা হয়েছে!"))
    }

    if (System.currentTimeMillis() > challenge.expiresAtMillis) {
      expireChallenge(challengeId)
      return Result.failure(Exception("দুঃখিত, এই চ্যালেঞ্জের ১০ মিনিটের সময়সীমা শেষ হয়ে গেছে!"))
    }

    if (user.walletBalance < challenge.amount) {
      return Result.failure(Exception("অপর্যাপ্ত ব্যালেন্স! এই চ্যালেঞ্জ একসেপ্ট করতে আপনার ওয়ালেটে ৳${challenge.amount.toInt()} টাকা থাকতে হবে।"))
    }

    // Deduct stake from opponent
    _currentUser.value = user.copy(walletBalance = user.walletBalance - challenge.amount)

    _challenges.value = _challenges.value.map {
      if (it.id == challengeId) {
        it.copy(
          status = ChallengeStatus.ACCEPTED,
          opponentId = user.id,
          opponentName = user.username,
          opponentIsOnline = true
        )
      } else it
    }

    _transactions.value = listOf(
      WalletTransaction(
        id = "tx_${System.currentTimeMillis()}",
        title = "চ্যালেঞ্জ জয়েন ফি (${challenge.game})",
        amount = challenge.amount,
        type = "CHALLENGE_HOLD",
        paymentMethod = "Wallet",
        timestamp = "এখন",
        isCredit = false,
        status = "হোল্ডকৃত"
      )
    ) + _transactions.value

    // Notify challenger
    _notifications.value = listOf(
      NotificationItem(
        id = "notif_${System.currentTimeMillis()}",
        title = "চ্যালেঞ্জ একসেপ্ট হয়েছে!",
        message = "${user.username} আপনার ${challenge.game} চ্যালেঞ্জ গ্রহণ করেছে। রুম প্রস্তুত করুন।",
        timestamp = "এইমাত্র",
        isRead = false,
        targetType = "CHALLENGE",
        targetId = challengeId
      )
    ) + _notifications.value

    return Result.success("চ্যালেঞ্জ একসেপ্ট হয়েছে! রুম আইডি ও পাসওয়ার্ডের জন্য চ্যালেঞ্জ ডিটেইলস পেজে যান।")
  }

  fun cancelChallenge(challengeId: String): Result<String> {
    val challenge = _challenges.value.find { it.id == challengeId }
      ?: return Result.failure(Exception("চ্যালেঞ্জ পাওয়া যায়নি!"))

    val user = _currentUser.value
    if (challenge.challengerId != user.id && !user.isAdmin) {
      return Result.failure(Exception("আপনি এই চ্যালেঞ্জ বাতিল করার অধিকারী নন!"))
    }

    if (challenge.status != ChallengeStatus.OPEN) {
      return Result.failure(Exception("ইতিমধ্যে একসেপ্ট হওয়া চ্যালেঞ্জ সরাসরি বাতিল করা যাবে না। পেনাল্টি প্রযোজ্য হতে পারে।"))
    }

    // Refund challenger stake
    _currentUser.value = user.copy(walletBalance = user.walletBalance + challenge.amount)

    _challenges.value = _challenges.value.map {
      if (it.id == challengeId) it.copy(status = ChallengeStatus.CANCELLED) else it
    }

    _transactions.value = listOf(
      WalletTransaction(
        id = "tx_${System.currentTimeMillis()}",
        title = "চ্যালেঞ্জ বাতিল রিফান্ড",
        amount = challenge.amount,
        type = "CHALLENGE_REFUND",
        paymentMethod = "Wallet",
        timestamp = "এখন",
        isCredit = true,
        status = "রিফান্ড"
      )
    ) + _transactions.value

    return Result.success("চ্যালেঞ্জ বাতিল করা হয়েছে এবং ৳${challenge.amount.toInt()} টাকা ফেরত দেওয়া হয়েছে।")
  }

  private fun expireChallenge(challengeId: String) {
    val challenge = _challenges.value.find { it.id == challengeId } ?: return
    if (challenge.status == ChallengeStatus.OPEN) {
      if (challenge.challengerId == _currentUser.value.id) {
        _currentUser.value = _currentUser.value.copy(
          walletBalance = _currentUser.value.walletBalance + challenge.amount
        )
      }
      _challenges.value = _challenges.value.map {
        if (it.id == challengeId) it.copy(status = ChallengeStatus.EXPIRED) else it
      }
    }
  }

  fun submitRoomCredentials(challengeId: String, roomId: String, roomPass: String): Result<String> {
    if (roomId.isBlank() || roomPass.isBlank()) {
      return Result.failure(Exception("রুম আইডি ও পাসওয়ার্ড উভয়ই প্রদান করতে হবে!"))
    }

    _challenges.value = _challenges.value.map {
      if (it.id == challengeId) {
        it.copy(
          roomId = roomId,
          roomPassword = roomPass,
          status = ChallengeStatus.ROOM_SUBMITTED
        )
      } else it
    }

    return Result.success("রুম তথ্য সফলভাবে সাবমিট হয়েছে! প্রতিপক্ষ এখন রুম আইডি ও পাসওয়ার্ড দেখতে পাবে।")
  }

  fun submitChallengeProof(challengeId: String, isChallenger: Boolean): Result<String> {
    _challenges.value = _challenges.value.map {
      if (it.id == challengeId) {
        if (isChallenger) {
          it.copy(challengerProofUrl = "proof_screenshot_challenger.jpg", status = ChallengeStatus.PROOF_SUBMITTED)
        } else {
          it.copy(opponentProofUrl = "proof_screenshot_opponent.jpg", status = ChallengeStatus.PROOF_SUBMITTED)
        }
      } else it
    }
    return Result.success("স্ক্রিনশট প্রুফ সফলভাবে আপলোড হয়েছে! এডমিন শীঘ্রই ফলাফল যাচাই করবেন।")
  }

  fun settleChallenge(challengeId: String, winnerId: String): Result<String> {
    val challenge = _challenges.value.find { it.id == challengeId }
      ?: return Result.failure(Exception("চ্যালেঞ্জ পাওয়া যায়নি!"))

    val totalPot = challenge.amount * 2.0
    val platformFee = totalPot * 0.20 // 20% platform fee
    val winnerPayout = totalPot - platformFee // 80% to winner

    _challenges.value = _challenges.value.map {
      if (it.id == challengeId) {
        it.copy(
          status = ChallengeStatus.FINISHED,
          winnerId = winnerId
        )
      } else it
    }

    // If current user is winner, credit payout
    if (_currentUser.value.id == winnerId) {
      _currentUser.value = _currentUser.value.copy(
        walletBalance = _currentUser.value.walletBalance + winnerPayout,
        wins = _currentUser.value.wins + 1,
        totalEarnings = _currentUser.value.totalEarnings + winnerPayout
      )
      _transactions.value = listOf(
        WalletTransaction(
          id = "tx_${System.currentTimeMillis()}",
          title = "১v১ চ্যালেঞ্জ জয়ী প্রাইজ (৮০%)",
          amount = winnerPayout,
          type = "CHALLENGE_WIN",
          paymentMethod = "Wallet",
          timestamp = "এখন",
          isCredit = true,
          status = "জমা সম্পন্ন"
        )
      ) + _transactions.value
    }

    return Result.success("চ্যালেঞ্জ সেটেলমেন্ট সম্পন্ন হয়েছে! মোট পট: ৳${totalPot.toInt()} | প্ল্যাটফর্ম ফি (২০%): ৳${platformFee.toInt()} | বিজয়ী পেআউট (৮০%): ৳${winnerPayout.toInt()}")
  }

  // --- Team Operations & Team Wallet ---
  fun isWithinWithdrawWindow(): Boolean {
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    // 7:00 PM (19) to 11:00 PM (23)
    return hour in 19..22
  }

  fun teamDeposit(teamId: String, amount: Double): Result<String> {
    if (amount <= 0) return Result.failure(Exception("সঠিক ডিপোজিট পরিমাণ প্রদান করুন!"))
    val user = _currentUser.value
    if (user.walletBalance < amount) {
      return Result.failure(Exception("আপনার ব্যক্তিগত ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই!"))
    }

    _currentUser.value = user.copy(walletBalance = user.walletBalance - amount)
    _teams.value = _teams.value.map {
      if (it.id == teamId) it.copy(balance = it.balance + amount) else it
    }

    return Result.success("টিম ওয়ালেটে ৳${amount.toInt()} টাকা সফলভাবে ডিপোজিট হয়েছে।")
  }

  fun teamWithdraw(teamId: String, amount: Double): Result<String> {
    // Check 7PM - 11PM withdraw rule
    if (!isWithinWithdrawWindow()) {
      return Result.failure(Exception("উইথড্র করার নির্ধারিত সময় সন্ধ্যা ৭:০০ টা থেকে রাত ১১:০০ টা পর্যন্ত। এই সময়ের বাইরে উইথড্র বন্ধ থাকবে।"))
    }

    val team = _teams.value.find { it.id == teamId }
      ?: return Result.failure(Exception("টিম পাওয়া যায়নি!"))

    val user = _currentUser.value
    if (team.adminId != user.id && !user.isAdmin) {
      return Result.failure(Exception("শুধুমাত্র টিম এডমিন উইথড্র রিকোয়েস্ট করতে পারবেন!"))
    }

    if (team.balance < amount) {
      return Result.failure(Exception("টিম ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই!"))
    }

    _teams.value = _teams.value.map {
      if (it.id == teamId) it.copy(balance = it.balance - amount) else it
    }
    _currentUser.value = user.copy(walletBalance = user.walletBalance + amount)

    return Result.success("টিম ওয়ালেট থেকে ৳${amount.toInt()} টাকা সফলভাবে উত্তোলন সম্পন্ন হয়েছে।")
  }

  fun sendTeamChallenge(
    challengerTeamId: String,
    opponentTeamId: String,
    game: String,
    amount: Double
  ): Result<String> {
    val challengerTeam = _teams.value.find { it.id == challengerTeamId }
      ?: return Result.failure(Exception("আপনার টিম পাওয়া যায়নি!"))

    if (challengerTeam.members.filter { it.isOnline }.size < 2) {
      return Result.failure(Exception("টিম চ্যালেঞ্জ পাঠাতে টিমে কমপক্ষে ২ জন অ্যাক্টিভ ও অনলাইন মেম্বার থাকতে হবে!"))
    }

    if (amount < 50.0 || amount > 200.0) {
      return Result.failure(Exception("টিম চ্যালেঞ্জ অ্যামাউন্ট ৫০ থেকে ২০০ টাকার মধ্যে নির্বাচন করুন!"))
    }

    if (challengerTeam.balance < amount) {
      return Result.failure(Exception("টিম ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই! প্রয়োজনীয় স্টেক: ৳${amount.toInt()}"))
    }

    val opponentTeam = _teams.value.find { it.id == opponentTeamId }
      ?: return Result.failure(Exception("প্রতিপক্ষ টিম নির্বাচন সঠিক নয়!"))

    // Hold from challenger team
    _teams.value = _teams.value.map {
      if (it.id == challengerTeamId) it.copy(balance = it.balance - amount) else it
    }

    val challenge = TeamChallenge(
      id = "tch_${System.currentTimeMillis()}",
      challengerTeamId = challengerTeamId,
      challengerTeamName = challengerTeam.name,
      opponentTeamId = opponentTeamId,
      opponentTeamName = opponentTeam.name,
      game = game,
      amount = amount,
      status = ChallengeStatus.OPEN
    )

    _teamChallenges.value = listOf(challenge) + _teamChallenges.value

    return Result.success("টিম চ্যালেঞ্জ সফলভাবে পাঠানো হয়েছে! প্রতিপক্ষ টিমের এডমিন নোটিফিকেশন পাবেন।")
  }

  // --- Complete Team Creation & Management Flow ---
  fun createTeam(name: String, tag: String, slogan: String, game: String): Result<Team> {
    if (name.isBlank() || tag.isBlank()) {
      return Result.failure(Exception("টিমের নাম ও ট্যাগ প্রদান করা আবশ্যক!"))
    }
    if (_teams.value.any { it.name.equals(name.trim(), ignoreCase = true) }) {
      return Result.failure(Exception("এই নামের টিম ইতিমধ্যে বিদ্যমান! অন্য নাম নির্বাচন করুন।"))
    }
    val user = _currentUser.value

    // If user already owns or is in a team, check
    val existingTeam = _teams.value.find { team -> team.members.any { it.id == user.id } }
    if (existingTeam != null) {
      return Result.failure(Exception("আপনি ইতিমধ্যে '${existingTeam.name}' টিমের সদস্য! নতুন টিম তৈরি করতে পূর্বের টিম ত্যাগ করুন।"))
    }

    val teamId = "team_${System.currentTimeMillis()}"
    val randomCode = (1000..9999).random()
    val cleanTag = tag.trim().uppercase().take(5)
    val publicId = "$cleanTag-$randomCode"

    val newTeam = Team(
      id = teamId,
      name = name.trim(),
      tag = cleanTag,
      publicId = publicId,
      adminId = user.id,
      adminName = user.username,
      balance = 0.0,
      members = listOf(
        TeamMember(
          id = user.id,
          name = user.username,
          role = "টিম লিডার / Captain",
          isOnline = true,
          ign = user.username
        )
      ),
      wins = 0,
      matches = 0,
      rating = 1200,
      isLive = true,
      slogan = if (slogan.isNotBlank()) slogan.trim() else "Never give up!",
      game = if (game.isNotBlank()) game.trim() else "Free Fire"
    )

    _teams.value = listOf(newTeam) + _teams.value
    return Result.success(newTeam)
  }

  fun sendJoinTeamRequest(teamId: String): Result<String> {
    val team = _teams.value.find { it.id == teamId }
      ?: return Result.failure(Exception("টিম পাওয়া যায়নি!"))

    val user = _currentUser.value
    if (team.members.any { it.id == user.id }) {
      return Result.failure(Exception("আপনি ইতিমধ্যে এই টিমের সদস্য!"))
    }
    if (team.joinRequests.any { it.userId == user.id }) {
      return Result.failure(Exception("ইতিমধ্যে জয়েন রিকোয়েস্ট পাঠানো হয়েছে! ক্যাপ্টেনের অনুমোদনের অপেক্ষায় আছে।"))
    }

    val req = TeamJoinRequest(
      id = "req_${System.currentTimeMillis()}",
      userId = user.id,
      userName = user.username,
      userIgn = user.username,
      isOnline = true
    )

    _teams.value = _teams.value.map {
      if (it.id == teamId) {
        it.copy(joinRequests = it.joinRequests + req)
      } else it
    }

    // Notify Captain
    _notifications.value = listOf(
      NotificationItem(
        id = "notif_${System.currentTimeMillis()}",
        title = "নতুন টিম জয়েন রিকোয়েস্ট",
        message = "${user.username} আপনার '${team.name}' টিমে যোগ দিতে চান।",
        timestamp = "এইমাত্র",
        isRead = false,
        targetType = "TEAM",
        targetId = teamId
      )
    ) + _notifications.value

    return Result.success("টিম জয়েন রিকোয়েস্ট সফলভাবে পাঠানো হয়েছে!")
  }

  fun acceptJoinTeamRequest(teamId: String, reqId: String): Result<String> {
    val team = _teams.value.find { it.id == teamId }
      ?: return Result.failure(Exception("টিম পাওয়া যায়নি!"))

    val req = team.joinRequests.find { it.id == reqId }
      ?: return Result.failure(Exception("অনুরোধটি পাওয়া যায়নি!"))

    val newMember = TeamMember(
      id = req.userId,
      name = req.userName,
      role = "Member",
      isOnline = req.isOnline,
      ign = req.userIgn
    )

    _teams.value = _teams.value.map {
      if (it.id == teamId) {
        it.copy(
          members = it.members + newMember,
          joinRequests = it.joinRequests.filter { r -> r.id != reqId }
        )
      } else it
    }

    _notifications.value = listOf(
      NotificationItem(
        id = "notif_${System.currentTimeMillis()}",
        title = "টিমে অন্তর্ভুক্ত হয়েছেন!",
        message = "অভিনন্দন! আপনি '${team.name}' টিমের সদস্য হয়েছেন।",
        timestamp = "এইমাত্র",
        isRead = false,
        targetType = "TEAM",
        targetId = teamId
      )
    ) + _notifications.value

    return Result.success("${req.userName} টিমে যোগ দিয়েছেন!")
  }

  fun rejectJoinTeamRequest(teamId: String, reqId: String): Result<String> {
    _teams.value = _teams.value.map {
      if (it.id == teamId) {
        it.copy(joinRequests = it.joinRequests.filter { r -> r.id != reqId })
      } else it
    }
    return Result.success("রিকোয়েস্ট বাতিল করা হয়েছে।")
  }

  fun joinTeamByPublicId(publicIdInput: String): Result<Team> {
    val trimmed = publicIdInput.trim()
    val team = _teams.value.find { it.publicId.equals(trimmed, ignoreCase = true) }
      ?: return Result.failure(Exception("প্রদত্ত টিম আইডি ($trimmed) দিয়ে কোনো টিম খুঁজে পাওয়া যায়নি!"))

    val user = _currentUser.value
    if (team.members.any { it.id == user.id }) {
      return Result.success(team)
    }

    // Auto join by valid direct ID code
    val newMember = TeamMember(
      id = user.id,
      name = user.username,
      role = "Member",
      isOnline = true,
      ign = user.username
    )

    _teams.value = _teams.value.map {
      if (it.id == team.id) {
        it.copy(members = it.members + newMember)
      } else it
    }

    return Result.success(team)
  }

  fun invitePlayerToTeam(teamId: String, invitedUserId: String, invitedUserName: String): Result<String> {
    val team = _teams.value.find { it.id == teamId }
      ?: return Result.failure(Exception("টিম পাওয়া যায়নি!"))

    if (team.members.any { it.id == invitedUserId }) {
      return Result.failure(Exception("$invitedUserName ইতিমধ্যে টিমের সদস্য!"))
    }

    // Add member
    val newMember = TeamMember(
      id = invitedUserId,
      name = invitedUserName,
      role = "Member",
      isOnline = true,
      ign = invitedUserName
    )

    _teams.value = _teams.value.map {
      if (it.id == teamId) it.copy(members = it.members + newMember) else it
    }

    _notifications.value = listOf(
      NotificationItem(
        id = "notif_${System.currentTimeMillis()}",
        title = "টিম ইনভাইটেশন!",
        message = "আপনাকে '${team.name}' টিমে যুক্ত করা হয়েছে।",
        timestamp = "এইমাত্র",
        isRead = false,
        targetType = "TEAM",
        targetId = teamId
      )
    ) + _notifications.value

    return Result.success("$invitedUserName-কে টিমে যোগ করা হয়েছে!")
  }

  fun removeTeamMember(teamId: String, memberId: String): Result<String> {
    val team = _teams.value.find { it.id == teamId }
      ?: return Result.failure(Exception("টিম পাওয়া যায়নি!"))

    if (memberId == team.adminId) {
      return Result.failure(Exception("টিম ক্যাপ্টেনকে রিমুভ করা সম্ভব নয়!"))
    }

    _teams.value = _teams.value.map {
      if (it.id == teamId) {
        it.copy(members = it.members.filter { m -> m.id != memberId })
      } else it
    }

    return Result.success("মেম্বারকে টিম থেকে রিমুভ করা হয়েছে।")
  }

  fun updateTeamMemberRole(teamId: String, memberId: String, newRole: String): Result<String> {
    _teams.value = _teams.value.map {
      if (it.id == teamId) {
        it.copy(members = it.members.map { m ->
          if (m.id == memberId) m.copy(role = newRole) else m
        })
      } else it
    }
    return Result.success("মেম্বার রোল আপডেট করা হয়েছে!")
  }

  // --- Wallet Operations ---
  fun requestDeposit(method: String, amount: Double, trxId: String): Result<String> {
    if (amount < 50.0) {
      return Result.failure(Exception("সর্বনিম্ন ডিপোজিট ৫০ টাকা!"))
    }
    if (trxId.length < 6) {
      return Result.failure(Exception("সঠিক ট্রানজেকশন আইডি (TrxID) প্রদান করুন!"))
    }

    // Instantly credit or create transaction
    _currentUser.value = _currentUser.value.copy(
      walletBalance = _currentUser.value.walletBalance + amount
    )

    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val currentTime = sdf.format(Date())

    _transactions.value = listOf(
      WalletTransaction(
        id = "tx_${System.currentTimeMillis()}",
        title = "$method ডিপোজিট",
        amount = amount,
        type = "DEPOSIT",
        paymentMethod = method,
        timestamp = "আজ $currentTime",
        isCredit = true,
        status = "সফল",
        trxId = trxId
      )
    ) + _transactions.value

    return Result.success("৳${amount.toInt()} টাকা ডিপোজিট সফলভাবে সম্পন্ন হয়েছে!")
  }

  fun requestWithdraw(method: String, number: String, amount: Double): Result<String> {
    if (!isWithinWithdrawWindow()) {
      return Result.failure(Exception("উইথড্র করার নির্ধারিত সময় সন্ধ্যা ৭:০০ টা থেকে রাত ১১:০০ টা পর্যন্ত। দয়া করে নির্ধারিত সময়ে উইথড্র করুন।"))
    }

    val user = _currentUser.value
    if (amount < 100.0) {
      return Result.failure(Exception("সর্বনিম্ন উইথড্র ১০০ টাকা!"))
    }
    if (user.walletBalance < amount) {
      return Result.failure(Exception("ওয়ালেটে পর্যাপ্ত ব্যালেন্স নেই!"))
    }
    if (number.length < 11) {
      return Result.failure(Exception("সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন!"))
    }

    _currentUser.value = user.copy(walletBalance = user.walletBalance - amount)

    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    val currentTime = sdf.format(Date())

    _transactions.value = listOf(
      WalletTransaction(
        id = "tx_${System.currentTimeMillis()}",
        title = "$method উত্তোলন ($number)",
        amount = amount,
        type = "WITHDRAW",
        paymentMethod = method,
        timestamp = "আজ $currentTime",
        isCredit = false,
        status = "প্রসেসিং"
      )
    ) + _transactions.value

    return Result.success("উইথড্র রিকোয়েস্ট সফলভাবে গ্রহণ করা হয়েছে! নির্ধারিত সময়ের মধ্যে টাকা আপনার অ্যাকাউন্টে পৌঁছে যাবে।")
  }

  // --- Dedicated Deposit Requests Workflow ---
  fun submitDepositRequest(
    walletType: DepositWalletType,
    teamId: String?,
    teamName: String?,
    paymentMethod: String,
    senderNumber: String,
    amount: Double,
    transactionId: String
  ): Result<String> {
    if (amount < 50.0) {
      return Result.failure(Exception("সর্বনিম্ন ডিপোজিট ৫০ টাকা!"))
    }
    if (senderNumber.length < 11) {
      return Result.failure(Exception("সঠিক ১১ ডিজিটের মোবাইল নম্বর দিন!"))
    }
    if (transactionId.length < 6) {
      return Result.failure(Exception("সঠিক ট্রানজেকশন আইডি (TrxID) প্রদান করুন!"))
    }

    if (walletType == DepositWalletType.TEAM_WALLET) {
      if (teamId == null || teamName == null) {
        return Result.failure(Exception("টিম ওয়ালেট ডিপোজিটের জন্য টিম নির্বাচন বাধ্যতামূলক!"))
      }
    }

    val sdf = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
    val formattedTime = sdf.format(Date())

    val request = DepositRequest(
      id = "dep_${System.currentTimeMillis()}",
      userId = _currentUser.value.id,
      userName = _currentUser.value.fullName,
      walletType = walletType,
      teamId = teamId,
      teamName = teamName,
      paymentMethod = paymentMethod,
      senderNumber = senderNumber,
      amount = amount,
      transactionId = transactionId,
      timestamp = formattedTime,
      status = "Pending"
    )

    _depositRequests.value = listOf(request) + _depositRequests.value

    // Also add pending transaction record for user history
    val targetDesc = if (walletType == DepositWalletType.TEAM_WALLET) "টিম ওয়ালেট ($teamName)" else "ব্যক্তিগত ওয়ালেট"
    _transactions.value = listOf(
      WalletTransaction(
        id = "tx_${System.currentTimeMillis()}",
        title = "$paymentMethod ডিপোজিট - $targetDesc",
        amount = amount,
        type = "DEPOSIT",
        paymentMethod = paymentMethod,
        timestamp = "আজ $formattedTime",
        isCredit = true,
        status = "অপেক্ষমাণ (Pending)",
        trxId = transactionId
      )
    ) + _transactions.value

    return Result.success("ডিপোজিট রিকোয়েস্ট সফলভাবে সাবমিট হয়েছে! এডমিন কর্তৃক যাচাইয়ের পর ব্যালেন্স যুক্ত হবে।")
  }

  fun approveDepositRequest(requestId: String): Result<String> {
    val req = _depositRequests.value.find { it.id == requestId }
      ?: return Result.failure(Exception("ডিপোজিট রিকোয়েস্ট পাওয়া যায়নি!"))

    if (req.status != "Pending") {
      return Result.failure(Exception("এই রিকোয়েস্টটি ইতিমধ্যে নিষ্পত্তি করা হয়েছে!"))
    }

    // Update request status
    _depositRequests.value = _depositRequests.value.map {
      if (it.id == requestId) it.copy(status = "Approved") else it
    }

    // Credit balance
    if (req.walletType == DepositWalletType.TEAM_WALLET && req.teamId != null) {
      _teams.value = _teams.value.map {
        if (it.id == req.teamId) it.copy(balance = it.balance + req.amount) else it
      }
    } else {
      if (_currentUser.value.id == req.userId) {
        _currentUser.value = _currentUser.value.copy(
          walletBalance = _currentUser.value.walletBalance + req.amount
        )
      }
    }

    // Update transactions status
    _transactions.value = _transactions.value.map {
      if (it.trxId == req.transactionId) it.copy(status = "সফল") else it
    }

    return Result.success("৳${req.amount.toInt()} টাকা ডিপোজিট অনুমোদন সম্পন্ন হয়েছে!")
  }

  fun rejectDepositRequest(requestId: String): Result<String> {
    val req = _depositRequests.value.find { it.id == requestId }
      ?: return Result.failure(Exception("ডিপোজিট রিকোয়েস্ট পাওয়া যায়নি!"))

    _depositRequests.value = _depositRequests.value.map {
      if (it.id == requestId) it.copy(status = "Rejected") else it
    }

    _transactions.value = _transactions.value.map {
      if (it.trxId == req.transactionId) it.copy(status = "বাতিল") else it
    }

    return Result.success("ডিপোজিট রিকোয়েস্ট বাতিল করা হয়েছে।")
  }

  fun updatePaymentSettings(
    bkashNumber: String,
    nagadNumber: String,
    bkashInstruction: String,
    nagadInstruction: String
  ) {
    _paymentSettings.value = PaymentMethodSettings(
      bkashNumber = bkashNumber,
      nagadNumber = nagadNumber,
      bkashInstruction = bkashInstruction,
      nagadInstruction = nagadInstruction
    )
  }

  // --- Notifications ---
  fun markNotificationRead(id: String) {
    _notifications.value = _notifications.value.map {
      if (it.id == id) it.copy(isRead = true) else it
    }
  }

  fun markAllNotificationsRead() {
    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
  }
}
