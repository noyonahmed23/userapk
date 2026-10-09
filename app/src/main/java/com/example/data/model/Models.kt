package com.example.data.model

data class UserProfile(
  val id: String,
  val username: String,
  val fullName: String,
  val phone: String,
  val email: String,
  val walletBalance: Double,
  val isOnline: Boolean,
  val matchesPlayed: Int,
  val wins: Int,
  val totalEarnings: Double,
  val freeFireUid: String,
  val pubgUid: String,
  val isAdmin: Boolean = false,
  val userCode: String = "KB-${id.takeLast(4)}"
)

data class GameCategory(
  val id: String,
  val name: String,
  val activeTournamentsCount: Int,
  val modes: List<String>,
  val iconEmoji: String
)

enum class TournamentStatus {
  JOIN_OPEN,
  JOIN_CLOSED,
  LIVE,
  FINISHED
}

data class TournamentSlot(
  val slotNumber: Int,
  val isFilled: Boolean,
  val playerId: String? = null,
  val playerName: String? = null,
  val playerUid: String? = null
)

data class Tournament(
  val id: String,
  val gameId: String,
  val gameTitle: String,
  val title: String,
  val description: String = "48 player slots. Each slot holds one player; minimum 2 slots.",
  val mode: String,
  val matchType: String = "Single Match",
  val teamSize: String = "1 player",
  val mapName: String,
  val entryFee: Double,
  val prizePool: Double,
  val perKillPrize: Double = 5.0,
  val maxSlots: Int = 48,
  val joinedSlots: Int = 0,
  val startTime: String,
  val status: TournamentStatus,
  val rules: String,
  val roomRevealMinutes: Int = 10,
  val roomCategory: String = "Manual Room",
  val roomId: String? = null,
  val roomPassword: String? = null,
  val prizePlace1: Double = 200.0,
  val prizePlace2: Double = 100.0,
  val prizePlace3: Double = 50.0,
  val prizePlace4: Double = 0.0,
  val prizePlace5: Double = 0.0,
  val slots: List<TournamentSlot> = emptyList(),
  val joinedPlayerIds: List<String> = emptyList(),
  val bannerRes: Int? = null
)

enum class ChallengeRule(val displayNameBn: String, val displayNameEn: String) {
  REGULAR("রেগুলার ম্যাচ", "Regular Match"),
  HEADSHOT_ONLY("হেডশট অনলি", "Headshot Only")
}

enum class ChallengeStatus(val labelBn: String) {
  OPEN("উন্মুক্ত"),
  ACCEPTED("গৃহীত"),
  ROOM_SUBMITTED("রুম প্রস্তুত"),
  PROOF_OPEN("প্রুফ সাবমিট চলছে"),
  PROOF_SUBMITTED("রিভিউ হচ্ছে"),
  FINISHED("সম্পন্ন"),
  CANCELLED("বাতিল"),
  EXPIRED("মেয়াদোত্তীর্ণ")
}

data class PlayerChallenge(
  val id: String,
  val codeTag: String = "KBD-261008-${id.takeLast(6).uppercase()}",
  val challengerId: String,
  val challengerName: String,
  val challengerIsOnline: Boolean,
  val game: String,
  val mode: String,
  val mapName: String,
  val rule: ChallengeRule,
  val amount: Double,
  val challengerNote: String,
  val createdAtMillis: Long,
  val expiresAtMillis: Long,
  val status: ChallengeStatus,
  val opponentId: String? = null,
  val opponentName: String? = null,
  val opponentIsOnline: Boolean? = null,
  val roomId: String? = null,
  val roomPassword: String? = null,
  val challengerProofUrl: String? = null,
  val opponentProofUrl: String? = null,
  val winnerId: String? = null
)

data class TeamMember(
  val id: String,
  val name: String,
  val role: String, // "Captain", "Co-Captain", "Member"
  val isOnline: Boolean,
  val ign: String = name
)

data class TeamJoinRequest(
  val id: String,
  val userId: String,
  val userName: String,
  val userIgn: String,
  val isOnline: Boolean,
  val timestamp: String = "এইমাত্র"
)

data class Team(
  val id: String,
  val name: String,
  val tag: String,
  val publicId: String,
  val adminId: String,
  val adminName: String,
  val balance: Double,
  val members: List<TeamMember>,
  val wins: Int,
  val matches: Int,
  val rating: Int,
  val isLive: Boolean,
  val slogan: String = "Victory through unity and skill!",
  val game: String = "Free Fire",
  val bannerUrl: String? = null,
  val joinRequests: List<TeamJoinRequest> = emptyList()
)

data class TeamChallenge(
  val id: String,
  val challengerTeamId: String,
  val challengerTeamName: String,
  val opponentTeamId: String,
  val opponentTeamName: String,
  val game: String,
  val amount: Double,
  val status: ChallengeStatus,
  val roomId: String? = null,
  val roomPassword: String? = null,
  val challengerProofSubmitted: Boolean = false,
  val opponentProofSubmitted: Boolean = false,
  val winnerTeamId: String? = null
)

data class WalletTransaction(
  val id: String,
  val title: String,
  val amount: Double,
  val type: String,
  val paymentMethod: String,
  val timestamp: String,
  val isCredit: Boolean,
  val status: String,
  val trxId: String? = null
)

data class NotificationItem(
  val id: String,
  val title: String,
  val message: String,
  val timestamp: String,
  val isRead: Boolean,
  val targetType: String,
  val targetId: String? = null
)

data class LeaderboardItem(
  val rank: Int,
  val userId: String,
  val name: String,
  val wins: Int,
  val matches: Int,
  val winRate: String,
  val score: Int,
  val earnings: Double,
  val isOnline: Boolean
)

data class BannerSlide(
  val id: String,
  val title: String,
  val subtitle: String,
  val targetDestination: String
)

data class PaymentMethodSettings(
  val bkashNumber: String = "",
  val nagadNumber: String = "",
  val bkashInstruction: String = "এডমিন কর্তৃক বিকাশ পেমেন্ট নম্বর সেট করা হয়নি।",
  val nagadInstruction: String = "এডমিন কর্তৃক নগদ পেমেন্ট নম্বর সেট করা হয়নি।"
)

enum class DepositWalletType(val labelBn: String) {
  TEAM_WALLET("Team Wallet"),
  REGULAR_WALLET("Regular Wallet")
}

data class DepositRequest(
  val id: String,
  val userId: String,
  val userName: String,
  val walletType: DepositWalletType,
  val teamId: String? = null,
  val teamName: String? = null,
  val paymentMethod: String, // "bKash" or "Nagad"
  val senderNumber: String,
  val amount: Double,
  val transactionId: String,
  val timestamp: String,
  val status: String = "Pending" // "Pending", "Approved", "Rejected"
)

