package com.example.easyvet.ui.advisories

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.easyvet.EasyVetApplication
import com.example.easyvet.data.local.entity.AdvisoryEntity
import com.example.easyvet.data.repository.AdvisoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class AdvisoryLanguage(val code: String, val nativeName: String, val englishName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "हिन्दी", "Hindi"),
    PUNJABI("pa", "ਪੰਜਾਬੀ", "Punjabi"),
    TAMIL("ta", "தமிழ்", "Tamil"),
    BENGALI("bn", "বাংলা", "Bengali"),
    MARATHI("mr", "मराठी", "Marathi"),
    TELUGU("te", "తెలుగు", "Telugu"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati")
}

data class TranslatedAdvisory(
    val original: AdvisoryEntity,
    val title: String,
    val summary: String,
    val steps: List<String>,
    val language: AdvisoryLanguage
)

data class AdvisoriesFilterGroup1(
    val selectedLanguage: AdvisoryLanguage = AdvisoryLanguage.ENGLISH,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val selectedAdvisory: AdvisoryEntity? = null
)

data class AdvisoriesFilterGroup2(
    val isPlayingAudio: Boolean = false,
    val playingAdvisoryId: String? = null,
    val audioProgress: Float = 0f,
    val audioCurrentTimeSec: Int = 0,
    val audioTotalDurationSec: Int = 150,
    val playbackSpeed: Float = 1.0f,
    val emergencyBroadcastSent: Boolean = false,
    val broadcastMessage: String? = null
)

data class AdvisoriesFilterParams(
    val g1: AdvisoriesFilterGroup1,
    val g2: AdvisoriesFilterGroup2
)

data class AdvisoriesUiState(
    val advisories: List<AdvisoryEntity> = emptyList(),
    val translatedAdvisories: List<TranslatedAdvisory> = emptyList(),
    val selectedLanguage: AdvisoryLanguage = AdvisoryLanguage.ENGLISH,
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val selectedAdvisory: AdvisoryEntity? = null,
    val isPlayingAudio: Boolean = false,
    val playingAdvisory: AdvisoryEntity? = null,
    val audioProgress: Float = 0f,
    val audioCurrentTimeSec: Int = 0,
    val audioTotalDurationSec: Int = 150,
    val playbackSpeed: Float = 1.0f,
    val emergencyBroadcastSent: Boolean = false,
    val broadcastMessage: String? = null,
    val totalAdvisoriesCount: Int = 0
)

class AdvisoriesViewModel(
    private val advisoryRepository: AdvisoryRepository,
    initialDiseaseFilter: String? = null,
    context: Context? = null
) : ViewModel() {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        context?.applicationContext?.let { appContext ->
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsReady = true
                    tts?.language = Locale.ENGLISH
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            isPlayingAudioFlow.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            isPlayingAudioFlow.value = false
                            audioProgressFlow.value = 1.0f
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            isPlayingAudioFlow.value = false
                        }
                    })
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }

    private val selectedLanguageFlow = MutableStateFlow(AdvisoryLanguage.ENGLISH)
    private val selectedCategoryFlow = MutableStateFlow("All")
    private val searchQueryFlow = MutableStateFlow(initialDiseaseFilter ?: "")
    private val selectedAdvisoryFlow = MutableStateFlow<AdvisoryEntity?>(null)

    private val isPlayingAudioFlow = MutableStateFlow(false)
    private val playingAdvisoryIdFlow = MutableStateFlow<String?>(null)
    private val audioProgressFlow = MutableStateFlow(0f)
    private val audioCurrentTimeSecFlow = MutableStateFlow(0)
    private val audioTotalDurationSec = 150
    private val playbackSpeedFlow = MutableStateFlow(1.0f)

    private val emergencyBroadcastSentFlow = MutableStateFlow(false)
    private val broadcastMessageFlow = MutableStateFlow<String?>(null)

    private var audioJob: Job? = null

    private val g1Flow = combine(
        selectedLanguageFlow,
        selectedCategoryFlow,
        searchQueryFlow,
        selectedAdvisoryFlow
    ) { lang, cat, search, advisory ->
        AdvisoriesFilterGroup1(lang, cat, search, advisory)
    }

    private val g2Flow = combine(
        isPlayingAudioFlow,
        playingAdvisoryIdFlow,
        audioProgressFlow,
        playbackSpeedFlow
    ) { playing, playingId, progress, speed ->
        AdvisoriesFilterGroup2(
            isPlayingAudio = playing,
            playingAdvisoryId = playingId,
            audioProgress = progress,
            audioCurrentTimeSec = (progress * audioTotalDurationSec).toInt(),
            audioTotalDurationSec = audioTotalDurationSec,
            playbackSpeed = speed,
            emergencyBroadcastSent = emergencyBroadcastSentFlow.value,
            broadcastMessage = broadcastMessageFlow.value
        )
    }

    private val filterParamsFlow = combine(
        g1Flow,
        g2Flow
    ) { g1, g2 ->
        AdvisoriesFilterParams(g1, g2)
    }

    val uiState: StateFlow<AdvisoriesUiState> = combine(
        advisoryRepository.allAdvisories,
        filterParamsFlow
    ) { advisories, params ->
        val g1 = params.g1
        val g2 = params.g2

        val filtered = advisories.filter { advisory ->
            val matchesCategory = g1.selectedCategory == "All" || advisory.category.contains(g1.selectedCategory, ignoreCase = true)
            val matchesSearch = g1.searchQuery.isBlank() ||
                    advisory.diseaseName.contains(g1.searchQuery, ignoreCase = true) ||
                    advisory.title.contains(g1.searchQuery, ignoreCase = true) ||
                    advisory.summary.contains(g1.searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }

        val translatedList = filtered.map { entity ->
            translateAdvisory(entity, g1.selectedLanguage)
        }

        val playingEntity = advisories.find { it.id == g2.playingAdvisoryId }

        AdvisoriesUiState(
            advisories = advisories,
            translatedAdvisories = translatedList,
            selectedLanguage = g1.selectedLanguage,
            selectedCategory = g1.selectedCategory,
            searchQuery = g1.searchQuery,
            selectedAdvisory = g1.selectedAdvisory,
            isPlayingAudio = g2.isPlayingAudio,
            playingAdvisory = playingEntity,
            audioProgress = g2.audioProgress,
            audioCurrentTimeSec = g2.audioCurrentTimeSec,
            audioTotalDurationSec = g2.audioTotalDurationSec,
            playbackSpeed = g2.playbackSpeed,
            emergencyBroadcastSent = emergencyBroadcastSentFlow.value,
            broadcastMessage = broadcastMessageFlow.value,
            totalAdvisoriesCount = advisories.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdvisoriesUiState()
    )

    fun setLanguage(language: AdvisoryLanguage) {
        selectedLanguageFlow.value = language
    }

    fun setCategory(category: String) {
        selectedCategoryFlow.value = category
    }

    fun setSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun selectAdvisory(advisory: AdvisoryEntity?) {
        selectedAdvisoryFlow.value = advisory
    }

    fun toggleAudioPlayback(advisoryId: String) {
        val currentPlaying = playingAdvisoryIdFlow.value
        val isPlaying = isPlayingAudioFlow.value

        if (currentPlaying != advisoryId || !isPlaying) {
            playingAdvisoryIdFlow.value = advisoryId
            audioProgressFlow.value = 0f
            isPlayingAudioFlow.value = true

            val advisory = uiState.value.translatedAdvisories.find { it.original.id == advisoryId }
            if (advisory != null && tts != null && isTtsReady) {
                val speechText = buildString {
                    append("Advisory protocol for ${advisory.original.diseaseName}. ")
                    append("${advisory.title}. ")
                    append("${advisory.summary}. ")
                    append("Key field steps: ")
                    advisory.steps.forEach { step -> append("$step. ") }
                }
                val locale = when (advisory.language) {
                    AdvisoryLanguage.HINDI -> Locale("hi", "IN")
                    AdvisoryLanguage.PUNJABI -> Locale("pa", "IN")
                    AdvisoryLanguage.TAMIL -> Locale("ta", "IN")
                    else -> Locale.ENGLISH
                }
                try {
                    tts?.language = locale
                } catch (e: Exception) {
                    tts?.language = Locale.ENGLISH
                }
                tts?.setSpeechRate(playbackSpeedFlow.value)
                tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "Advisory_$advisoryId")
            }
            startAudioTimer()
        } else {
            isPlayingAudioFlow.value = false
            tts?.stop()
            audioJob?.cancel()
        }
    }

    fun seekAudio(progress: Float) {
        audioProgressFlow.value = progress.coerceIn(0f, 1f)
    }

    fun cyclePlaybackSpeed() {
        val current = playbackSpeedFlow.value
        playbackSpeedFlow.value = when (current) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
    }

    fun triggerEmergencyBroadcast(diseaseName: String) {
        viewModelScope.launch {
            emergencyBroadcastSentFlow.value = true
            val langName = selectedLanguageFlow.value.englishName
            broadcastMessageFlow.value = "Emergency Outbreak Voice & SMS Alert triggered in $langName for $diseaseName to all registered livestock owners!"
            delay(5000)
            emergencyBroadcastSentFlow.value = false
            broadcastMessageFlow.value = null
        }
    }

    private fun startAudioTimer() {
        audioJob?.cancel()
        audioJob = viewModelScope.launch {
            while (isPlayingAudioFlow.value && audioProgressFlow.value < 1.0f) {
                delay((1000 / playbackSpeedFlow.value).toLong())
                val newProgress = audioProgressFlow.value + (1.0f / audioTotalDurationSec)
                if (newProgress >= 1.0f) {
                    audioProgressFlow.value = 1.0f
                    isPlayingAudioFlow.value = false
                } else {
                    audioProgressFlow.value = newProgress
                }
            }
        }
    }

    private fun translateAdvisory(entity: AdvisoryEntity, lang: AdvisoryLanguage): TranslatedAdvisory {
        return when (lang) {
            AdvisoryLanguage.HINDI -> TranslatedAdvisory(
                original = entity,
                title = when (entity.id) {
                    "ADV-001" -> "एफएमडी (खुरपका-मुंहपका) क्षेत्र प्रकोप प्रबंधन दिशानिर्देश"
                    "ADV-002" -> "एंथ्रेक्स शव प्रबंधन एवं जैव-सुरक्षा चेतावनी"
                    else -> "एलएसडी (लंपी स्किन रोग) मक्खी नियंत्रण व उपचार"
                },
                summary = when (entity.id) {
                    "ADV-001" -> "चारागाह मार्गों तथा पशु बाजारों में वायरस प्रसार रोकने हेतु तत्काल संगरोध प्रक्रियाएं।"
                    "ADV-002" -> "मानव संक्रमण व बीजाणु संदूषण से बचने के लिए अचानक पशु मृत्यु पर सख्त सुरक्षा।"
                    else -> "त्वचा की गांठों से पीड़ित मवेशियों की मक्खी नियंत्रण व सहायक देखभाल।"
                },
                steps = entity.detailedSteps.map { translateStepToHindi(it) },
                language = lang
            )
            AdvisoryLanguage.PUNJABI -> TranslatedAdvisory(
                original = entity,
                title = when (entity.id) {
                    "ADV-001" -> "ਮੂੰਹ-ਖੁਰ ਬਿਮਾਰੀ (FMD) ਫੀਲਡ ਪ੍ਰਕੋਪ ਪ੍ਰਬੰਧਨ ਪ੍ਰੋਟੋਕੋਲ"
                    "ADV-002" -> "ਐਂਥ੍ਰੈਕਸ ਮ੍ਰਿਤ ਦੇਹ ਪ੍ਰਬੰਧਨ ਅਤੇ ਜੈਵਿਕ ਸੁਰੱਖਿਆ ਚੇਤਾਵਨੀ"
                    else -> "ਲੰਪੀ ਸਕਿਨ ਰੋਗ (LSD) ਮੱਖੀ ਨਿਯੰਤਰਣ ਅਤੇ ਇਲਾਜ"
                },
                summary = when (entity.id) {
                    "ADV-001" -> "ਚਰਾਗਾਹਾਂ ਅਤੇ ਮੰਡੀਆਂ ਵਿੱਚ ਵਿਸ਼ਾਣੂ ਫੈਲਣ ਤੋਂ ਰੋਕਣ ਲਈ ਤੁਰੰਤ ਇਕਾਂਤਵਾਸ।"
                    "ADV-002" -> "ਇਨਸਾਨੀ ਲਾਗ ਅਤੇ ਬੀਜਾਣੂ ਫੈਲਣ ਤੋਂ ਬਚਾਅ ਲਈ ਅਚਾਨਕ ਮੌਤ 'ਤੇ ਸਖ਼ਤ ਕਦਮ।"
                    else -> "ਚਮੜੀ ਦੀਆਂ ਗੰਢਾਂ ਵਾਲੇ ਪਸ਼ੂਆਂ ਲਈ ਮੱਖੀਆਂ 'ਤੇ ਕਾਬੂ ਅਤੇ ਸਹਾਇਕ ਦੇਖਭਾਲ।"
                },
                steps = entity.detailedSteps,
                language = lang
            )
            AdvisoryLanguage.TAMIL -> TranslatedAdvisory(
                original = entity,
                title = when (entity.id) {
                    "ADV-001" -> "கோமாரி நோய் (FMD) பரவல் தடுப்பு வழிகாட்டுதல்"
                    "ADV-002" -> "ஆந்த்ராக்ஸ் இறப்பு மேலாண்மை மற்றும் பாதுகாப்பு எச்சரிக்கை"
                    else -> "தோல் கழலை நோய் (LSD) பூச்சி கட்டுப்பாடு மற்றும் சிகிச்சை"
                },
                summary = when (entity.id) {
                    "ADV-001" -> "சந்தைகள் மற்றும் மேய்ச்சல் நிலங்களில் வைரஸ் பரவுவதைத் தடுக்க தனிமைப்படுத்துதல்."
                    "ADV-002" -> "மனிதர்களுக்குத் தொற்று பரவாமல் இருக்க திடீர் கால்நடை இறப்புகளில் பாதுகாப்பு."
                    else -> "கால்நடைகளில் கொசு மற்றும் ஈக்களைக் கட்டுப்படுத்தி சிகிச்சை அளித்தல்."
                },
                steps = entity.detailedSteps,
                language = lang
            )
            else -> TranslatedAdvisory(
                original = entity,
                title = entity.title,
                summary = entity.summary,
                steps = entity.detailedSteps,
                language = lang
            )
        }
    }

    private fun translateStepToHindi(step: String): String {
        return when {
            step.contains("Isolate infected livestock", ignoreCase = true) ->
                "1. संक्रमित पशुओं को स्वस्थ झुंड से कम से कम 500 मीटर दूर सूखे, छायादार संगरोध बाड़े में अलग करें।"
            step.contains("Restrict all vehicle", ignoreCase = true) ->
                "2. फार्म परिसर में वाहनों व बाहरी व्यक्तियों का प्रवेश रोकें। रोगाणुनाशक फुटबाथ का प्रयोग करें।"
            step.contains("Provide soft green forage", ignoreCase = true) ->
                "3. प्रभावित पशुओं को कोमल हरा चारा व साफ पानी दें; मुंह व पैरों के छालों को लाल दवा (पोटेशियम परमैंगनेट) से धोएं।"
            step.contains("DO NOT OPEN OR PERFORM NECROPSY", ignoreCase = true) ->
                "1. शव को कदापि न खोलें और न ही चीरा लगाएं! हवा के संपर्क से बीजाणु (spore) बनते हैं।"
            step.contains("Cover carcass with tarpaulins", ignoreCase = true) ->
                "2. कुत्ते व मांसाहारी पक्षियों द्वारा संक्रमण रोकने हेतु शव को त्रिपाल या कटीली झाड़ियों से ढकें।"
            else -> step
        }
    }

    class Factory(
        private val application: EasyVetApplication,
        private val initialDiseaseFilter: String? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AdvisoriesViewModel::class.java)) {
                return AdvisoriesViewModel(
                    advisoryRepository = application.advisoryRepository,
                    initialDiseaseFilter = initialDiseaseFilter,
                    context = application.applicationContext
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class ${modelClass.name}")
        }
    }
}
